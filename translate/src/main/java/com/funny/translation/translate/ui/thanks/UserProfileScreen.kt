package com.funny.translation.translate.ui.thanks

import android.app.Activity.RESULT_OK
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import cn.qhplus.emo.photo.activity.PhotoClipperActivity
import cn.qhplus.emo.photo.activity.PhotoPickResult
import cn.qhplus.emo.photo.activity.PhotoPickerActivity
import cn.qhplus.emo.photo.activity.getPhotoClipperResult
import cn.qhplus.emo.photo.activity.getPhotoPickResult
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import cn.qhplus.emo.photo.coil.CoilPhotoProvider
import coil.compose.AsyncImage
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.trans.login.ui.LoginRoute
import com.funny.trans.login.ui.addLoginRoutes
import com.funny.translation.AppConfig
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.animateComposable
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.network.api
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import com.funny.translation.translate.extentions.formatBraceStyle
import com.funny.translation.translate.navigateSingleTop
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.translate.utils.QQUtils
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.launch

private const val TAG = "UserProfileScreen"

enum class UserProfileScreenRoutes {
    Settings;

    val route:String get() = "user_profile_route_${name.lowercase()}"
}

fun NavGraphBuilder.addUserProfileRoutes(
    navHostController: NavHostController,
    onLoginSuccess: (UserInfoBean) -> Unit
) {
    navigation(UserProfileScreenRoutes.Settings.route, TranslateScreen.UserProfileScreen.route){
        animateComposable(UserProfileScreenRoutes.Settings.route){
            UserProfileSettings(navHostController = navHostController)
        }
        addLoginRoutes(navHostController, onLoginSuccess)
    }
}


