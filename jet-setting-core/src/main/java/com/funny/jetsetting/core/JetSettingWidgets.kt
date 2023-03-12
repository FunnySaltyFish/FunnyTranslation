package com.funny.jetsetting.core

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.jetsetting.core.ui.FunnyIcon
import com.funny.jetsetting.core.ui.IconWidget
import com.funny.jetsetting.core.ui.throttleClick

private val DefaultJetSettingModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 24.dp, vertical = 12.dp)

private val EmptyAction = {}

@Composable
fun JetSettingCheckbox(
    modifier: Modifier = DefaultJetSettingModifier,
    state: MutableState<Boolean>,
    imageVector: ImageVector? = null,
    resourceId: Int? = null,
    iconTintColor: Color = MaterialTheme.colorScheme.onBackground,
    text: String,
    description: String? = null,
    interceptor: () -> Boolean = { true },
    onCheck: (Boolean) -> Unit
) {
    ConstraintLayout(modifier) {
        val (icon, textColumn, checkbox) = createRefs()

        val funnyIcon = FunnyIcon(imageVector, resourceId)
        funnyIcon.get()?.let {
            IconWidget(Modifier.constrainAs(icon){
                start.linkTo(parent.start)
                centerVerticallyTo(parent)
            }, funnyIcon, tintColor = iconTintColor)
        }

        Checkbox(checked = state.value, onCheckedChange = {
            if (!interceptor()) return@Checkbox
            state.value = it
            onCheck(it)
        }, modifier = Modifier.constrainAs(checkbox){
            end.linkTo(parent.end)
            centerVerticallyTo(parent)
        })

        Column(modifier = Modifier.constrainAs(textColumn){
            start.linkTo(icon.end, margin = 20.dp)
            end.linkTo(checkbox.start)
            centerVerticallyTo(parent)
            width = Dimension.preferredWrapContent
        }, horizontalAlignment = Alignment.Start) {
            Text(text, fontSize = 24.sp, fontWeight = FontWeight.W700, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
            description?.let{
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.background).copy(0.65f),
                )
            }
        }
    }
}

@Composable
fun JetSettingCheckbox(
    modifier: Modifier = DefaultJetSettingModifier,
    key: String,
    default: Boolean = false,
    imageVector: ImageVector? = null,
    resourceId: Int? = null,
    iconTintColor: Color = MaterialTheme.colorScheme.onBackground,
    text: String,
    description: String? = null,
    interceptor: () -> Boolean = { true },
    onCheck: (Boolean) -> Unit
) {
    val state = rememberDataSaverState(key = key, default = default)
    JetSettingCheckbox(state = state, modifier = modifier, imageVector = imageVector, resourceId = resourceId, iconTintColor = iconTintColor, text = text, description = description, onCheck = onCheck, interceptor = interceptor)
}

@Composable
fun JetSettingTile(
    modifier: Modifier = DefaultJetSettingModifier,
    imageVector: ImageVector? = null,
    resourceId: Int? = null,
    iconTintColor: Color = MaterialTheme.colorScheme.onBackground,
    text: String,
    interceptor: () -> Boolean = { true },
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .throttleClick(interactionSource = remember {
                MutableInteractionSource()
            }, indication = LocalIndication.current) {
                if (interceptor()) onClick()
            }
            .then(modifier),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val funnyIcon = FunnyIcon(imageVector, resourceId)
        funnyIcon.get()?.let {
            IconWidget(funnyIcon = funnyIcon, tintColor = iconTintColor)
            Spacer(modifier = Modifier.width(20.dp))
        }
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.W700, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Click to jump")
    }
}


@Composable
fun JetSettingDialog(
    modifier: Modifier = DefaultJetSettingModifier,
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

