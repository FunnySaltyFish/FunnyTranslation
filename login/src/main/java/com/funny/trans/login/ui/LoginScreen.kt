package com.funny.trans.login.ui

import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.trans.login.R
import com.funny.trans.login.bean.UserBean
import com.funny.trans.login.utils.UserUtils
import com.funny.translation.helper.BiometricUtils
import com.funny.translation.helper.toastOnUi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val WIDTH_FRACTION = 0.8f

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (UserBean) -> Unit,
) {
    val vm: LoginViewModel = viewModel()
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
        Spacer(modifier = Modifier.height(24.dp))

        val pagerState = rememberPagerState()
        val scope = rememberCoroutineScope()
        fun changePage(index: Int) = scope.launch {
            pagerState.animateScrollToPage(index)
        }
        TabRow(
            pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth(WIDTH_FRACTION)
                .clip(shape = RoundedCornerShape(12.dp))
                .background(Color.Transparent),
            containerColor = Color.Transparent
            //backgroundColor = Color.Unspecified
        ) {
            Tab(pagerState.currentPage == 0, onClick = { changePage(0) }) {
                Text(
                    "登录",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Tab(pagerState.currentPage == 1, onClick = { changePage(1) }) {
                Text(
                    "注册",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalPager(
            count = 2,
            modifier = Modifier.fillMaxHeight(),
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> LoginForm(vm, onLoginSuccess = onLoginSuccess)
                1 -> RegisterForm(vm, onRegisterSuccess = { changePage(0) })
            }
        }
    }
}

@Composable
fun LoginForm(vm: LoginViewModel, onLoginSuccess: (UserBean) -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxWidth(WIDTH_FRACTION),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputUserName(vm)
        Spacer(modifier = Modifier.height(12.dp))
        if (vm.shouldVerifyEmailWhenLogin){
            InputEmailWrapper(modifier = Modifier.fillMaxWidth(), vm = vm, shouldInputEmail = false)
            Spacer(modifier = Modifier.height(12.dp))
        }
        CompletableButton(
            onClick = {
                if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                    BiometricUtils.validateFingerPrint(
                        context as AppCompatActivity,
                        data = vm.loginData,
                        onNotSupport = { msg: String -> context.toastOnUi(msg) },
                        onFail = { context.toastOnUi("认证失败！") },
                        onSuccess = { encryptedInfo, iv ->
                            context.toastOnUi("指纹认证成功！")
                            vm.finishValidateFingerPrint = true
                            vm.encryptedInfo = encryptedInfo
                            vm.iv = iv
                        },
                        onError = { errorCode, errorMsg -> context.toastOnUi("认证失败！（$errorCode: $errorMsg）") },
                        onNewFingerPrint = { email ->
                            if(email.isNotEmpty()){
                                try{
                                    scope.launch {
                                        UserUtils.sendVerifyEmail(vm.username, email)
                                        vm.shouldVerifyEmailWhenLogin = true
                                        vm.email = email
                                        context.toastOnUi("邮件发送成功，请注意查收！")
                                    }
                                }catch (e: Exception){
                                    context.toastOnUi("邮件发送失败！")
                                }
                            }

                        }
                    )
                } else {
                    context.toastOnUi("您的安卓版本过低，不支持指纹认证！将使用密码认证~", Toast.LENGTH_LONG)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = vm.isValidUsername,
            completed = vm.finishValidateFingerPrint
        ) {
            Text("验证指纹")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                vm.login(
                    onSuccess = {
                        context.toastOnUi("登录成功！")
                        onLoginSuccess(it)
                    },
                    onError = { msg ->
                        context.toastOnUi("登录失败！（$msg）")
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled =
                if (vm.shouldVerifyEmailWhenLogin) {
                    vm.isValidUsername && vm.finishValidateFingerPrint && vm.isValidEmail && vm.verifyCode.length == 6
                } else {
                    vm.isValidUsername && vm.finishValidateFingerPrint
                }
        ) {
            Text("登录")
        }
    }
}

@Composable
fun RegisterForm(vm: LoginViewModel, onRegisterSuccess: () -> Unit = {}) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxWidth(WIDTH_FRACTION),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputUserName(vm)
        Spacer(modifier = Modifier.height(8.dp))
        InputEmailWrapper(modifier = Modifier.fillMaxWidth(), vm = vm)
        Spacer(modifier = Modifier.height(12.dp))
        CompletableButton(
            onClick = {
                if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                    BiometricUtils.setFingerPrint(
                        context as AppCompatActivity,
                        data = vm.loginData,
                        onNotSupport = { msg: String -> context.toastOnUi(msg) },
                        onFail = { context.toastOnUi("认证失败！") },
                        onSuccess = { encryptedInfo, iv ->
                            context.toastOnUi("添加指纹成功！")
                            vm.finishSetFingerPrint = true
                            vm.encryptedInfo = encryptedInfo
                            vm.iv = iv
                        },
                        onError = { errorCode, errorMsg -> context.toastOnUi("认证失败！（$errorCode: $errorMsg）") }
                    )
                } else {
                    context.toastOnUi("您的安卓版本过低，不支持指纹认证！将使用密码认证~", Toast.LENGTH_LONG)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = vm.isValidUsername,
            completed = vm.finishSetFingerPrint
        ) {
            Text("添加指纹")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                vm.register(
                    onSuccess = {
                        context.toastOnUi("注册成功！")
                        onRegisterSuccess()
                    },
                    onError = { msg ->
                        context.toastOnUi("注册失败！（$msg）")
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6 && vm.finishSetFingerPrint
        ) {
            Text("注册")
        }
    }
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

@Composable
fun InputUserName(vm: LoginViewModel) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = vm.username,
        onValueChange = { vm.username = it },
        isError = vm.username != "" && !vm.isValidUsername,
        label = { Text(text = "用户名") },
        placeholder = { Text("3-16位，无特殊符号") },
        singleLine = true
    )
}

@Composable
private fun InputEmailWrapper(modifier: Modifier, vm: LoginViewModel, shouldInputEmail: Boolean = true) {
    val context = LocalContext.current
    InputEmail(
        value = vm.email,
        onValueChange = { vm.email = it },
        isError = vm.email != "" && !vm.isValidEmail,
        modifier = modifier,
        verifyCode = vm.verifyCode,
        onVerifyCodeChange = { vm.verifyCode = it },
        shouldInputEmail = shouldInputEmail,
        onClick = { vm.sendVerifyEmail(context) }
    )
}

@Composable
fun InputEmail(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    isError: Boolean = false,
    verifyCode: String,
    onVerifyCodeChange: (String) -> Unit = {},
    shouldInputEmail: Boolean,
    onClick: () -> Unit
) {
    Column(modifier) {
        if (shouldInputEmail) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                isError = isError,
                label = { Text(text = "邮箱") },
                placeholder = { Text("请输入主流的合法邮箱") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                trailingIcon = {
                    CountDownTimeButton(
                        modifier = Modifier.weight(1f),
                        onClick = onClick,
                        enabled = !isError
                    )
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = verifyCode,
            onValueChange = onVerifyCodeChange,
            isError = false,
            label = { Text(text = "验证码") },
            placeholder = { Text("请输入收到的验证码") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
    }
}

/**
 * 带倒计时的按钮
 *
 */
@Composable
fun CountDownTimeButton(
    modifier: Modifier,
    onClick: () -> Unit,
    countDownTime: Int = 60,
    text: String = "获取验证码",
    enabled: Boolean = true
) {
    var time by remember { mutableStateOf(countDownTime) }
    var isTiming by remember { mutableStateOf(false) }
    LaunchedEffect(isTiming) {
        while (isTiming) {
            delay(1000)
            time--
            if (time == 0) {
                isTiming = false
                time = countDownTime
            }
        }
    }
    TextButton(
        onClick = {
            if (!isTiming) {
                isTiming = true
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled && !isTiming
    ) {
        Text(text = if (isTiming) "${time}s" else text)
    }
}