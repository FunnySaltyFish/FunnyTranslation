package com.funny.translation.translate.ui.widget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.funny.translation.translate.R


@Composable
fun RoundCornerButton(
    text : String,
    onClick : ()->Unit = {},
    background : Color = MaterialTheme.colors.secondary,
    modifier: Modifier = Modifier
) {
    Button(onClick = onClick, shape = CircleShape, modifier=modifier, colors = buttonColors(backgroundColor = background), contentPadding = PaddingValues(horizontal = 36.dp,vertical = 12.dp)) {
        Text(text = text, color = Color.White)
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