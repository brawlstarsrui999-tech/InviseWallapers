package com.wallapers

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.res.ColorStateList
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private enum class Screen { HOME, SETTINGS, RULES }

    private var currentScreen = Screen.HOME
    private var rulesReturnScreen = Screen.HOME
    private var selectedWallpaper = 0

    private val preferences by lazy(LazyThreadSafetyMode.NONE) {
        getSharedPreferences("wallapers_preferences", MODE_PRIVATE)
    }

    private val wallpaperNames = listOf(
        "Северное сияние",
        "Тихие волны",
        "Лавандовый сад",
        "Ночная орбита"
    )

    private val wallpaperDescriptions = listOf(
        "Мягкое сияние над северным берегом",
        "Спокойный ритм морских линий",
        "Тёплые оттенки и плавные формы",
        "Глубокий цвет с лёгким свечением"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = color(COLOR_BACKGROUND)
        window.navigationBarColor = color(COLOR_BACKGROUND)
        selectedWallpaper = preferences.getInt(KEY_SELECTED_WALLPAPER, 0).coerceIn(wallpaperNames.indices)
        showHome()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        when (currentScreen) {
            Screen.HOME -> super.onBackPressed()
            Screen.SETTINGS -> showHome()
            Screen.RULES -> returnFromRules()
        }
    }

    private fun showHome() {
        currentScreen = Screen.HOME
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), dp(28))
        }

        addBlock(content, smallLabel("КОЛЛЕКЦИЯ  /  04"), bottom = 10)
        val heading = bodyText("Обои с\nнастроением", 32f, COLOR_TEXT, bold = true).apply {
            setLineSpacing(dp(-2).toFloat(), 1f)
        }
        addBlock(content, heading, bottom = 8)
        addBlock(
            content,
            bodyText("Спокойные оттенки, мягкие формы и немного настроения для твоего экрана.", 14f, COLOR_MUTED),
            bottom = 20
        )

        content.addView(
            createHeroCard(),
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(270)).apply { bottomMargin = dp(14) }
        )
        addBlock(content, primaryButton("Установить на главный экран   →") { confirmWallpaperInstall() }, bottom = 27)

        val sectionHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        sectionHeader.addView(
            bodyText("Другие настроения", 19f, COLOR_TEXT, bold = true),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        sectionHeader.addView(smallLabel("4 ВАРИАНТА"))
        addBlock(content, sectionHeader, bottom = 12)

        val firstRow = wallpaperRow(0, 1)
        val secondRow = wallpaperRow(2, 3)
        content.addView(firstRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(184)))
        content.addView(space(12), LinearLayout.LayoutParams(1, dp(12)))
        content.addView(secondRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(184)))

        if (preferences.getBoolean(KEY_SHOW_TIPS, true)) {
            addBlock(
                content,
                infoCard(
                    "✦",
                    "Без лишнего доступа",
                    "Wallapers оформляет экран и не запрашивает список установленных приложений."
                ),
                top = 20,
                bottom = 18
            )
        }

        val rulesLink = bodyText("Правила использования приложения   →", 14f, COLOR_ACCENT, bold = true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(16), dp(8), dp(16))
            isClickable = true
            isFocusable = true
            contentDescription = "Открыть правила использования приложения"
            setOnClickListener {
                performOptionalHaptic(it)
                openRules()
            }
        }
        addBlock(content, rulesLink, top = 2)
        addBlock(content, bodyText("Только обои. Без скрытых функций.", 12f, COLOR_DIM).apply { gravity = Gravity.CENTER }, top = 0)

        display(homeHeader(), content)
    }

    private fun showSettings() {
        currentScreen = Screen.SETTINGS
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(30))
        }

        addBlock(content, smallLabel("ПЕРСОНАЛИЗАЦИЯ"), bottom = 10)
        addBlock(content, bodyText("Настрой приложение\nпод себя.", 29f, COLOR_TEXT, bold = true), bottom = 8)
        addBlock(
            content,
            bodyText("Небольшие настройки интерфейса Wallapers.", 14f, COLOR_MUTED),
            bottom = 20
        )

        addBlock(
            content,
            settingSwitchRow(
                title = "Тактильный отклик",
                description = "Лёгкая вибрация при выборе обоев.",
                key = KEY_HAPTICS,
                defaultValue = true
            ),
            bottom = 10
        )
        addBlock(
            content,
            settingSwitchRow(
                title = "Подсказки на главной",
                description = "Показывать заметку о доступах приложения.",
                key = KEY_SHOW_TIPS,
                defaultValue = true
            ),
            bottom = 24
        )

        addBlock(content, smallLabel("О ПРИЛОЖЕНИИ"), bottom = 10)
        addBlock(
            content,
            infoCard(
                "W",
                "Wallapers  ·  версия 1.0",
                "Локальная коллекция абстрактных обоев. Выбор и настройки сохраняются на этом устройстве."
            ),
            bottom = 18
        )
        addBlock(
            content,
            secondaryButton("Правила использования приложения") { openRules() },
            bottom = 8
        )
        addBlock(
            content,
            bodyText("Приложение не клонирует и не скрывает другие приложения.", 12f, COLOR_DIM),
            top = 6
        )

        display(innerHeader("Настройки") { showHome() }, content)
    }

    private fun showRules() {
        currentScreen = Screen.RULES
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(30))
        }

        addBlock(content, smallLabel("ПРОЗРАЧНОСТЬ"), bottom = 10)
        val heading = bodyText("Только обои.\nНикаких скрытых функций.", 28f, COLOR_TEXT, bold = true).apply {
            setLineSpacing(dp(-1).toFloat(), 1f)
        }
        addBlock(content, heading, bottom = 12)
        addBlock(
            content,
            bodyText(
                "Wallapers создан для оформления экрана. Приложение не клонирует, не скрывает и не запускает сторонние приложения.",
                14f,
                COLOR_MUTED
            ),
            bottom = 20
        )

        addBlock(
            content,
            infoCard(
                "✓",
                "Ваши данные остаются у вас",
                "Приложение не запрашивает список установленных приложений и не требует аккаунта. Выбранная тема и настройки хранятся локально на устройстве."
            ),
            bottom = 20
        )

        addBlock(content, smallLabel("КАК ПОЛЬЗОВАТЬСЯ"), bottom = 10)
        addBlock(content, numberedRule("01", "Выберите обои", "Нажмите на карточку с понравившимся оформлением."), bottom = 8)
        addBlock(content, numberedRule("02", "Примените", "Кнопка установки задаёт изображение для главного экрана."), bottom = 8)
        addBlock(content, numberedRule("03", "Управляйте приватностью в Android", "Для списка приложений и настроек доступа используйте системные функции устройства."), bottom = 20)

        addBlock(content, smallLabel("СИСТЕМНЫЕ НАСТРОЙКИ ANDROID"), bottom = 10)
        addBlock(
            content,
            secondaryButton("Открыть настройки приложений") {
                openAndroidSettings(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            },
            bottom = 10
        )
        addBlock(
            content,
            secondaryButton("Открыть настройки конфиденциальности") {
                openAndroidSettings(Settings.ACTION_PRIVACY_SETTINGS)
            },
            bottom = 16
        )
        addBlock(
            content,
            bodyText(
                "Доступные системные функции зависят от версии Android и модели телефона.",
                12f,
                COLOR_DIM
            )
        )

        display(innerHeader("Правила использования") { returnFromRules() }, content)
    }

    private fun homeHeader(): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }

        val mark = FrameLayout(this).apply {
            background = rounded(color(COLOR_ACCENT), 13)
            contentDescription = "Знак Wallapers"
        }
        mark.addView(
            bodyText("W", 20f, COLOR_BACKGROUND, bold = true).apply { gravity = Gravity.CENTER },
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        row.addView(mark, LinearLayout.LayoutParams(dp(40), dp(40)))

        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(11), 0, 0, 0)
        }
        titleColumn.addView(bodyText("Wallapers", 17f, COLOR_TEXT, bold = true))
        titleColumn.addView(smallLabel("WALLPAPER STUDIO").apply { textSize = 8f; letterSpacing = 0.12f })
        row.addView(titleColumn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val settings = iconButton("⚙︎", "Настройки") { showSettings() }
        row.addView(settings, LinearLayout.LayoutParams(dp(44), dp(44)))
        return row
    }

    private fun innerHeader(title: String, onBack: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }
        row.addView(iconButton("←", "Назад", onBack), LinearLayout.LayoutParams(dp(44), dp(44)))
        row.addView(
            bodyText(title, 16f, COLOR_TEXT, bold = true).apply { setPadding(dp(13), 0, 0, 0) },
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        return row
    }

    @Suppress("DEPRECATION")
    private fun display(header: View, content: View) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(color(COLOR_BACKGROUND))
            clipToPadding = false
        }
        root.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68)))

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = false
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
        scroll.addView(content, ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        root.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                view.setPadding(0, bars.top, 0, bars.bottom)
            } else {
                view.setPadding(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom)
            }
            insets
        }
        setContentView(root)
    }

    private fun createHeroCard(): FrameLayout {
        val card = roundedFrame(26)
        card.contentDescription = "Выбранные обои: ${wallpaperNames[selectedWallpaper]}"
        card.addView(
            WallpaperArtView(this, selectedWallpaper),
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        val topLine = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(14), dp(14), 0)
        }
        val badge = bodyText("ВЫБОР ДНЯ", 10f, COLOR_TEXT, bold = true).apply {
            letterSpacing = 0.08f
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = rounded(Color.argb(115, 8, 15, 13), 30)
        }
        topLine.addView(badge)
        topLine.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        val favorite = iconButton(if (isFavorite(selectedWallpaper)) "♥" else "♡", "Избранное") {
            toggleFavorite()
        }
        topLine.addView(favorite, LinearLayout.LayoutParams(dp(42), dp(42)))
        card.addView(topLine, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP))

        val caption = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(13))
            background = rounded(Color.argb(180, 9, 15, 13), 18)
        }
        caption.addView(smallLabel("${(selectedWallpaper + 1).toString().padStart(2, '0')}  /  WALLAPERS"))
        caption.addView(space(4))
        caption.addView(bodyText(wallpaperNames[selectedWallpaper], 19f, COLOR_TEXT, bold = true))
        caption.addView(bodyText(wallpaperDescriptions[selectedWallpaper], 12f, COLOR_SOFT, topPadding = 3))
        val captionParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        ).apply {
            setMargins(dp(12), 0, dp(12), dp(12))
        }
        card.addView(caption, captionParams)
        card.setOnClickListener { confirmWallpaperInstall() }
        return card
    }

    private fun wallpaperRow(firstIndex: Int, secondIndex: Int): LinearLayout {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(wallpaperThumbnail(firstIndex), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        row.addView(space(12), LinearLayout.LayoutParams(dp(12), 1))
        row.addView(wallpaperThumbnail(secondIndex), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        return row
    }

    private fun wallpaperThumbnail(index: Int): FrameLayout {
        val card = roundedFrame(20)
        card.addView(
            WallpaperArtView(this, index),
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        val titlePanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(11), dp(9), dp(11), dp(10))
            background = rounded(Color.argb(174, 9, 15, 13), 14)
        }
        titlePanel.addView(bodyText(wallpaperNames[index], 13f, COLOR_TEXT, bold = true))
        titlePanel.addView(bodyText(if (index == selectedWallpaper) "Выбрано" else "Нажми, чтобы выбрать", 10f, COLOR_SOFT, topPadding = 3))
        val panelParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        ).apply { setMargins(dp(8), 0, dp(8), dp(8)) }
        card.addView(titlePanel, panelParams)
        card.isClickable = true
        card.isFocusable = true
        card.contentDescription = "Выбрать обои: ${wallpaperNames[index]}"
        card.setOnClickListener {
            performOptionalHaptic(it)
            if (selectedWallpaper != index) {
                selectedWallpaper = index
                preferences.edit().putInt(KEY_SELECTED_WALLPAPER, index).apply()
                showHome()
            } else {
                confirmWallpaperInstall()
            }
        }
        return card
    }

    private fun settingSwitchRow(title: String, description: String, key: String, defaultValue: Boolean): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(12), dp(14))
            background = rounded(color(COLOR_SURFACE), 20)
        }
        val copy = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        copy.addView(bodyText(title, 15f, COLOR_TEXT, bold = true))
        copy.addView(bodyText(description, 12f, COLOR_MUTED, topPadding = 4))
        row.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val toggle = Switch(this).apply {
            isChecked = preferences.getBoolean(key, defaultValue)
            thumbTintList = ColorStateList.valueOf(if (isChecked) color(COLOR_ACCENT) else color(COLOR_SOFT))
            trackTintList = ColorStateList.valueOf(if (isChecked) Color.argb(110, 195, 239, 111) else color(COLOR_DIM))
        }
        toggle.setOnCheckedChangeListener { _, checked ->
            preferences.edit().putBoolean(key, checked).apply()
            toggle.thumbTintList = ColorStateList.valueOf(if (checked) color(COLOR_ACCENT) else color(COLOR_SOFT))
            toggle.trackTintList = ColorStateList.valueOf(if (checked) Color.argb(110, 195, 239, 111) else color(COLOR_DIM))
        }
        row.addView(toggle, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            leftMargin = dp(8)
        })
        return row
    }

    private fun infoCard(icon: String, title: String, description: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(color(COLOR_SURFACE), 20)
        }
        val iconBadge = TextView(this).apply {
            text = icon
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(color(COLOR_ACCENT))
            background = rounded(color(COLOR_SURFACE_HIGHLIGHT), 13)
        }
        card.addView(iconBadge, LinearLayout.LayoutParams(dp(38), dp(38)))
        val copy = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(1), 0, 0)
        }
        copy.addView(bodyText(title, 15f, COLOR_TEXT, bold = true))
        copy.addView(bodyText(description, 12f, COLOR_MUTED, topPadding = 6))
        card.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        return card
    }

    private fun numberedRule(number: String, title: String, description: String): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            setPadding(dp(14), dp(13), dp(14), dp(13))
            background = rounded(color(COLOR_SURFACE), 18)
        }
        row.addView(
            bodyText(number, 11f, COLOR_ACCENT, bold = true).apply { gravity = Gravity.CENTER },
            LinearLayout.LayoutParams(dp(38), dp(30))
        )
        val copy = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, 0, 0)
        }
        copy.addView(bodyText(title, 14f, COLOR_TEXT, bold = true))
        copy.addView(bodyText(description, 12f, COLOR_MUTED, topPadding = 4))
        row.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        return row
    }

    private fun primaryButton(text: String, action: () -> Unit): TextView = actionButton(text, primary = true, action = action)

    private fun secondaryButton(text: String, action: () -> Unit): TextView = actionButton(text, primary = false, action = action)

    private fun actionButton(text: String, primary: Boolean, action: () -> Unit): TextView {
        val base = if (primary) color(COLOR_ACCENT) else color(COLOR_SURFACE_HIGHLIGHT)
        val textColor = if (primary) color(COLOR_BACKGROUND) else color(COLOR_TEXT)
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
            setTextColor(textColor)
            minHeight = dp(54)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = ripple(base, 18)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                performOptionalHaptic(it)
                action()
            }
        }
    }

    private fun iconButton(glyph: String, description: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = glyph
            textSize = if (glyph == "⚙︎") 21f else 23f
            gravity = Gravity.CENTER
            setTextColor(color(COLOR_TEXT))
            background = ripple(color(COLOR_SURFACE), 15)
            isClickable = true
            isFocusable = true
            contentDescription = description
            setOnClickListener {
                performOptionalHaptic(it)
                action()
            }
        }
    }

    private fun smallLabel(text: String): TextView = bodyText(text, 10f, COLOR_ACCENT, bold = true).apply {
        letterSpacing = 0.10f
    }

    private fun bodyText(
        text: String,
        size: Float,
        textColor: String,
        bold: Boolean = false,
        topPadding: Int = 0
    ): TextView = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color(textColor))
        includeFontPadding = false
        if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
        if (topPadding > 0) setPadding(0, dp(topPadding), 0, 0)
        setLineSpacing(dp(2).toFloat(), 1f)
    }

    private fun addBlock(parent: LinearLayout, view: View, top: Int = 0, bottom: Int = 0) {
        parent.addView(view, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        })
    }

    private fun space(height: Int): View = View(this).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(height))
    }

    private fun rounded(color: Int, radiusDp: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun roundedFrame(radiusDp: Int): FrameLayout = FrameLayout(this).apply {
        background = rounded(Color.TRANSPARENT, radiusDp)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: android.graphics.Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, dp(radiusDp).toFloat())
            }
        }
        clipToOutline = true
        setBackgroundColor(Color.TRANSPARENT)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(270))
    }

    private fun ripple(baseColor: Int, radiusDp: Int): RippleDrawable = RippleDrawable(
        ColorStateList.valueOf(Color.argb(36, 255, 255, 255)),
        rounded(baseColor, radiusDp),
        rounded(Color.WHITE, radiusDp)
    )

    private fun toggleFavorite() {
        val favorites = preferences.getStringSet(KEY_FAVORITES, emptySet())?.toMutableSet() ?: mutableSetOf()
        val key = selectedWallpaper.toString()
        val nowFavorite = if (favorites.contains(key)) {
            favorites.remove(key)
            false
        } else {
            favorites.add(key)
            true
        }
        preferences.edit().putStringSet(KEY_FAVORITES, favorites).apply()
        showHome()
        Toast.makeText(this, if (nowFavorite) "Добавлено в избранное" else "Убрано из избранного", Toast.LENGTH_SHORT).show()
    }

    private fun isFavorite(index: Int): Boolean =
        preferences.getStringSet(KEY_FAVORITES, emptySet())?.contains(index.toString()) == true

    private fun confirmWallpaperInstall() {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setTitle("Установить обои?")
            .setMessage("«${wallpaperNames[selectedWallpaper]}» будет установлено на главный экран. Экран блокировки не изменится.")
            .setNegativeButton("Не сейчас", null)
            .setPositiveButton("Установить") { _, _ -> installWallpaper() }
            .show()
    }

    private fun installWallpaper() {
        val bitmap = WallpaperArt.createBitmap(1080, 1920, selectedWallpaper)
        try {
            WallpaperManager.getInstance(this).setBitmap(
                bitmap,
                null,
                true,
                WallpaperManager.FLAG_SYSTEM
            )
            Toast.makeText(this, "Обои установлены на главный экран", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Не удалось установить обои. Попробуйте ещё раз.", Toast.LENGTH_LONG).show()
        } finally {
            bitmap.recycle()
        }
    }

    private fun openRules() {
        rulesReturnScreen = currentScreen
        showRules()
    }

    private fun returnFromRules() {
        if (rulesReturnScreen == Screen.SETTINGS) showSettings() else showHome()
    }

    private fun openAndroidSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Системные настройки недоступны", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performOptionalHaptic(view: View) {
        if (preferences.getBoolean(KEY_HAPTICS, true)) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    private fun color(hex: String): Int = Color.parseColor(hex)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private companion object {
        const val COLOR_BACKGROUND = "#0E1511"
        const val COLOR_SURFACE = "#17211B"
        const val COLOR_SURFACE_HIGHLIGHT = "#223027"
        const val COLOR_TEXT = "#F3F4EC"
        const val COLOR_MUTED = "#AFBCAF"
        const val COLOR_SOFT = "#D7E0D2"
        const val COLOR_DIM = "#738177"
        const val COLOR_ACCENT = "#C3EF6F"
        const val KEY_SELECTED_WALLPAPER = "selected_wallpaper"
        const val KEY_FAVORITES = "favorite_wallpapers"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_SHOW_TIPS = "show_home_tips"
    }
}

