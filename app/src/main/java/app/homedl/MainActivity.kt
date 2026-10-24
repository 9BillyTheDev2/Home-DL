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
    private val selected = linkedSetOf<Media>()
    @Volatile private var busy = false
    @Volatile private var operation: String? = null
    private var cancelled = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extractor = Extractor(applicationContext)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 16)
            setBackgroundColor(Color.rgb(245, 247, 251))
            fitsSystemWindows = true
        }
        setContentView(root)
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
    }

    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); acceptShare(intent) }
    private fun acceptShare(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && !busy) link.setText(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty())
    }

    private fun begin(message: String, timeoutSeconds: Long, action: (String, AtomicBoolean) -> Unit) {
        if (busy) return
        busy = true
        val id = UUID.randomUUID().toString()
        val token = AtomicBoolean(false)
        cancelled = token; operation = id
        analyze.isEnabled = false; download.isEnabled = false; link.isEnabled = false; cancel.isEnabled = true
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        status.text = message
        val deadline = timer.schedule({
            token.set(true); extractor.cancel(id)
        }, timeoutSeconds, TimeUnit.SECONDS)
        worker.submit {
            try {
                extractor.init()
                if (token.get()) throw InterruptedException()
                action(id, token)
            } catch (_: Exception) {
                ui { status.text = if (token.get()) "Opération arrêtée (annulation ou délai dépassé)." else
                    "Échec : lien inaccessible, connexion requise ou média non pris en charge. Aucun identifiant n’est envoyé par Home-DL." }
            } finally {
                deadline.cancel(false)
                ui {
                    busy = false; operation = null
                    analyze.isEnabled = true; link.isEnabled = true; cancel.isEnabled = false
                    download.isEnabled = selected.isNotEmpty()
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }

    private fun analyzeLink() {
        val url = try { Links.parse(link.text.toString()) } catch (e: Exception) { status.text = e.message; return }
        items.removeAllViews(); selected.clear()
        begin("Analyse en cours…", 120) { id, token ->
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
            orientation = LinearLayout.VERTICAL; setPadding(12, 16, 12, 16)
        }
        val image = ImageView(this).apply {
            setBackgroundColor(Color.rgb(224, 231, 240)); scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "Aperçu de la vidéo"
        }
        box.addView(image, LinearLayout.LayoutParams(-1, 360))
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
        begin("Préparation du téléchargement…", 1800) { id, token ->
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
