package com.funny.translation.utils

import android.content.Context
import com.danikula.videocache.HttpProxyCacheServer
import android.speech.tts.TextToSpeech
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import com.funny.translation.thread.InternetTTSThread
import com.funny.translation.MainActivity
import com.funny.translation.bean.Consts
import java.lang.Exception
import java.util.*

object TTSUtil {
    var proxy: HttpProxyCacheServer? = null
    var localTTS: TextToSpeech? = null
    var internetTTS: MediaPlayer? = null
    var internetTTSThread: InternetTTSThread? = null
    fun initLocal(ctx: Context?) {
        localTTS = TextToSpeech(ctx) { status -> // TODO: Implement this method
            if (status != TextToSpeech.SUCCESS) {
                localTTS = null
            }
        }
    }

    private fun initInternet(ctx: Context?, handler: Handler?) {
        internetTTSThread = InternetTTSThread(ctx, handler)
        internetTTSThread!!.name = "InternetTTSThread"
        internetTTSThread!!.start()
    }

    fun rePlayInternet(ctx: Context?, url: String?) {
        try {
            if (internetTTSThread?.isAlive == true) {
                internetTTSThread?.setAddUrl(url)
            }
        } catch (e: Exception) {
            ApplicationUtil.print(ctx, "朗读发生错误！")
            e.printStackTrace()
        }
    }

    fun playOrPauseInternet() {
        if (internetTTSThread != null && internetTTSThread!!.isAlive) {
            internetTTSThread!!.playOrPause()
        }
    }

    fun getInternetUrl(TTSEngine: Short, text: String, language: Short): String? {
        var url: String? = null
        if (text == "0c1be36a95") {
            url = "funny://egg_1_4"
            return url
        }
        when (TTSEngine) {
            Consts.TTS_BAIDU -> try {
                //文言文处理为中文
                val lANGUAGE =
                    if (language == Consts.LANGUAGE_WENYANWEN) Consts.LANGUAGES[Consts.LANGUAGE_CHINESE.toInt()][Consts.ENGINE_BAIDU_NORMAL.toInt()] else Consts.LANGUAGES[language.toInt()][Consts.ENGINE_BAIDU_NORMAL.toInt()]
                //	"https://tts.baidu.com/text2audio?tex=" + URLEncoder.encode(text, "utf-8") + "&cuid=baike&ctp=1&pdt=301&vol=9&rate=32&per=0&lan=" + lANGUAGE;
                url = String.format(
                    "https://fanyi.baidu.com/gettts?lan=%s&text=%s&spd=3&source=wise",
                    lANGUAGE,
                    Uri.encode(text)
                ) //将 转为%20而不是加号
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Consts.TTS_YOUDAO -> url =
                String.format("http://dict.youdao.com/dictvoice?audio=%s", Uri.encode(text))
        }
        return url
    }

    fun speak(ctx: Context, text: String, language: Short, TTSEngine: Short) {
        if (TTSEngine == Consts.TTS_LOCAL) {
            if (localTTS == null) {
                initLocal(ctx)
                //				ApplicationUtil.print(ctx,"正在初始化本地朗读引擎……\n如需朗读请稍等再次点击");
//				return;
            }
            val result = localTTS!!.setLanguage(getLanguage(language))
            if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                && result != TextToSpeech.LANG_AVAILABLE
            ) {
                ApplicationUtil.print(ctx, "您的本地TTS暂不支持该种语言的朗读")
                return
            }
            localTTS!!.speak(text, TextToSpeech.QUEUE_ADD, null)
        } else { //网络
            if (internetTTSThread == null) {
                val activity = ctx as MainActivity
                initInternet(ctx, activity.handler)
                //				ApplicationUtil.print(ctx,"正在初始化网络朗读引擎……\n如需朗读请稍等再次点击");
//				return;
            }
            rePlayInternet(ctx, getInternetUrl(TTSEngine, text, language))
        }
    }

    fun getLanguage(targetLanguage: Short): Locale {
        return when (targetLanguage) {
            Consts.LANGUAGE_CHINESE, Consts.LANGUAGE_WENYANWEN -> Locale.CHINESE
            Consts.LANGUAGE_ENGLISH -> Locale.ENGLISH
            Consts.LANGUAGE_FRENCH -> Locale.FRENCH
            Consts.LANGUAGE_JAPANESE -> Locale.JAPANESE
            Consts.LANGUAGE_GERMANY -> Locale.GERMANY
            Consts.LANGUAGE_KOREAN -> Locale.KOREAN
            else -> Locale.CHINESE
        }
    }

    fun destroyTTS() {
        if (localTTS != null) {
            localTTS!!.shutdown()
        }
        if (internetTTSThread != null) {
            internetTTSThread!!.destroyTTS()
        }
    }
}