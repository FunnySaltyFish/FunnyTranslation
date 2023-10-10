package com.funny.translation.translate.ui.widget

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.translate.R
import com.funny.translation.ui.FixedSizeIcon

@Composable
fun RoundCornerButton(
    text : String,
    modifier: Modifier = Modifier,
    background : Color = MaterialTheme.colorScheme.secondary,
    onClick : ()->Unit = {},
    extraContent : @Composable ()->Unit
) {
    Button(onClick = onClick, shape = CircleShape, modifier=modifier, colors = buttonColors(containerColor = background), contentPadding = PaddingValues(horizontal = 36.dp,vertical = 12.dp)) {
        Text(text = text, color = Color.White)
        extraContent()
    }
}

@ExperimentalAnimationApi
@Composable
fun SelectableChip(
    initialSelect : Boolean = false,
    text: String = "",
    onClick: () -> Unit
) {
    var selected by remember {
        mutableStateOf(initialSelect)
    }
    val background by animateColorAsState(targetValue = if(selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface)
    val textColor by animateColorAsState(targetValue = if(selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface.copy(0.5f))

    //val border = if (selected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, textColor)
    Button(
        onClick = {
            onClick()
            selected = !selected
        },
        shape = CircleShape,
        modifier = Modifier.selectable(selected, true, null, {}),
        colors = buttonColors(contentColor = textColor, containerColor = background),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = textColor, fontSize = 12.sp)
    }
}

@Composable
fun ExchangeButton(
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: () -> Unit = {},
) {
    var clickOnce by remember {
        mutableStateOf(false)
    }
    val rotateValue by animateFloatAsState(targetValue = if(clickOnce) 0f else 180f)
    IconButton(
        onClick = {
            clickOnce = !clickOnce
            onClick()
        },
        Modifier
            .background(
                Color.Transparent,
                CircleShape
            ).graphicsLayer {
                rotationZ = rotateValue
            }
    ) {
        FixedSizeIcon(
            painterResource(id = R.drawable.ic_exchange), tint = tint,
            contentDescription = stringResource(R.string.exchange),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ExpandMoreButton(
    modifier: Modifier = Modifier,
    expand: Boolean,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: (Boolean) -> Unit
) {
    val rotationValue by animateFloatAsState(
        targetValue = if (expand) -180f else 0f,
        animationSpec = tween(700)
    )
    IconButton(onClick = {
        onClick(!expand)
    }, modifier = modifier) {
        FixedSizeIcon(
            Icons.Default.ArrowDropDown,
            stringResource(id = R.string.expand),
            modifier = Modifier.graphicsLayer {
                rotationX = rotationValue
            },
            tint = tint
        )
    }
}