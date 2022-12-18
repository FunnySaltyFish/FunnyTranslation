package com.funny.translation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.AppConfig
import com.funny.translation.helper.DateUtils
import com.funny.translation.ui.SystemBarSettings


private val LightColors = lightColorScheme(
    surfaceTint = md_theme_light_surfaceTint,
    onErrorContainer = md_theme_light_onErrorContainer,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    tertiary = md_theme_light_tertiary,
    error = md_theme_light_error,
    outline = md_theme_light_outline,
    onBackground = md_theme_light_onBackground,
    background = md_theme_light_background,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    surface = md_theme_light_surface,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    secondary = md_theme_light_secondary,
    inversePrimary = md_theme_light_inversePrimary,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    primary = md_theme_light_primary,
)


private val DarkColors = darkColorScheme(
    surfaceTint = md_theme_dark_surfaceTint,
    onErrorContainer = md_theme_dark_onErrorContainer,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    tertiary = md_theme_dark_tertiary,
    error = md_theme_dark_error,
    outline = md_theme_dark_outline,
    onBackground = md_theme_dark_onBackground,
    background = md_theme_dark_background,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    surface = md_theme_dark_surface,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    secondary = md_theme_dark_secondary,
    inversePrimary = md_theme_dark_inversePrimary,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    primary = md_theme_dark_primary,
)

private val SpringFestivalColorPalette = lightColorScheme(
    primary = MaterialColors.Red500,
    tertiary = MaterialColors.Red700,
    secondary = MaterialColors.RedA400,
    onSecondary = Color.White,
    primaryContainer = MaterialColors.Red200.copy(alpha = 0.7f),
    onPrimaryContainer = MaterialColors.RedA700
)

@Composable
fun TransTheme(
    dark: Boolean = isSystemInDarkTheme(),
    dynamic: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamic) {
        val context = LocalContext.current
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (dark) DarkColors else
            if (AppConfig.sSpringFestivalTheme.value && DateUtils.isSpringFestival)
                SpringFestivalColorPalette
            else LightColors
    }
    SystemBarSettings()
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

val ColorScheme.isLight: Boolean
    @Composable
    @ReadOnlyComposable
    get() = !isSystemInDarkTheme()