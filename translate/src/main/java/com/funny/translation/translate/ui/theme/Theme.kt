package com.funny.translation.translate.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.funny.cmaterialcolors.MaterialColors
import com.funny.cmaterialcolors.MaterialColors.Companion.Blue500
import com.funny.cmaterialcolors.MaterialColors.Companion.Blue700
import com.funny.cmaterialcolors.MaterialColors.Companion.BlueA200
import com.funny.cmaterialcolors.MaterialColors.Companion.BlueA700
import com.funny.cmaterialcolors.MaterialColors.Companion.LightBlue200
import com.funny.cmaterialcolors.MaterialColors.Companion.Red200
import com.funny.cmaterialcolors.MaterialColors.Companion.Red500
import com.funny.cmaterialcolors.MaterialColors.Companion.Red700
import com.funny.cmaterialcolors.MaterialColors.Companion.RedA200
import com.funny.cmaterialcolors.MaterialColors.Companion.RedA400
import com.funny.cmaterialcolors.MaterialColors.Companion.RedA700
import com.funny.data_saver.core.LocalDataSaver
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.utils.DateUtils

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    primary = Color(0xff3d557b),
    primaryVariant = BlueA700,
    secondary = Color(0xff2b3c56),
    onSecondary = Color.White,

    background = Color(0xff303135),
    onBackground = Color.White,
    surface = Color(0xff2b3c56).copy(0.7f),
    onSurface = Color.White
)

private val LightColorPalette = lightColors(
    primary = Blue500,
    primaryVariant = BlueA700,
    secondary = Color(0xff4785da),
    onSecondary = Color.White,

    background = Color.White,
    surface = Color(0xffdde8f9),
    onSurface = Color(0xff4785da)

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

private val SpringFestivalColorPalette = lightColors(
    primary = Red500,
    primaryVariant = Red700,
    secondary = RedA400,
    onSecondary = Color.White,
    surface = Red200.copy(alpha = 0.7f),
    onSurface = RedA700
)



@Composable
fun TransTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val dataSaver = LocalDataSaver.current
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        if (dataSaver.readData(Consts.KEY_SPRING_THEME,true) && DateUtils.isSpringFestival) SpringFestivalColorPalette
        else LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}