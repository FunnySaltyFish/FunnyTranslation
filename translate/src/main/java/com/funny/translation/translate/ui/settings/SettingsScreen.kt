package com.funny.translation.translate.ui.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.funny.cmaterialcolors.MaterialColors
import com.funny.jetsetting.core.JetSettingCheckbox
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.AppConfig
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.ui.widget.RoundCornerButton
import com.funny.translation.translate.ui.widget.SimpleDialog
import com.funny.translation.translate.utils.DateUtils
import com.funny.translation.translate.utils.FloatWindowUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private const val TAG = "SettingScreen"
@Composable
fun SettingsScreen() {
    val systemUiController = rememberSystemUiController()
    val navController = rememberNavController()
    val context = LocalContext.current

    val showFloatWindowTipDialog = remember {
        mutableStateOf(false)
    }
    val floatWindowTip = """
        悬浮窗使用需要权限，请正确授予相应权限
        因悬浮窗焦点问题，如需输入内容请先点击右上角 编辑 图标切换至编辑模式，编辑完后再次点击退出
        如果你是 Android9 及以下系统，可长按 译 按钮翻译剪切板内容
    """.trimIndent()

    SimpleDialog(openDialog = showFloatWindowTipDialog, title = "悬浮窗说明", message = floatWindowTip, dismissButtonText = "")

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {
        Text(
            text = stringResource(id = R.string.setting_ui),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        JetSettingCheckbox(
            key = Consts.KEY_SHOW_STATUS_BAR,
            text = stringResource(R.string.setting_show_status_bar),
            resourceId = R.drawable.ic_status_bar,
            iconTintColor = MaterialColors.Blue700
        ){
            systemUiController.isStatusBarVisible = it
//            systemUiController.setStatusBarColor(Color.Transparent, darkIcons = darkIcon)
//            Log.d(TAG, "SettingsScreen: statusBar ${systemUiController.}")
        }
        JetSettingCheckbox(
            key = Consts.KEY_SHOW_FLOAT_WINDOW,
            text = stringResource(R.string.setting_show_float_window),
            resourceId = R.drawable.ic_float_window,
            iconTintColor = MaterialColors.Orange700
        ){
            if(!AppConfig.INIT_FLOATING_WINDOW)FloatWindowUtils.initFloatingWindow(context)
            if (AppConfig.INIT_FLOATING_WINDOW){
                if(it)FloatWindowUtils.showFloatWindow()
                else FloatWindowUtils.hideFloatWindow()
            }
        }
        Text(text = stringResource(R.string.about_float_window), modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.End)
            .padding(8.dp)
            .clickable {
                showFloatWindowTipDialog.value = true
            }, fontSize = 12.sp, fontWeight = FontWeight.W500, textAlign = TextAlign.End)
        if(DateUtils.isSpringFestival){
            JetSettingCheckbox(
                key = Consts.KEY_SPRING_THEME,
                text = stringResource(R.string.setting_spring_theme),
                resourceId = R.drawable.ic_theme,
                iconTintColor = MaterialColors.Red700,
                default = true
            ){
                Toast.makeText(context, "已${if(it){"设置"}else{"取消"}}春节限定主题，下次启动应用生效",Toast.LENGTH_SHORT).show()
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.about),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.open_source_library))
            IconButton(onClick = {
                navController.navigate()
            }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Jump")
            }
        }
    }
}