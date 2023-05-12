package com.funny.translation.translate.ui.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.jetsetting.core.JetSettingSwitch
import com.funny.jetsetting.core.JetSettingTile
import com.funny.jetsetting.core.ui.SettingItemCategory
import com.funny.translation.AppConfig
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.DateUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.widget.SimpleDialog
import com.funny.translation.translate.utils.SortResultUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*

private const val TAG = "SettingScreen"

@Composable
fun SettingsScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        SettingItemCategory(
            title = {
                ItemHeading(text = stringResource(id = R.string.setting_translate))
            }
        ) {
            JetSettingSwitch(
                state = AppConfig.sEnterToTranslate,
                resourceId = R.drawable.ic_enter,
                text = stringResource(R.string.setting_enter_to_translate)
            ) {
                if (it) context.toastOnUi("已开启回车翻译，部分输入法可能无效，敬请谅解~")
            }
            JetSettingSwitch(
                state = AppConfig.sShowTransHistory,
                resourceId = R.drawable.ic_history,
                text = stringResource(R.string.setting_show_history)
            ) {

            }
            JetSettingSwitch(
                state = AppConfig.sTextMenuFloatingWindow,
                resourceId = R.drawable.ic_float_window,
                text = stringResource(R.string.setting_text_menu_floating_window),
                description = stringResource(id = R.string.setting_text_menu_floating_window_desc)
            ) {

            }
            JetSettingTile(
                resourceId = R.drawable.ic_sort,
                text = stringResource(R.string.sort_result),
            ) {
                navController.navigate(TranslateScreen.SortResultScreen.route)
            }
            JetSettingTile(
                resourceId = R.drawable.ic_select,
                text = stringResource(R.string.select_language),
            ) {
                navController.navigate(TranslateScreen.SelectLanguageScreen.route)
            }
            val openConfirmDeleteDialogState = remember { mutableStateOf(false) }
            SimpleDialog(
                openDialogState = openConfirmDeleteDialogState,
                title = stringResource(R.string.message_confirm),
                message = stringResource(R.string.confirm_delete_history_desc),
                dismissButtonAction = {
                    scope.launch(Dispatchers.IO) {
                        appDB.transHistoryDao.clearAll()
                    }
                    context.toastOnUi("已清空历史记录")
                },
                dismissButtonText = "残忍删除",
                confirmButtonText = "我再想想"
            )
            JetSettingTile(
                imageVector = Icons.Default.Delete,
                text = stringResource(R.string.clear_trans_history),
            ) {
                openConfirmDeleteDialogState.value = true
            }
        }

        if (DateUtils.isSpringFestival) {
            SettingItemCategory(title = {
                ItemHeading(text = stringResource(id = R.string.setting_time_limited))
            }) {
                JetSettingSwitch(
                    state = AppConfig.sSpringFestivalTheme,
                    resourceId = R.drawable.ic_theme,
                    text = stringResource(R.string.setting_spring_theme),
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
        }


        SettingItemCategory(
            title = {
                ItemHeading(text = stringResource(id = R.string.trans_pro))
            }
        ) {

            // 并行翻译
            ProJetSettingCheckbox(
                state = AppConfig.sParallelTrans,
                text = stringResource(id = R.string.parallel_trans),
                description = stringResource(id = R.string.parallel_trans_desc),
                resourceId = R.drawable.ic_parallel
            )
            JetSettingTile(
                resourceId = R.drawable.ic_theme,
                text = stringResource(id = R.string.theme)
            ) {
                navController.navigate(TranslateScreen.ThemeScreen.route)
            }
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
    onCheckedChange: (Boolean) -> Unit = {}
) {
    JetSettingSwitch(
        state = state,
        imageVector = imageVector,
        resourceId = resourceId,
        text = text,
        description = description,
        interceptor = DefaultVipInterceptor,
        onCheck = onCheckedChange
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
internal fun ItemHeading(text: String) {
    Text(
        modifier = Modifier.semantics { heading() },
        text = text,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
    )
}