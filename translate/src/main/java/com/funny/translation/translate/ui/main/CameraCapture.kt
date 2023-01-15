package com.funny.translation.translate.ui.main

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.*
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.utils.executor
import com.funny.translation.ui.Permission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.io.File

@ExperimentalCoroutinesApi
@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onSavedImageFile: (Uri) -> Unit = { },
    onError: (Exception) -> Unit = {}
) {
    val context = LocalContext.current
    Permission(
        permission = Manifest.permission.CAMERA,
        description = stringResource(id = R.string.need_camera_permission_tip),
    ) {
        Box(modifier = modifier) {
            val coroutineScope = rememberCoroutineScope()

            val imageCaptureUseCase by remember {
                mutableStateOf(
                    ImageCapture.Builder()
                        .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()
                )
            }
            Box {
                CameraPreview(modifier = Modifier.fillMaxSize(), imageCaptureUseCase = imageCaptureUseCase, cameraSelector = cameraSelector)
                CapturePictureButton(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
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
            }
        }
    }
}

private val photoCachePath = File(FunnyApplication.ctx.cacheDir.absolutePath + "/temp_captured_photo.png").also {
    Log.d("CameraCapture", "photoPath: $it")
}