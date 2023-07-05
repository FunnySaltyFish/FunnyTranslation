package com.funny.translation.translate.utils

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.funny.translation.helper.DeviceUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.Language
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.engine.TextTranslationEngines
import java.io.IOException

object AudioPlayer {
    private const val TAG = "AudioPlayer"

    var currentPlayingText : String by mutableStateOf("")

    private val mediaPlayer : MediaPlayer by lazy{
        MediaPlayer().apply {
            setOnPreparedListener {
                it.seekTo(0)
                it.start()
            }
        }
    }

    fun playOrPause(
        word : String,
        language: Language,
        onComplete : ()->Unit = {},
        onInterrupt: ()->Unit = {},
        onError : (Exception)->Unit,
    ){
        if (language == Language.AUTO) {
            appCtx.toastOnUi("当前设置的语言为自动选择，请手动指定语言")
            return
        }
        if (DeviceUtils.isMute()) {
            appCtx.toastOnUi("当前设备似乎在静音，请调整音量")
        }
        val url = getUrl(word, language)
        Log.d(TAG, "play: url:$url")
        try {
            mediaPlayer.setOnErrorListener { _, _, _ ->
                onError(IOException("Load internet media error!"))
                currentPlayingText = ""
                false
            }
            mediaPlayer.setOnCompletionListener {
                currentPlayingText = ""
                onComplete()
            }
            if(mediaPlayer.isPlaying) {
                // 点两次当做暂停
                if (currentPlayingText == word) {
                    appCtx.toastOnUi("已停止当前朗读~")
                    mediaPlayer.pause()
                    currentPlayingText = ""
                    onComplete()
                    return
                } else {
                    mediaPlayer.pause()
                    onInterrupt()
                }
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(FunnyApplication.ctx, Uri.parse(url))
            mediaPlayer.prepareAsync()
            currentPlayingText = word
        }catch (e : Exception){
            e.printStackTrace()
            onError(e)
            currentPlayingText = ""
        }
    }

    fun pause(){
        if(mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            currentPlayingText = ""
        }
    }

    private val languageMapping: Map<Language, String> by lazy {
            hashMapOf<Language, String>().apply {
                putAll(TextTranslationEngines.BaiduNormal.languageMapping)
                this[Language.CHINESE_YUE] = "cte"
            }
        }

    private fun getUrl(word: String, language: Language) = String.format(
        "https://fanyi.baidu.com/gettts?lan=%s&text=%s&spd=3&source=wise",
        languageMapping[language] ?: "auto",
        java.net.URLEncoder.encode(word, "UTF-8")
    )
}