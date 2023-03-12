package com.funny.translation.translate.ui.settings

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.jetsetting.core.JetSettingCheckbox
import com.funny.jetsetting.core.JetSettingTile
import com.funny.translation.AppConfig
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.activity.WebViewActivity
import com.funny.translation.Consts
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.widget.HeadingText
import com.funny.translation.translate.ui.widget.SimpleDialog
import com.funny.translation.helper.DateUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.utils.EasyFloatUtils
import com.funny.translation.translate.utils.SortResultUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*

private const val TAG = "SettingScreen"

@Composable
fun SettingsScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current

    val showFloatWindowTipDialog = remember {
        mutableStateOf(false)
    }
    val scrollState = rememberScrollState()
    val floatWindowTip = """
        悬浮窗使用需要权限，请正确授予相应权限
        悬浮球可拖动，移动至指定位置即可删除。翻译悬浮窗右下角可直接打开应用
        如果你是 Android9 及以下系统，可长按 译 按钮翻译剪切板内容
    """.trimIndent()

    SimpleDialog(
        openDialogState = showFloatWindowTipDialog,
        title = stringResource(R.string.float_window_tip),
        message = floatWindowTip,
        dismissButtonText = ""
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {

        ItemHeading(text = stringResource(id = R.string.setting_ui))
        JetSettingCheckbox(
            state = AppConfig.sHideStatusBar,
            text = stringResource(R.string.setting_hide_status_bar),
            resourceId = R.drawable.ic_status_bar,
            iconTintColor = MaterialColors.BlueGrey700
        ) {

        }
        JetSettingCheckbox(
            state = AppConfig.sHideBottomNavBar,
            text = stringResource(R.string.setting_hide_nav_bar),
            resourceId = R.drawable.ic_bottom_bar,
            iconTintColor = MaterialColors.Blue700
        ) {

        }
        JetSettingCheckbox(
            state = AppConfig.sUseNewNavigation,
            text = stringResource(id = R.string.custom_nav),
            resourceId = R.drawable.ic_custom_nav,
            iconTintColor = MaterialColors.DeepOrangeA200
        ) {
            Toast.makeText(
                context, "已${
                    if (it) {
                        "启动"
                    } else {
                        "关闭"
                    }
                }新导航栏", Toast.LENGTH_SHORT
            ).show()
        }
        JetSettingCheckbox(
            state = AppConfig.sTransPageInputBottom,
            text = stringResource(R.string.setting_trans_page_input_bottom),
            resourceId = R.drawable.ic_input_bottom,
            iconTintColor = MaterialColors.Brown800
        ) {

        }
        if (DateUtils.isSpringFestival) {
            JetSettingCheckbox(
                state = AppConfig.sSpringFestivalTheme,
                text = stringResource(R.string.setting_spring_theme),
                resourceId = R.drawable.ic_theme,
                iconTintColor = MaterialColors.Red700,
            ) {
                Toast.makeText(
                    context, "已${
                        if (it) {
                            "设置"
                        } else {
                            "取消"
                        }
                    }春节限定主题", Toast.LENGTH_SHORT
                ).show()
            }
        }
        JetSettingCheckbox(
            key = Consts.KEY_SHOW_FLOAT_WINDOW,
            text = stringResource(R.string.setting_show_float_window),
            resourceId = R.drawable.ic_float_window,
            iconTintColor = MaterialColors.Orange700
        ) {
            try {
                if (it) EasyFloatUtils.showFloatBall(context as Activity)
                else EasyFloatUtils.hideAllFloatWindow()
            } catch (e: Exception) {
                context.toastOnUi("显示悬浮窗失败，请检查是否正确授予权限！")
                DataSaverUtils.saveData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
            }
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
                .padding(24.dp, 8.dp)
                .clickable {
                    showFloatWindowTipDialog.value = true
                },
            text = stringResource(R.string.about_float_window),
            fontSize = 16.sp,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.primary
        )

        ItemHeading(text = stringResource(id = R.string.others))
        JetSettingCheckbox(
            state = AppConfig.sEnterToTranslate,
            text = stringResource(R.string.setting_enter_to_translate),
            resourceId = R.drawable.ic_enter,
            iconTintColor = MaterialColors.Teal700
        ) {
            if (it) context.toastOnUi("已开启回车翻译，部分输入法可能无效，敬请谅解~")
        }
        JetSettingCheckbox(
            state = AppConfig.sAutoFocus,
            text = stringResource(R.string.setting_auto_focus),
            resourceId = R.drawable.ic_keyboard,
            iconTintColor = MaterialColors.Red400
        ) {

        }
        JetSettingCheckbox(
            state = AppConfig.sShowTransHistory,
            text = stringResource(R.string.setting_show_history),
            resourceId = R.drawable.ic_history,
            iconTintColor = MaterialColors.Lime700
        ) {

        }
        JetSettingCheckbox(
            state = AppConfig.sShowImageTransBtn,
            text = stringResource(R.string.setting_show_image_trans_btn),
            resourceId = R.drawable.ic_album,
            iconTintColor = MaterialColors.PinkA700
        ) {

        }
        JetSettingCheckbox(
            state = AppConfig.sTextMenuFloatingWindow,
            text = stringResource(R.string.setting_text_menu_floating_window),
            description = stringResource(id = R.string.setting_text_menu_floating_window_desc),
            resourceId = R.drawable.ic_float_window,
            iconTintColor = MaterialColors.DeepPurpleA200
        ) {

        }
        JetSettingTile(
            text = stringResource(R.string.sort_result),
            resourceId = R.drawable.ic_sort,
            iconTintColor = MaterialColors.DeepOrangeA700,
        ) {
            navController.navigate(TranslateScreen.SortResultScreen.route)
        }
        JetSettingTile(
            text = stringResource(R.string.select_language),
            resourceId = R.drawable.ic_select,
            iconTintColor = MaterialColors.LightBlueA700,
        ) {
            navController.navigate(TranslateScreen.SelectLanguageScreen.route)
        }
        val openConfirmDeleteDialogState = remember { mutableStateOf(false) }
        SimpleDialog(
            openDialogState = openConfirmDeleteDialogState,
            title = stringResource(R.string.message_confirm),
            message = stringResource(R.string.confirm_delete_history_desc),
            dismissButtonAction = {
                scope.launch(Dispatchers.IO){
                    appDB.transHistoryDao.clearAll()
                }
                context.toastOnUi("已清空历史记录")
            },
            dismissButtonText = "残忍删除",
            confirmButtonText = "我再想想"
        )
        JetSettingTile(
            text = stringResource(R.string.clear_trans_history),
            imageVector =  Icons.Default.Delete,
            iconTintColor = MaterialColors.RedA700,
        ) {
            openConfirmDeleteDialogState.value = true
        }

        ItemHeading(text = stringResource(id = R.string.trans_pro))
        // 并行翻译
        ProJetSettingCheckbox(
            state = AppConfig.sParallelTrans,
            text = stringResource(id = R.string.parallel_trans),
            description = stringResource(id = R.string.parallel_trans_desc),
            resourceId = R.drawable.ic_parallel,
            iconTintColor = MaterialColors.DeepOrange100
        )
        JetSettingTile(
            text = stringResource(id = R.string.theme),
            resourceId = R.drawable.ic_theme,
            iconTintColor = MaterialTheme.colorScheme.primary
        ) {
            navController.navigate(TranslateScreen.ThemeScreen.route)
        }

        ItemHeading(text = stringResource(id = R.string.about))
        JetSettingTile(
            text = stringResource(R.string.source_code),
            resourceId = R.drawable.ic_github,
            iconTintColor = MaterialColors.Purple700
        ) {
            context.toastOnUi(FunnyApplication.resources.getText(R.string.welcome_star),)
            WebViewActivity.start(context, "https://github.com/FunnySaltyFish/FunnyTranslation")
        }
        JetSettingTile(
            text = stringResource(id = R.string.open_source_library),
            resourceId = R.drawable.ic_open_source_library,
            iconTintColor = MaterialColors.DeepOrange700
        ) {
            navController.navigate(TranslateScreen.AboutScreen.route)
        }
        JetSettingTile(
            text = stringResource(R.string.privacy),
            resourceId = R.drawable.ic_privacy,
            iconTintColor = MaterialColors.Amber700
        ) {
            WebViewActivity.start(context, "https://api.funnysaltyfish.fun/trans/v1/api/privacy")
        }
    }
}

