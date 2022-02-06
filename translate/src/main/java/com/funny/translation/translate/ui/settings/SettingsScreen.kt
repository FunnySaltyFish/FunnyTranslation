package com.funny.translation.translate.ui.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.cmaterialcolors.MaterialColors
import com.funny.jetsetting.core.JetSettingCheckbox
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.AppConfig
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.utils.DateUtils
import com.funny.translation.translate.utils.FloatWindowUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController
private const val TAG = "SettingScreen"
@Composable
fun SettingsScreen() {
    val systemUiController = rememberSystemUiController()
    val darkIcon = !MaterialTheme.colors.isLight
    val statusBarColor = MaterialTheme.colors.background
    val context = LocalContext.current
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
//            systemUiController.setStatusBarColor(Color.Transparent, darkIcons = darkIcon)
//            Log.d(TAG, "SettingsScreen: statusBar ${systemUiController.}")
        }
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
    }
}