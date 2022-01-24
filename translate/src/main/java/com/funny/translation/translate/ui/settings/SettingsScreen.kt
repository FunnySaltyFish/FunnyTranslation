package com.funny.translation.translate.ui.settings

import android.util.Log
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
import com.funny.translation.translate.bean.Consts
import com.google.accompanist.systemuicontroller.rememberSystemUiController
private const val TAG = "SettingScreen"
@Composable
fun SettingsScreen() {
    val systemUiController = rememberSystemUiController()
    val darkIcon = !MaterialTheme.colors.isLight
    val statusBarColor = MaterialTheme.colors.background
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
    }
}