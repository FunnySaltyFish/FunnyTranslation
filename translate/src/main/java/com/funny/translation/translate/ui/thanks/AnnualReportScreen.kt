package com.funny.translation.translate.ui.thanks

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.loading.LoadingContent
import com.funny.compose.loading.LoadingState
import com.funny.translation.AppConfig
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.widget.AutoFadeInComposableColumn
import com.funny.translation.translate.ui.widget.AutoIncreaseAnimatedNumber
import com.funny.translation.translate.ui.widget.FadeInColumnScope
import com.funny.translation.translate.ui.widget.rememberAutoFadeInColumnState
import com.funny.translation.ui.animatedGradientBackground
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.DurationUnit

private const val TAG = "AnnualReportScreen"

@Composable
fun AnnualReportScreen() {
    val vm : AnnualReportViewModel = viewModel()
    val systemUiController = rememberSystemUiController()

    DisposableEffect(key1 = systemUiController){
        systemUiController.isNavigationBarVisible = false
        onDispose {
            systemUiController.isNavigationBarVisible = !AppConfig.sHideBottomNavBar.value
        }
    }

    LoadingContent(
        loader = vm::loadAnnualReport,
        initialValue = vm.loadingState,
        retry = { vm.loadingState = LoadingState.Loading },
        failure = { err, _ ->
            AutoFadeInComposableColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .animatedGradientBackground(MaterialColors.DeepPurple800, Color.Black)
            ) {
                LabelText(
                    text = stringResource(R.string.no_annual_report),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                        .padding(24.dp)
                )
            }
        }
    ) {
        AnnualReport(vm = vm)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnnualReport(vm : AnnualReportViewModel) {
    val state = rememberPagerState {
        6
    }
    VerticalPager(state = state, beyondBoundsPageCount = 0, modifier = Modifier
        .fillMaxSize()
        .animatedGradientBackground(
            MaterialColors.DeepPurple800, Color.Black
        )) { page ->
        /**
         *
         * 总共六页
         * 第一页：你的2023年度报告生成完成，耗时 {vm.loadingTime}
         *        点击查看
         * 第二页：2023年你一共翻译了 ${vm.totalTranslateTimes} 个字
         * 第三页：有些时刻，你可能已经遗忘，但我们还记得
         *
         *        ${vm.earliestTime}，你打开译站，开始了翻译
         *        这是你最早的一天，还记得是做什么吗？
         *        ${vm.latestTime}，你仍然没有放下译站
         *        这是你最晚的一天，记得早些休息
         * 第四页：你最常用的源语言是
         *       ${vm.mostCommonSourceLanguage}
         *       使用了 ${vm.mostCommonSourceLanguageTimes} 次
         *
         *       你最常用的目标语言是
         *       ${vm.mostCommonTargetLanguage}
         *       使用了 ${vm.mostCommonTargetLanguageTimes} 次
         * 第五页：译站的一大特点就是多引擎翻译
         *        你一共使用过 ${vm.enginesUsesList.size} 个引擎
         *        你最常用的引擎是
         *        ${vm.enginesUsesList[0].first}
         *        使用了 ${vm.enginesUsesList[0].second} 次
         *        其余引擎使用情况如下
         * 第六页：2020年1月1日，译站迎来了第一次代码提交
         *        到今天，已经过了
         *        ${} 天
         *        在这期间，译站提交了代码达 110 次
         *        发布了 40 个版本
         *
         *        目前，驿站App代码量超 17000 行
         *        应用下载量约 9000 次（包含应用内更新）
         *        赞助者 30+ 人
         *
         *        译站的未来，我们一起期待
         *        感谢一路支持
         *
         *        @译站 2023年度报告
         *
         */
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            when (page) {
                0 -> AnnualReportPart1(loadingDuration = vm.loadingDuration, loadLatest = vm.shouldLoadLatest)
                1 -> AnnualReportPart2(totalTranslateTimes = vm.totalTranslateTimes, totalTranslateWords = vm.totalTranslateWords)
                2 -> AnnualReportPart3(earliestTime = vm.earliestTime, latestTime = vm.latestTime)
                3 -> AnnualReportPart4(mostCommonSourceLanguage = vm.mostCommonSourceLanguage, mostCommonSourceLanguageTimes = vm.mostCommonSourceLanguageTimes, mostCommonTargetLanguage = vm.mostCommonTargetLanguage, mostCommonTargetLanguageTimes = vm.mostCommonTargetLanguageTimes)
                4 -> AnnualReportPart5(engineUsedList = vm.enginesUsesList)
                5 -> AnnualReportPart6()
            }
        }
    }
}

