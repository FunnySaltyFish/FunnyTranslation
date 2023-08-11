package com.funny.translation.translate.ui.ai.componets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.funny.translation.translate.R

@Composable
fun ChatInputTextField(
    modifier: Modifier,
    input: String,
    onValueChange: (String) -> Unit,
    sendAction: () -> Unit,
) {
    TextField(
        value = input,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(stringResource(id = R.string.chat_input_hint)) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        trailingIcon = {
            val clearBtnVisible by remember(input) {
                derivedStateOf {
                    input != ""
                }
            }
            AnimatedContent(
                targetState = clearBtnVisible,
                transitionSpec = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) togetherWith
                            fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                },
                label = ""
            ) { visible ->
                Row {
                    if (visible) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(id = R.string.clear_content)
                            )
                        }
                        IconButton(onClick = sendAction) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = stringResource(id = R.string.send)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_mic),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp)
                        )
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_image),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    )
}