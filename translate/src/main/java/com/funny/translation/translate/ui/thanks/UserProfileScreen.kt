package com.funny.translation.translate.ui.thanks

import android.app.Activity.RESULT_OK
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.navigation.NavHostController
import cn.qhplus.emo.photo.activity.*
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import cn.qhplus.emo.photo.coil.CoilPhotoProvider
import cn.qhplus.emo.photo.data.PhotoProvider
import cn.qhplus.emo.photo.ui.PhotoThumbnailWithViewer
import coil.compose.AsyncImage
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.bean.UserBean
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import com.funny.translation.translate.utils.QQUtils
import kotlinx.coroutines.launch


private const val TAG = "UserProfileScreen"

@Composable
fun UserProfileScreen(navHostController: NavHostController) {
    val activityVM = LocalActivityVM.current
    val context = LocalContext.current
    val avatarPickResult: MutableState<PhotoPickResult?> = remember {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()

//    val imageChooseLauncher = rememberLauncherForActivityResult(){
//        if (it.isNotEmpty()){
//            val mediaResource = it[0]
//            val imageUri = mediaResource.uri
//            val imagePath = mediaResource.path
//            val imageWidth = mediaResource.width
//            val imageHeight = mediaResource.height
//            Log.d(TAG, "UserProfileScreen: imgUrl: $imageUri")
//        }
//    }
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
                        context.toastOnUi("头像上传成功！")
                        avatarPickResult.value = null
                    } else {
                        context.toastOnUi("头像上传失败！")
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


    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier
                .fillMaxWidth()

                .clickable {
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
                .padding(8.dp)
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.avatar))
            AsyncImage(
                model = activityVM.userInfo.avatar_url,
                contentDescription = "头像",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                placeholder = painterResource(R.drawable.ic_loading)
            )
        }
        Divider(Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(64.dp))
        Button(modifier = Modifier.align(CenterHorizontally), onClick = {
            activityVM.userInfo = UserBean()
            navHostController.popBackStack()
        }) {
            Text(text = "退出登录")
        }

        val text = remember {
            buildAnnotatedString {
                append("其他功能开发中，可以加入内测群")
                pushStringAnnotation(
                    tag = "url",
                    annotation = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D"
                )
                withStyle(style = SpanStyle(color = MaterialColors.BlueA700)) {
                    append(" 857362450 ")
                }
                pop()
                append("抢先体验开发中功能")
            }
        }
        ClickableText(
            text = text,
            modifier = Modifier.fillMaxWidth(0.9f),
            style = TextStyle(color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
        ) { index ->
            // 根据tag取出annotation并打印
            text.getStringAnnotations(tag = "url", start = index, end = index).firstOrNull()
                ?.let { _ ->
                    QQUtils.joinQQGroup(context, "mlEwPbkeUQMuwoyp44lROPeD938exo56")
                }
        }
    }
}

@Composable
fun PhotoPickerPage(pickResult: PhotoPickResult?) {
    LazyColumn(Modifier.fillMaxSize()) {
//        item(key = "pick-photo") {
//            val context = LocalContext.current
//            CommonItem("Pick Photo") {
//                pickLauncher.launch(
//                    PhotoPickerActivity.intentOf(
//                        context,
//                        CoilMediaPhotoProviderFactory::class.java,
//                        pickedItems = arrayListOf<Uri>().apply { pickResult.value?.list?.mapTo(this) { it.uri } }
//                    )
//                )
//            }
//        }

        if (pickResult != null && pickResult.list.isNotEmpty()) {
            item(key = pickResult.list.map { it.id }.joinToString(",")) {
                val images = remember(pickResult) {
                    pickResult.list.map {
                        CoilPhotoProvider(
                            it.uri,
                            ratio = it.ratio()
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(text = "原图：${pickResult.isOriginOpen}")

                    PhotoThumbnailWithViewer(
                        images = images
                    )
                }
            }
        }
    }
}
