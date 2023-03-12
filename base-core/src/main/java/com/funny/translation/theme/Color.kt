package com.funny.translation.theme
import androidx.compose.ui.graphics.Color
import com.funny.cmaterialcolors.MaterialColors
import kotlinx.collections.immutable.toImmutableList

val ThemeStaticColors by lazy {
    val colorClass = MaterialColors.Companion::class
    val colorList = colorClass.members
    colorList.sortedBy { it.name }.filter {
        // 提取数字，找到大于500的
        ("\\d+".toRegex().find(it.name)?.value?.toIntOrNull() ?: 0) > 500
    }.map { it.call(MaterialColors) as Color }.toImmutableList()
}

val md_theme_light_primary = Color(0xFF0061A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFD1E4FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001D36)
val md_theme_light_secondary = Color(0xFF006493)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFCAE6FF)
val md_theme_light_onSecondaryContainer = Color(0xFF001E30)
val md_theme_light_tertiary = Color(0xFF006970)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFF7DF4FF)
val md_theme_light_onTertiaryContainer = Color(0xFF002022)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFDFCFF)
val md_theme_light_onBackground = Color(0xFF1A1C1E)
val md_theme_light_surface = Color(0xFFFDFCFF)
val md_theme_light_onSurface = Color(0xFF1A1C1E)
val md_theme_light_surfaceVariant = Color(0xFFDFE2EB)
val md_theme_light_onSurfaceVariant = Color(0xFF43474E)
val md_theme_light_outline = Color(0xFF73777F)
val md_theme_light_inverseOnSurface = Color(0xFFF1F0F4)
val md_theme_light_inverseSurface = Color(0xFF2F3033)
val md_theme_light_inversePrimary = Color(0xFF9ECAFF)
val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF0061A4)

val md_theme_dark_primary = Color(0xFF9ECAFF)
val md_theme_dark_onPrimary = Color(0xFF003258)
val md_theme_dark_primaryContainer = Color(0xFF00497D)
val md_theme_dark_onPrimaryContainer = Color(0xFFD1E4FF)
val md_theme_dark_secondary = Color(0xFF8DCDFF)
val md_theme_dark_onSecondary = Color(0xFF00344F)
val md_theme_dark_secondaryContainer = Color(0xFF004B70)
val md_theme_dark_onSecondaryContainer = Color(0xFFCAE6FF)
val md_theme_dark_tertiary = Color(0xFF4DD9E4)
val md_theme_dark_onTertiary = Color(0xFF00363A)
val md_theme_dark_tertiaryContainer = Color(0xFF004F54)
val md_theme_dark_onTertiaryContainer = Color(0xFF7DF4FF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF1A1C1E)
val md_theme_dark_onBackground = Color(0xFFE2E2E6)
val md_theme_dark_surface = Color(0xFF1A1C1E)
val md_theme_dark_onSurface = Color(0xFFE2E2E6)
val md_theme_dark_surfaceVariant = Color(0xFF43474E)
val md_theme_dark_onSurfaceVariant = Color(0xFFC3C7CF)
val md_theme_dark_outline = Color(0xFF8D9199)
val md_theme_dark_inverseOnSurface = Color(0xFF1A1C1E)
val md_theme_dark_inverseSurface = Color(0xFFE2E2E6)
val md_theme_dark_inversePrimary = Color(0xFF0061A4)
val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFF9ECAFF)


val seed = Color(0xFF2196F3)
