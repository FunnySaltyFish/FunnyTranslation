package com.funny.translation.translate.ui.main

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.qhplus.emo.photo.activity.*
import cn.qhplus.emo.photo.coil.CoilMediaPhotoProviderFactory
import cn.qhplus.emo.photo.ui.GesturePhoto
import com.funny.compose.loading.LoadingState
import com.funny.translation.AppConfig
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.CustomPhotoPickerActivity
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.ui.widget.AutoResizedText
import com.funny.translation.translate.ui.widget.CustomCoilProvider
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
    val imagePickResult: MutableState<PhotoPickResult?> = remember {
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
            vm.imageUri = null
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
            imagePickResult.value = null
        }
    }

    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.getPhotoPickResult()?.let { result ->
                    imagePickResult.value = result
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
                                imagePickResult.value?.list?.mapTo(
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
    // 图片为了铺满屏幕进行的缩放
    var imageInitialScale by remember { mutableStateOf(1f) }
    var scaleByWidth by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var composableHeight by remember { mutableStateOf(0f) }
    BoxWithConstraints(modifier = modifier
        // .clickable { showResult = !showResult }
    ){
        LaunchedEffect(maxWidth, maxHeight){
            // 竖屏以宽为比例缩放，横屏以高为比例
            scaleByWidth = maxWidth < maxHeight
            with(density) {
                imageInitialScale = if (scaleByWidth) maxWidth.toPx() / vm.imgWidth else maxHeight.toPx() / vm.imgHeight
                composableHeight = maxHeight.toPx()
            }

        }

        val lazyListState = rememberLazyListState()
        val photoProvider = remember(vm.imageUri) {
            vm.imageUri?.let {
                CustomCoilProvider(it, it, if (vm.imgWidth < vm.imgHeight) vm.imgWidth.toFloat() / vm.imgHeight else vm.imgHeight.toFloat() / vm.imgWidth, lazyListState)
            }
        }
        photoProvider?.let {
            GesturePhoto(
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                imageRatio = photoProvider.ratio,
                isLongImage = photoProvider.isLongImage(),
                onBeginPullExit = { false },
                shouldTransitionExit = false,
                onTapExit = { showResult = !showResult }
            ) { _, gestureScale, rect, onImageRatioEnsured ->
                // imageGestureScale = gestureScale
                // imageOffsetRect = rect
                // Log.d(TAG, "ResultPart: gestureScale: $gestureScale, rect: $rect")
                photoProvider.photo().Compose(
                    contentScale = if (scaleByWidth) ContentScale.FillWidth else ContentScale.FillHeight,
                    isContainerDimenExactly = true,
                    onSuccess = {},
                    onError = { context.toastOnUi("加载图片失败") }
                )

                if (vm.translateState.isLoading){
                    CircularProgressIndicator(
                        Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center))
                } else if (vm.translateState.isSuccess){
                    val alpha by animateFloatAsState(targetValue = if (showResult) 1f else 0f)
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .alpha(alpha)
                        .background(Color.LightGray.copy(0.9f))
                        .clipToBounds()
                    ){
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((vm.imgHeight * imageInitialScale / density.density).dp)
                                .align(Alignment.Center)
                                .border(2.dp, color = Color.White)
                        ) {
                            val data = (vm.translateState as LoadingState.Success).data
                            data.content.forEach { part ->
                                val w = remember { (part.width * imageInitialScale / density.density).dp }
                                val h = remember { (part.height * imageInitialScale / density.density).dp }
                                AutoResizedText(
                                    modifier = Modifier
                                        .size(w, h)
                                        .offset {
                                            IntOffset(
                                                (part.x * imageInitialScale).toInt(),
                                                (-(lazyListState.firstVisibleItemIndex*composableHeight+lazyListState.firstVisibleItemScrollOffset) + part.y * imageInitialScale).toInt()
                                            )
                                        },
                                    text = part.target,
                                    color = Color.White,
                                )
                            }
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
