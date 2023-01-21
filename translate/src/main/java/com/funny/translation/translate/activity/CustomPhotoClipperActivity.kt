package com.funny.translation.translate.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import cn.qhplus.emo.photo.activity.PhotoClipperActivity
import cn.qhplus.emo.photo.data.PhotoProvider
import cn.qhplus.emo.photo.ui.clipper.PhotoClipper
import cn.qhplus.emo.ui.core.Loading
import cn.qhplus.emo.ui.core.modifier.throttleClick
import cn.qhplus.emo.ui.core.modifier.windowInsetsCommonNavPadding
import kotlinx.coroutines.launch
import com.funny.translation.translate.R
import java.lang.Integer.min

class CustomPhotoClipperActivity: PhotoClipperActivity() {
    private val clipStatus = mutableStateOf(false)

    @Composable
    override fun PageContent(photoProvider: PhotoProvider) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val w = constraints.maxWidth
            val h = constraints.maxHeight
            val center = remember(w, h) {
                Offset(w/2f, h/2f)
            }
            PhotoClipper(
                photoProvider = photoProvider,
                clipFocusArea = Rect(center = center, min(w, h) / 3f)
            ) { doClip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .windowInsetsCommonNavPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .throttleClick {
                                finish()
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(id = R.string.cancel),
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .throttleClick {
                                val bitmap = doClip()
                                if (bitmap == null) {
                                    onClipFailed()
                                } else {
                                    clipStatus.value = true
                                    lifecycleScope.launch {
                                        onClipFinished(bitmap)
                                        clipStatus.value = false
                                    }
                                }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(id = R.string.confirm),
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            ClipHanding()
        }
    }

    @Composable
    override fun ClipHanding() {
        val isHanding = clipStatus.value
        if (isHanding) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Loading(
                    modifier = Modifier.align(Alignment.Center),
                    size = 64.dp,
                    lineColor = Color.White
                )
            }
        }
    }
}