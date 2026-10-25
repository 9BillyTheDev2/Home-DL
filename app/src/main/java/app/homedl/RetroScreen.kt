package app.homedl

import android.app.Activity
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

/** Original terminal/time-machine visual language. No external assets or fonts. */
class RetroScreen(private val activity: Activity) {
    companion object {
        val BACKGROUND = Color.rgb(4, 10, 12)
        val PANEL = Color.rgb(11, 23, 25)
        val GREEN = Color.rgb(107, 255, 156)
        val CYAN = Color.rgb(107, 230, 243)
        val ORANGE = Color.rgb(255, 177, 83)
        val TEXT = Color.rgb(231, 246, 239)
        val MUTED = Color.rgb(158, 185, 177)
        val BORDER = Color.rgb(39, 69, 64)
    }
    val root = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(BACKGROUND)
    }
    val link = EditText(activity)
    val analyze = Button(activity)
    val clearLink = Button(activity)
    val diagnostic = Button(activity)
    val browserInstagram = Button(activity)
    val status = TextView(activity)
    val updateEngine = Button(activity)
    val automaticUpdates = Switch(activity)
    val updateApp = Button(activity)
    val automaticAppUpdates = Switch(activity)
    val appUpdateStatus = TextView(activity)
    val appUpdateProgress = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
        max = 100
        isIndeterminate = false
        progressTintList = android.content.res.ColorStateList.valueOf(CYAN)
        visibility = View.GONE
        contentDescription = "Progression de la mise à jour"
    }
    private val operationPanel = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        visibility = View.GONE
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = shape(PANEL, BORDER)
    }
    private val operationLabel = label("", 12f, TEXT)
    private val operationBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
        max = 100
        progressTintList = ColorStateList.valueOf(GREEN)
        indeterminateTintList = ColorStateList.valueOf(CYAN)
        progressBackgroundTintList = ColorStateList.valueOf(BORDER)
    }
    val items = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
    val download = Button(activity)
    val cancel = Button(activity)
    private val rain = CodeRain(activity)
    private val phase = label("EN ATTENTE", 12f, GREEN)
    private val count = label("00 MÉDIA", 12f, CYAN)
    private val placeholder = LinearLayout(activity)
    private val preferences = activity.getSharedPreferences("appearance", Activity.MODE_PRIVATE)

    init {
        val scroll = ScrollView(activity).apply {
            isFillViewport = true
            clipToPadding = false
        }
        val content = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val header = FrameLayout(activity).apply {
            background = shape(PANEL, BORDER)
            clipToOutline = true
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        header.addView(rain, FrameLayout.LayoutParams(-1, -1))
        val headerText = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(16))
        }
        headerText.addView(label("PERSONAL MEDIA TERMINAL", 10f, MUTED))
        headerText.addView(label("HOME–DL", 34f, TEXT).apply {
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            letterSpacing = 0.05f
        })
        headerText.addView(label("VOS LIENS. VOS MÉDIAS. VOTRE TEMPS.", 10f, GREEN))
        val dashboard = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(18), 0, 0)
        }
        dashboard.addView(counter("SOURCE", "HTTPS", ORANGE), LinearLayout.LayoutParams(0, -2, 1f))
        dashboard.addView(counter("CONNEXION", "DIRECTE", CYAN), LinearLayout.LayoutParams(0, -2, 1f))
        dashboard.addView(counter("DESTINATION", "LOCAL", GREEN), LinearLayout.LayoutParams(0, -2, 1f))
        headerText.addView(dashboard)
        header.addView(headerText)
        content.addView(header, spaced(-1, -2, 0, 16))

        val appPanel = section(content, "HOME-DL  /  MISES À JOUR", CYAN)
        appUpdateStatus.apply {
            text = "Version " + activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
            textSize = 12f
            setTextColor(MUTED)
            setPadding(0, 0, 0, dp(8))
        }
        appPanel.addView(appUpdateStatus)
        appPanel.addView(appUpdateProgress, LinearLayout.LayoutParams(-1, dp(12)).apply {
            bottomMargin = dp(12)
        })
        decorateButton(updateApp, "Vérifier la mise à jour de l’app", false)
        appPanel.addView(updateApp, LinearLayout.LayoutParams(-1, -2))

        val source = section(content, "01  /  ENTREZ DANS LE FLUX", ORANGE)
        source.addView(label("Collez un lien, récupérez l’instant.", 15f, TEXT))
        link.apply {
            hint = "https://…"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(false)
            setHorizontallyScrolling(false)
            gravity = Gravity.TOP or Gravity.START
            maxLines = 3
            minHeight = dp(56)
            textSize = 15f
            typeface = Typeface.MONOSPACE
            setTextColor(TEXT)
            setHintTextColor(MUTED)
            background = shape(BACKGROUND, BORDER, 10)
            backgroundTintList = null
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        val linkRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        linkRow.addView(link, LinearLayout.LayoutParams(0, -2, 1f))
        decorateButton(clearLink, "×", false)
        clearLink.contentDescription = "Effacer le lien et les résultats"
        clearLink.textSize = 28f
        clearLink.setTextColor(Color.rgb(255, 121, 132))
        linkRow.addView(clearLink, LinearLayout.LayoutParams(dp(48), dp(56)).apply { leftMargin = dp(8) })
        source.addView(linkRow, spaced(-1, -2, 12, 10))
        decorateButton(analyze, "Analyser le lien", true)
        source.addView(analyze, LinearLayout.LayoutParams(-1, dp(52)))

        val feedback = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = shape(PANEL, BORDER)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        val feedbackHead = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        feedbackHead.addView(phase, LinearLayout.LayoutParams(0, -2, 1f))
        feedbackHead.addView(count)
        feedback.addView(feedbackHead)
        status.apply {
            text = "Prêt à analyser un lien.\nInstagram public : photos, vidéos et carrousels."
            textSize = 13f
            setTextColor(MUTED)
            setPadding(0, dp(8), 0, 0)
            setTextIsSelectable(true)
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        feedback.addView(status)
        decorateButton(diagnostic, "Voir / copier le diagnostic", false)
        diagnostic.visibility = View.GONE
        feedback.addView(diagnostic, spaced(-1, dp(48), 10, 0))
        decorateButton(browserInstagram, "Essayer le navigateur local", true)
        browserInstagram.visibility = View.GONE
        feedback.addView(browserInstagram, spaced(-1, -2, 10, 0))
        content.addView(feedback, spaced(-1, -2, 0, 16))

        val settingsToggle = Button(activity)
        decorateButton(settingsToggle, "Réglages du terminal  +", false)
        content.addView(settingsToggle, spaced(-1, dp(48), 0, 8))
        val settings = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = shape(PANEL, BORDER)
            setPadding(dp(14), dp(8), dp(14), dp(12))
            visibility = View.GONE
        }
        automaticUpdates.apply {
            text = "Moteur : mise à jour automatique"
            textSize = 13f
            setTextColor(TEXT)
            minHeight = dp(56)
            thumbTintList = ColorStateList.valueOf(GREEN)
            trackTintList = ColorStateList.valueOf(BORDER)
        }
        settings.addView(automaticUpdates)
        decorateButton(updateEngine, "Vérifier le moteur maintenant", false)
        settings.addView(updateEngine, LinearLayout.LayoutParams(-1, dp(48)))
        automaticAppUpdates.apply {
            text = "Application : rechercher les mises à jour"
            textSize = 13f
            setTextColor(TEXT)
            minHeight = dp(56)
            thumbTintList = ColorStateList.valueOf(CYAN)
            trackTintList = ColorStateList.valueOf(BORDER)
        }
        settings.addView(automaticAppUpdates)
        val animation = Switch(activity).apply {
            text = "Pluie de code animée"
            textSize = 13f
            setTextColor(TEXT)
            minHeight = dp(56)
            thumbTintList = ColorStateList.valueOf(CYAN)
            trackTintList = ColorStateList.valueOf(BORDER)
            isChecked = preferences.getBoolean("rain", true)
            setOnCheckedChangeListener { _, enabled ->
                preferences.edit().putBoolean("rain", enabled).apply()
                rain.setAnimationEnabled(enabled)
            }
        }
        rain.setAnimationEnabled(animation.isChecked)
        settings.addView(animation)
        content.addView(settings, spaced(-1, -2, 0, 12))
        settingsToggle.setOnClickListener {
            val open = settings.visibility != View.VISIBLE
            settings.visibility = if (open) View.VISIBLE else View.GONE
            settingsToggle.text = if (open) "Réglages du terminal  −" else "Réglages du terminal  +"
        }

        content.addView(label("02  /  VOTRE SÉLECTION", 11f, CYAN).apply {
            setPadding(0, dp(12), 0, dp(12))
        })
        placeholder.apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = shape(PANEL, BORDER)
            setPadding(dp(18), dp(28), dp(18), dp(28))
            addView(label("[  ↓  ]", 30f, GREEN))
            addView(label("Le prochain instant commence ici.", 14f, TEXT).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(10), 0, dp(6))
            })
            addView(label("Les aperçus apparaîtront après l’analyse.", 12f, MUTED).apply {
                gravity = Gravity.CENTER
            })
        }
        content.addView(placeholder, spaced(-1, -2, 0, 12))
        content.addView(items)
        content.addView(label("QUALITÉ MAXIMALE ACCESSIBLE  /  SANS PUBLICITÉ", 9f, MUTED).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, dp(18))
        })

        val actions = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, 0)
        }
        operationPanel.addView(operationLabel)
        operationPanel.addView(operationBar, LinearLayout.LayoutParams(-1, dp(16)))
        actions.addView(operationPanel, spaced(-1, -2, 0, 8))
        decorateButton(download, "Télécharger la sélection", true)
        download.isEnabled = false
        actions.addView(download, LinearLayout.LayoutParams(-1, dp(54)))
        decorateButton(cancel, "Annuler l’opération", false)
        cancel.visibility = View.GONE
        cancel.isEnabled = false
        actions.addView(cancel, spaced(-1, dp(48), 8, 0))
        actions.addView(label("Téléchargements / Home-DL · Gardez l’app ouverte", 10f, MUTED).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(9), 0, dp(2))
        })
        root.addView(actions)
    }

    /** Pinned outside the scroll area, so progress stays visible above the action buttons. */
    fun progress(message: String, percent: Int? = null) {
        operationPanel.visibility = View.VISIBLE
        operationLabel.text = message
        operationBar.isIndeterminate = percent == null
        if (percent != null) operationBar.progress = percent.coerceIn(0, 100)
        operationBar.contentDescription = message
    }

    fun hideProgress() { operationPanel.visibility = View.GONE }

    fun showFailure() { phase.text = "OPÉRATION INTERROMPUE"; phase.setTextColor(ORANGE) }

    fun setBusy(value: Boolean) {
        phase.text = if (value) "FLUX EN COURS" else "EN ATTENTE"
        phase.setTextColor(if (value) ORANGE else GREEN)
        cancel.visibility = if (value) View.VISIBLE else View.GONE
    }

    fun setMediaCount(value: Int) {
        count.text = "${value.toString().padStart(2, '0')} MÉDIA(S)"
        placeholder.visibility = if (value == 0) View.VISIBLE else View.GONE
    }

    fun decorateCard(view: View) {
        view.background = shape(PANEL, BORDER)
        view.setPadding(dp(12), dp(12), dp(12), dp(12))
        view.layoutParams = spaced(-1, -2, 0, 12)
    }

    fun styleSelection(box: CheckBox) {
        box.setTextColor(TEXT)
        box.buttonTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(GREEN, MUTED)
        )
        box.minHeight = dp(56)
    }

    fun resume() { rain.resume() }
    fun pause() { rain.pause() }

    private fun counter(title: String, value: String, color: Int) = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        addView(label(title, 8f, MUTED))
        addView(label(value, 16f, color).apply {
            typeface = Typeface.create("monospace", Typeface.BOLD)
            setPadding(0, dp(5), 0, 0)
        })
    }

    private fun section(parent: LinearLayout, title: String, color: Int): LinearLayout {
        val section = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = shape(PANEL, BORDER)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            addView(label(title, 11f, color).apply { setPadding(0, 0, 0, dp(12)) })
        }
        parent.addView(section, spaced(-1, -2, 0, 14))
        return section
    }

    private fun label(value: String, size: Float, color: Int) = TextView(activity).apply {
        text = value
        textSize = size
        typeface = Typeface.MONOSPACE
        setTextColor(color)
    }

    private fun decorateButton(button: Button, title: String, primary: Boolean) {
        button.text = title
        button.textSize = 14f
        button.typeface = Typeface.create("monospace", Typeface.BOLD)
        button.isAllCaps = false
        button.minHeight = dp(48)
        button.setPadding(dp(12), dp(8), dp(12), dp(8))
        button.backgroundTintList = null
        button.setTextColor(ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()),
            intArrayOf(Color.rgb(108, 134, 124), if (primary) BACKGROUND else TEXT)
        ))
        val states = android.graphics.drawable.StateListDrawable().apply {
            addState(intArrayOf(-android.R.attr.state_enabled), shape(Color.rgb(19, 37, 32), BORDER, 12))
            addState(intArrayOf(), if (primary) GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(GREEN, CYAN)
            ).apply { cornerRadius = dp(12).toFloat() } else shape(PANEL, BORDER, 12))
        }
        button.background = RippleDrawable(ColorStateList.valueOf(0x3334EEA0), states, null)
    }

    private fun shape(fill: Int, border: Int, radius: Int = 16) = GradientDrawable().apply {
        setColor(fill)
        setStroke(dp(1), border)
        cornerRadius = dp(radius).toFloat()
    }
    private fun spaced(width: Int, height: Int, top: Int, bottom: Int) =
        LinearLayout.LayoutParams(width, height).apply { setMargins(0, dp(top), 0, dp(bottom)) }
    private fun dp(value: Int) = (value * activity.resources.displayMetrics.density).toInt()
}

