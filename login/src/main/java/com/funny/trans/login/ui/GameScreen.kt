package com.funny.trans.login.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.trans.login.bean.GameStatus
import com.funny.trans.login.bean.IGameListener
import com.funny.trans.login.bean.MemoryNumberGame

/**
 * @author  FunnySaltyFish
 * @date    2022/8/2 14:19
 */

@Composable
fun SquareLayout(modifier: Modifier, content: @Composable () -> Unit) {
    Layout(modifier = modifier, content = content) { measureables, constraints ->
        check(measureables.size == 1) { "this layout must have only a child!" }
        val width = Math.min(constraints.maxWidth, constraints.maxHeight)
        val childConstrains = Constraints.fixed(width, width)
        val placeable = measureables.map { it.measure(childConstrains) }
        layout(width, width) { placeable.forEach { it.placeRelative(0, 0) } }
    }
}

@Composable
fun GameScreen(modifier: Modifier) {
    val leftGame by remember {
        mutableStateOf(MemoryNumberGame())
    }

    Row(
        modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemoryNumberGameContainer(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .weight(0.49f), game = leftGame
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width((1f).dp)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(0.12f),
                    RoundedCornerShape(2.dp)
                )
        )
        InputContainer(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.49f)
        )
    }
}

@Composable
fun InputContainer(modifier: Modifier) {
    val vm: GameViewModel = viewModel()

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        InputByOtherTextField(
            onFocused = { vm.what = GameViewModel.WHAT_PASSWORD },
            valueProvider = { vm.password },
            errorProvider = { vm.password.isNotEmpty() && vm.isPwdError },
            labelText = "密码",
            placeholderText = "长度8-16位，包含大小写字母和数字"
        )
        Spacer(modifier = Modifier.height(12.dp))
        InputByOtherTextField(
            onFocused = { vm.what = GameViewModel.WHAT_REPEAT_PASSWORD },
            valueProvider = { vm.repeatPassword },
            errorProvider = { vm.repeatPassword.isNotEmpty() && vm.isRepeatPwdError },
            labelText = "确认密码",
            placeholderText = "请再次输入密码"
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "请通过左侧游戏输入内容",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp
        )
    }
}

@Composable
fun InputByOtherTextField(
    onFocused: () -> Unit,
    valueProvider: () -> String,
    errorProvider: () -> Boolean,
    labelText: String,
    placeholderText: String,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val focused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(focused) {
        if (focused) {
            onFocused()
        }
    }
    OutlinedTextField(
        value = valueProvider(),
        onValueChange = {},
        label = { Text(labelText) },
        isError = errorProvider(),
        placeholder = { Text(placeholderText) },
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.Password
        ),
        readOnly = true
    )
}

@Composable
fun MemoryNumberGameContainer(modifier: Modifier, game: MemoryNumberGame) {
    val vm: GameViewModel = viewModel()
    val density = LocalDensity.current
    val blockHeight by remember {
        derivedStateOf { game.height / game.cols }
    }
    val blockHeightDp = remember(blockHeight) {
        with(density) {
            blockHeight.toDp()
        }
    }
    val TAG = "MemoryNumberGame"
    var lastTime = 0L
    var i = 0
    LaunchedEffect(key1 = game) {
        game.init()
        game.gameListener = object : IGameListener {
            override fun onInput(char: Char) {
                vm.dispatchInput(char)
            }

            override fun onFail() {
                vm.password = ""
                vm.repeatPassword = ""
                vm.what = GameViewModel.WHAT_PASSWORD
            }

            override fun onDelete() {
                vm.dispatchDelete()
            }
        }
        while (true) {
            withFrameMillis {
                if ((i++) % 20 != 0) return@withFrameMillis
                if (lastTime != 0L) game.update((it - lastTime).toInt())
                lastTime = it
            }
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(modifier = Modifier.fillMaxWidth(), text = game.tipText, textAlign = TextAlign.End)
        when (game.status) {
            is GameStatus.Waiting -> Button(onClick = { game.restart() }) { Text(text = "开始游戏") }
            is GameStatus.Playing -> SquareLayout(
                Modifier
                    .fillMaxSize(0.8f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .onSizeChanged { size ->
                        game.width = size.width
                        game.height = size.height
                    }) {
                Box(modifier = Modifier.fillMaxSize()) {
                    game.blocks.forEach { block ->
                        Box(modifier = Modifier
                            .offset {
                                IntOffset(
                                    blockHeight * block.place.first,
                                    blockHeight * block.place.second
                                )
                            }
                            .size(blockHeightDp)
                            .background(Color.Green)
                            .clickable {
                                game.clickBlock(block)
                            }) boxBlock@{
                            Crossfade(
                                modifier = Modifier.align(Alignment.Center),
                                targetState = block.isShow
                            ) {
                                if (it) {
                                    if (block is MemoryNumberGame.Block.NumberBlock) {
                                        Text(
                                            text = block.number.toString(),
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else if (block is MemoryNumberGame.Block.CharacterBlock) {
                                        Text(
                                            text = block.char.toString(),
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
            is GameStatus.Fail -> Column(modifier = modifier) {
                Text(
                    text = "糟糕！失败了~",
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { game.restart() }) {
                    Text(text = "点我重试")
                }
            }
        }
    }
}