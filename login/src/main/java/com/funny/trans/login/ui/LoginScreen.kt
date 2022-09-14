@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.trans.login.ui

import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.funny.trans.login.GameActivity
import com.funny.trans.login.R
import com.funny.translation.bean.UserBean
import com.funny.translation.helper.UserUtils
import com.funny.translation.AppConfig
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
    val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("GameActivityResult", "LoginScreen: data: ${result.data?.extras}")
        result.data?.getStringExtra("password")?.let {
            Log.d("GameActivityResult", "LoginScreen: password: $it")
            vm.password = it
            vm.passwordType = "2"
        }
    }

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
                1 -> RegisterForm(vm, activityLauncher, onRegisterSuccess = { changePage(0) })
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
            InputEmailWrapper(modifier = Modifier.fillMaxWidth(), vm = vm, shouldInputEmail = true, initialSent = true)
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (vm.passwordType == "2"){
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = vm.password, onValueChange = { vm.password = it}, enabled = true, placeholder = {
                Text(text = "长度8-16位，包含大小写字母和数字")
            }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), label = {
                Text(text = "密码")
            })
        } else CompletableButton(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    BiometricUtils.validateFingerPrint(
                        context as AppCompatActivity,
                        username = vm.username,
                        did = AppConfig.androidId,
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
                                        vm.shouldVerifyEmailWhenLogin = true
                                        vm.email = email
                                        BiometricUtils.uploadFingerPrint(username = vm.username)
                                        UserUtils.sendVerifyEmail(vm.username, email)
                                        context.toastOnUi("邮件发送成功，请注意查收！")
                                    }
                                }catch (e: Exception){
                                    context.toastOnUi("邮件发送失败！")
                                }
                            }
                        },
                        onUsePassword = {
                            vm.passwordType = "2"
                            vm.password = ""
                        }
                    )
                } else {
                    context.toastOnUi("您的安卓版本过低，不支持指纹认证！将使用密码认证~", Toast.LENGTH_LONG)
                    vm.passwordType = "2"
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
                    when(vm.passwordType){
                        "1" -> vm.isValidUsername && vm.finishValidateFingerPrint
                        "2" -> vm.isValidUsername && vm.password.length >= 8 && vm.password.length <= 16
                        else -> false
                    }
                }
        ) {
            Text("登录")
        }
    }
}

@Composable
fun RegisterForm(vm: LoginViewModel, activityLauncher: ActivityResultLauncher<Intent>, onRegisterSuccess: () -> Unit = {}) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxWidth(WIDTH_FRACTION),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputUserName(vm)
        Spacer(modifier = Modifier.height(8.dp))
        InputEmailWrapper(modifier = Modifier.fillMaxWidth(), vm = vm)
        Spacer(modifier = Modifier.height(12.dp))
        if (vm.passwordType == "2"){
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = vm.password, onValueChange = { vm.password = it}, enabled = true, placeholder = {
                Text(text = "长度8-16位，包含大小写字母和数字")
            }, label = { Text(text = "密码") }, readOnly = !AppConfig.lowerThanM)
            Spacer(modifier = Modifier.height(8.dp))
            CompletableButton(
                    modifier = Modifier.fillMaxWidth(),
            enabled = true,
            onClick = { activityLauncher.launch(Intent(context, GameActivity::class.java)) }) {
                Text(text = "点我输入密码")
            }
        } else CompletableButton(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                        onError = { errorCode, errorMsg -> context.toastOnUi("认证失败！（$errorCode: $errorMsg）") },
                        onUsePassword = {
                            vm.passwordType = "2"
                            vm.password = ""
                        }
                    )
                } else {
                    context.toastOnUi("您的安卓版本过低，不支持指纹认证！将使用密码认证~", Toast.LENGTH_LONG)
                    vm.passwordType = "2"
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
            enabled =
                if(vm.passwordType == "1")
                    vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6 && vm.finishSetFingerPrint
                else
                    vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6 && vm.password.length >= 8
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
private fun InputEmailWrapper(modifier: Modifier, vm: LoginViewModel, shouldInputEmail: Boolean = true, initialSent: Boolean = false) {
    val context = LocalContext.current
    InputEmail(
        value = vm.email,
        onValueChange = { vm.email = it },
        isError = vm.email != "" && !vm.isValidEmail,
        modifier = modifier,
        verifyCode = vm.verifyCode,
        onVerifyCodeChange = { vm.verifyCode = it },
        shouldInputEmail = shouldInputEmail,
        initialSent = initialSent,
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
    initialSent: Boolean,
    onClick: () -> Unit
) {
    Column(modifier) {
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
                    enabled = value != "" && !isError,
                    initialSent = initialSent // 当需要
                )
            },
        )
        Spacer(modifier = Modifier.height(8.dp))

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
    enabled: Boolean = true,
    initialSent: Boolean = false
) {
    var time by remember { mutableStateOf(countDownTime) }
    var isTiming by remember { mutableStateOf(initialSent) }
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