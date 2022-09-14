package com.funny.translation.translate.ui.thanks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.funny.translation.translate.LocalActivityVM

@Composable
fun UserProfileScreen(navHostController: NavHostController) {
    val activityVM = LocalActivityVM.current
    var uid = activityVM.uid
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 24.dp)) {
        OutlinedButton(modifier = Modifier.align(CenterHorizontally), onClick = {
            uid = -1
            navHostController.popBackStack()
        }) {
            Text(text = "退出登录")
        }
        Text(text = "其他功能开发中，可以加入内测群857362450抢先体验开发中功能")
    }
}