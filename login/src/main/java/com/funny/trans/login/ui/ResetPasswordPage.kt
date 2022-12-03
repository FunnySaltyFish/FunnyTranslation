@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.trans.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.toastOnUi

@Composable
fun ResetPasswordPage(
    navController: NavController
) {
    Column(
        Modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val vm = viewModel<LoginViewModel>()
        val context = LocalContext.current

        Spacer(modifier = Modifier.height(60.dp))
        Column(Modifier.fillMaxWidth(WIDTH_FRACTION)) {
            InputUserName(vm = vm)
            Spacer(modifier = Modifier.height(8.dp))
            InputEmail(
                modifier = Modifier.fillMaxWidth(),
                value = vm.email,
                onValueChange = { vm.email = it },
                isError = vm.email != "" && !vm.isValidEmail,
                verifyCode = vm.verifyCode,
                onVerifyCodeChange = { vm.verifyCode = it },
                initialSent = false,
                onClick = { vm.sendResetPasswordEmail(context) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            InputPassword(vm = vm, readonly = false)
            // 重复密码
            var repeatPassword by remember { mutableStateOf("") }

            val isRepeatPwdError by remember {
                derivedStateOf { vm.password != repeatPassword }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                modifier = Modifier.fillMaxWidth(),
                isError = isRepeatPwdError,
                label = { Text("重复密码") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val enable by remember {
                derivedStateOf {
                    vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6 && UserUtils.isValidPassword(vm.password) && vm.password == repeatPassword
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                vm.resetPassword(context, onSuccess = {
                    context.toastOnUi("密码重置成功！")
                    navController.popBackStack()
                })
            }, enabled = enable) {
                Text(text = "重置密码")
            }
        }
    }
}