private class WallpaperArtView(context: Context, private val wallpaperIndex: Int) : View(context) {
    init {
        setWillNotDraw(false)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: android.graphics.Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 24f * resources.displayMetrics.density)
            }
        }
        clipToOutline = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        WallpaperArt.draw(canvas, width.toFloat(), height.toFloat(), wallpaperIndex)
    }
}

private object WallpaperArt {
    private data class Palette(val top: Int, val bottom: Int, val glow: Int, val accent: Int)

    private val palettes = listOf(
        Palette(Color.parseColor("#15222C"), Color.parseColor("#243B4D"), Color.parseColor("#86DBB5"), Color.parseColor("#B7D9D0")),
        Palette(Color.parseColor("#11272A"), Color.parseColor("#28564F"), Color.parseColor("#9DE6CE"), Color.parseColor("#66B9B0")),
        Palette(Color.parseColor("#241A35"), Color.parseColor("#68486F"), Color.parseColor("#F0A6C8"), Color.parseColor("#D5B2FF")),
        Palette(Color.parseColor("#11172B"), Color.parseColor("#344A70"), Color.parseColor("#8CE7E8"), Color.parseColor("#A8A0FF"))
    )

    fun createBitmap(width: Int, height: Int, index: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap), width.toFloat(), height.toFloat(), index)
        return bitmap
    }

    fun draw(canvas: Canvas, width: Float, height: Float, index: Int) {
        if (width <= 0f || height <= 0f) return
        val palette = palettes[index.mod(palettes.size)]
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.shader = LinearGradient(0f, 0f, width * 0.7f, height, palette.top, palette.bottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null

        paint.shader = RadialGradient(
            width * 0.76f,
            height * 0.24f,
            maxOf(width, height) * 0.72f,
            intArrayOf(withAlpha(palette.glow, 110), withAlpha(palette.glow, 0)),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null

        when (index.mod(palettes.size)) {
            0 -> drawAurora(canvas, width, height, palette, paint)
            1 -> drawWaves(canvas, width, height, palette, paint)
            2 -> drawGarden(canvas, width, height, palette, paint)
            else -> drawOrbit(canvas, width, height, palette, paint)
        }
        drawStars(canvas, width, height, palette, paint)

        paint.shader = LinearGradient(
            0f,
            height * 0.55f,
            0f,
            height,
            intArrayOf(Color.TRANSPARENT, Color.argb(90, 4, 9, 9)),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, height * 0.55f, width, height, paint)
        paint.shader = null
    }

    private fun drawAurora(canvas: Canvas, width: Float, height: Float, palette: Palette, paint: Paint) {
        val first = Path().apply {
            moveTo(-width * 0.08f, height * 0.59f)
            cubicTo(width * 0.14f, height * 0.42f, width * 0.33f, height * 0.64f, width * 0.52f, height * 0.51f)
            cubicTo(width * 0.72f, height * 0.38f, width * 0.84f, height * 0.50f, width * 1.08f, height * 0.30f)
            lineTo(width * 1.08f, height * 0.49f)
            cubicTo(width * 0.87f, height * 0.66f, width * 0.68f, height * 0.57f, width * 0.49f, height * 0.68f)
            cubicTo(width * 0.28f, height * 0.80f, width * 0.13f, height * 0.63f, -width * 0.08f, height * 0.78f)
            close()
        }
        fillPath(canvas, first, withAlpha(palette.glow, 94), paint)

        val second = Path().apply {
            moveTo(-width * 0.05f, height * 0.69f)
            cubicTo(width * 0.18f, height * 0.55f, width * 0.34f, height * 0.76f, width * 0.58f, height * 0.63f)
            cubicTo(width * 0.78f, height * 0.52f, width * 0.92f, height * 0.60f, width * 1.06f, height * 0.48f)
            lineTo(width * 1.06f, height * 0.61f)
            cubicTo(width * 0.81f, height * 0.77f, width * 0.70f, height * 0.68f, width * 0.51f, height * 0.79f)
            cubicTo(width * 0.31f, height * 0.90f, width * 0.14f, height * 0.72f, -width * 0.05f, height * 0.88f)
            close()
        }
        fillPath(canvas, second, withAlpha(palette.accent, 65), paint)
    }

    private fun drawWaves(canvas: Canvas, width: Float, height: Float, palette: Palette, paint: Paint) {
        drawWave(canvas, width, height, 0.53f, 0.065f, withAlpha(palette.glow, 76), paint)
        drawWave(canvas, width, height, 0.64f, 0.055f, withAlpha(palette.accent, 86), paint)
        drawWave(canvas, width, height, 0.77f, 0.045f, Color.argb(58, 219, 248, 224), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf(1.5f, width * 0.004f)
        paint.color = Color.argb(100, 223, 248, 228)
        val line = Path().apply {
            moveTo(width * 0.06f, height * 0.45f)
            cubicTo(width * 0.29f, height * 0.38f, width * 0.68f, height * 0.51f, width * 1.04f, height * 0.38f)
        }
        canvas.drawPath(line, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawGarden(canvas: Canvas, width: Float, height: Float, palette: Palette, paint: Paint) {
        val centerX = width * 0.53f
        val centerY = height * 0.47f
        val unit = minOf(width, height)
        canvas.save()
        canvas.translate(centerX, centerY)
        for (petal in 0 until 6) {
            canvas.save()
            canvas.rotate(petal * 60f)
            paint.color = withAlpha(if (petal % 2 == 0) palette.glow else palette.accent, 52)
            canvas.drawOval(RectF(-unit * 0.14f, -unit * 0.47f, unit * 0.14f, unit * 0.08f), paint)
            canvas.restore()
        }
        paint.color = Color.argb(150, 248, 205, 221)
        canvas.drawCircle(0f, 0f, unit * 0.075f, paint)
        canvas.restore()

        paint.color = withAlpha(palette.accent, 58)
        canvas.drawCircle(width * 0.12f, height * 0.72f, unit * 0.17f, paint)
        paint.color = withAlpha(palette.glow, 38)
        canvas.drawCircle(width * 0.92f, height * 0.25f, unit * 0.20f, paint)
    }

    private fun drawOrbit(canvas: Canvas, width: Float, height: Float, palette: Palette, paint: Paint) {
        val cx = width * 0.58f
        val cy = height * 0.43f
        val unit = minOf(width, height)
        canvas.save()
        canvas.rotate(-27f, cx, cy)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf(1.5f, unit * 0.008f)
        paint.color = Color.argb(118, 210, 232, 248)
        canvas.drawOval(RectF(width * 0.12f, height * 0.22f, width * 1.02f, height * 0.63f), paint)
        paint.strokeWidth = maxOf(1f, unit * 0.004f)
        paint.color = Color.argb(62, 226, 233, 255)
        canvas.drawOval(RectF(width * 0.04f, height * 0.16f, width * 1.10f, height * 0.69f), paint)
        paint.style = Paint.Style.FILL
        canvas.restore()

        paint.shader = RadialGradient(
            cx - unit * 0.04f,
            cy - unit * 0.04f,
            unit * 0.21f,
            intArrayOf(Color.argb(245, 196, 236, 238), palette.glow, Color.argb(255, 76, 112, 154)),
            floatArrayOf(0f, 0.52f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, unit * 0.20f, paint)
        paint.shader = null
        paint.color = Color.argb(220, 229, 238, 244)
        canvas.drawCircle(width * 0.83f, height * 0.62f, unit * 0.027f, paint)
    }

    private fun drawWave(canvas: Canvas, width: Float, height: Float, baseline: Float, amplitude: Float, color: Int, paint: Paint) {
        val path = Path().apply {
            moveTo(0f, height * baseline)
            cubicTo(width * 0.27f, height * (baseline - amplitude), width * 0.64f, height * (baseline + amplitude), width, height * (baseline - amplitude * 0.35f))
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        fillPath(canvas, path, color, paint)
    }

    private fun drawStars(canvas: Canvas, width: Float, height: Float, palette: Palette, paint: Paint) {
        val points = arrayOf(
            0.18f to 0.19f, 0.37f to 0.12f, 0.82f to 0.14f, 0.90f to 0.34f,
            0.12f to 0.39f, 0.74f to 0.54f, 0.28f to 0.83f, 0.89f to 0.82f,
            0.51f to 0.28f, 0.42f to 0.72f
        )
        points.forEachIndexed { i, point ->
            val radius = if (i % 3 == 0) 2.4f else 1.35f
            paint.color = Color.argb(if (i % 2 == 0) 150 else 88, 245, 250, 239)
            canvas.drawCircle(width * point.first, height * point.second, radius * resourcesDensity(width), paint)
        }
        paint.color = withAlpha(palette.accent, 78)
    }

    private fun fillPath(canvas: Canvas, path: Path, color: Int, paint: Paint) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawPath(path, paint)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

    private fun resourcesDensity(width: Float): Float = (width / 390f).coerceAtLeast(0.72f)
}
