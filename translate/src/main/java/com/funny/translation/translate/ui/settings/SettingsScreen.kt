package com.funny.translation.translate.ui.settings

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
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
import com.funny.cmaterialcolors.MaterialColors
import com.funny.jetsetting.core.JetSettingCheckbox
import com.funny.jetsetting.core.JetSettingTile
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.WebViewActivity
import com.funny.translation.translate.bean.AppConfig
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.widget.SimpleDialog
import com.funny.translation.translate.utils.DateUtils
import com.funny.translation.translate.utils.EasyFloatUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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
        因悬浮窗焦点问题，如需输入内容请先点击右上角 编辑 图标切换至编辑模式，编辑完后再次点击退出
        如果你是 Android9 及以下系统，可长按 译 按钮翻译剪切板内容
    """.trimIndent()

    SimpleDialog(openDialog = showFloatWindowTipDialog, title = "悬浮窗说明", message = floatWindowTip, dismissButtonText = "")

    Column(modifier = Modifier
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
            key = Consts.KEY_HIDE_NAVIGATION_BAR,
            default = false,
            text = stringResource(R.string.setting_hide_nav_bar),
            resourceId = R.drawable.ic_status_bar,
            iconTintColor = MaterialColors.Blue700
        ){
            systemUiController.isNavigationBarVisible = !it
        }
        JetSettingCheckbox(
            key = Consts.KEY_SHOW_FLOAT_WINDOW,
            text = stringResource(R.string.setting_show_float_window),
            resourceId = R.drawable.ic_float_window,
            iconTintColor = MaterialColors.Orange700
        ){
            try {
                if(it) EasyFloatUtils.showFloatBall(context as Activity)
                else EasyFloatUtils.hideFloatBall()
            }catch (e:Exception){
                Toast.makeText(context,"显示悬浮窗失败，请检查是否正确授予权限！",Toast.LENGTH_LONG).show()
                DataSaverUtils.saveData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
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

        JetSettingTile(
            text = stringResource(R.string.source_code),
            resourceId = R.drawable.ic_github,
            iconTintColor = MaterialColors.Purple700
        ) {
            Toast.makeText(context, FunnyApplication.resources.getText(R.string.welcome_star), Toast.LENGTH_SHORT).show()
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