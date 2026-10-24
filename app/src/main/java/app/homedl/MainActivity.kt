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
    private lateinit var extractor: Extractor
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
    @Volatile private var busy = false
    @Volatile private var operation: String? = null
    private var cancelled = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extractor = Extractor(applicationContext)
        engineUpdates = EngineUpdates(applicationContext)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(12))
            setBackgroundColor(Color.rgb(245, 247, 251))
            fitsSystemWindows = false
        }
        setContentView(root)
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout())
                val keyboard = insets.getInsets(android.view.WindowInsets.Type.ime())
                view.setPadding(bars.left + dp(16), bars.top + dp(16),
                    bars.right + dp(16), maxOf(bars.bottom, keyboard.bottom) + dp(12))
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft + dp(16), insets.systemWindowInsetTop + dp(16),
                    insets.systemWindowInsetRight + dp(16), insets.systemWindowInsetBottom + dp(12))
            }
            insets
        }
        root.requestApplyInsets()
        root.addView(TextView(this).apply { text = "Home-DL"; textSize = 30f; setTextColor(Color.rgb(0, 24, 51)) })
        root.addView(TextView(this).apply { text = "Collez un lien. Choisissez. Téléchargez."; textSize = 16f })
        link = EditText(this).apply {
            hint = "https://…"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
            maxLines = 3
        }
        root.addView(link)
        analyze = Button(this).apply { text = "Analyser"; setOnClickListener { analyzeLink() } }
        root.addView(analyze)
        status = TextView(this).apply { text = "Prototype vidéo • Photos et carrousels non validés"; textSize = 15f }
        root.addView(status)
        updateEngine = Button(this).apply {
            text = "Mettre le moteur à jour"
            setOnClickListener {
                // Explicit request only; no link or media is included in this update.
                begin("Mise à jour du moteur…", 180, Stage.UPDATE, initialize = false) { _, token ->
                    extractor.init()
                    if (token.get()) throw InterruptedException()
                    val result = engineUpdates.check(token)
                    if (token.get()) throw InterruptedException()
                    ui { status.text = "$result Vous pouvez relancer l’analyse." }
                }
            }
        }
        root.addView(updateEngine)
        automaticUpdates = Switch(this).apply {
            text = "Mises à jour automatiques du moteur"
            isChecked = engineUpdates.enabled()
            setOnCheckedChangeListener { _, enabled -> engineUpdates.setEnabled(enabled) }
        }
        root.addView(automaticUpdates)
        items = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(items) }, LinearLayout.LayoutParams(-1, 0, 1f))
        download = Button(this).apply {
            text = "Télécharger la sélection"; isEnabled = false
            setOnClickListener { downloadSelected() }
        }
        cancel = Button(this).apply {
            text = "Annuler"; isEnabled = false
            setOnClickListener { cancelled.set(true); operation?.let { extractor.cancel(it) }; status.text = "Annulation…" }
        }
        root.addView(download); root.addView(cancel)
        root.addView(TextView(this).apply {
            text = "Fichiers : Téléchargements / Home-DL\nGardez cette fenêtre ouverte pendant le test."
            textSize = 12f
        })
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
                    ui { status.text = "$reason\nLe moteur existant est conservé. Vous pouvez analyser un lien." }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); acceptShare(intent) }
    private fun acceptShare(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && !busy) link.setText(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty())
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun begin(message: String, timeoutSeconds: Long, stage: Stage,
                      initialize: Boolean = true, action: (String, AtomicBoolean) -> Unit) {
        if (busy) return
        busy = true
        val id = UUID.randomUUID().toString()
        val token = AtomicBoolean(false)
        cancelled = token; operation = id
        analyze.isEnabled = false; download.isEnabled = false; link.isEnabled = false; cancel.isEnabled = true
        updateEngine.isEnabled = false
        automaticUpdates.isEnabled = false
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        status.text = message
        val deadline = timer.schedule({
            token.set(true); extractor.cancel(id)
        }, timeoutSeconds, TimeUnit.SECONDS)
        worker.submit {
            var currentStage = if (initialize) Stage.INIT else stage
            try {
                if (initialize) extractor.init()
                currentStage = stage
                if (token.get()) throw InterruptedException()
                action(id, token)
            } catch (e: Exception) {
                val diagnostic = if (token.get()) "STOP • Opération arrêtée (annulation ou délai dépassé)."
                    else Diagnostics.describe(currentStage, e)
                ui { status.text = diagnostic }
            } finally {
                deadline.cancel(false)
                ui {
                    busy = false; operation = null
                    analyze.isEnabled = true; link.isEnabled = true; cancel.isEnabled = false
                    updateEngine.isEnabled = true
                    automaticUpdates.isEnabled = true
                    download.isEnabled = selected.isNotEmpty()
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }

    private fun analyzeLink() {
        val url = try { Links.parse(link.text.toString()) } catch (e: Exception) { status.text = e.message; return }
        items.removeAllViews(); selected.clear()
        begin("Analyse en cours…", 120, Stage.ANALYZE) { id, token ->
            val found = extractor.analyze(url, id)
            if (token.get()) throw InterruptedException()
            ui {
                status.text = if (found.isEmpty()) "Aucune vidéo détectée. Les photos ne sont pas encore prises en charge."
                    else "${found.size} vidéo(s) détectée(s). Sélectionnez les éléments."
                found.forEach { media -> addCard(media) }
            }
        }
    }

    private fun addCard(media: Media) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(12), dp(8), dp(12))
        }
        val image = ImageView(this).apply {
            setBackgroundColor(Color.rgb(224, 231, 240)); scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "Aperçu de la vidéo"
        }
        box.addView(image, LinearLayout.LayoutParams(-1, dp(190)))
        box.addView(CheckBox(this).apply {
            text = "${media.title}\n${media.quality}"; textSize = 16f
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
        begin("Préparation du téléchargement…", 1800, Stage.DOWNLOAD) { id, token ->
            var saved = 0
            for (media in batch) {
                if (token.get()) throw InterruptedException()
                val directory = File(cacheDir, "download-${UUID.randomUUID()}").apply { mkdirs() }
                try {
                    val file = extractor.download(media, directory, id) { percent ->
                        ui { status.text = "Fichier ${saved + 1}/${batch.size} • ${percent.toInt().coerceIn(0, 100)} %" }
                    }
                    MediaStoreWriter.save(applicationContext, file) { token.get() }
                    saved++
                    ui { status.text = "${saved}/${batch.size} fichier(s) enregistré(s)." }
                } finally { directory.deleteRecursively() }
            }
            ui { status.text = "Terminé : ${saved} fichier(s) dans Téléchargements / Home-DL." }
        }
    }

    private fun ui(action: () -> Unit) { runOnUiThread { if (!isDestroyed) action() } }

    override fun onDestroy() {
        cancelled.set(true); operation?.let { extractor.cancel(it) }
        worker.shutdownNow(); previews.shutdownNow(); timer.shutdownNow()
        super.onDestroy()
    }
}
