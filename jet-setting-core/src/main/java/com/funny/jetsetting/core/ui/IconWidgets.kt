package com.funny.jetsetting.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    tintColor : Color = MaterialTheme.colorScheme.secondary,
    contentDescription: String? = null,
) {
    val icon = funnyIcon.get()
    if (icon is ImageVector){
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tintColor, modifier = modifier.size(24.dp))
    }else if(icon is Int){
        Icon(painter = painterResource(id = icon), contentDescription = contentDescription, tint = tintColor, modifier = modifier.size(24.dp))
    }

}