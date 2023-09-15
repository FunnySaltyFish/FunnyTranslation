@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.trans.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.funny.trans.login.R
import com.funny.translation.AppConfig
import com.funny.translation.ui.MarkdownText

@Composable
fun CancelAccountPage(
    navController: NavHostController
) {
    Column(
        Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val vm = viewModel<LoginViewModel>()
        val context = LocalContext.current

        SideEffect {
            AppConfig.userInfo.value.takeIf { it.isValid() }?.let {
                vm.email = it.email
                vm.username = it.username
            }
        }

        TipDialog(navController)

        Spacer(modifier = Modifier.height(60.dp))
        Column(Modifier.fillMaxWidth(WIDTH_FRACTION)) {
            InputUsername(
                usernameProvider = vm::username,
                updateUsername = vm::updateUsername,
                isValidUsernameProvider = vm::isValidUsername
            )
            Spacer(modifier = Modifier.height(8.dp))
            InputEmail(
                modifier = Modifier.fillMaxWidth(),
                value = vm.email,
                onValueChange = { vm.email = it },
                isError = vm.email != "" && !vm.isValidEmail,
                verifyCode = vm.verifyCode,
                onVerifyCodeChange = { vm.verifyCode = it },
                initialSent = false,
                onClick = { vm.sendCancelAccountEmail(context) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            val enable by remember {
                derivedStateOf {
                    vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                vm.cancelAccount {
                    AppConfig.logout()
                    navController.popBackStack() // 账号详情
                    navController.popBackStack() // 主页
                }
            }, enabled = enable) {
                Text(text = stringResource(R.string.confirm_cancel))
            }
        }
    }
}

@Composable
fun TipDialog(navController: NavController) {
    var showTipDialog by remember { mutableStateOf(true) }
    if (showTipDialog) {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text(stringResource(R.string.warning)) },
            text = { MarkdownText(stringResource(R.string.cancel_account_tip)) },
            confirmButton = {
                CountDownTimeButton(
                    modifier = Modifier,
                    onClick = { showTipDialog = false },
                    text = stringResource(id = R.string.confirm),
                    initialSent = true,
                    countDownTime = 10
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showTipDialog = false
                    navController.popBackStack()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}