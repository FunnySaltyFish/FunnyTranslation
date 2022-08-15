package com.funny.trans.login.ui

import android.os.Build
import android.system.Os
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.trans.login.utils.SoterUtils
import com.funny.translation.helper.BiometricUtils
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.toastOnUi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlin.random.Random

private const val WIDTH_FRACTION = 0.8f


@Composable
fun LoginScreen() {
    val vm : LoginViewModel = viewModel()
    val infiniteTransition = rememberInfiniteTransition()

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = isSystemInDarkTheme()
    LaunchedEffect(key1 = systemUiController) {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }

    val offset by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = InfiniteRepeatableSpec(
            animation = TweenSpec(
                durationMillis = 10000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse,
        )
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    0f to MaterialTheme.colorScheme.surface,
                    offset to MaterialTheme.colorScheme.tertiaryContainer,
                    1f to MaterialTheme.colorScheme.surface,
                )
            )
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var selectedTab by remember { mutableStateOf(0) }
        Spacer(modifier = Modifier.height(24.dp))
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth(WIDTH_FRACTION)
                .clip(shape = RoundedCornerShape(12.dp))
                .background(Color.Transparent),
            containerColor = Color.Transparent
            //backgroundColor = Color.Unspecified
        ) {
            Tab(selectedTab == 0, onClick = { selectedTab = 0 }){
                Text("登录", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelLarge)
            }
            Tab(selectedTab == 1, onClick = { selectedTab = 1 }){
                Text("注册", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Crossfade(targetState = selectedTab, animationSpec = TweenSpec(800)) { i ->
            when(i) {
                0 -> LoginForm(vm)
                1 -> RegisterForm(vm)
            }
        }
    }
}

@Composable
fun LoginForm(vm: LoginViewModel) {
    Column(Modifier.fillMaxWidth(WIDTH_FRACTION), horizontalAlignment = Alignment.CenterHorizontally) {
        InputUserName(vm)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("登录")
        }
    }
}

@Composable
fun RegisterForm(vm: LoginViewModel) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth(WIDTH_FRACTION), horizontalAlignment = Alignment.CenterHorizontally) {
        InputUserName(vm)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = {
                if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                    BiometricUtils.setFingerPrint(
                        context as AppCompatActivity,
                        onNotSupport = { msg: String -> context.toastOnUi(msg) },
                        onFail = { context.toastOnUi("认证失败！") },
                        onSuccess = { context.toastOnUi("认证成功！") },
                        onError = { errorCode, errorMsg -> context.toastOnUi("认证失败！（$errorCode: $errorMsg）") }
                    )
                }else {
                    context.toastOnUi("您的安卓版本过低，不支持指纹认证！将使用密码认证~", Toast.LENGTH_LONG)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("添加指纹")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("注册")
        }
    }
}

@Composable
fun InputUserName(vm: LoginViewModel) {
    var username by remember { vm.usernameState }
    val isUsernameValid by remember { vm.isValidUsername }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = username,
        onValueChange = { username = it },
        isError = username != "" && !isUsernameValid,
        label = { Text(text = "用户名") },
        placeholder = { Text("3-16位，无特殊符号") },
        singleLine = true
    )
}