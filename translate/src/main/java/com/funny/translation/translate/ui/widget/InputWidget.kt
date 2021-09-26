package com.funny.translation.translate.ui.widget

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.translate.R

@Composable
fun InputText(
    text : String,
    updateText : (String)->Unit
) {
    BasicTextField(
        value = text,
        onValueChange = updateText,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(16.dp)),
        textStyle = TextStyle(fontSize = 18.sp),
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
                Box(modifier = Modifier.align(Alignment.Bottom)) {
                    IconButton(onClick = { updateText("") }) {
                        Icon(Icons.Default.Delete, stringResource(id = R.string.clear_content), tint = MaterialTheme.colors.secondary)
                    }
                }
            }
        }
    )
}