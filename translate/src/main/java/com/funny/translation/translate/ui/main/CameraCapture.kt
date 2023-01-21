package com.funny.translation.translate.ui.main

import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.TorchCallback
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.utils.executor
import com.funny.translation.ui.Permission
import com.funny.translation.ui.touchToScale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.io.File

@ExperimentalCoroutinesApi
@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onSavedImageFile: (Uri) -> Unit = { },
    onError: (Exception) -> Unit = {},
    startChooseImage: () -> Unit,
) {
    val context = LocalContext.current
    Permission(
        permission = Manifest.permission.CAMERA,
        description = stringResource(id = R.string.need_camera_permission_tip),
    ) {
        Box(modifier = modifier) {
            val coroutineScope = rememberCoroutineScope()
            val cameraState: MutableState<Camera?> = remember {
                mutableStateOf(null)
            }

            val imageCaptureUseCase by remember {
                mutableStateOf(
                    ImageCapture.Builder()
                        .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()
                )
            }
            Box {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    imageCaptureUseCase = imageCaptureUseCase,
                    cameraSelector = cameraSelector,
                    cameraState = cameraState
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlashlightButton(cameraControl = cameraState.value?.cameraControl)
                    CapturePictureButton(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(16.dp),
                        onClick = {
                            coroutineScope.launch {
                                imageCaptureUseCase.takePicture(
                                    OutputFileOptions.Builder(photoCachePath).build(),
                                    context.executor,
                                    object : OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            outputFileResults.savedUri?.let { onSavedImageFile(it) }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            onError(exception)
                                        }
                                    }
                                )
                            }
                        }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_album),
                        contentDescription = stringResource(R.string.album),
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .touchToScale(onClick = startChooseImage)
                            .size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashlightButton(
    cameraControl: CameraControl?
) {
    val context = LocalContext.current
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    var flashlightEnabled by remember {
        mutableStateOf(false)
    }
    val torchCallback = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            object : TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    flashlightEnabled = enabled
                }
            }
        } else {
            null
        }
    }

    DisposableEffect(cameraManager) {
        torchCallback?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.registerTorchCallback(it, null)
            }
        }
        onDispose {
            torchCallback?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager.unregisterTorchCallback(it)
                }
            }
        }
    }

    IconToggleButton(
        checked = flashlightEnabled,
        onCheckedChange = {
            flashlightEnabled = it
            cameraControl?.enableTorch(it)
        }
    ) {
        Icon(if (flashlightEnabled) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff, "闪光灯", modifier = Modifier.size(36.dp), tint = Color.White)
    }
}

private val photoCachePath =
    File(FunnyApplication.ctx.cacheDir.absolutePath + "/temp_captured_photo.png").also {
        Log.d("CameraCapture", "photoPath: $it")
    }