/** Small, decorative header animation. No bitmaps, network, timers or background activity. */
private class CodeRain(context: android.content.Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE }
    private val heads = FloatArray(18) { -it * 17f }
    private val glyphs = charArrayOf('0', '1', ':', '/', '+', '7', '8', '9')
    private var running = false
    private var enabled = true
    private var frame = 0
    init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }

    fun setAnimationEnabled(value: Boolean) { enabled = value; invalidate() }
    fun resume() { running = true; invalidate() }
    fun pause() { running = false }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val unit = resources.displayMetrics.density
        paint.textSize = 11 * unit
        val step = 16 * unit
        for (column in heads.indices) {
            val x = width * (column + 0.5f) / heads.size
            for (row in 0..5) {
                paint.color = if (row == 0) Color.argb(48, 107, 255, 156) else Color.argb(24, 107, 255, 156)
                val y = heads[column] - row * step
                canvas.drawText(glyphs[(column + row + frame / 4) % glyphs.size].toString(), x, y, paint)
            }
        }
        // Original circuit/time-stream lines, kept behind the header text.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = unit
        paint.color = Color.argb(90, 255, 177, 83)
        val path = Path().apply {
            moveTo(width * 0.65f, 12 * unit)
            lineTo(width * 0.78f, 12 * unit)
            lineTo(width * 0.91f, height * 0.30f)
            lineTo(width.toFloat(), height * 0.30f)
        }
        canvas.drawPath(path, paint)
        paint.color = Color.argb(75, 107, 230, 243)
        canvas.drawLine(width * 0.80f, height - 13 * unit, width.toFloat(), height - 13 * unit, paint)
        paint.style = Paint.Style.FILL
        if (running && enabled && isShown && ValueAnimator.areAnimatorsEnabled()) {
            for (i in heads.indices) {
                heads[i] += unit * (2 + i % 3)
                if (heads[i] > height + 6 * step) heads[i] = -step
            }
            frame++
            postInvalidateDelayed(83L) // about 12 fps, only while this screen is resumed
        }
    }
}
