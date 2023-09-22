package com.funny.trans.login.ui

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.funny.trans.login.R
import com.funny.translation.AppConfig
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.VibratorUtils
import com.funny.translation.helper.biomertic.BiometricUtils
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.network.api
import com.funny.translation.ui.MarkdownText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal const val WIDTH_FRACTION = 0.8f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginPage(
    navController: NavController,
    onLoginSuccess: (UserInfoBean) -> Unit,
) {
    val vm: LoginViewModel = viewModel()
    val activityLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("GameActivityResult", "LoginScreen: data: ${result.data?.extras}")
            result.data?.getStringExtra("password")?.let {
                Log.d("GameActivityResult", "LoginScreen: password: $it")
                vm.password = it
                vm.passwordType = PASSWORD_TYPE_FINGERPRINT
            }
        }

    Column(
        Modifier
            .fillMaxHeight()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        val pagerState = rememberPagerState(pageCount = { 2 })
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
                    stringResource(R.string.login),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Tab(pagerState.currentPage == 1, onClick = { changePage(1) }) {
                Text(
                    stringResource(R.string.register),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        var privacyGranted by remember { mutableStateOf(false) }
        val shrinkAnim = remember { Animatable(0f) }
        val context = LocalContext.current
        val remindToGrantPrivacyAction = remember {
            {
                scope.launch {
                    intArrayOf(20, 0).forEach {
                        shrinkAnim.animateTo(it.toFloat(), spring(Spring.DampingRatioHighBouncy))
                    }
                }
                VibratorUtils.vibrate(70)
                context.toastOnUi(R.string.tip_confirm_privacy_first)
            }
        }

        HorizontalPager(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = pagerState,
        ) { page ->
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                when (page) {
                    0 -> LoginForm(
                        navController,
                        vm,
                        onLoginSuccess = onLoginSuccess,
                        privacyGranted = privacyGranted,
                        remindToGrantPrivacyAction = remindToGrantPrivacyAction
                    )

                    1 -> RegisterForm(
                        vm,
                        onRegisterSuccess = { changePage(0) },
                        privacyGranted = privacyGranted,
                        remindToGrantPrivacyAction = remindToGrantPrivacyAction
                    )
                }
            }
        }
        Row(
            Modifier
                .padding(8.dp)
                .offset { IntOffset(0, shrinkAnim.value.roundToInt()) },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = privacyGranted, onCheckedChange = { privacyGranted = it })
            MarkdownText(
                stringResource(R.string.tip_agree_privacy),
                color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.background).copy(
                    0.8f
                ),
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun LoginForm(
    navController: NavController,
    vm: LoginViewModel,
    privacyGranted: Boolean = false,
    onLoginSuccess: (UserInfoBean) -> Unit = {},
    remindToGrantPrivacyAction: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(
        Modifier
            .fillMaxWidth(WIDTH_FRACTION)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputUsernameWrapper(
            vm,
            if (vm.passwordType == PASSWORD_TYPE_FINGERPRINT) ImeAction.Done else ImeAction.Next
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (vm.shouldVerifyEmailWhenLogin) {
            InputEmailWrapper(modifier = Modifier.fillMaxWidth(), vm = vm, initialSent = true)
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (vm.passwordType == PASSWORD_TYPE_PASSWORD) {
            InputPasswordWrapper(vm = vm)
        } else CompletableButton(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    BiometricUtils.validateFingerPrint(
                        context as AppCompatActivity,
                        username = vm.username,
                        did = AppConfig.androidId,
                        onNotSupport = { msg: String -> context.toastOnUi(msg) },
                        onFail = { context.toastOnUi(R.string.validate_fingerprint_failed_unknown_reason) },
                        onSuccess = { encryptedInfo, iv ->
                            context.toastOnUi(R.string.validate_fingerprint_success)
                            vm.finishValidateFingerPrint = true
                            vm.encryptedInfo = encryptedInfo
                            vm.iv = iv
                        },
                        onError = { errorCode, errorMsg ->
                            context.toastOnUi(string(R.string.validate_fingerprint_failed_with_msg, errorCode, errorMsg))
                        },
                        onNewFingerPrint = { email ->
                            if (email.isNotEmpty()) {
                                try {
                                    scope.launch {
                                        vm.shouldVerifyEmailWhenLogin = true
                                        vm.email = email
                                        BiometricUtils.uploadFingerPrint(username = vm.username)
                                        api(UserUtils.userService::sendVerifyEmail, vm.username, email)
                                    }
                                } catch (e: Exception) {
                                    context.toastOnUi(R.string.error_sending_email)
                                }
                            }
                        },
                        onUsePassword = {
                            vm.passwordType = PASSWORD_TYPE_PASSWORD
                            vm.password = ""
                        }
                    )
                } else {
                    context.toastOnUi(
                        R.string.fingerprint_not_support,
                        Toast.LENGTH_LONG
                    )
                    vm.passwordType = PASSWORD_TYPE_PASSWORD
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = vm.isValidUsername,
            completed = vm.finishValidateFingerPrint
        ) {
            Text(stringResource(R.string.validate_fingerprint))
        }
        ExchangePasswordType(
            passwordType = vm.passwordType
        ) { vm.passwordType = it }
        Spacer(modifier = Modifier.height(12.dp))

        // 因为下面的表达式变化速度快过UI的变化速度，为了减少重组次数，此处使用 derivedStateOf
        val enabledLogin by remember {
            derivedStateOf {
                if (vm.shouldVerifyEmailWhenLogin) {
                    vm.isValidUsername && vm.finishValidateFingerPrint && vm.isValidEmail && vm.verifyCode.length == 6
                } else {
                    when (vm.passwordType) {
                        PASSWORD_TYPE_FINGERPRINT -> vm.isValidUsername && vm.finishValidateFingerPrint
                        PASSWORD_TYPE_PASSWORD -> vm.isValidUsername && UserUtils.isValidPassword(vm.password)
                        else -> false
                    }
                }
            }
        }
        Button(
            onClick = {
                if (!privacyGranted) {
                    remindToGrantPrivacyAction()
                    return@Button
                }
                vm.login(
                    onSuccess = {
                        onLoginSuccess(it)
                    },
                    onError = { msg ->
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabledLogin
        ) {
            Text(stringResource(id = R.string.login))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    navController.navigate(LoginRoute.FindUsernamePage.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(LoginRoute.LoginPage.route) {
                            inclusive = false
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.forgot_username))
            }
            TextButton(
                onClick = {
                    navController.navigate(LoginRoute.ResetPasswordPage.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(LoginRoute.LoginPage.route) {
                            inclusive = false
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.forgot_password))
            }
        }
    }
}

@Composable
private fun RegisterForm(
    vm: LoginViewModel,
    privacyGranted: Boolean,
    onRegisterSuccess: () -> Unit = {},
    remindToGrantPrivacyAction: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth(WIDTH_FRACTION)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputUsernameWrapper(vm)
        Spacer(modifier = Modifier.height(8.dp))
        InputEmailWrapper(modifier = Modifier.fillMaxWidth(), vm = vm)
        // 邀请码
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = vm.inviteCode,
            onValueChange = { vm.inviteCode = it },
            isError = vm.inviteCode != "" && !vm.isValidInviteCode,
            label = { Text(text = stringResource(R.string.invite_code)) },
            placeholder = { Text(stringResource(R.string.please_input_invite_code)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (vm.passwordType == PASSWORD_TYPE_PASSWORD) {
            InputPasswordWrapper(vm = vm)
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            CompletableButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        BiometricUtils.setFingerPrint(
                            context as AppCompatActivity,
                            username = vm.username,
                            did = AppConfig.androidId,
                            onNotSupport = { msg: String -> context.toastOnUi(msg) },
                            onFail = { context.toastOnUi(string(R.string.validate_fingerprint_failed_unknown_reason)) },
                            onSuccess = { encryptedInfo, iv ->
                                context.toastOnUi(string(R.string.add_fingerprint_success))
                                vm.finishSetFingerPrint = true
                                vm.encryptedInfo = encryptedInfo
                                vm.iv = iv
                            },
                            onError = { errorCode, errorMsg -> context.toastOnUi(
                                string(
                                    R.string.validate_fingerprint_failed_with_msg,
                                    errorCode,
                                    errorMsg
                                )) },
                            onUsePassword = {
                                vm.passwordType = PASSWORD_TYPE_PASSWORD
                                vm.password = ""
                            }
                        )
                    } else {
                        context.toastOnUi(
                            string(R.string.fingerprint_not_support),
                            Toast.LENGTH_LONG
                        )
                        vm.passwordType = PASSWORD_TYPE_PASSWORD
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = vm.isValidUsername,
                completed = vm.finishSetFingerPrint
            ) {
                Text(stringResource(R.string.add_fingerprint))
            }
        }
        ExchangePasswordType(
            passwordType = vm.passwordType
        ) { vm.passwordType = it }
        Spacer(modifier = Modifier.height(12.dp))
        val enableRegister by remember {
            derivedStateOf {
                if (vm.passwordType == PASSWORD_TYPE_FINGERPRINT)
                    vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6 && vm.finishSetFingerPrint
                else
                    vm.isValidUsername && vm.isValidEmail && vm.verifyCode.length == 6 && UserUtils.isValidPassword(vm.password)
            }
        }
        Button(
            onClick = {
                if (!privacyGranted) {
                    remindToGrantPrivacyAction()
                    return@Button
                }
                vm.register(
                    onSuccess = onRegisterSuccess,
                    onError = { msg ->
                        context.toastOnUi(string(R.string.register_failed_with_msg, msg))
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enableRegister
        ) {
            Text(stringResource(id = R.string.register))
        }
    }
}

@Composable
private fun ExchangePasswordType(
    passwordType: String,
    updatePasswordType: (String) -> Unit
) {
    if (passwordType == PASSWORD_TYPE_PASSWORD && !AppConfig.lowerThanM) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.clickable { updatePasswordType(PASSWORD_TYPE_FINGERPRINT) },
            text = stringResource(R.string.change_to_fingerprint),
            style = MaterialTheme.typography.labelSmall
        )
    } else if (passwordType == PASSWORD_TYPE_FINGERPRINT) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.clickable { updatePasswordType(PASSWORD_TYPE_PASSWORD) },
            text = stringResource(R.string.change_to_password),
            style = MaterialTheme.typography.labelSmall
        )
    }
}


@Composable
private fun InputUsernameWrapper(
    vm: LoginViewModel,
    imeAction: ImeAction = ImeAction.Next,
) {
    InputUsername(
        usernameProvider = vm::username,
        updateUsername = vm::updateUsername,
        isValidUsernameProvider = vm::isValidUsername,
        imeAction = imeAction
    )
}

@Composable
private fun InputEmailWrapper(
    modifier: Modifier, vm: LoginViewModel, initialSent: Boolean = false
) {
    val context = LocalContext.current
    InputEmail(
        modifier = modifier,
        value = vm.email,
        onValueChange = { vm.email = it },
        isError = vm.email != "" && !vm.isValidEmail,
        verifyCode = vm.verifyCode,
        onVerifyCodeChange = { vm.verifyCode = it },
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
    initialSent: Boolean,
    onClick: () -> Unit
) {
    Column(modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = { onValueChange(it.lowercase()) },
            isError = isError,
            label = { Text(text = stringResource(R.string.Email)) },
            placeholder = { Text(stringResource(R.string.please_input_validate_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
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

        val isVerifyCodeError by remember(verifyCode) {
            derivedStateOf { verifyCode != "" && verifyCode.length != 6 }
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = verifyCode,
            onValueChange = onVerifyCodeChange,
            isError = isVerifyCodeError,
            label = { Text(text = stringResource(R.string.verify_code)) },
            placeholder = { Text(stringResource(R.string.please_input_verify_code)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
    }
}

@Composable
private fun InputPasswordWrapper(
    vm: LoginViewModel
) {
    InputPassword(
        passwordProvider = vm::password,
        updatePassword = vm::updatePassword,
    )
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
    text: String = stringResource(R.string.get_verify_code),
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