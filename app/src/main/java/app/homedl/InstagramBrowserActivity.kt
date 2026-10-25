package app.homedl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.*
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import org.json.JSONTokener
import org.json.JSONObject

/** Visible, isolated anonymous browser. No native JavaScript bridge and no account import. */
class InstagramBrowserActivity : Activity() {
    companion object {
        private var directorySelected = false
        const val SOURCE = "source"
        const val DATA = "media_data"
        const val CODE = "browser_code"
        const val TRACE = "browser_trace"
    }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var web: WebView
    private lateinit var label: TextView
    private lateinit var bar: ProgressBar
    private lateinit var source: String
    private lateinit var script: String
    private val events = mutableListOf<String>()
    private var finished = false
    private var polling = false
    private var loaded = false
    private var started = 0L
    private fun event(value: String) {
        if (events.size < 32) events.add("+${(System.nanoTime() - started) / 1_000_000}ms $value")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        started = System.nanoTime()
        source = intent.getStringExtra(SOURCE).orEmpty()
        val code = runCatching { InstagramPublic.reference(source) }.getOrElse { finishFailure("IG_BROWSER_URL"); return }
        // This activity runs in its own app process; never shares the main process WebView store.
        synchronized(InstagramBrowserActivity::class.java) {
            if (!directorySelected) { WebView.setDataDirectorySuffix("instagram_anonymous"); directorySelected = true }
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(RetroScreen.BACKGROUND)
            setPadding(16, 16, 16, 16)
        }
        label = TextView(this).apply {
            text = "Navigateur local · session anonyme\nChargement de la publication…"
            setTextColor(RetroScreen.TEXT); textSize = 15f
            setPadding(8, 8, 8, 8)
        }
        root.addView(label)
        bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100; isIndeterminate = true
            progressTintList = android.content.res.ColorStateList.valueOf(RetroScreen.GREEN)
            indeterminateTintList = android.content.res.ColorStateList.valueOf(RetroScreen.CYAN)
        }
        root.addView(bar, LinearLayout.LayoutParams(-1, 16))
        web = try { WebView(this) } catch (_: Exception) { finishFailure("IG_BROWSER_UNAVAILABLE"); return }
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(Button(this).apply {
            text = "Annuler et revenir à Home-DL"
            setOnClickListener { finishFailure("IG_BROWSER_STOP") }
        })
        setContentView(root)
        root.setOnApplyWindowInsetsListener { view, insets ->
            @Suppress("DEPRECATION")
            view.setPadding(16 + insets.systemWindowInsetLeft, 16 + insets.systemWindowInsetTop,
                16 + insets.systemWindowInsetRight, 16 + insets.systemWindowInsetBottom)
            insets
        }
        root.requestApplyInsets()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            safeBrowsingEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mediaPlaybackRequiresUserGesture = true
        }
        script = assets.open("browser/instagram.js").bufferedReader().use { it.readText() }
            .replace("__EXPECTED_CODE__", JSONObject.quote(code))
            .replace("__EXPECTED_ID__", JSONObject.quote(InstagramPublic.mediaId(code)))
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(web, script, setOf("https://www.instagram.com", "https://instagram.com"))
            event("BROWSER_DOCUMENT_START ENABLED")
        } else event("BROWSER_DOCUMENT_START UNAVAILABLE")
        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, value: Int) {
                if (!finished && !loaded) {
                    bar.isIndeterminate = false; bar.progress = value
                    label.text = "Chargement de la page Instagram : $value %\nSession anonyme sur le téléphone"
                }
            }
            override fun onPermissionRequest(request: PermissionRequest) { request.deny() }
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.isForMainFrame) return false
                val target = request.url.toString()
                return when (InstagramBrowserResult.navigation(target)) {
                    InstagramBrowserResult.Navigation.IGNORE -> {
                        event("BROWSER_NAVIGATION IGNORED " + InstagramBrowserResult.navigationSummary(target))
                        // Reject this target only; keep inspecting the original page. Never launch an app intent.
                        true
                    }
                    InstagramBrowserResult.Navigation.ACCESS -> {
                        event("BROWSER_NAVIGATION ACCESS")
                        finishFailure("IG_BROWSER_ACCESS"); true
                    }
                    InstagramBrowserResult.Navigation.PAGE -> false
                }
            }
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val uri = request.url
                val host = uri.host.orEmpty().lowercase()
                val allowed = uri.scheme == "https" && (host == "instagram.com" || host.endsWith(".instagram.com") || InstagramPublic.cdn(host))
                return if (allowed) null else WebResourceResponse("text/plain", "UTF-8", 403, "Blocked external origin", emptyMap(), java.io.ByteArrayInputStream(byteArrayOf()))
            }
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                if (!InstagramBrowserResult.allowedPage(url) || InstagramBrowserResult.accessPage(url)) { finishFailure("IG_BROWSER_ACCESS"); return }
                event("BROWSER_PAGE BEGIN")
            }
            override fun onPageCommitVisible(view: WebView, url: String) {
                if (!finished && InstagramBrowserResult.allowedPage(url) && !InstagramBrowserResult.accessPage(url)) {
                    event("BROWSER_PAGE VISIBLE")
                    startInspection()
                }
            }
            override fun onPageFinished(view: WebView, url: String) {
                if (finished || !InstagramBrowserResult.allowedPage(url)) return
                event("BROWSER_PAGE LOADED")
                startInspection()
            }
            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                finishFailure("IG_BROWSER_RENDER"); return true
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) { event("BROWSER_NETWORK code=${error.errorCode}"); finishFailure("IG_BROWSER_NETWORK") }
            }
            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                if (request.isForMainFrame) { event("BROWSER_HTTP status=${response.statusCode}"); finishFailure("IG_BROWSER_HTTP") }
            }
        }
        web.setDownloadListener { target, _, _, _, _ ->
            event("BROWSER_DOWNLOAD IGNORED " + InstagramBrowserResult.navigationSummary(target))
            // A detached download has no proven post identity; do not accept it or close the page.
        }
        val cookies = CookieManager.getInstance()
        cookies.setAcceptCookie(true)
        cookies.setAcceptThirdPartyCookies(web, false)
        cookies.removeAllCookies {
            if (!finished) {
                WebStorage.getInstance().deleteAllData()
                event("BROWSER_ANONYMOUS BEGIN")
                web.loadUrl("https://www.instagram.com/" + (if (Uri.parse(source).path.orEmpty().startsWith("/reel")) "reel/" else "p/") + code + "/")
            }
        }
        handler.postDelayed({ finishFailure("IG_BROWSER_EMPTY") }, 45000)
    }

    private fun startInspection() {
        loaded = true
        bar.isIndeterminate = true
        label.text = "Lecture des médias après JavaScript…\nAucun compte connecté · délai maximal 45 s"
        web.evaluateJavascript(script, null)
        if (!polling) { polling = true; poll() }
    }

    private fun poll() {
        if (finished) return
        web.evaluateJavascript("window.__homeDlBrowser ? window.__homeDlBrowser.snapshot() : null") { encoded ->
            if (finished) return@evaluateJavascript
            try {
                val raw = JSONTokener(encoded).nextValue() as? String
                if (raw != null && raw.length <= 240000) {
                    val result = JSONObject(raw)
                    if (result.optJSONObject("product") != null) {
                        val media = InstagramBrowserResult.media(source, raw)
                        event("BROWSER_MEDIA total=${media.size}; " + InstagramBrowserResult.summary(raw))
                        complete("OK", raw); return@evaluateJavascript
                    }
                    if (result.optBoolean("login")) { finishFailure("IG_BROWSER_ACCESS"); return@evaluateJavascript }
                    // Fixed counters only, no URLs, HTML, cookies, author names or response bodies.
                    if (events.count { it.contains("BROWSER_SCAN") } < 3) event("BROWSER_SCAN " + InstagramBrowserResult.summary(raw))
                }
            } catch (_: Exception) { event("BROWSER_PARSE REJECTED"); finishFailure("IG_BROWSER_PARSE"); return@evaluateJavascript }
            handler.postDelayed({ poll() }, 2000)
        }
    }

    private fun finishFailure(code: String) { complete(code, null) }
    private fun complete(code: String, raw: String?) {
        if (finished) return
        finished = true; handler.removeCallbacksAndMessages(null)
        event("BROWSER_END $code")
        setResult(if (code == "OK") RESULT_OK else RESULT_CANCELED, Intent().apply {
            putExtra(CODE, code); putExtra(TRACE, events.joinToString("\n"))
            if (raw != null) putExtra(DATA, raw)
        })
        finish()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { finishFailure("IG_BROWSER_STOP") }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::web.isInitialized) {
            web.stopLoading()
            (web.parent as? ViewGroup)?.removeView(web)
            web.clearCache(true); web.clearHistory(); web.destroy()
            CookieManager.getInstance().removeAllCookies(null)
            WebStorage.getInstance().deleteAllData()
        }
        super.onDestroy()
    }
}
