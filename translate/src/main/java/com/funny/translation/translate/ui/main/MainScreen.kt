@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.translation.translate.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.translation.AppConfig
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.engine.selectKey
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.widget.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "MainScreen"
internal typealias SimpleAction = () -> Unit

// 用于选择引擎时的回调
private interface UpdateSelectedEngine {
    fun add(engine: TranslationEngine)
    fun remove(engine: TranslationEngine)
}

enum class ShowListType {
    History, Result
}

// 当前主页面正处在什么状态
enum class MainScreenState {
    Normal,     // 正常情况
    Inputting,  // 正在输入
    Translating // 正在翻译
}

/**
 * 项目的翻译页面, [图片](https://web.funnysaltyfish.fun/temp_img/202111102032441.jpg)
 */
@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MainScreen() {
    TextTransScreen()
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun TextTransScreen() {
    val vm: MainViewModel = viewModel()

    // 内置引擎
    val bindEngines by vm.bindEnginesFlow.collectAsState(emptyList())
    // 插件
    val jsEngines by vm.jsEnginesFlow.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    // 使用 staticCompositionLocal 传递主页面 scaffold 的 snackbarHostState
    // 方便各个页面展示 snackBar
    // CompositionLocal 相关知识可参阅 https://developer.android.google.cn/jetpack/compose/compositionlocal?hl=zh-cn
    val snackbarHostState = LocalSnackbarState.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activityVM: ActivityViewModel = LocalActivityVM.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    // Compose函数会被反复重新调用（重组），所以变量要remember
    val updateSelectedEngine = remember {
        object : UpdateSelectedEngine {
            override fun add(engine: TranslationEngine) {
                vm.addSelectedEngines(engine)
            }

            override fun remove(engine: TranslationEngine) {
                vm.removeSelectedEngine(engine)
            }
        }
    }

    val showSnackbar: (String) -> Unit = remember {
        {
            scope.launch {
                snackbarHostState.showSnackbar(it, withDismissAction = true)
            }
        }
    }

    var showEngineSelect by remember { mutableStateOf(false) }
    if (showEngineSelect) {
        AlertDialog(onDismissRequest = { showEngineSelect = false }, text = {
            EngineSelect(
                modifier = Modifier,
                bindEngines,
                jsEngines,
                updateSelectedEngine
            )
        }, confirmButton = {
            TextButton(onClick = { showEngineSelect = false }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        })
    }

    val showEngineSelectAction = remember {
        {
            showEngineSelect = true
        }
    }

    // 使用 BoxWithConstraints 用于适配横竖屏
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth > 720.dp) { // 横屏
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Drawer(
                    Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                MainPart(isScreenHorizontal = true, showEngineSelectAction = showEngineSelectAction, showSnackbar = showSnackbar, openDrawerAction = null)
            }
        } else {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            BackHandler(enabled = drawerState.isOpen) {
                scope.launch {
                    drawerState.close()
                }
            }
            ModalNavigationDrawer(
                drawerContent = {
                    Drawer(
                        Modifier
                            .fillMaxHeight()
                            .width(300.dp)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(12.dp)
                    )
                },
                drawerState = drawerState
            ) {
                MainPart(
                    isScreenHorizontal = false,
                    showEngineSelectAction = showEngineSelectAction,
                    showSnackbar = showSnackbar,
                    openDrawerAction = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        }
    }


    // DisposableEffect 是副作用的一种，相较于其他几个 SideEffect，特点在于可取消
    // 有关更多副作用，可参阅 https://developer.android.google.cn/jetpack/compose/side-effects?hl=zh-cn
    // 此处用于观察生命周期
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val text = activityVM.tempTransConfig.sourceString?.trim() ?: ""
                if (text != "") {
                    vm.translateText = text
                    if (activityVM.tempTransConfig.sourceLanguage != null) {
                        vm.sourceLanguage = activityVM.tempTransConfig.sourceLanguage!!
                    }
                    if (activityVM.tempTransConfig.targetLanguage != null) {
                        vm.targetLanguage = activityVM.tempTransConfig.targetLanguage!!
                    }
                    vm.translate()
                    activityVM.tempTransConfig.clear()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                softwareKeyboardController?.hide()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
private fun MainPart(
    modifier: Modifier = Modifier,
    isScreenHorizontal: Boolean,
    showEngineSelectAction: SimpleAction,
    showSnackbar: (String) -> Unit,
    openDrawerAction: SimpleAction?,
) {
    val vm: MainViewModel = viewModel()
    SimpleNavigation(currentScreen = vm.currentState, modifier = modifier) { state ->
        when (state) {
            MainScreenState.Normal -> MainPartNormal(
                vm = vm,
                isScreenHorizontal = isScreenHorizontal,
                showEngineSelectAction = showEngineSelectAction,
                openDrawerAction = openDrawerAction
            )
            MainScreenState.Inputting -> MainPartInputting(
                vm = vm,
                showEngineSelectAction = showEngineSelectAction,
                showSnackbar = showSnackbar
            )
            MainScreenState.Translating -> MainPartTranslating(vm = vm)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@ExperimentalAnimationApi
@Composable
private fun EngineSelect(
    modifier: Modifier,
    bindEngines: List<TranslationEngine> = arrayListOf(),
    jsEngines: List<TranslationEngine> = arrayListOf(),
    updateSelectEngine: UpdateSelectedEngine
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(id = R.string.bind_engine),
            fontWeight = W600
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = spacedBy(8.dp),
        ) {
            bindEngines.forEach { task ->
                var taskSelected by rememberDataSaverState(
                    key = task.selectKey,
                    default = task.selected
                )
                FilterChip(selected = taskSelected, onClick = {
                    if (!taskSelected) { // 选中了
                        updateSelectEngine.add(task)
                    } else updateSelectEngine.remove(task)
                    taskSelected = !taskSelected
                }, label = {
                    Text(text = task.name)
                })
            }
        }

        if (jsEngines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.plugin_engine),
                fontWeight = W600
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = spacedBy(8.dp)
            ) {
                jsEngines.forEach { task ->
                    var taskSelected by rememberDataSaverState(
                        key = task.selectKey,
                        default = task.selected
                    )
                    FilterChip(selected = taskSelected, onClick = {
                        if (!taskSelected) { // 选中了
                            updateSelectEngine.add(task)
                        } else updateSelectEngine.remove(task)
                        taskSelected = !taskSelected
                    }, label = {
                        Text(text = task.name)
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun Drawer(
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    val drawerItemIcon = @Composable { resId: Int, contentDescription: String ->
        Icon(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
    // 刷新用户信息
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val state = rememberPullRefreshState(refreshing = refreshing, onRefresh = {
        scope.launch {
            refreshing = true
            val user = AppConfig.userInfo.value
            if (user.isValid()){
                try {
                    UserUtils.getUserInfo(user.uid)?.let {
                        AppConfig.userInfo.value = it
                        context.toastOnUi("更新用户信息成功~")
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                    context.toastOnUi("更新用户信息失败！")
                }
            }
            delay(100) // 组件bug：时间过短，收不回去
            refreshing = false
        }
    })
    Box(modifier = modifier.pullRefresh(state)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            UserInfoPanel(navHostController = navController)
            Spacer(modifier = Modifier.height(8.dp))
            // trans_pro
            NavigationDrawerItem(
                icon = {
                    drawerItemIcon(R.drawable.ic_vip, stringResource(id = R.string.trans_pro))
                },
                label = { Text(text = stringResource(id = R.string.trans_pro)) },
                selected = false,
                onClick = {
                    navController.navigateSingleTop(TranslateScreen.TransProScreen.route)
                }
            )
            NavigationDrawerItem(
                icon = {
                    drawerItemIcon(
                        R.drawable.ic_settings,
                        stringResource(id = R.string.nav_settings)
                    )
                },
                label = { Text(text = stringResource(id = R.string.nav_settings)) },
                selected = false,
                onClick = {
                    navController.navigateSingleTop(TranslateScreen.SettingScreen.route)
                }
            )
            // about
            NavigationDrawerItem(
                icon = {
                    drawerItemIcon(R.drawable.ic_about, stringResource(id = R.string.about))
                },
                label = { Text(text = stringResource(id = R.string.about)) },
                selected = false,
                onClick = {
                    navController.navigateSingleTop(TranslateScreen.AboutScreen.route)
                }
            )
            // thanks
            NavigationDrawerItem(
                icon = {
                    drawerItemIcon(R.drawable.ic_thanks, stringResource(id = R.string.nav_thanks))
                },
                label = { Text(text = stringResource(id = R.string.nav_thanks)) },
                selected = false,
                onClick = {
                    navController.navigateSingleTop(TranslateScreen.ThanksScreen.route)
                }
            )
        }
        PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
    }
}
