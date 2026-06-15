package com.klodit.almizan.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────
//  AL-MIZAN LIGHT COLOR SCHEME
//
//  primary         = Navy800   (#364150)  → headers, top bars, primary buttons bg
//  onPrimary       = White                → text/icons on primary bg
//  primaryContainer= Navy50    (#ECEFF1)  → page background
//  onPrimaryContainer = Navy900           → text on page bg
//
//  secondary       = Green500  (#4CAF50)  → CTA buttons, active states, progress
//  onSecondary     = White                → text on green buttons
//  secondaryContainer = Green50           → light green card backgrounds
//  onSecondaryContainer = Navy900         → text on green cards
//
//  surface         = White                → cards, sheets
//  onSurface       = Navy900              → text on cards
//  surfaceVariant  = Navy30   (#F8FAFB)   → input field background
//  onSurfaceVariant= Navy500              → hint text, labels
//
//  outline         = Navy100  (#DDE3E8)   → borders
//  outlineVariant  = Grey200             → dividers
//
//  error           = Red600   (#E53935)
//  errorContainer  = Red50
//  onError         = White
//
//  tertiary        = Blue800  (#1565C0)   → info/document accent
//  tertiaryContainer = Blue50            → blue info card bg
// ─────────────────────────────────────────────

private val AlMizanLightColors = lightColorScheme(
    primary              = Navy800,
    onPrimary            = NavyWhite,
    primaryContainer     = Navy50,
    onPrimaryContainer   = Navy900,

    secondary            = Green500,
    onSecondary          = NavyWhite,
    secondaryContainer   = Green50,
    onSecondaryContainer = Navy900,

    tertiary             = Blue800,
    onTertiary           = NavyWhite,
    tertiaryContainer    = Blue50,
    onTertiaryContainer  = Navy900,

    surface              = NavyWhite,
    onSurface            = Navy900,
    surfaceVariant       = Navy30,
    onSurfaceVariant     = Navy500,

    background           = Navy50,
    onBackground         = Navy900,

    outline              = Navy100,
    outlineVariant       = Grey200,

    error                = Red600,
    onError              = NavyWhite,
    errorContainer       = Red50,
    onErrorContainer     = Red600,
)

@Composable
fun AlMizanTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AlMizanLightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AlMizanTypography,
        content     = content
    )
}

// ─────────────────────────────────────────────
//  CONVENIENCE EXTENSIONS
//  Use these in screens instead of raw Color vals
// ─────────────────────────────────────────────

// MaterialTheme.colorScheme.primary        → Navy800  (header bg)
// MaterialTheme.colorScheme.secondary      → Green500 (buttons, progress)
// MaterialTheme.colorScheme.background     → Navy50   (page bg)
// MaterialTheme.colorScheme.surface        → White    (cards)
// MaterialTheme.colorScheme.surfaceVariant → Navy30   (field bg)
// MaterialTheme.colorScheme.outline        → Navy100  (borders)
// MaterialTheme.colorScheme.onSurface      → Navy900  (dark text)
// MaterialTheme.colorScheme.onSurfaceVariant → Navy500 (mid text / hints)
// MaterialTheme.colorScheme.error          → Red600
// MaterialTheme.colorScheme.tertiary       → Blue800  (info)
// MaterialTheme.colorScheme.tertiaryContainer → Blue50 (info card bg)