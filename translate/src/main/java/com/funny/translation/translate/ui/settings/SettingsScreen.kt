package com.funny.translation.translate.ui.settings

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.jetsetting.core.JetSettingCheckbox
import com.funny.jetsetting.core.JetSettingTile
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.WebViewActivity
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.widget.SimpleDialog
import com.funny.translation.translate.utils.DateUtils
import com.funny.translation.translate.utils.EasyFloatUtils
import com.funny.translation.translate.utils.SortResultUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.burnoutcrew.reorderable.*

private const val TAG = "SettingScreen"

@Composable
fun SettingsScreen() {
    val systemUiController = rememberSystemUiController()
    val navController = LocalNavController.current
    val context = LocalContext.current

    val showFloatWindowTipDialog = remember {
        mutableStateOf(false)
    }
    val scrollState = rememberScrollState()
    val floatWindowTip = """
        悬浮窗使用需要权限，请正确授予相应权限
        悬浮球可拖动，移动至指定位置即可删除。翻译悬浮窗右下角可直接打开应用
        如果你是 Android9 及以下系统，可长按 译 按钮翻译剪切板内容
    """.trimIndent()

    SimpleDialog(
        openDialog = showFloatWindowTipDialog,
        title = "悬浮窗说明",
        message = floatWindowTip,
        dismissButtonText = ""
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = stringResource(id = R.string.setting_ui),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        JetSettingCheckbox(
            key = Consts.KEY_HIDE_STATUS_BAR,
            default = true,
            text = stringResource(R.string.setting_hide_status_bar),
            resourceId = R.drawable.ic_status_bar,
            iconTintColor = MaterialColors.BlueGrey700
        ) {
            systemUiController.isStatusBarVisible = !it
        }
        JetSettingCheckbox(
            key = Consts.KEY_HIDE_NAVIGATION_BAR,
            default = false,
            text = stringResource(R.string.setting_hide_nav_bar),
            resourceId = R.drawable.ic_bottom_bar,
            iconTintColor = MaterialColors.Blue700
        ) {
            systemUiController.isNavigationBarVisible = !it
        }
        JetSettingCheckbox(
            key = Consts.KEY_CUSTOM_NAVIGATION,
            default = true,
            text = stringResource(id = R.string.custom_nav),
            resourceId = R.drawable.ic_custom_nav,
            iconTintColor = MaterialColors.DeepOrangeA200
        ) {
            Toast.makeText(
                context, "已${
                    if (it) {
                        "启动"
                    } else {
                        "关闭"
                    }
                }新导航栏，下次启动应用生效", Toast.LENGTH_SHORT
            ).show()
        }
        JetSettingCheckbox(
            key = Consts.KEY_SHOW_FLOAT_WINDOW,
            text = stringResource(R.string.setting_show_float_window),
            resourceId = R.drawable.ic_float_window,
            iconTintColor = MaterialColors.Orange700
        ) {
            try {
                if (it) EasyFloatUtils.showFloatBall(context as Activity)
                else EasyFloatUtils.hideFloatBall()
            } catch (e: Exception) {
                Toast.makeText(context, "显示悬浮窗失败，请检查是否正确授予权限！", Toast.LENGTH_LONG).show()
                DataSaverUtils.saveData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
            }
        }
        Text(text = stringResource(R.string.about_float_window), modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.End)
            .padding(8.dp)
            .clickable {
                showFloatWindowTipDialog.value = true
            }, fontSize = 12.sp, fontWeight = FontWeight.W500, textAlign = TextAlign.End
        )
        if (DateUtils.isSpringFestival) {
            JetSettingCheckbox(
                key = Consts.KEY_SPRING_THEME,
                text = stringResource(R.string.setting_spring_theme),
                resourceId = R.drawable.ic_theme,
                iconTintColor = MaterialColors.Red700,
                default = true
            ) {
                Toast.makeText(
                    context, "已${
                        if (it) {
                            "设置"
                        } else {
                            "取消"
                        }
                    }春节限定主题，下次启动应用生效", Toast.LENGTH_SHORT
                ).show()
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.others),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        JetSettingTile(
            text = stringResource(R.string.sort_result),
            resourceId = R.drawable.ic_sort,
            iconTintColor = MaterialColors.DeepOrangeA700,
        ) {
            navController.navigate(TranslateScreen.SortResultScreen.route)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.about),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )

        JetSettingTile(
            text = stringResource(R.string.source_code),
            resourceId = R.drawable.ic_github,
            iconTintColor = MaterialColors.Purple700
        ) {
            Toast.makeText(
                context,
                FunnyApplication.resources.getText(R.string.welcome_star),
                Toast.LENGTH_SHORT
            ).show()
            WebViewActivity.start(context, "https://github.com/FunnySaltyFish/FunnyTranslation")
        }
        JetSettingTile(
            text = stringResource(id = R.string.open_source_library),
            resourceId = R.drawable.ic_open_source_library,
            iconTintColor = MaterialColors.DeepOrange700
        ) {
            navController.navigate(TranslateScreen.AboutScreen.route)
        }
        JetSettingTile(
            text = stringResource(R.string.privacy),
            resourceId = R.drawable.ic_privacy,
            iconTintColor = MaterialColors.Amber700
        ) {
            WebViewActivity.start(context, "https://api.funnysaltyfish.fun/trans/v1/api/privacy")
        }
    }
}

@Composable
fun SortResult(
    modifier: Modifier = Modifier
) {
    val state = rememberReorderState()
    val vm : SettingsScreenViewModel = viewModel()
    val data = vm.localEngineNamesState.toMutableStateList()
    LazyColumn(
        state = state.listState,
        modifier = modifier
            .then(
                Modifier.reorderable(
                    state,
                    onMove = { from, to -> data.move(from.index, to.index) })
            )
    ) {
        itemsIndexed(data, { i, _ -> i }) { i, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggedItem(state.offsetByIndex(i))
                    .background(MaterialTheme.colors.surface)
                    .detectReorderAfterLongPress(state)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(24.dp))
                    Icon(painterResource(id = R.drawable.ic_drag),"Drag to sort")
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                Divider()
            }
        }
    }

    DisposableEffect(key1 = null){
        onDispose {
            if(!SortResultUtils.checkEquals(data)){
                Log.d(TAG, "SortResult: 不相等")
                SortResultUtils.resetMappingAndSave(data)
            }
        }
    }
}