@Composable
fun AnnualReportPart1(
    loadingDuration: Duration,
    loadLatest: Boolean
) {
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxWidth()
            .padding(24.dp)) {
        TitleText("译站 2023\n年度报告")
        Spacer(height = 8.dp)
        LabelText(text = "你的年度报告加载完成\n耗时 ${loadingDuration.toString(DurationUnit.MILLISECONDS, decimals = 0)}")
        Spacer(height = 48.dp)
        TipText(text = "下滑开启")
        TipText(text = if (loadLatest) "*2023你似乎还不认识译站，已自动切换数据到至今" else "*统计数据开始于2023/01/01 仅本地数据")
    }
}

@Composable
fun AnnualReportPart2(
    totalTranslateTimes: Int,
    totalTranslateWords: Int
) {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        LabelText(text = "在这一年，你一共翻译了")

        Row(modifier = Modifier) {
            AnimatedNumber(
                startAnim = state.currentFadeIndex == 1,
                number = totalTranslateTimes,
            )

            ResultText(text = "次", modifier = Modifier.align(Alignment.CenterVertically))
        }
        LabelText(text = "总共")
        Row(modifier = Modifier) {
            AnimatedNumber(
                startAnim = state.currentFadeIndex == 3,
                number = totalTranslateWords,
            )

            ResultText(text = "字", modifier = Modifier.align(Alignment.CenterVertically))
        }
        LabelText(text = "相当于 %.2f 篇 800 字的高考作文".format(totalTranslateWords / 800.0))
    }
}

@Composable
fun AnnualReportPart3(
    earliestTime :Long,
    latestTime : Long
) {
    // 时间戳转化为 xx年xx月xx日 xx:xx:xx
    fun formatTime(time: Long): String {
        val localDateTime = Instant.fromEpochMilliseconds(time).toLocalDateTime(TimeZone.currentSystemDefault())
        val year = localDateTime.year
        val month = localDateTime.monthNumber
        val day = localDateTime.dayOfMonth
        val hour = localDateTime.hour
        val minute = localDateTime.minute
        val second = localDateTime.second
        return "${year}年${month}月${day}日 %02d:%02d:%02d".format(hour, minute, second)
    }

    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp)) {
        TipText(text = "有些时刻，你可能已经遗忘，但我们还记得")
        Spacer(height = 8.dp)

        ResultText(text = formatTime(earliestTime))
        LabelText(text = "你打开译站，开始了翻译")
        Spacer(height = 8.dp)
        TipText(text = "这是你最早的一天，还记得是做什么吗？")

        Spacer(height = 100.dp)

        ResultText(text = formatTime(latestTime))
        LabelText(text = "你仍然没有放下译站")
        Spacer(height = 8.dp)
        TipText(text = "这是你最晚的一天，记得早些休息")
    }
}

@Composable
fun AnnualReportPart4(
    mostCommonSourceLanguage: String,
    mostCommonSourceLanguageTimes: Int,
    mostCommonTargetLanguage: String,
    mostCommonTargetLanguageTimes: Int
) {
    val state = rememberAutoFadeInColumnState()
    LaunchedEffect(key1 = state.currentFadeIndex){
        Log.d(TAG, "AnnualReportPart4: ${state.currentFadeIndex}")
    }
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "你最常用的源语言是")
        Spacer(height = 8.dp)
        ResultText(text = mostCommonSourceLanguage)
        Spacer(height = 18.dp)
        TipText(text = "使用了")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = mostCommonSourceLanguageTimes, startAnim = state.currentFadeIndex == 5)
            ResultText(text = "次")
        }

        Spacer(height = 48.dp)
        TipText(text = "你最常用的目标语言是")
        Spacer(height = 8.dp)
        ResultText(text = mostCommonTargetLanguage)
        Spacer(height = 18.dp)
        TipText(text = "使用了")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = mostCommonTargetLanguageTimes, startAnim = state.currentFadeIndex == 12)
            ResultText(text = "次")
        }

        Spacer(height = 48.dp)
        TipText(text = "你的刻苦，相信能被看见")
    }
}

