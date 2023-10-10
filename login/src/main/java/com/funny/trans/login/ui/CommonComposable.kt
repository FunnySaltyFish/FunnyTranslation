package com.funny.trans.login.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.funny.trans.login.R
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.helper.UserUtils
import com.funny.translation.ui.FixedSizeIcon

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
    updatePassword: (String) -> Unit,
    labelText: String = stringResource(R.string.password),
) {
    val password = passwordProvider()
    val isPwdError by remember(password) {
        derivedStateOf { password != "" && !UserUtils.isValidPassword(password) }
    }
    ConcealableTextField(modifier = Modifier.fillMaxWidth(),
        value = password,
        onValueChange = updatePassword,
        placeholder = {
            Text(text = stringResource(R.string.password_rule))
        },
        label = { Text(text = labelText) },
        isError = isPwdError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
    )
}

@Composable
fun ConcealableTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var hiddenInput by rememberStateOf(value = true)
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = true,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        prefix = prefix,
        label = label,
        trailingIcon = {
            AnimatedContent(hiddenInput, label = "hidden_input_icon") {
                if (it) {
                    FixedSizeIcon(
                        painterResource(id = R.drawable.ic_hidden_password),
                        contentDescription = stringResource(R.string.click_to_show_password),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { hiddenInput = false },
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    FixedSizeIcon(
                        painterResource(id = R.drawable.ic_show_password),
                        contentDescription = stringResource(R.string.click_to_hide_password),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { hiddenInput = true },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (hiddenInput) PasswordVisualTransformation('*') else VisualTransformation.None
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
        if (completed) FixedSizeIcon(
            painterResource(id = R.drawable.ic_finish),
            contentDescription = stringResource(R.string.finished),
            modifier = Modifier.padding(start = 4.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}