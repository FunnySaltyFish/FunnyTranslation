package com.funny.translation.translate.ui.main

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.qhplus.emo.photo.activity.*
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import coil.compose.rememberAsyncImagePainter
import com.funny.translation.AppConfig
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.ui.widget.AutoResizedText
import com.funny.translation.translate.ui.widget.LoadingState
import com.funny.translation.translate.ui.widget.SimpleDialog
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

private const val TAG = "ImageTransScreen"

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
    var photoName by rememberSaveable { mutableStateOf("") }
    val currentEnabledLanguages by enabledLanguages.collectAsState()
    val systemUiController = rememberSystemUiController()

    // 进入页面时隐藏底部栏
    DisposableEffect(key1 = systemUiController) {
        systemUiController.isNavigationBarVisible = false
        onDispose {
            systemUiController.isNavigationBarVisible = !AppConfig.sHideBottomNavBar.value
        }
    }

    val clipperLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.let { intent ->
                val uri = UCrop.getOutput(intent)
                vm.updateImageUri(uri)
                val width = UCrop.getOutputImageWidth(intent)
                val height = UCrop.getOutputImageHeight(intent)
                vm.updateImgSize(width, height)
            }
        }
    }

    val doClip = remember {
        { uri: Uri ->
            clipperLauncher.launch(
                UCrop.of(uri, DESTINATION_IMAGE_URI)
                    .withOptions(UCrop.Options().apply {
                        setCompressionFormat(Bitmap.CompressFormat.JPEG)
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
        Box(modifier = Modifier.fillMaxSize()) {
            CameraCapture(
                modifier = modifier,
                onSavedImageFile = { uri ->
                    photoName = "photo_${System.currentTimeMillis()}.jpg"
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
            LanguageSelectRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(0.dp, 24.dp),
                exchangeButtonTint = Color.White,
                sourceLanguage = vm.sourceLanguage,
                updateSourceLanguage = vm::updateSourceLanguage,
                targetLanguage = vm.targetLanguage,
                updateTargetLanguage = vm::updateTargetLanguage,
                enabledLanguages = currentEnabledLanguages
            )
        }

    }

}

@Composable
private fun ImageTranslationPart(
    vm: ImageTransViewModel
) {
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

    LaunchedEffect(vm.imageUri, vm.sourceLanguage, vm.targetLanguage, vm.translateEngine){
        vm.translate()
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
            EngineSelect(engine = vm.translateEngine, updateEngine = vm::updateTranslateEngine, allEngines = vm.allEngines)
        }
        ResultPart(modifier = Modifier.fillMaxSize(), vm = vm)
    }
}

@Composable
private fun ResultPart(modifier: Modifier, vm: ImageTransViewModel) {
    val density = LocalDensity.current
    var showResult by remember { mutableStateOf(true) }
    var scale by remember { mutableStateOf(1f) }
    var scaleByWidth by remember { mutableStateOf(true) }
    Box(modifier = modifier
        .clickable { showResult = !showResult }
        .onGloballyPositioned {
            val scaleW = it.size.width.toFloat() / vm.imgWidth
            val scaleH = it.size.height.toFloat() / vm.imgHeight
            // 取较小的缩放比例进行缩放，保证图片显示完整
            if (scaleW < scaleH) {
                scaleByWidth = true
                scale = scaleW
            } else {
                scaleByWidth = false
                scale = scaleH
            }
            Log.d(
                TAG,
                "ResultPart: box size: ${it.size}, imgSize: (${vm.imgWidth}, ${vm.imgHeight}), scale: $scale"
            )
        }){
        Image(
            modifier = Modifier.apply { if (scaleByWidth) fillMaxWidth() else fillMaxHeight() },
            painter = rememberAsyncImagePainter(vm.imageUri),
            contentDescription = "Captured image",
        )
        if (vm.translateState.isLoading){
            CircularProgressIndicator(
                Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        } else if (vm.translateState.isSuccess && showResult){
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray.copy(0.9f))
            ){
                val data = (vm.translateState as LoadingState.Success).data
                data.content.forEach { part ->
                    val sizes = remember(part) {
                        density.run {
                            arrayOf(part.width.toDp(), part.height.toDp(), part.x.toDp(), part.y.toDp())
                        }.map { it * scale }
                    }
                    Box(
                        modifier = Modifier
                            .size(sizes[0], sizes[1])
                            .offset(sizes[2], sizes[3])
//                            .border(width = 2.dp, color = Color.White)
                    ){
                        SelectionContainer {
                            AutoResizedText(text = part.target, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineSelect(
    engine: ImageTranslationEngine,
    updateEngine: (ImageTranslationEngine) -> Unit,
    allEngines: List<ImageTranslationEngine>
){
    var expand by remember {
        mutableStateOf(false)
    }
    TextButton(onClick = { expand = !expand }) {
        Text(text = engine.name)
        DropdownMenu(expanded = expand, onDismissRequest = { expand = false }) {
            allEngines.forEach {
                DropdownMenuItem(
                    text = { Text(text = it.name) },
                    onClick = {
                        updateEngine(it)
                        expand = false
                    }
                )
            }
        }
    }
}

private val DESTINATION_IMAGE_URI = File(FunnyApplication.ctx.cacheDir.absolutePath + "/temp_des_img.png").toUri()
