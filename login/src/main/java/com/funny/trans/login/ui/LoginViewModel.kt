package com.funny.trans.login.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.funny.trans.login.utils.UserUtils

class LoginViewModel : ViewModel() {
    val usernameState = mutableStateOf("")
    val passwordState = mutableStateOf("")

    val isValidUsername = derivedStateOf { UserUtils.isValidUsername(usernameState.value) }
}