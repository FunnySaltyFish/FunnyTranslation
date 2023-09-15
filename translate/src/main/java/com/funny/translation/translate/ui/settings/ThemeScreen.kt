package com.funny.translation.translate.ui.settings

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.qhplus.emo.photo.activity.PhotoPickerActivity
import cn.qhplus.emo.photo.activity.getPhotoPickResult
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import coil.compose.AsyncImage
import com.funny.cmaterialcolors.MaterialColors
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.getKeyColors
import com.funny.translation.helper.toastOnUi
import com.funny.translation.theme.ThemeConfig
import com.funny.translation.theme.ThemeStaticColors
import com.funny.translation.theme.ThemeType
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import com.funny.translation.translate.ui.widget.ArrowTile
import com.funny.translation.translate.ui.widget.HeadingText
import com.funny.translation.translate.ui.widget.RadioTile
import com.funny.translation.ui.touchToScale
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThemeScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val themeType by ThemeConfig.sThemeType
        var selectedColorIndex by rememberDataSaverState(
            key = "key_color_theme_selected_index",
            default = 0
        )
        Spacer(modifier = Modifier.height(40.dp))
        HeadingText(text = stringResource(id = R.string.theme))
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .touchToScale()
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ){
            LabelText(text = stringResource(R.string.preview_theme_here), color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(12.dp))
        RadioTile(text = stringResource(R.string.default_str), selected = themeType == ThemeType.StaticDefault) {
            ThemeConfig.updateThemeType(ThemeType.StaticDefault)
        }
        RadioTile(text = stringResource(R.string.dynamic_color), selected = themeType.isDynamic) {
            // Android 12以上才选
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                ThemeConfig.updateThemeType(ThemeType.DynamicNative)
            else
                ThemeConfig.updateThemeType(ThemeType.DynamicFromImage(MaterialColors.Blue700))
        }
        RadioTile(text = stringResource(R.string.custom), selected = themeType is ThemeType.StaticFromColor) {
            ThemeConfig.updateThemeType(ThemeType.StaticFromColor(ThemeStaticColors.get(selectedColorIndex)))
        }
        Divider()
        Spacer(modifier = Modifier.height(12.dp))
        AnimatedContent(targetState = themeType.id) { id ->
            when(id) {
                // 使用动态
                0, 1 -> SelectDynamicTheme(modifier = Modifier.fillMaxWidth())
                2 -> {
                    SelectColorTheme(
                        modifier = Modifier.fillMaxWidth(),
                        ThemeStaticColors,
                        { selectedColorIndex }
                    ) { index ->
                        selectedColorIndex = index
                        ThemeConfig.updateThemeType(ThemeType.StaticFromColor(ThemeStaticColors[index]))
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun LabelText(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}


@Composable
private fun SelectDynamicTheme(modifier: Modifier) {
    Column(modifier = modifier) {
        val context = LocalContext.current
        var themeType by ThemeConfig.sThemeType
        var selectImageUri: Uri? by rememberDataSaverState(
            key = "key_dynamic_theme_selected_img_uri",
            default = null
        )
        val pickLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    it.data?.getPhotoPickResult()?.let { result ->
                        val img = result.list[0]
                        selectImageUri = img.uri
                        changeThemeFromImageUri(context, img.uri)
                    }
                }
            }

        RadioTile(text = stringResource(R.string.wallpaper_color_extraction), selected = themeType == ThemeType.DynamicNative) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                themeType = ThemeType.DynamicNative
            else context.toastOnUi(R.string.android_12_required)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ArrowTile(text = stringResource(R.string.select_from_image)) {
            if (DefaultVipInterceptor()) {
                pickLauncher.launch(
                    PhotoPickerActivity.intentOf(
                        context,
                        CoilMediaPhotoProviderFactory::class.java,
                        CustomPhotoPickerActivity::class.java,
                        pickedItems = arrayListOf(),
                        pickLimitCount = 1,
                    )
                )
            }
        }

        if (selectImageUri != null) {
            Spacer(modifier = Modifier.height(12.dp))
            AsyncImage(model = selectImageUri, contentDescription = "Selected Image", modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    if (themeType !is ThemeType.DynamicFromImage)
                        changeThemeFromImageUri(context, selectImageUri!!)
                }
            )
        }
    }
}




@Composable
private fun SelectColorTheme(modifier: Modifier, colors: ImmutableList<Color>, selectedColorIndexProvider: () -> Int, updateSelectColorIndex: (Int) -> Unit) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(8),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp)
    ) {
        val selectedId = selectedColorIndexProvider()
        // 以圆形展示颜色们，可选择
        itemsIndexed(colors) { i, color ->
            Box(modifier = Modifier
                .background(color, CircleShape)
                .clip(CircleShape)
                .clickable {
                    if (!DefaultVipInterceptor()) return@clickable
                    updateSelectColorIndex(i)
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val w = constraints.maxWidth
                    layout(w, w) {
                        placeable.placeRelative(0, 0)
                    }
                }, contentAlignment = Alignment.Center
            ) {
                if (i == selectedId) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .offset(0.dp, 6.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun changeThemeFromImageUri(context: Context, uri: Uri) {
    val inputStream = context.contentResolver.openInputStream(uri)
    inputStream?.use {
        val bytes = BitmapUtil.getBitmapFormUri(context, 400, 600, 1024*1024, uri)
        bytes ?: return
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val color = bitmap.getKeyColors(1)[0]
        ThemeConfig.updateThemeType(ThemeType.DynamicFromImage(color))
//        bitmap.getKeyColors(1, MaterialColors.Blue700) { color ->
//            ThemeConfig.updateThemeType(ThemeType.DynamicFromImage(color))
//        }
    }
}