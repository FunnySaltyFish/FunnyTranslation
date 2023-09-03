package com.funny.translation.theme

import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.funny.cmaterialcolors.MaterialColors
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.DateUtils
import com.funny.translation.helper.DeviceUtils
import com.funny.translation.helper.toastOnUi
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.PaletteStyle
import com.kyant.monet.TonalPalettes
import com.kyant.monet.dynamicColorScheme


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

sealed class ThemeType(val id: Int) {
    object StaticDefault: ThemeType(-1)
    object DynamicNative : ThemeType(0)
    class DynamicFromImage(val color: Color) : ThemeType(1)
    class StaticFromColor(val color: Color): ThemeType(2)

    val isDynamic get() = this is DynamicNative || this is DynamicFromImage

    override fun toString(): String {
        return when(this){
            StaticDefault -> "StaticDefault"
            DynamicNative -> "DynamicNative"
            is DynamicFromImage -> "DynamicFromImage#${this.color}"
            is StaticFromColor -> "StaticFromColor${this.color}"
        }
    }

    companion object {
        val Default = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicNative
        } else {
            StaticDefault
        }

        val Saver = { themeType: ThemeType ->
            when(themeType){
                StaticDefault -> "-1#0"
                DynamicNative -> "0#0"
                is DynamicFromImage -> "1#${themeType.color.toArgb()}"
                is StaticFromColor -> "2#${themeType.color.toArgb()}"
            }
        }

        val Restorer = { str: String ->
            val (id, color) = str.split("#")
            when(id){
                "-1" -> StaticDefault
                "0" -> DynamicNative
                "1" -> DynamicFromImage(Color(color.toInt()))
                "2" -> StaticFromColor(Color(color.toInt()))
                else -> throw IllegalArgumentException("Unknown ThemeType: $str")
            }
        }
    }
}

object ThemeConfig {
    private const val TAG = "ThemeConfig"
    var sThemeType: MutableState<ThemeType> = mutableDataSaverStateOf(DataSaverUtils, "theme_type", ThemeType.Default)

    fun updateThemeType(new: ThemeType){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && new == ThemeType.DynamicNative) {
            BaseApplication.ctx.toastOnUi("Android 12 以上才支持动态主题哦")
            return
        }

        // 如果是 FromXXX，必须 64 位才行
        if (
            (new is ThemeType.DynamicFromImage || new is ThemeType.StaticFromColor)
            && !DeviceUtils.is64Bit()
        ) {
            BaseApplication.ctx.toastOnUi("抱歉，由于库底层限制，仅 64 位机型才支持自定义取色")
            return
        }

        sThemeType.value = new
        Log.d(TAG, "updateThemeType: $new")
    }
}

@Composable
fun TransTheme(
    dark: Boolean = isSystemInDarkTheme(),
    hideStatusBar: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (AppConfig.sSpringFestivalTheme.value && DateUtils.isSpringFestival)
            SpringFestivalColorPalette
        else when (ThemeConfig.sThemeType.value) {
            ThemeType.StaticDefault -> if (dark) DarkColors else LightColors
            ThemeType.DynamicNative -> run {
                // 小于 Android 12 直接跳过
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@run null
                val context = LocalContext.current
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            else -> null
        }

    val mContent = @Composable {
        // SystemBarSettings(hideStatusBar)
        val darkTheme = isSystemInDarkTheme()
        val context = LocalContext.current as ComponentActivity
        val c = MaterialTheme.colorScheme.primaryContainer.toArgb()
        DisposableEffect(darkTheme) {
            context.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ) { darkTheme },
                navigationBarStyle =
                    if (darkTheme) SystemBarStyle.dark(transparent)
                    else SystemBarStyle.light(transparent, c),
            )
            context.window.navigationBarDividerColor = transparent

            onDispose {}
        }
        content()
    }

    when (ThemeConfig.sThemeType.value) {
        ThemeType.StaticDefault, ThemeType.DynamicNative -> {
            MaterialTheme(
                colorScheme = colorScheme ?: if (dark) DarkColors else LightColors,
                content = mContent
            )
        }
        is ThemeType.DynamicFromImage -> {
            MonetTheme(
                color = (ThemeConfig.sThemeType.value as ThemeType.DynamicFromImage).color,
                content = mContent
            )
        }
        is ThemeType.StaticFromColor -> {
            MonetTheme(
                color = (ThemeConfig.sThemeType.value as ThemeType.StaticFromColor).color,
                content = mContent
            )
        }
    }
}

val ColorScheme.isLight: Boolean
    @Composable
    @ReadOnlyComposable
    get() = !isSystemInDarkTheme()

@Composable
fun MonetTheme(color: Color, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTonalPalettes provides TonalPalettes(
            keyColor = color,
            // There are several styles for TonalPalettes
            // PaletteStyle.TonalSpot for default, .Spritz for muted style, .Vibrant for vibrant style,...
            style = PaletteStyle.TonalSpot
        )
    ) {
        MaterialTheme(
            colorScheme = dynamicColorScheme(isDark = isSystemInDarkTheme()),
            content = content
        )
    }
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0x00, 0x00, 0x00, 0x00)

private val transparent = android.graphics.Color.argb(0x00, 0x00, 0x00, 0x00)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)