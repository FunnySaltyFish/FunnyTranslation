package com.funny.translation.translate.ui.thanks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.utils.QQUtils

@OptIn(ExperimentalTextApi::class)
@Composable
fun UserProfileScreen(navHostController: NavHostController) {
    val activityVM = LocalActivityVM.current
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 12.dp, end = 12.dp)) {
        OutlinedButton(modifier = Modifier.align(CenterHorizontally), onClick = {
            activityVM.uid = -1
            activityVM.token = ""
            navHostController.popBackStack()
        }) {
            Text(text = "退出登录")
        }

        val text = remember {
            buildAnnotatedString {
                append("其他功能开发中，可以加入内测群")
                pushStringAnnotation(tag = "url", annotation = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D")
                withStyle(style = SpanStyle(color = MaterialColors.BlueA700)) {
                    append(" 857362450 ")
                }
                pop()
                append("抢先体验开发中功能")
            }
        }
        ClickableText(text = text, modifier = Modifier.fillMaxWidth(), style = TextStyle(textAlign = TextAlign.Center, fontSize = 14.sp)){ index ->
            // 根据tag取出annotation并打印
            text.getStringAnnotations(tag = "url", start = index, end = index).firstOrNull()?.let { annotation ->
                QQUtils.joinQQGroup(context, "mlEwPbkeUQMuwoyp44lROPeD938exo56")
            }

        }
    }
}