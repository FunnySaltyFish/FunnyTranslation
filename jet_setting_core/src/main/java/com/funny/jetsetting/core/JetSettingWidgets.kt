package com.funny.jetsetting.core

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.LocalDataSaver
import com.funny.jetsetting.core.ui.FunnyIcon
import com.funny.jetsetting.core.ui.IconWidget


@Composable
fun JetSettingCheckbox(
    key : String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 0.dp, vertical = 8.dp),
    imageVector: ImageVector? = null,
    resourceId : Int? = null,
    iconTintColor : Color = MaterialTheme.colors.onBackground,
    text : String,
    default : Boolean = false,
    onCheck : (Boolean) -> Unit
) {
    val dataSaveInterface = LocalDataSaver.current
    var checked by remember {
        mutableStateOf(dataSaveInterface.readData(key, default))
    }
    Row(modifier, horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
        val funnyIcon = FunnyIcon(imageVector, resourceId)
        funnyIcon.get()?.let {
            IconWidget(funnyIcon, tintColor = iconTintColor)
            Spacer(modifier = Modifier.width(24.dp))
        }
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.W700, modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = {
            checked =  it
            dataSaveInterface.saveData(key, it)
            onCheck(it)
        })
    }
}