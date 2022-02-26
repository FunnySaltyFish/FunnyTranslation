package com.funny.translation.translate.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import com.funny.translation.codeeditor.CodeEditorActivity
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.trans.Language
import com.funny.translation.trans.allLanguages
import com.funny.translation.trans.findLanguageById
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.WebViewActivity
import com.funny.translation.translate.bean.AppConfig
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.engine.TranslationEngines
import com.yhao.floatwindow.FloatWindow
import com.yhao.floatwindow.IFloatWindow
import com.yhao.floatwindow.MoveType
import com.yhao.floatwindow.PermissionListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

data class TranslationConfig(val sourceString:String, val sourceLanguage: Language, val targetLanguage: Language)

object FloatWindowUtils {
    const val TAG = "FloatWindowUtils"
    var translateConfigFlow = MutableStateFlow(TranslationConfig("",Language.AUTO,Language.CHINESE))
    var translateJob : Job? = null

    fun initScreenSize(activity: Activity){
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        AppConfig.SCREEN_WIDTH = displayMetrics.widthPixels
        AppConfig.SCREEN_HEIGHT = displayMetrics.heightPixels
    }

    fun initFloatingWindow(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.layout_float_window, null)

        var editable = false
        val edittext = view.findViewById<EditText>(R.id.float_window_input)

        val spinnerSource : Spinner = view.findViewById<Spinner?>(R.id.float_window_spinner_source).apply {
            adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                addAll(allLanguages.map { it.displayText })
            }
            setSelection(Language.AUTO.id)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    translateConfigFlow.value = translateConfigFlow.value.copy(
                        sourceString = edittext.text.trim().toString(),
                        sourceLanguage = findLanguageById(position))
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }

        val spinnerTarget : Spinner = view.findViewById<Spinner?>(R.id.float_window_spinner_target).apply {
            adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item).apply {
                addAll(allLanguages.map { it.displayText })
            }
            setSelection(Language.CHINESE.id)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    translateConfigFlow.value = translateConfigFlow.value.copy(
                        sourceString = edittext.text.trim().toString(),
                        targetLanguage = findLanguageById(position))
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }

        val rotateAnimation = RotateAnimation(0f,360f,
            Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f).apply {
            duration = 300
        }

        view.findViewById<ImageButton?>(R.id.float_window_exchange_button).apply {
            setOnClickListener {
                val temp = spinnerSource.selectedItemPosition
                spinnerSource.setSelection(spinnerTarget.selectedItemPosition, true)
                spinnerTarget.setSelection(temp, true)
                startAnimation(rotateAnimation)
            }
        }

        val resultText : TextView = view.findViewById(R.id.float_window_text)

        view.findViewById<ImageButton?>(R.id.float_window_edit_button).apply {
            setOnClickListener {
                if (editable){
                    FloatWindow.get().setUneditable()
                    setColorFilter(Color.BLACK)
                }else{
                    FloatWindow.get().setEditable()
                    setColorFilter(Color.WHITE)
                }
                editable = !editable
            }
        }

        translateJob = GlobalScope.launch(Dispatchers.IO) {
            translateConfigFlow.collect {
                kotlin.runCatching {
                    if (it.sourceString.isNotBlank()){
                        val sourceLanguage = findLanguageById(spinnerSource.selectedItemPosition)
                        val targetLanguage = findLanguageById(spinnerTarget.selectedItemPosition)
                        val task = TranslateUtils.createTask(TranslationEngines.BaiduNormal, it.sourceString, sourceLanguage, targetLanguage)
                        withContext(Dispatchers.Main){
                            resultText.text = "正在翻译……"
                        }
                        task.translate()
                        withContext(Dispatchers.Main){
                            resultText.text = task.result.basicResult.trans
                        }
                    }
                }.onFailure {
                    withContext(Dispatchers.Main){
                        resultText.text = "翻译失败！${it.localizedMessage}"
                    }
                }
            }
        }

        view.findViewById<Button?>(R.id.float_window_translate).apply {
            setOnLongClickListener {
                val clipboardText = ClipBoardUtil.get(context).trim()
                Log.d(TAG, "clipboardText: $clipboardText")
                if(clipboardText != ""){
                    edittext.setText(clipboardText)
                    translateConfigFlow.value = translateConfigFlow.value.copy(sourceString = clipboardText)
                }
                true
            }
            setOnClickListener {
                val inputText = edittext.text.trim()
                if(inputText.isNotEmpty()){
                    translateConfigFlow.value = translateConfigFlow.value.copy(sourceString = inputText.toString())
                }
            }
        }

        FloatWindow
            .with(FunnyApplication.ctx)
            .setPermissionListener(object : PermissionListener {
                override fun onSuccess() {

                }

                override fun onFail() {
                    AppConfig.INIT_FLOATING_WINDOW = false
                    Toast.makeText(context, "悬浮窗权限授予失败，悬浮窗无法使用",Toast.LENGTH_SHORT).show()
                }
            })
            .setView(view)
            .setWidth(AppConfig.SCREEN_WIDTH * 9 / 10)
            .setHeight(400)
            .setX(AppConfig.SCREEN_WIDTH * 1 / 20)
            .setY(100)
            .setDesktopShow(true)
            .setFilter(false, WebViewActivity::class.java, CodeEditorActivity::class.java)

            .setMoveType(MoveType.active)
            .build()

        AppConfig.INIT_FLOATING_WINDOW = true
    }

    fun showFloatWindow(){
        if (AppConfig.INIT_FLOATING_WINDOW) {
            FloatWindow.get().apply {
                show()
                updateFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                )
            }
        }
    }

    private fun IFloatWindow.setEditable(){
        if (AppConfig.INIT_FLOATING_WINDOW) {
            updateFlags(WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR)
        }
    }

    private fun IFloatWindow.setUneditable(){
        if (AppConfig.INIT_FLOATING_WINDOW) {
            updateFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
        }
    }

    fun hideFloatWindow(){
        if (AppConfig.INIT_FLOATING_WINDOW){
            FloatWindow.get().apply {
                hide()
                updateFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            }
        }
    }

    fun destroyFloatWindow(){
        if (AppConfig.INIT_FLOATING_WINDOW){
            FloatWindow.destroy()
            DataSaverUtils.saveData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
            translateJob?.cancel()
        }
    }

    @Composable
    fun TransFloatWindow() {
        var text by remember {
            mutableStateOf("")
        }
        Column {
            OutlinedTextField(value = text, onValueChange = {
                text = it
            })
        }
    }
}