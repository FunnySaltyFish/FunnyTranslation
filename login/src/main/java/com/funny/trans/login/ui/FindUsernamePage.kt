@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.trans.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.trans.login.R
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi

@Composable
fun FindUsernamePage() {
    Column(
        Modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val vm = viewModel<LoginViewModel>()
        val context = LocalContext.current

        Spacer(modifier = Modifier.height(60.dp))
        Column(Modifier.fillMaxWidth(WIDTH_FRACTION)) {
            InputEmail(
                modifier = Modifier.fillMaxWidth(),
                value = vm.email,
                onValueChange = { vm.email = it },
                isError = vm.email != "" && !vm.isValidEmail,
                verifyCode = vm.verifyCode,
                onVerifyCodeChange = { vm.verifyCode = it },
                initialSent = false,
                onClick = { vm.sendFindUsernameEmail(context) }
            )


            Spacer(modifier = Modifier.height(8.dp))
            val enable by remember {
                derivedStateOf {
                    vm.isValidEmail && vm.verifyCode.length == 6
                }
            }

            val usernameList = remember {
                mutableStateListOf<String>()
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                vm.findUsername(context, onSuccess = {
                    usernameList.clear()
                    usernameList.addAll(it)
                    context.toastOnUi(string(R.string.find_account_amount, it.size))
                })
            }, enabled = enable) {
                Text(text = stringResource(R.string.query_related_account))
            }

            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(Modifier.fillMaxWidth()) {
                items(usernameList.size) {
                    Text(text = usernameList[it], modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}