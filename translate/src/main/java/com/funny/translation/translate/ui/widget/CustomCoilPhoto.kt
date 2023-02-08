package com.funny.translation.translate.ui.widget

import android.net.Uri
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import cn.qhplus.emo.photo.coil.CoilImageDecoderFactory
import cn.qhplus.emo.photo.coil.CoilPhotoProvider
import cn.qhplus.emo.photo.data.BitmapRegionHolderDrawable
import cn.qhplus.emo.photo.data.BitmapRegionProvider
import cn.qhplus.emo.photo.data.Photo
import cn.qhplus.emo.photo.data.PhotoResult
import cn.qhplus.emo.photo.ui.BitmapRegionItem
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomCoilPhoto(
    val uri: Uri,
    val isLongImage: Boolean,
    var lazyListState: LazyListState
) : Photo {

    @Composable
    override fun Compose(
        contentScale: ContentScale,
        isContainerDimenExactly: Boolean,
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)?,
    ) {
        if (isLongImage) {
            LongImage(onSuccess, onError, lazyListState)
        } else {
            val context = LocalContext.current
            val model = remember(context, uri, onSuccess, onError) {
                ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(true)
                    .decoderFactory(CoilImageDecoderFactory.defaultInstance)
                    .listener(onError = { _, result ->
                        onError?.invoke(result.throwable)
                    }) { _, result ->
                        onSuccess?.invoke(PhotoResult(uri, result.drawable))
                    }.build()
            }
            AsyncImage(
                model = model,
                contentDescription = "",
                contentScale = contentScale,
                alignment = Alignment.Center,
                modifier = Modifier.let {
                    if (isContainerDimenExactly) {
                        it.fillMaxSize()
                    } else {
                        it
                    }
                }
            )
        }
    }

    @Composable
    fun LongImage(
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)?,
        lazyListState: LazyListState,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            var images by remember {
                mutableStateOf(emptyList<BitmapRegionProvider>())
            }
            val context = LocalContext.current
            LaunchedEffect(key1 = constraints.maxWidth, key2 = constraints.maxHeight) {
                val result = withContext(Dispatchers.IO) {
                    val request = ImageRequest.Builder(context)
                        .data(uri)
                        .crossfade(true)
                        .size(constraints.maxWidth, constraints.maxHeight)
                        .scale(Scale.FILL)
                        .setParameter("isLongImage", true)
                        .decoderFactory(CoilImageDecoderFactory.defaultInstance)
                        .build()
                    context.imageLoader.execute(request)
                }
                if (result is SuccessResult) {
                    (result.drawable as? BitmapRegionHolderDrawable)?.bitmapRegion?.let {
                        images = it.list
                    }
                    onSuccess?.invoke(PhotoResult(uri, result.drawable))
                } else if (result is ErrorResult) {
                    onError?.invoke(result.throwable)
                }
            }
            if (images.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {
                    items(images) { image ->
                        BoxWithConstraints() {
                            val width = constraints.maxWidth
                            val height = width * image.height / image.width
                            val heightDp = with(LocalDensity.current) {
                                height.toDp()
                            }
                            BitmapRegionItem(image, maxWidth, heightDp)
                        }
                    }
                }
            }
        }
    }
}

class CustomCoilProvider(uri: Uri, thumbUri: Uri = uri, ratio: Float, val lazyListState: LazyListState):
    CoilPhotoProvider(uri, thumbUri, ratio){
    override fun photo(): Photo {
        return CustomCoilPhoto(uri, isLongImage(), lazyListState)
    }
}