internal val DefaultVipInterceptor = {
    if (!AppConfig.isVip()) {
        appCtx.toastOnUi("此设置为会员专享功能，请先开通后再使用~")
        false
    } else {
        true
    }
}

@Composable
private fun ProJetSettingCheckbox(
    state: MutableState<Boolean>,
    text: String,
    description: String? = null,
    resourceId: Int? = null,
    imageVector: ImageVector? = null,
    iconTintColor: Color = MaterialTheme.colorScheme.primary,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    JetSettingCheckbox(
        state = state,
        text = text,
        description = description,
        resourceId = resourceId,
        imageVector = imageVector,
        iconTintColor = iconTintColor,
        onCheck = onCheckedChange,
        interceptor = DefaultVipInterceptor
    )
}


@Composable
fun SortResult(
    modifier: Modifier = Modifier
) {
    val state = rememberReorderState()
    val localEngines by SortResultUtils.localEngines.collectAsState()
    val data by remember {
        derivedStateOf {
            localEngines.toMutableStateList()
        }
    }
    LazyColumn(
        state = state.listState,
        modifier = modifier
            .then(
                Modifier.reorderable(
                    state,
                    onMove = { from, to -> data.move(from.index, to.index) })
            )
    ) {
        itemsIndexed(data, { i, _ -> i }) { i, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggedItem(state.offsetByIndex(i))
                    .background(MaterialTheme.colorScheme.surface)
                    .detectReorderAfterLongPress(state)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(24.dp))
                    Icon(painterResource(id = R.drawable.ic_drag),"Drag to sort")
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                Divider()
            }
        }
    }

    DisposableEffect(key1 = null){
        onDispose {
            if(!SortResultUtils.checkEquals(data)){
                Log.d(TAG, "SortResult: 不相等")
                SortResultUtils.resetMappingAndSave(data)
            }
        }
    }
}

