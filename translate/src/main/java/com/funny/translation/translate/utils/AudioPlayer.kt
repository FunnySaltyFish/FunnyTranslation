package com.funny.translation.translate.utils

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.funny.translation.trans.Language
import com.funny.translation.translate.FunnyApplication
import java.io.IOException

object AudioPlayer {
    private const val TAG = "AudioPlayer"
    private val mediaPlayer : MediaPlayer by lazy{
        MediaPlayer().apply {
            setOnPreparedListener {
                it.seekTo(0)
                it.start()
            }
            setOnCompletionListener {
                //it?.release()
            }
        }
    }

    fun play(
        word : String,
        language: Language,
        onError : (Exception)->Unit
    ){
        val url = getUrl(word, language)
        Log.d(TAG, "play: url:$url")
        try {
            mediaPlayer.setOnErrorListener { _, _, _ ->
                onError(IOException("Load internet media error!"))
                false
            }
            if(mediaPlayer.isPlaying) mediaPlayer.pause()
            mediaPlayer.reset()
            mediaPlayer.setDataSource(FunnyApplication.ctx, Uri.parse(url))
            mediaPlayer.prepareAsync()
        }catch (e : Exception){
            e.printStackTrace()
            onError(e)
        }
    }

    private val languageMapping: Map<Language, String>
        get() = mapOf(
            Language.AUTO to "auto",
            Language.CHINESE to "zh",
            Language.ENGLISH to "en",
            Language.JAPANESE to "jp",
            Language.KOREAN to "kor",
            Language.FRENCH to "fra",
            Language.RUSSIAN to "ru",
            Language.GERMANY to "de",
            Language.WENYANWEN to "zh",
            Language.THAI to "th"
        )

    private fun getUrl(word: String, language: Language) = String.format(
        "https://fanyi.baidu.com/gettts?lan=%s&text=%s&spd=3&source=wise",
        languageMapping[language]?:"auto",
        Uri.encode(word)
    )
}