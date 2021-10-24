package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funny.translation.translate.R

@Composable
fun ComingSoon() {
    var supportDialog by remember {
        mutableStateOf(false)
    }
    Surface {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(50.dp)
        ) {
            LottieView(R.raw.working, modifier = Modifier.height(360.dp))
            Text(
                text = stringResource(id = R.string.comming_soon),
                style = typography.h5,
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "软件仍在建设中\n点击此处给勤劳的开发者点个支持吧",
                style = typography.subtitle2,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        supportDialog = true
                    }
            )
        }
    }
    if(supportDialog){
        AlertDialog(
            onDismissRequest = {  },
            title = {
                Text("赞助")
            },
            text = {
                Image(
                    painter = painterResource(id = R.drawable.sponser),
                    stringResource(id = R.string.sponser),
                    modifier = Modifier.fillMaxWidth(),
                    alignment = Alignment.Center,
                )
            },
            buttons = {
                Button(onClick = { supportDialog = false }, modifier = Modifier.fillMaxWidth(), colors = buttonColors(backgroundColor = MaterialTheme.colors.surface)) {
                    Text("关闭", color = MaterialTheme.colors.onSurface)
                }
            }
        )
    }
}