@Composable
fun AnnualReportPart5(
    engineUsedList: List<Pair<String, Int>>
){
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "译站的一大特点就是多引擎翻译")
        Spacer(height = 8.dp)
        TipText(text = "你一共使用过")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = engineUsedList.size, startAnim = state.currentFadeIndex == 3)
            ResultText(text = "个引擎")
        }
        if (engineUsedList.isEmpty()){
            Spacer(height = 48.dp)
            TipText(text = "很遗憾，之后再体验吧")
        } else {
            Spacer(height = 24.dp)
            TipText(text = "你最常用的引擎是")
            Spacer(height = 8.dp)
            ResultText(text = engineUsedList[0].first)
            Spacer(height = 18.dp)
            TipText(text = "使用了")
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedNumber(
                    number = engineUsedList[0].second,
                    startAnim = state.currentFadeIndex == 10
                )
                ResultText(text = "次")
            }
            Spacer(height = 48.dp)
            TipText(text = "其余引擎使用情况如下")
            Spacer(height = 8.dp)
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .heightIn(0.dp, 200.dp)) {
                itemsIndexed(engineUsedList.subList(1, engineUsedList.size)) { _, it ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = it.first, color = Color.White.copy(0.8f))
                        Text(text = "${it.second}次", color = Color.White.copy(0.8f))
                    }
                }
            }
        }

        Spacer(height = 24.dp)
        TipText(text = "更多彩的功能，仍在未来等待")

    }
}

@Composable
fun AnnualReportPart6() {
    val state = rememberAutoFadeInColumnState()
    AutoFadeInComposableColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        state = state
    ) {
        TipText(text = "2020年1月1日，译站迎来了第一次代码提交")
        Spacer(height = 8.dp)
        TipText(text = "到今天，已经过了")
        Spacer(height = 2.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = ((System.currentTimeMillis() - 1577808000000) / 86400000).toInt(), startAnim = state.currentFadeIndex == 4, textSize = 32.sp)
            ResultText(text = "天")
        }
        Spacer(height = 18.dp)
        TipText(text = "在这期间，译站提交了代码达")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 317, startAnim = state.currentFadeIndex == 7, textSize = 32.sp)
            ResultText(text = "次")
        }
        Spacer(height = 18.dp)
        TipText(text = "发布了")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 61, startAnim = state.currentFadeIndex == 10, textSize = 32.sp)
            ResultText(text = "个版本")
        }

        Spacer(height = 18.dp)
        TipText(text = "目前，译站App代码量超")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 31000, startAnim = state.currentFadeIndex == 13, textSize = 32.sp)
            ResultText(text = "行")
        }
        Spacer(height = 8.dp)
        TipText(text = "应用下载量约")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 20000, startAnim = state.currentFadeIndex == 16, textSize = 32.sp)
            ResultText(text = "次")
        }
        Spacer(height = 8.dp)
        TipText(text = "注册用户")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedNumber(number = 1000, startAnim = state.currentFadeIndex == 19, textSize = 32.sp)
            ResultText(text = "+人")
        }

        Spacer(height = 18.dp)
        TipText(text = "译站的未来，我们一起期待")
        Spacer(height = 8.dp)
        TipText(text = "感谢一路支持")
        Spacer(height = 24.dp)

        TipText(text = "@译站 2023年度报告")
    }
}

@Composable
private fun LabelText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = modifier, color = Color.White.copy(alpha = 0.8f))
}

@Composable
private fun ResultText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, modifier = modifier, color = Color.White)
}


@Composable
private fun TipText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = modifier, color = Color.White.copy(alpha = 0.6f))
}

@Composable
private fun TitleText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = modifier, color = Color.White)
}

@Composable
private fun FadeInColumnScope.AnimatedNumber(
    startAnim: Boolean,
    number: Int,
    textSize: TextUnit = 48.sp,
) {
    AutoIncreaseAnimatedNumber(
        modifier = Modifier,
        startAnim = startAnim,
        number = number,
        durationMills = 1000,
        textSize = textSize,
        textColor = Color.White,
        textWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun FadeInColumnScope.Spacer(
    whetherFade: Boolean = false,
    height: Dp
) {
    Spacer(modifier = Modifier
        .fadeIn(whetherFade)
        .height(height))
}


