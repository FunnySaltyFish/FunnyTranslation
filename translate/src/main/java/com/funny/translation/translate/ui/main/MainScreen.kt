package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.trans.Translation
import com.funny.translation.trans.TranslationResult
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.bean.RoundCornerConfig
import com.funny.translation.translate.ui.widget.ExchangeButton
import com.funny.translation.translate.ui.widget.ExpandMoreButton
import com.funny.translation.translate.ui.widget.InputText
import com.funny.translation.translate.ui.widget.RoundCornerButton

private const val TAG = "MainScreen"

@ExperimentalAnimationApi
@Composable
//@Preview
fun MainScreen() {
    val vm : MainViewModel = viewModel()
    val transText by vm.translateText.collectAsState()
    val sourceLanguage by vm.sourceLanguage.collectAsState()
    val targetLanguage by vm.targetLanguage.collectAsState()
    Column(
        modifier = Modifier
            .padding(16.dp, 12.dp)
            .fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RoundCornerButton(text = sourceLanguage.displayText)
            ExchangeButton {
                Log.d(TAG, "MainScreen: clicked")
            }
            RoundCornerButton(text = targetLanguage.displayText)
        }
        Spacer(modifier = Modifier.height(12.dp))
        InputText(text = transText, updateText = { vm.translateText.value = it })
        Spacer(modifier = Modifier.height(18.dp))
        TranslationList()
    }

}

@Composable
fun PreviewTransItem(
    roundCornerConfig: RoundCornerConfig
) {
    TranslationItem(
        result = TranslationResult(
            0,
            Translation("哈哈阿萨德很骄傲大结局氨基酸看大家艾克"),
            "源语言",
            details = arrayListOf()
        ),
        roundCornerConfig = roundCornerConfig
    )
}

@Preview
@Composable
fun TranslationList() {
//    Box(modifier = Modifier
//        .fillMaxWidth()
//        .background(color = MaterialTheme.colors.primary, shape = RoundedCornerShape(8.dp))
//        .padding(12.dp)){
//
//    }
    LazyColumn {
        item {
            PreviewTransItem(RoundCornerConfig.Top)
        }
        item {
            Spacer(modifier = Modifier.height(2.dp))
        }
        item {
            PreviewTransItem(RoundCornerConfig.Bottom)
        }
    }
}

@Composable
fun MainAppbar() {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.app_name))
        }
    )
}


@Composable
fun TranslationItem(
    result: TranslationResult,
    roundCornerConfig: RoundCornerConfig
) {
    val cornerSize = 16.dp
    val shape = when (roundCornerConfig) {
        is RoundCornerConfig.Top -> RoundedCornerShape(topStart = cornerSize, topEnd = cornerSize)
        is RoundCornerConfig.Bottom -> RoundedCornerShape(
            bottomEnd = cornerSize,
            bottomStart = cornerSize
        )
        is RoundCornerConfig.All -> RoundedCornerShape(cornerSize)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface, shape = shape)
            .padding(12.dp)
            .animateContentSize()

    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "百度翻译", color = MaterialColors.Grey600, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.basicResult.trans,
                color = MaterialTheme.colors.secondary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { /*TODO*/ }, modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.secondary)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_copy_content),
                        contentDescription = stringResource(id = R.string.copy_content),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { /*TODO*/ }, modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.secondary)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_speak),
                        contentDescription = stringResource(id = R.string.speak),
                        tint = Color.White
                    )
                }
                result.details?.let {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        ExpandMoreButton {

                        }
                    }

                }
            }
        }
    }
}

