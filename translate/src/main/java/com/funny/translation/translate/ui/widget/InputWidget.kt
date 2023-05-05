package com.funny.translation.translate.ui.widget

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doOnTextChanged
import com.funny.translation.AppConfig
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import kotlin.math.roundToInt

private const val TAG = "InputWidget"

@ExperimentalComposeUiApi
@Composable
fun InputText(
    modifier: Modifier,
    textProvider: () -> String,
    updateText: (String) -> Unit,
    shouldRequest: Boolean,
    updateFocusRequest: (Boolean) -> Unit,
    translateAction: (() -> Unit)? = null
) {
    val enterToTranslate by AppConfig.sEnterToTranslate
    // 因为 Compose 的 BasicTextField 下某些输入法的部分功能不能用，所以临时改回 EditText
    val textColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
    val inputMethodManager = FunnyApplication.ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val density = LocalDensity.current.density
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    DisposableEffect(key1 = Unit){
        onDispose {
            softwareKeyboardController?.hide()
        }
    }
    AndroidView(factory = {
        EditText(it).apply {
            doOnTextChanged { text, start, before, count -> updateText(text.toString()) }
//            maxLines = 6
            hint = context.getString(R.string.trans_text_input_hint)
            background = null
            textSize = 20f
            gravity = Gravity.TOP
            setPaddingRelative((20 * density).roundToInt(), (4 * density).roundToInt(), (20 * density).roundToInt(), (4 * density).roundToInt())
            setTextColor(textColor)

            if (enterToTranslate){
                imeOptions = EditorInfo.IME_ACTION_DONE
                inputType = EditorInfo.TYPE_CLASS_TEXT
                setImeActionLabel(context.getString(R.string.translate), EditorInfo.IME_ACTION_DONE)
                setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        translateAction?.invoke()
                        false
                    }else {
                        true
                    }
                }
            }

             setOnFocusChangeListener { v, hasFocus -> updateFocusRequest(hasFocus) }
        }
    }, update = {
        val text = textProvider()
        if (it.text.toString() != text) {
            it.setText(text)
            // 光标至于末尾
            it.setSelection(text.length)
        }
        if (shouldRequest && !it.isFocused){
            it.requestFocus().also { Log.d(TAG, "InputText: requestFocus") }
            inputMethodManager.showSoftInput(it, 0)
        }
        else if(!shouldRequest && it.isFocused) {
            it.clearFocus().also { Log.d(TAG, "InputText: clearFocus") }
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }, modifier = modifier)
//    BasicTextField(
//        modifier = modifier.padding(8.dp),
//        value = text,
//        onValueChange = updateText,
//        maxLines = 6,
//        decorationBox = { innerTextField ->
//            if (text == "") Text(text = "译你所译……", color = LocalContentColor.current.copy(0.8f))
//            innerTextField()
//        },
//        keyboardActions = KeyboardActions(onDone = {
//            if (enterToTranslate) translateAction?.invoke()
//        }),
//        textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp),
//        keyboardOptions = if (enterToTranslate) KeyboardOptions(imeAction = ImeAction.Done) else KeyboardOptions.Default
//    )
}