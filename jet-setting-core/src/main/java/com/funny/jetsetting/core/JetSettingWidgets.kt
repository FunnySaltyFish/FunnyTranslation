package com.funny.jetsetting.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.jetsetting.core.ui.FunnyIcon
import com.funny.jetsetting.core.ui.IconWidget
import com.funny.jetsetting.core.ui.SettingBaseItem

private val DefaultJetSettingModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 24.dp, vertical = 12.dp)

private val EmptyAction = {}

@Composable
fun JetSettingSwitch(
    modifier: Modifier = Modifier,
    state: MutableState<Boolean>,
    imageVector: ImageVector? = null,
    resourceId: Int? = null,
    text: String,
    description: String? = null,
    interceptor: () -> Boolean = { true },
    onCheck: (Boolean) -> Unit
) {
    SettingBaseItem(
        modifier = modifier,
        icon = {
            val icon = FunnyIcon(imageVector, resourceId)
            IconWidget(funnyIcon = icon, tintColor = MaterialTheme.colorScheme.onSurface)
        },
        title = {
            Text(text = text)
        },
        text = {
            description?.let {
                Text(text = it)
            }
        },
        action = {
            Switch(checked = state.value, onCheckedChange = {
                if (interceptor.invoke()) {
                    state.value = it
                    onCheck(it)
                }
            })
        },
        onClick = {
            state.value = !state.value
        }
    )
}

@Composable
fun JetSettingSwitch(
    modifier: Modifier = Modifier,
    key: String,
    default: Boolean = false,
    imageVector: ImageVector? = null,
    resourceId: Int? = null,
    text: String,
    description: String? = null,
    interceptor: () -> Boolean = { true },
    onCheck: (Boolean) -> Unit
) {
    val state = rememberDataSaverState(key = key, default = default)
    JetSettingSwitch(
        modifier = modifier,
        state = state,
        imageVector = imageVector,
        resourceId = resourceId,
        text = text,
        description = description,
        interceptor = interceptor,
        onCheck = onCheck
    )
}

@Composable
fun JetSettingTile(
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    resourceId: Int? = null,
    text: String,
    interceptor: () -> Boolean = { true },
    onClick: () -> Unit
) {
    SettingBaseItem(
        modifier = Modifier.padding(vertical = 4.dp),
        title = {
            Text(text = text)
        },
        action = {
           Icon(Icons.Default.KeyboardArrowRight, "Goto", )
        },
        icon = {
            val icon = FunnyIcon(imageVector, resourceId)
            IconWidget(funnyIcon = icon, tintColor = MaterialTheme.colorScheme.onSurface)
        },
        onClick = {
            if (interceptor.invoke()) {
                onClick()
            }
        }
    )
}


@Composable
fun JetSettingDialog(
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    resourceId: Int? = null,
    iconTintColor: Color = MaterialTheme.colorScheme.onBackground,
    text: String,
    dialogTitle: String = stringResource(id = R.string.hint),
    confirmButtonAction: () -> Unit? = EmptyAction,
    confirmButtonText: String = "确认",
    dismissButtonAction: () -> Unit? = EmptyAction,
    dismissButtonText: String = "取消",
    dialogContent: @Composable () -> Unit
) {
    var openDialogState by remember {
        mutableStateOf(false)
    }
    if (openDialogState) {
        AlertDialog(
            title = {
                Text(text = dialogTitle)
            },
            text = dialogContent,
            onDismissRequest = { openDialogState = false },
            confirmButton = {
                if (confirmButtonAction != EmptyAction)
                    Button(
                        onClick = {
                            openDialogState = false
                            confirmButtonAction()
                        }) {
                        Text(confirmButtonText)
                    }
            },
            dismissButton = {
                if (dismissButtonText.isNotEmpty())
                    Button(
                        onClick = {
                            openDialogState = false
                            dismissButtonAction()
                        }) {
                        Text(dismissButtonText)
                    }
            }
        )
    }

    Row(
        modifier.clickable { openDialogState = true },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val funnyIcon = FunnyIcon(imageVector, resourceId)
        funnyIcon.get()?.let {
            IconWidget(funnyIcon = funnyIcon, tintColor = iconTintColor)
            Spacer(modifier = Modifier.width(24.dp))
        }
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.W700, modifier = Modifier.weight(1f))
    }
}

