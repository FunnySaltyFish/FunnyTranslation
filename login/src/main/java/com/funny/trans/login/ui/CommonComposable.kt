package com.funny.trans.login.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.funny.trans.login.R
import com.funny.translation.helper.UserUtils

@Composable
fun InputUsername(
    usernameProvider: () -> String,
    updateUsername: (String) -> Unit,
    isValidUsernameProvider: () -> Boolean,
    imeAction: ImeAction = ImeAction.Next
) {
    val username = usernameProvider()
    val isValidUsername = isValidUsernameProvider()
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = username,
        onValueChange = updateUsername,
        isError = username != "" && !isValidUsername,
        label = { Text(text = stringResource(R.string.username)) },
        placeholder = { Text(stringResource(R.string.username_rule)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = imeAction
        ),
    )
}

@Composable
fun InputPassword(
    passwordProvider: () -> String,
    updatePassword: (String) -> Unit
) {
    val password = passwordProvider()
    val isPwdError by remember {
        derivedStateOf { password != "" && !UserUtils.isValidPassword(password) }
    }
    OutlinedTextField(modifier = Modifier.fillMaxWidth(),
        value = password,
        onValueChange = updatePassword,
        enabled = true,
        placeholder = {
            Text(text = stringResource(R.string.password_rule))
        },
        label = { Text(text = stringResource(R.string.password)) },
        isError = isPwdError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun CompletableButton(
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    completed: Boolean = false,
    text: @Composable () -> Unit
) {
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        text()
        if (completed) Icon(
            painterResource(id = R.drawable.ic_finish),
            contentDescription = stringResource(R.string.finished),
            modifier = Modifier.padding(start = 4.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}