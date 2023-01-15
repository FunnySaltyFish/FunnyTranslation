package com.funny.translation.translate.ui.main

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cn.qhplus.emo.photo.activity.*
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import cn.qhplus.emo.photo.coil.CoilPhotoProvider
import coil.compose.rememberAsyncImagePainter
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ImageTransScreen(
    modifier: Modifier,
) {
    var imageUri by remember { mutableStateOf(EMPTY_IMAGE_URI) }
    val context = LocalContext.current
    val avatarPickResult: MutableState<PhotoPickResult?> = remember {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()
    var photoName by rememberSaveable {
        mutableStateOf("")
    }

    val clipperLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.getPhotoClipperResult()?.let { img ->
                if (photoName == "") return@rememberLauncherForActivityResult
                imageUri = img.uri
            }
        }
    }

    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
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

    if (imageUri != EMPTY_IMAGE_URI) {
        Box(modifier = modifier) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Captured image"
            )
            Button(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = {
                    imageUri = EMPTY_IMAGE_URI
                }
            ) {
                Text("Remove image")
            }
        }
    } else {
        Box(modifier = modifier) {
            CameraCapture(
                modifier = modifier,
                onSavedImageFile = { uri ->
                    imageUri = uri
                },
                onError = {
                    context.toastOnUi("拍照失败！")
                }
            )
            Button(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(4.dp),
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
                Text("Select from Gallery")
            }
        }

    }
}

val EMPTY_IMAGE_URI: Uri = Uri.parse("file://dev/null")
