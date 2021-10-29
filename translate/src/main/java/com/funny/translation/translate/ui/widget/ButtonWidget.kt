package com.funny.translation.translate.ui.widget

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.translate.R

@Composable
fun RoundCornerButton(
    text : String,
    modifier: Modifier = Modifier,
    background : Color = MaterialTheme.colors.secondary,
    onClick : ()->Unit = {},
    extraContent : @Composable ()->Unit
) {
    Button(onClick = onClick, shape = CircleShape, modifier=modifier, colors = buttonColors(backgroundColor = background), contentPadding = PaddingValues(horizontal = 36.dp,vertical = 12.dp)) {
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
    val background by animateColorAsState(targetValue = if(selected) MaterialTheme.colors.secondary else MaterialTheme.colors.surface)
    val textColor by animateColorAsState(targetValue = if(selected) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSurface.copy(0.5f))

    //val border = if (selected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, textColor)
    Button(
        onClick = {
            onClick()
            selected = !selected
        },
        shape = CircleShape,
        modifier = Modifier,
        colors = buttonColors(contentColor = textColor, backgroundColor = background),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = textColor, fontSize = 12.sp)
    }
}

@Composable
fun ExchangeButton(
    onClick: () -> Unit = {}
) {
    //var clickOnce = true
    //val rotateValue by animateFloatAsState(targetValue = if(clickOnce) 0f else 180f)
    IconButton(
        onClick = {
            onClick()
            //clickOnce=!clickOnce
        },
        Modifier
            .background(
                MaterialTheme.colors.surface,
                CircleShape
            )
    ) {
        Icon(
            painterResource(id = R.drawable.ic_exchange), tint = Color.White,
            contentDescription = stringResource(R.string.exchange),
        )
    }
}

@Composable
fun ExpandMoreButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var expand by remember {
        mutableStateOf(false)
    }
    val rotationValue by animateFloatAsState(targetValue = if (expand) -180f else 0f)
    IconButton(onClick = {
        expand = !expand
        onClick()
    }, modifier = modifier) {
        Icon(
            Icons.Default.ArrowDropDown,
            stringResource(id = R.string.expand),
            modifier = Modifier.graphicsLayer {
                rotationX = rotationValue
            }
        )
    }
}