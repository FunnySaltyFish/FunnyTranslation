package com.funny.translation.translate.ui.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.funny.jetsetting.core.JetSettingSwitch
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.utils.EasyFloatUtils
import com.funny.translation.ui.touchToScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatWindowScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.float_window))
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            },
        )
        JetSettingSwitch(state = AppConfig.sShowFloatWindow, text = stringResource(id = R.string.open_float_window)) {
            try {
                if (it) EasyFloatUtils.showFloatBall(context as Activity)
                else EasyFloatUtils.hideAllFloatWindow()
            } catch (e: Exception) {
                context.toastOnUi("显示悬浮窗失败，请检查是否正确授予权限！")
                DataSaverUtils.saveData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier
                .touchToScale()
                .fillMaxWidth(0.9f)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Text(text = stringResource(id = R.string.float_window_tip), modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp))
        }
    }


}