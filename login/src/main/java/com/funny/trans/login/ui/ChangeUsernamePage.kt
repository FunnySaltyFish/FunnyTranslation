package com.funny.trans.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.funny.translation.AppConfig
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.toastOnUi
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun ChangeUsernamePage(navController: NavController) {
    Column(Modifier.fillMaxSize().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val user by AppConfig.userInfo
        var username by remember { mutableStateOf(user.username) }
        val canChangeUsername by remember { derivedStateOf { user.canChangeUsername() } }
        val nextChangeUsernameString = remember(user) { user.nextChangeUsernameTimeStr() }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        Column(Modifier.fillMaxWidth(WIDTH_FRACTION), horizontalAlignment = Alignment.CenterHorizontally) {
            InputUsername(
                usernameProvider = { username },
                updateUsername = { username = it },
                isValidUsernameProvider = { UserUtils.isValidUsername(username) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (canChangeUsername) {
                Button(onClick = {
                    scope.launch {
                        try {
                            UserUtils.changeUsername(user.uid, username)
                            context.toastOnUi("修改用户名成功")
                            AppConfig.userInfo.value = user.copy(username = username, lastChangeUsernameTime = Date())
                            navController.popBackStack()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            context.toastOnUi(e.message)
                        }
                    }
                }) {
                    Text(text = "确认修改")
                }
            } else {
                // 您每 30 天可以修改一次用户名
                Text(text = buildAnnotatedString {
                    append("您每 30 天可以修改一次用户名，下次修改的时间为")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(nextChangeUsernameString)
                    }
                })
            }
        }
    }

}