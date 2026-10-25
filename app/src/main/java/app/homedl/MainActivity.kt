package app.homedl

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import android.widget.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val previews = Executors.newFixedThreadPool(2)
    private val timer = Executors.newSingleThreadScheduledExecutor()
    private val appChecks = Executors.newSingleThreadExecutor()
    private val appCheckToken = AtomicBoolean(false)
    private lateinit var appUpdates: AppUpdates
    private var availableRelease: AppRelease? = null
    private var appChecking = false
    private var waitingForInstallPermission = false
    private lateinit var extractor: Extractor
    private lateinit var screen: RetroScreen
    private lateinit var link: EditText
    private lateinit var status: TextView
    private lateinit var items: LinearLayout
    private lateinit var analyze: Button
    private lateinit var download: Button
    private lateinit var cancel: Button
    private lateinit var updateEngine: Button
    private lateinit var engineUpdates: EngineUpdates
    private lateinit var automaticUpdates: Switch
    private val selected = linkedSetOf<Media>()
    private var lastDiagnostic: String? = null
    private var failed = false
    private var progressText = ""
    private var progressPercent: Int? = null
    private var progressStarted = 0L
    @Volatile private var busy = false
    @Volatile private var operation: String? = null
    private var cancelled = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extractor = Extractor(applicationContext)
        engineUpdates = EngineUpdates(applicationContext)
        screen = RetroScreen(this)
        appUpdates = AppUpdates(applicationContext)
        val root = screen.root
        link = screen.link
        analyze = screen.analyze
        status = screen.status
        items = screen.items
        download = screen.download
        cancel = screen.cancel
        updateEngine = screen.updateEngine
        automaticUpdates = screen.automaticUpdates
        screen.automaticAppUpdates.isChecked = appUpdates.enabled()
        screen.automaticAppUpdates.setOnCheckedChangeListener { _, value -> appUpdates.setEnabled(value) }
        screen.updateApp.setOnClickListener {
            if (busy) return@setOnClickListener
            if (appUpdates.hasDownloaded()) offerInstall()
            else availableRelease?.let { release ->
                android.app.AlertDialog.Builder(this).setTitle("Home-DL " + release.versionName)
                    .setMessage("Télécharger cette mise à jour ? Android vous demandera ensuite de confirmer son installation. Vos réglages seront conservés si la signature correspond.")
                    .setPositiveButton("Télécharger") { _, _ -> downloadApp(release) }
                    .setNegativeButton("Plus tard", null).show()
            } ?: checkAppUpdates()
        }
        setContentView(root)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout())
                val keyboard = insets.getInsets(android.view.WindowInsets.Type.ime())
                view.setPadding(bars.left + dp(16), bars.top + dp(12),
                    bars.right + dp(16), maxOf(bars.bottom, keyboard.bottom) + dp(12))
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft + dp(16), insets.systemWindowInsetTop + dp(12),
                    insets.systemWindowInsetRight + dp(16), insets.systemWindowInsetBottom + dp(12))
            }
            insets
        }
        root.requestApplyInsets()
        analyze.setOnClickListener { analyzeLink() }
        screen.clearLink.setOnClickListener {
            if (!busy) {
                screen.hideProgress()
                link.text.clear()
                items.removeAllViews()
                selected.clear()
                lastDiagnostic = null
                screen.diagnostic.visibility = View.GONE
                screen.setMediaCount(0)
                screen.setBusy(false)
                download.isEnabled = false
                status.text = "Lien effacé. Prêt pour une nouvelle analyse."
                link.requestFocus()
            }
        }
        screen.diagnostic.setOnClickListener { showDiagnostic() }
        download.setOnClickListener { downloadSelected() }
        cancel.setOnClickListener {
            cancelled.set(true)
            operation?.let { extractor.cancel(it) }
            status.text = "Annulation…"
        }
        automaticUpdates.isChecked = engineUpdates.enabled()
        automaticUpdates.setOnCheckedChangeListener { _, enabled -> engineUpdates.setEnabled(enabled) }
        updateEngine.setOnClickListener {
            begin("Mise à jour du moteur…", 180, Stage.UPDATE, initialize = false) { _, token ->
                extractor.init()
                if (token.get()) throw InterruptedException()
                val result = engineUpdates.check(token)
                if (token.get()) throw InterruptedException()
                ui { status.text = "$result Vous pouvez relancer l’analyse." }
            }
        }
        acceptShare(intent)
        if (engineUpdates.due()) {
            begin("Vérification du moteur…", 100, Stage.UPDATE, initialize = false) { _, token ->
                extractor.init()
                if (token.get()) throw InterruptedException()
                try {
                    val result = engineUpdates.check(token)
                    if (token.get()) throw InterruptedException()
                    ui { status.text = "$result Collez un lien pour commencer." }
                } catch (error: Exception) {
                    if (token.get()) throw error
                    val reason = Diagnostics.describe(Stage.UPDATE, error)
                    val report = Diagnostics.report(Stage.UPDATE, error, appVersion(), engineUpdates.version(), "moteur")
                    ui {
                        lastDiagnostic = report
                        screen.diagnostic.visibility = View.VISIBLE
                        status.text = "$reason\nLe moteur existant est conservé. Vous pouvez analyser un lien."
                    }
                }
            }
        }
        if (appUpdates.due()) checkAppUpdates()
    }

    private fun checkAppUpdates() {
        if (appChecking) return
        appChecking = true
        screen.updateApp.isEnabled = false
        screen.appUpdateStatus.text = "Recherche d’une nouvelle version…"
        screen.appUpdateProgress.isIndeterminate = true
        screen.appUpdateProgress.visibility = View.VISIBLE
        if (!busy) screen.progress("Recherche d’une nouvelle version…")
        appChecks.submit {
            try {
                val release = appUpdates.check(appCheckToken)
                ui {
                    availableRelease = release
                    screen.appUpdateStatus.text = if (release == null) "Version " + appVersion() + " · aucune version plus récente publiée."
                        else "Nouvelle version disponible : " + release.versionName
                    screen.updateApp.text = if (release == null) "Vérifier la mise à jour de l’app"
                        else "Mettre Home-DL à jour"
                }
            } catch (_: Exception) {
                ui { screen.appUpdateStatus.text = "Vérification de l’app indisponible. Vous pouvez réessayer ici." }
            } finally {
                ui {
                    appChecking = false; screen.updateApp.isEnabled = !busy
                    screen.appUpdateProgress.visibility = View.GONE
                    if (!busy) screen.progress(screen.appUpdateStatus.text.toString(), 100)
                }
            }
        }
    }

    private fun downloadApp(release: AppRelease) {
        begin("Téléchargement de Home-DL " + release.versionName + "…", 200, Stage.UPDATE, initialize = false) { _, token ->
            ui {
                screen.appUpdateProgress.isIndeterminate = false
                screen.appUpdateProgress.progress = 0
                screen.appUpdateProgress.visibility = View.VISIBLE
                screen.appUpdateStatus.text = "Connexion au serveur de mise à jour… 0 %"
                screen.updateApp.text = "Téléchargement en cours…"
            }
            try {
                appUpdates.download(release, token) { percent -> ui {
                    showProgress("Mise à jour : $percent %", percent)
                    screen.appUpdateProgress.progress = percent
                    val message = if (percent == 100) "Téléchargement : 100 %. Vérification de l’APK…"
                        else "Téléchargement de Home-DL ${release.versionName} : $percent %"
                    screen.appUpdateStatus.text = message
                    status.text = message
                } }
                if (token.get()) throw InterruptedException()
                ui {
                    screen.appUpdateProgress.progress = 100
                    screen.updateApp.text = "Installer la mise à jour téléchargée"
                    screen.appUpdateStatus.text = "APK vérifiée. Appuyez sur Installer ; Android demandera confirmation."
                    status.text = "Mise à jour prête. Appuyez sur Installer pour continuer."
                }
            } catch (error: Exception) {
                ui {
                    screen.appUpdateStatus.text = if (token.get()) "Téléchargement arrêté. Vous pouvez réessayer."
                        else Diagnostics.describe(Stage.UPDATE, error)
                    screen.updateApp.text = "Réessayer le téléchargement"
                    screen.appUpdateProgress.visibility = View.GONE
                }
                throw error
            }
        }
    }

    private fun offerInstall() {
        val allowed = packageManager.canRequestPackageInstalls()
        android.app.AlertDialog.Builder(this).setTitle("Installer la mise à jour")
            .setMessage(if (allowed) "Android va vous demander de confirmer la mise à jour de Home-DL."
                else "Autorisez Home-DL comme source d’installation dans l’écran Android qui va s’ouvrir. Revenez ensuite ici pour confirmer l’installation.")
            .setPositiveButton(if (allowed) "Installer" else "Ouvrir les réglages") { _, _ ->
                try {
                    waitingForInstallPermission = !allowed
                    appUpdates.install(this)
                } catch (_: Exception) {
                    waitingForInstallPermission = false
                    screen.appUpdateStatus.text = "Installation impossible : fichier absent, version ou signature refusée. Relancez une vérification."
                }
            }.setNegativeButton("Plus tard", null).show()
    }

    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); acceptShare(intent) }
    private fun acceptShare(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && !busy) link.setText(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty())
    }

    @Suppress("DEPRECATION")
    private fun appVersion(): String = packageManager.getPackageInfo(packageName, 0).versionName ?: "inconnue"

    private fun showDiagnostic() {
        val report = lastDiagnostic ?: return
        val body = TextView(this).apply {
            text = report
            textSize = 13f
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setTextIsSelectable(true)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Diagnostic local")
            .setView(ScrollView(this).apply { addView(body) })
            .setPositiveButton("Copier") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Diagnostic Home-DL", report)
                clip.description.extras = android.os.PersistableBundle().apply {
                    putBoolean("android.content.extra.IS_SENSITIVE", true)
                }
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Diagnostic copié. Vous pouvez le coller dans la conversation.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun begin(message: String, timeoutSeconds: Long, stage: Stage,
                      initialize: Boolean = true, action: (String, AtomicBoolean) -> Unit) {
        if (busy) return
        busy = true
        failed = false
        lastDiagnostic = null
        screen.diagnostic.visibility = View.GONE
        screen.clearLink.isEnabled = false
        val platform = Links.platform(link.text.toString())
        screen.setBusy(true)
        val id = UUID.randomUUID().toString()
        val token = AtomicBoolean(false)
        cancelled = token; operation = id
        analyze.isEnabled = false; download.isEnabled = false; link.isEnabled = false; cancel.isEnabled = true
        updateEngine.isEnabled = false
        automaticUpdates.isEnabled = false
        screen.updateApp.isEnabled = false
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        status.text = message
        progressStarted = android.os.SystemClock.elapsedRealtime()
        showProgress(message)
        val ticker = timer.scheduleAtFixedRate({ ui {
            if (operation == id) renderProgress()
        } }, 1, 1, TimeUnit.SECONDS)
        val deadline = timer.schedule({
            token.set(true); extractor.cancel(id)
        }, timeoutSeconds, TimeUnit.SECONDS)
        worker.submit {
            var currentStage = if (initialize) Stage.INIT else stage
            AnalysisTrace.start { step -> ui {
                if (operation == id && stage != Stage.DOWNLOAD) showProgress(stepLabel(step))
            } }
            try {
                if (initialize) AnalysisTrace.step(AnalysisTrace.Step.ENGINE_INIT) { extractor.init() }
                currentStage = stage
                if (token.get()) throw InterruptedException()
                if (stage == Stage.DOWNLOAD) AnalysisTrace.step(AnalysisTrace.Step.MEDIA_DOWNLOAD) { action(id, token) }
                else action(id, token)
            } catch (e: Exception) {
                val diagnostic = if (token.get()) "STOP • Opération arrêtée (annulation ou délai dépassé)."
                    else Diagnostics.describe(currentStage, e)
                val report = if (token.get()) null else Diagnostics.report(
                    currentStage, e, appVersion(), engineUpdates.version(), platform
                )
                ui {
                    failed = !token.get()
                    lastDiagnostic = report
                    screen.diagnostic.visibility = if (report == null) View.GONE else View.VISIBLE
                    status.text = diagnostic
                }
            } finally {
                AnalysisTrace.clear()
                deadline.cancel(false)
                ticker.cancel(false)
                ui {
                    if (token.get()) showProgress("Opération arrêtée", 0)
                    else if (failed) showProgress("Échec — ouvrez le diagnostic", 0)
                    else showProgress(status.text.toString(), 100)
                    busy = false; operation = null
                    screen.setBusy(false)
                    if (failed) screen.showFailure()
                    screen.clearLink.isEnabled = true
                    analyze.isEnabled = true; link.isEnabled = true; cancel.isEnabled = false
                    updateEngine.isEnabled = true
                    automaticUpdates.isEnabled = true
                    screen.updateApp.isEnabled = !appChecking
                    download.isEnabled = selected.isNotEmpty()
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }

    private fun analyzeLink() {
        val url = try { Links.parse(link.text.toString()) } catch (e: Exception) { status.text = e.message; return }
        link.setSelection(0)
        items.removeAllViews(); selected.clear(); screen.setMediaCount(0)
        begin("Analyse en cours…", 120, Stage.ANALYZE, initialize = Links.platform(url) != "Instagram") { id, token ->
            val found = extractor.analyze(url, id, token)
            if (token.get()) throw InterruptedException()
            val report = Diagnostics.analysisSuccess(appVersion(), engineUpdates.version(), Links.platform(url), found.size)
            ui {
                lastDiagnostic = report
                screen.diagnostic.visibility = View.VISIBLE
                status.text = if (found.isEmpty()) "Aucun média exploitable détecté."
                    else "${found.size} média(s) détecté(s). Sélectionnez les éléments."
                screen.setMediaCount(found.size)
                found.forEach { media -> addCard(media) }
            }
        }
    }

    private fun addCard(media: Media) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(12), dp(8), dp(12))
        }
        screen.decorateCard(box)
        val image = ImageView(this).apply {
            setBackgroundColor(RetroScreen.BACKGROUND); scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = if (media.kind == MediaKind.PHOTO) "Aperçu de la photo" else "Aperçu de la vidéo"
        }
        box.addView(image, LinearLayout.LayoutParams(-1, dp(190)))
        box.addView(CheckBox(this).apply {
            text = "${media.title}\n${media.quality}"; textSize = 16f
            screen.styleSelection(this)
            setOnCheckedChangeListener { _, checked ->
                if (checked) selected.add(media) else selected.remove(media)
                download.isEnabled = !busy && selected.isNotEmpty()
            }
        })
        items.addView(box)
        media.preview?.let { url ->
            previews.submit {
                var connection: HttpURLConnection? = null
                try {
                    connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000; connection.readTimeout = 10000
                    connection.instanceFollowRedirects = false
                    if (connection.responseCode != 200) return@submit
                    val bytes = connection.inputStream.use { input ->
                        val output = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            if (output.size() + n > 2 * 1024 * 1024) return@submit
                            output.write(buffer, 0, n)
                        }
                        output.toByteArray()
                    }
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    val options = BitmapFactory.Options().apply {
                        var sample = 1
                        while (bounds.outWidth / sample > 720 || bounds.outHeight / sample > 720) sample *= 2
                        inSampleSize = sample
                    }
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    ui { image.setImageBitmap(bitmap) }
                } catch (_: Exception) { /* Preview failure must not block selection. */ }
                finally { connection?.disconnect() }
            }
        }
    }

    private fun downloadSelected() {
        val batch = selected.toList()
        if (batch.isEmpty()) return
        begin("Préparation du téléchargement…", 1800, Stage.DOWNLOAD,
            initialize = batch.any { it.directUrl == null }) { id, token ->
            var saved = 0
            for (media in batch) {
                if (token.get()) throw InterruptedException()
                val fileNumber = saved + 1
                val directory = File(cacheDir, "download-${UUID.randomUUID()}").apply { mkdirs() }
                try {
                    ui { showProgress("Connexion — fichier ${fileNumber}/${batch.size}") }
                    val file = extractor.download(media, directory, id, token) { percent ->
                        ui {
                            val known = percent.isFinite() && percent >= 0
                            val value = percent.toInt().coerceIn(0, 100)
                            val message = "Fichier ${fileNumber}/${batch.size}" + if (known) " • $value %" else " • taille inconnue"
                            status.text = message
                            showProgress(message, if (known) value else null)
                        }
                    }
                    ui { showProgress("Enregistrement — fichier ${fileNumber}/${batch.size}") }
                    MediaStoreWriter.save(applicationContext, file) { token.get() }
                    saved++
                    ui { status.text = "${saved}/${batch.size} fichier(s) enregistré(s)." }
                } finally { directory.deleteRecursively() }
            }
            ui { status.text = "Terminé : ${saved} fichier(s) dans Téléchargements / Home-DL." }
        }
    }

    private fun showProgress(message: String, percent: Int? = null) {
        progressText = message
        progressPercent = percent
        renderProgress()
    }

    private fun renderProgress() {
        val elapsed = ((android.os.SystemClock.elapsedRealtime() - progressStarted) / 1000).coerceAtLeast(0)
        screen.progress(progressText + if (busy) " · ${elapsed}s" else "", progressPercent)
    }

    private fun stepLabel(step: AnalysisTrace.Step): String = when (step) {
        AnalysisTrace.Step.ENGINE_INIT -> "Initialisation du moteur…"
        AnalysisTrace.Step.INSTAGRAM_REFERENCE -> "Lecture du lien Instagram…"
        AnalysisTrace.Step.INSTAGRAM_SESSION -> "Ouverture de la session publique Instagram…"
        AnalysisTrace.Step.INSTAGRAM_ACCESS -> "Vérification de la réponse Instagram…"
        AnalysisTrace.Step.INSTAGRAM_SHORTCODE -> "Recherche des médias Instagram (1/2)…"
        AnalysisTrace.Step.INSTAGRAM_POLARIS -> "Recherche des médias Instagram (2/2)…"
        AnalysisTrace.Step.ENGINE_ANALYZE -> "Analyse des médias…"
        AnalysisTrace.Step.X_PUBLIC_PHOTOS -> "Recherche des photos X…"
        AnalysisTrace.Step.TIKTOK_RESOLVE -> "Résolution du lien TikTok…"
        AnalysisTrace.Step.TIKTOK_PUBLIC_PHOTOS -> "Recherche des photos TikTok…"
        AnalysisTrace.Step.MEDIA_PARSE -> "Préparation de la sélection…"
        AnalysisTrace.Step.MEDIA_DOWNLOAD -> "Préparation du téléchargement…"
    }

    private fun ui(action: () -> Unit) { runOnUiThread { if (!isDestroyed) action() } }

    override fun onResume() {
        super.onResume()
        if (::screen.isInitialized) {
            screen.resume()
            if (::appUpdates.isInitialized && appUpdates.hasDownloaded()) screen.updateApp.text = "Installer la mise à jour téléchargée"
            if (waitingForInstallPermission) {
                waitingForInstallPermission = false
                if (packageManager.canRequestPackageInstalls()) offerInstall()
            }
        }
    }
    override fun onPause() { if (::screen.isInitialized) screen.pause(); super.onPause() }

    override fun onDestroy() {
        cancelled.set(true); operation?.let { extractor.cancel(it) }
        worker.shutdownNow(); previews.shutdownNow(); timer.shutdownNow()
        appCheckToken.set(true); appChecks.shutdownNow()
        super.onDestroy()
    }
}
