package com.funny.trans.login.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

/**
 * @author  FunnySaltyFish
 * @date    2022/8/2 16:07
 */
class GameViewModel : ViewModel(){
    companion object {
        const val WHAT_PASSWORD = 0
        const val WHAT_REPEAT_PASSWORD = 1
        private const val TAG = "GameViewModel"
    }
    var password by mutableStateOf("")
    var repeatPassword by mutableStateOf("")
    var what = WHAT_PASSWORD
    private val patternPwd = Pattern.compile("(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])[a-zA-Z0-9]{8,16}")

    val isPwdError by derivedStateOf {
        !(password.length in 8..16 && patternPwd.matcher(password).matches())
    }

    val isRepeatPwdError by derivedStateOf {
        !(password == repeatPassword)
    }

    fun dispatchInput(char: Char){
        when(what){
            WHAT_PASSWORD -> password += char
            WHAT_REPEAT_PASSWORD -> repeatPassword += char
        }
    }

    fun dispatchDelete() {
        when(what){
            WHAT_PASSWORD -> password = password.dropLast(1)
            WHAT_REPEAT_PASSWORD -> repeatPassword = repeatPassword.dropLast(1)
        }
    }
}