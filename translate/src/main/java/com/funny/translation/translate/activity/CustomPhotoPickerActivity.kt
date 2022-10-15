package com.funny.translation.translate.activity

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import cn.qhplus.emo.photo.activity.PhotoPickerActivity
import cn.qhplus.emo.photo.data.MediaDataProvider
import cn.qhplus.emo.photo.ui.picker.*
import cn.qhplus.emo.photo.vm.PhotoPickerViewModel
import cn.qhplus.emo.ui.core.ex.setNavTransparent
import cn.qhplus.emo.ui.core.ex.setNormalDisplayCutoutMode
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomPhotoPickerActivity : PhotoPickerActivity() {
    private val viewModel by viewModels<PhotoPickerViewModel>(factoryProducer = {
        object : AbstractSavedStateViewModelFactory(this@CustomPhotoPickerActivity, intent?.extras) {
            override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                val constructor = modelClass.getDeclaredConstructor(
                    Application::class.java,
                    SavedStateHandle::class.java,
                    MediaDataProvider::class.java,
                    Array<String>::class.java
                )
                return constructor.newInstance(
                    this@CustomPhotoPickerActivity.application,
                    handle,
                    dataProvider(),
                    supportedMimeTypes()
                )
            }
        }
    })

    @Composable
    override fun PageContentWithConfigProvider(viewModel: PhotoPickerViewModel) {
        val photoPickerConfig = PhotoPickerConfig(
            topBarSendFactory =
            { canSendSelf, maxSelectCount, selectCountFlow, onClick ->
                PhotoSendTopBarItem(
                    text = FunnyApplication.ctx.getString(R.string.message_confirm),
                    canSendSelf = canSendSelf,
                    maxSelectCount = maxSelectCount,
                    selectCountFlow = selectCountFlow,
                    onClick = onClick
                )
            }
        )
        CompositionLocalProvider(LocalPhotoPickerConfig provides photoPickerConfig) {
            PageContent(viewModel = viewModel)
        }
        Log.d("PageContent", "PageContentWithConfigProvider: ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).run {
            isAppearanceLightNavigationBars = false
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.setNavTransparent()
        window.setNormalDisplayCutoutMode()
        setContent {
            PageContentWithConfigProvider(viewModel)
        }

        lifecycleScope.launch {
            viewModel.finishFlow.collectLatest {
                if (it != null) {
                    onHandleSend(it)
                } else {
                    finish()
                }
            }
        }
    }
}