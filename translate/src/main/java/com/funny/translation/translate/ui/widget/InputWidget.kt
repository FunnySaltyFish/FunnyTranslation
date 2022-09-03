package com.funny.translation.translate.ui.widget

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.Consts
import com.funny.translation.translate.R

private const val TAG = "InputWidget"

@ExperimentalComposeUiApi
@Composable
fun InputText(
    modifier: Modifier,
    text: String,
    updateText: (String) -> Unit,
    translateAction: (() -> Unit)? = null
) {
    val enterToTranslate by rememberDataSaverState(Consts.KEY_ENTER_TO_TRANSLATE, default = true)

    BasicTextField(
        modifier = modifier.padding(8.dp),
        value = text,
        onValueChange = updateText,
        maxLines = 6,
        decorationBox = { innerTextField ->
            if (text == "") Text(text = "译你所译……", color = LocalContentColor.current.copy(0.8f))
            innerTextField()
        },
        keyboardActions = KeyboardActions(onDone = {
            if (enterToTranslate) translateAction?.invoke()
        }),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp),
        keyboardOptions = if (enterToTranslate) KeyboardOptions(imeAction = ImeAction.Done) else KeyboardOptions.Default
    )
}