@Composable
fun SelectLanguage(modifier: Modifier) {
    val data = remember {
        allLanguages.map { DataSaverUtils.readData(it.selectedKey, true) }.toMutableStateList()
    }

    fun setEnabledState(language: Language, enabled: Boolean){
        DataSaverUtils.saveData(language.selectedKey, enabled)
        if (enabled) {
            enabledLanguages.value = (enabledLanguages.value + language).sortedBy { it.id }
        }else{
            enabledLanguages.value = (enabledLanguages.value - language).sortedBy { it.id }
        }
    }

    fun setAllState(state : Boolean){
        for (i in 0 until data.size){
            data[i] = state
            setEnabledState(allLanguages[i], state)
        }
    }

    DisposableEffect(key1 = Unit){
        onDispose {
            // 如果什么都没选，退出的时候默认帮忙选几个
            data.firstOrNull{it} ?: kotlin.run {
                for (i in 0..2){
                    setEnabledState(allLanguages[i], true)
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(12.dp)
    ) {
        item {
            var selectAll by rememberSaveable {
                // 当所有开始都被选上时，默认就是全选状态
                mutableStateOf(data.firstOrNull { !it } == null )
            }
            val tintColor by animateColorAsState(targetValue = if (selectAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
            IconButton(onClick = {
                selectAll = !selectAll
                setAllState(selectAll)
            }, modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)) {
                Icon(painter = painterResource(id = R.drawable.ic_select_all), contentDescription = "是否全选", tint= tintColor)
            }
        }

        itemsIndexed(data, { i, _ -> i }) { i, selected ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = allLanguages[i].displayText,
                    modifier = Modifier.padding(16.dp)
                )
                Checkbox(checked = selected, onCheckedChange = {
                    data[i] = it
                    setEnabledState(allLanguages[i], it)
                })
            }
        }
    }
}

@Composable
private fun ItemHeading(text: String) {
    HeadingText(
        modifier = Modifier.padding(24.dp, 12.dp),
        text = text
    )
}