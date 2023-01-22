package com.funny.translation.translate.ui.main

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.qhplus.emo.photo.activity.*
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import coil.compose.rememberAsyncImagePainter
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.ImageTranslationPart
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.ui.widget.SimpleDialog
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ImageTransScreen(
    modifier: Modifier,
) {
    val vm: ImageTransViewModel = viewModel()
    val context = LocalContext.current
    val avatarPickResult: MutableState<PhotoPickResult?> = remember {
        mutableStateOf(null)
    }
    var photoName by rememberSaveable {
        mutableStateOf("")
    }

    val clipperLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.let { intent -> UCrop.getOutput(intent) } ?: return@rememberLauncherForActivityResult
            vm.updateImageUri(uri)
        }
    }

    val doClip = remember {
        { uri: Uri ->
            clipperLauncher.launch(
                UCrop.of(uri, DESTINATION_IMAGE_URI)
                    .withOptions(UCrop.Options().apply {
                        setCompressionFormat(Bitmap.CompressFormat.PNG)
                    })
                    .getIntent(context)
            )
        }
    }

    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.getPhotoPickResult()?.let { result ->
                    avatarPickResult.value = result
                    val img = result.list[0]
                    photoName = img.name
                    doClip(img.uri)
                }
            }
        }

    if (vm.imageUri != null) {
        ImageTranslationPart(vm = vm)
    } else {
        CameraCapture(
            modifier = modifier,
            onSavedImageFile = { uri ->
                photoName = "photo_${System.currentTimeMillis()}.png"
                doClip(uri)
            },
            onError = {
                context.toastOnUi("拍照失败！")
            },
            startChooseImage = {
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
        )
    }

}

@Composable
private fun ImageTranslationPart(
    vm: ImageTransViewModel
) {
    val context = LocalContext.current
    val goBackTipDialogState = remember {
        mutableStateOf(false)
    }
    val currentEnabledLanguages by enabledLanguages.collectAsState()
    val goBack = remember {
        {
            if (vm.isTranslating()) goBackTipDialogState.value = true
            else vm.updateImageUri(null)
        }
    }

    SimpleDialog(
        openDialogState = goBackTipDialogState,
        stringResource(id = R.string.tip),
        "当前翻译正在进行中，您确定要退出吗？",
        confirmButtonAction = {
            vm.updateImageUri(null)
        }
    )

    BackHandler(onBack = goBack)
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
            IconButton(onClick = goBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            LanguageSelectRow(
                modifier = Modifier,
                sourceLanguage = vm.sourceLanguage,
                updateSourceLanguage = vm::updateSourceLanguage,
                targetLanguage = vm.targetLanguage,
                updateTargetLanguage = vm::updateTargetLanguage,
                enabledLanguages = currentEnabledLanguages
            )

        }
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = rememberAsyncImagePainter(vm.imageUri),
            contentDescription = "Captured image"
        )
    }
}

private val EMPTY_IMAGE_URI: Uri = Uri.parse("file://dev/null")
private val DESTINATION_IMAGE_URI = File(FunnyApplication.ctx.cacheDir.absolutePath + "/temp_des_img.png").toUri()
