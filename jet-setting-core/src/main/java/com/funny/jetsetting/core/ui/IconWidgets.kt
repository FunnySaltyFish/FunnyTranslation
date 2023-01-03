package com.funny.jetsetting.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class FunnyIcon(
    private val imageVector: ImageVector?= null,
    private val resourceId : Int? = null
){
    fun get() = imageVector ?: resourceId
}

@Composable
fun IconWidget(
    modifier: Modifier = Modifier,
    funnyIcon : FunnyIcon,
    tintColor : Color = MaterialTheme.colorScheme.secondary
) {
    val icon = funnyIcon.get()
    Box(modifier = modifier.size(64.dp).clip(CircleShape).background(tintColor.copy(alpha = 0.3f))){
        if (icon is ImageVector){
            Icon(imageVector = icon, contentDescription = "", tint = tintColor, modifier = Modifier.size(32.dp).align(Alignment.Center))
        }else if(icon is Int){
            Icon(painter = painterResource(id = icon), contentDescription = "", tint = tintColor, modifier = Modifier.size(24.dp).align(Alignment.Center))
        }
    }

}