@Composable
fun UserProfileSettings(navHostController: NavHostController) {
    val activityVM = LocalActivityVM.current
    val context = LocalContext.current
    val avatarPickResult: MutableState<PhotoPickResult?> = remember {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()
    var photoName by rememberSaveable {
        mutableStateOf("")
    }

    val clipperLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK) {
            it.data?.getPhotoClipperResult()?.let { img ->
                if (photoName == "") return@rememberLauncherForActivityResult
                scope.launch {
                    val avatarUrl = UserUtils.uploadUserAvatar(context, img.uri, photoName, img.width, img.height, activityVM.uid)
                    if (avatarUrl != ""){
                        activityVM.userInfo = activityVM.userInfo.copy(avatar_url = avatarUrl)
                        context.toastOnUi(R.string.upload_avatar_success)
                        avatarPickResult.value = null
                    } else {
                        context.toastOnUi(R.string.upload_avatar_failed)
                    }
                }
            }
        }
    }

    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.getPhotoPickResult()?.let { result ->
                    avatarPickResult.value = result
                    val img = result.list[0]
                    photoName = img.name
                    clipperLauncher.launch(
                        PhotoClipperActivity.intentOf(
                            context,
                            CoilPhotoProvider(img.uri, ratio = img.ratio())
                        )
                    )
                }
            }
        }

    val userInfo = AppConfig.userInfo.value
    CommonPage(
        title = stringResource(id = R.string.user_profile) + "(${userInfo.username})"
    ) {
        Column(
            Modifier
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = CenterHorizontally
        ) {
            Tile(
                text = stringResource(id = R.string.avatar),
                onClick = {
                    pickLauncher.launch(
                        PhotoPickerActivity.intentOf(
                            context,
                            CoilMediaPhotoProviderFactory::class.java,
                            CustomPhotoPickerActivity::class.java,
                            pickedItems = arrayListOf<Uri>().apply {
                                avatarPickResult.value?.list?.mapTo(
                                    this
                                ) { it.uri }
                            },
                            pickLimitCount = 1,
                        )
                    )
                }
            ) {
                AsyncImage(
                    model = userInfo.avatar_url,
                    contentDescription = stringResource(id = R.string.avatar),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.ic_loading)
                )
            }
            Tile(text = stringResource(R.string.change_username), onClick = {
                navHostController.navigateSingleTop(LoginRoute.ChangeUsernamePage.route)
            })
            Tile(text = stringResource(R.string.modify_password), onClick = {
                navHostController.navigateSingleTop(LoginRoute.ResetPasswordPage.route)
            })
            Tile(text = stringResource(R.string.img_remaining_points)) {
                Text(text = userInfo.img_remain_points.toString())
            }
            // 剩余 AI 文字点数
            Tile(text = stringResource(R.string.ai_remaining_text_points), onClick = {
                navHostController.navigateSingleTop(
                    TranslateScreen.BuyAIPointScreen.route.formatBraceStyle(
                        "planName" to "ai_text_point"
                    )
                )
            }) {
                Text(text = "%.3f".format(userInfo.ai_text_point))
            }
            // 剩余 AI 语音点数
            Tile(text = stringResource(R.string.ai_remaining_voice_points), onClick = {
                navHostController.navigateSingleTop(
                    TranslateScreen.BuyAIPointScreen.route.formatBraceStyle(
                        "planName" to "ai_voice_point"
                    )
                )
            }) {
                Text(text = "%.3f".format(userInfo.ai_voice_point))
            }
            Tile(text = stringResource(R.string.vip_end_time)) {
                Text(text = userInfo.vipEndTimeStr())
            }
            Divider()
            // 生成邀请码
            Tile(text = stringResource(R.string.invite_code), onClick = {
                if (userInfo.invite_code.isBlank()) {
                    scope.launch {
                        api(UserUtils.userService::generateInviteCode) {
                            addSuccess {
                                AppConfig.userInfo.value =
                                    userInfo.copy(invite_code = it.data ?: "")
                            }
                        }
                    }
                } else {
                    val txt = string(R.string.invite_user_content, userInfo.invite_code)
                    ClipBoardUtil.copy(context, txt)
                    context.toastOnUi(R.string.copied_to_clipboard)
                }
            }) {
                Text(userInfo.invite_code.ifEmpty { stringResource(R.string.click_to_generate) })
            }
            // 查询被邀请人
            val (showInviteUserDialog, update) = remember { mutableStateOf(false) }
            Tile(text = stringResource(R.string.invited_users), onClick = {
                update(true)
            }) {
                InvitedUserAlertDialog(show = showInviteUserDialog, updateShow = update)
            }
            Divider()
            Tile(text = stringResource(R.string.disable_account), onClick = {
                navHostController.navigateSingleTop(LoginRoute.CancelAccountPage.route)
            })
            Divider()
            Spacer(modifier = Modifier.height(64.dp))
            Button(modifier = Modifier.align(CenterHorizontally), onClick = {
                AppConfig.logout()
                navHostController.popBackStack()
            }) {
                Text(text = stringResource(R.string.logout))
            }

            val text = remember {
                buildAnnotatedString {
                    append(string(R.string.join_group_tip_p1))
                    pushStringAnnotation(
                        tag = "url",
                        annotation = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D"
                    )
                    withStyle(style = SpanStyle(color = MaterialColors.BlueA700)) {
                        append(" 857362450 ")
                    }
                    pop()
                    append(string(R.string.join_group_tip_p2))
                }
            }
            ClickableText(
                text = text,
                modifier = Modifier.fillMaxWidth(0.9f),
                style = TextStyle(
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            ) { index ->
                // 根据tag取出annotation并打印
                text.getStringAnnotations(tag = "url", start = index, end = index).firstOrNull()
                    ?.let {
                        QQUtils.joinQQGroup(context, "mlEwPbkeUQMuwoyp44lROPeD938exo56")
                    }
            }
        }
    }
}

@Composable
private fun Tile(
    text: String,
    onClick: () -> Unit = {},
    endIcon: @Composable () -> Unit = {
        FixedSizeIcon(imageVector = Icons.Default.ArrowRight, "")
    }
) {
    Row(
        Modifier
            .height(66.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text)
        endIcon()
    }
}

@Composable
private fun InvitedUserAlertDialog(
    show: Boolean,
    updateShow: (Boolean) -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = { updateShow(false) },
            title = {
                Text(text = stringResource(id = R.string.invited_users))
            },
            text = {
                val (state, retry) = rememberRetryableLoadingState(loader = ::loadInvitedUsers)
                LazyColumn {
                    loadingList(state, retry, key = { it.uid }, empty = {
                        Text(
                            text = stringResource(id = R.string.empty_invited_users),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }) { item ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Uid: " + item.uid)
                            Text(text = item.register_time)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { updateShow(false) }) {
                    Text(text = stringResource(R.string.ok))
                }
            },
        )
    }
}

private suspend fun loadInvitedUsers() = api(UserUtils.userService::getInviteUsers) ?: emptyList()