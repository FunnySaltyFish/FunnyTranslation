package com.funny.translation.translate.ui.widget

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.editableText
import androidx.compose.ui.semantics.requestFocus
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.translate.R

private const val TAG = "InputWidget"
@ExperimentalComposeUiApi
@Composable
fun InputText(
    text : String,
    updateText : (String)->Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember {
        FocusRequester()
    }
    BasicTextField(
        value = text,
        onValueChange = updateText,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(16.dp)),
        textStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.colors.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
        maxLines = 6,
        decorationBox = { innerTextField ->
            Row{
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp)
                        .align(Alignment.Top)
                        .animateContentSize()
                ) {
                    innerTextField()
                }
                //Box(modifier = Modifier.align(Alignment.Vertical.Bottom))
                Box(modifier = Modifier.align(Alignment.Bottom).wrapContentSize()) {
                    IconButton(onClick = {
                        updateText("")
                        focusRequester.requestFocus()
                        keyboard?.show()
                        Log.d(TAG, "InputText: 手动展示软键盘")
                    }) {
                        Icon(Icons.Default.Delete, stringResource(id = R.string.clear_content), tint = MaterialTheme.colors.onSurface)
                    }
                }
            }
        }
    )
}