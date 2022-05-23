应用整体采用多 `module` 结构，共包含下列 `module`

- **translate： 主体的翻译页面**
- **base-core：基础模块，定义了基本Bean，以API形式引入第三方模块提供给其他部分**
- **codeeditor：代码编辑器页面**
- **jet_setting_core：设置页面的基本组件**
- editor、language-base、language-universal：来源于开源项目[sora-editor](https://github.com/Rosemoe/sora-editor)，代码编辑器 View
- buildSrc：依赖版本管理


下面对各 module 进行介绍

### base-core

基础模块，定义了基本Bean，以API形式引入第三方模块提供给其他部分

![image-20220416221012678](http://img.funnysaltyfish.fun/i/2022/04/16/625ace4c9e9c7.png)

#### debug

用于插件调试，定义了插件调试输出接口`DebugTarget`。该类允许将插件输出信息显示至不同位置，目前输出位置包含：

- 控制台
- 插件调试页面（见**codeeditor-runner**）



#### helper

包含部分常用的拓展方法、工具类，如 Uri读写、数据持久化等

![image-20220416221456412](http://img.funnysaltyfish.fun/i/2022/04/16/625acf60f3d64.png)



#### js

该模块定义了 JavaScript 插件相关的基本内容，包含代表插件的实体Bean、插件相关配置、插件运行接口、数据库相关DAO等。其中核心类为`JsBean`，此类实现了 Java/Kotlin 和 Rhino JavaScript 交互的相关逻辑，完成了插件的加载、执行，并输出调试信息至`DebugTarget`。对于较耗时的插件加载和执行，将在协程中执行。

![image-20220416221953729](http://img.funnysaltyfish.fun/i/2022/04/16/625ad08a42e57.png)



#### network

网络相关的基本类，包含对 `OkHttp` 封装的 `get`、`put`等方法，对`Retrofit`初始化的封装`ServiceCreator`，以及 OkHttp 的自定义拦截器 。上述封装确保了全局仅有单个 `OkHttpClient` ，避免不必要的内存消耗，并可以以较简易的写法发起 get/post 请求，类似如下：

```kotlin
val realUrl = String.format("....")  
val headers = hashMapOf(
	"User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.73"
)
val html = OkHttpUtils.get(realUrl, headers)
```



#### trans

![image-20220416234103253](http://img.funnysaltyfish.fun/i/2022/04/16/625ae38fba861.png)

该包下包含最基本的翻译语言、翻译引擎接口、核心翻译任务抽象类、翻译结果等，是整个程序中最核心的逻辑部分之一。`Languages`为密闭类，定义了中文、英文等语言，`TranslationEngine`为可选择的翻译引擎，`CoreTranslationTask`为运行时的翻译任务，二者通过反射进行相互关联。

`TranslationEngine`包含了引擎的基本信息，代码如下：

```kotlin
/**
 * 翻译引擎
 * @property name String 引擎名称，全应用唯一
 * @property supportLanguages List<Language> 该引擎支持的语言
 * @property languageMapping Map<Language, String> 各语言对应的code，如"zh-CN"等
 * @property selected Boolean 记录当前引擎是否被选择
 * @property taskClass KClass<out CoreTranslationTask> 引擎运行时生成的任务，通过反射动态生成
 */
interface TranslationEngine {
    val name : String
    val supportLanguages: List<Language>
    val languageMapping : Map<Language , String>
    var selected : Boolean

    val taskClass : KClass<out CoreTranslationTask>
}

// 引擎本地持久化时的Key
val TranslationEngine.selectKey
    get() = this.name + "_SELECTED"
```



`CoreTranslationTask`定义了整个翻译的抽象过程，包含以下步骤：

![image-20220416234449445](http://img.funnysaltyfish.fun/i/2022/04/16/625ae4721cd2d.png)

解释如下：

- `madeURL` : 方法，无参数，返回字符串。如果插件需要访问某网址，可在此处生成完整URL。如果不需要可以返回""。
- `getBasicText` ：方法，参数为madeURL返回的URL，返回字符串。该方法用于获取原始Html/Xml/Json等文件，返回结果将传递给后续进行解析。
- `getFormattedResult` ：方法，参数为getBasicText返回的原始内容，无返回值。该方法用于解析并提取有效信息，并返回最终结果供应用显示



另还有一个BaseApplication用于获取全局的Application Context，此处不赘述



### translate

此 module 下是**应用的主体页面**部分，主要的逻辑和UI都在此处

![image-20220417155547122](http://img.funnysaltyfish.fun/i/2022/04/17/625bc80444e33.png)



#### 根目录

包含几个Activity:

- TransActivity：也就是 Main Activity
- WebViewActivity： 应用内打开网页时会跳转到这个 Activity 打开
- ErrorDialogActivity：应用崩溃时显示的对话框 Activity，用于给出提示、发送错误报告

以及：

- AppNavigation：导航相关，定义路由相关信息
- ActivityVM：整个Activity 生命周期内共享的ViewModel，用于共享一些信息（各Composable可通过`LocalActivityVM.current`获取到同一实例）



#### bean

一些实体Bean、全局常量、应用配置等

#### database

数据库相关，基于 Room 创建 Database 并制定迁移策略，定义全局变量`appDB`

#### engine

内置翻译引擎的集合，形式为密闭类，实现`TranslationEngine`接口并配置各引擎基本信息。类似如下：

```kotlin
object BaiduNormal : TranslationEngines() {
    override val name: String
        get() = stringResource(R.string.engine_baidu)

    override val languageMapping: Map<Language, String>
        get() = mapOf(
            Language.AUTO to "auto",
            Language.CHINESE to "zh",
            Language.ENGLISH to "en",
            Language.JAPANESE to "jp",
            Language.KOREAN to "kor",
            Language.FRENCH to "fra",
            Language.RUSSIAN to "ru",
            Language.GERMANY to "de",
            Language.WENYANWEN to "wyw",
            Language.THAI to "th",
            Language.PORTUGUESE to "pt",
            Language.VIETNAMESE to "vie",
            Language.ITALIAN to "it"
        )

    override val taskClass: KClass<out CoreTranslationTask>
        get() = TranslationBaiduNormal::class
}
```

#### extensions

常用拓展方法和拓展属性

#### network

![image-20220417160443094](http://img.funnysaltyfish.fun/i/2022/04/17/625bca1be1a42.png)

各种 Retrofit 的 Service，并在 TransNetwork 中提供全局懒加载的单例



#### task

![image-20220417160605447](http://img.funnysaltyfish.fun/i/2022/04/17/625bca6e404f4.png)

`CoreTranslationTask`的具体实现类，包含各引擎（谷歌、有道、百度……）具体实现。各类通过类代理关联到相应引擎，并重写相关方法。以百度为例：

```kotlin
class TranslationBaiduNormal :
    BasicTranslationTask(), TranslationEngine by TranslationEngines.BaiduNormal {
    companion object{
        var TAG = "BaiduTranslation"
    }

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        val from = languageMapping[sourceLanguage]
        val to = languageMapping[targetLanguage]
        val headersMap = hashMapOf(
            "Referer" to "FunnyTranslation"
        )
        val apiUrl = "$url?text=$sourceString&engine=baidu&source=$from&target=$to"
        val transResult = OkHttpUtils.get(apiUrl, headersMap)
        return transResult
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        val obj = JSONObject(basicText)
        if (obj.getInt("code")==50){
            result.setBasicResult(obj.getString("translation"))
            result.detailText = obj.getString("detail")
        }else{
            result.setBasicResult(obj.getString("error_msg"))
        }
    }

    override fun madeURL(): String {
        return "https://api.funnysaltyfish.fun/trans/v1/api/translate"
    }

    override val isOffline: Boolean
        get() = false
}
```

#### utils

![](http://img.funnysaltyfish.fun/i/2022/04/17/625bcba9d3bbe.png)

各种工具类



#### ui

UI 页面，由  Jetpack Compose 搭建，按不同页面组织。每个页面均包含 UI 及对应 ViewModel:

![image-20220417161351873](http://img.funnysaltyfish.fun/i/2022/04/17/625bcc40ab007.png)

其中：

- widget：包含了一些通用的小微件，如按钮、Chip、Loading之类，也包含自定义的底部导航栏

- screen：密闭类，定义路由

  ```kotlin
  sealed class TranslateScreen(val icon : NavigationIcon, val titleId : Int, val route : String) {
      object MainScreen : TranslateScreen(NavigationIcon(Icons.Default.Home), R.string.nav_main, "nav_trans_main")
      object SettingScreen : TranslateScreen(NavigationIcon(Icons.Default.Settings), R.string.nav_settings, "nav_trans_settings")
      object PluginScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_plugin), R.string.nav_plugin, "nav_trans_plugin")
      object ThanksScreen : TranslateScreen(NavigationIcon(resourceId = R.drawable.ic_thanks), R.string.nav_thanks, "nav_thanks")
      object AboutScreen : TranslateScreen(NavigationIcon(Icons.Default.Settings),R.string.about,"nav_trans_setting")
      object SortResultScreen : TranslateScreen(NavigationIcon(Icons.Default.Settings),R.string.sort_result,"nav_trans_sort_result")
  }
  ```



下面以主翻译页面介绍。对应代码：MainScreen

主页面针对不同屏幕尺寸做了适配，这一功能是通过最外层的 `BoxWithConstraints` 实现的，代码大致如下：

```kotlin
BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth > 720.dp) { // 横屏
            Row(...) { // 横向布局 左 | 右
                EngineSelect(...) // 引擎选择
                Box(...) // 分割线
                TranslatePart(...) // 翻译主体
            }
        }else{// 竖屏，纵向布局
            Column(...) { // 纵向布局
                AnimatedVisibility(...) {
                    EngineSelect(...) // 引擎选择
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(...) // 小横条
                Spacer(modifier = Modifier.height(12.dp))
                TranslatePart(...) // 翻译部分
            }
        }
    }
```

`EngineSelect`是引擎选择部分，实现了引擎的加载（主要是插件的异步加载）和选择罗辑，并将选好的引擎保存到ViewModel里

`TranslatePart`则是主要的翻译部分，包含输入框、语种选择、结果展示等。各部分均由更细节部分组合而来，代码大致如下：

```kotlin
fun TranslatePart(
    vm: MainViewModel,
    showSnackbar: (String) -> Unit,
    modifier : Modifier
) {
    val transText by vm.translateText.observeAsState("")
    val sourceLanguage by vm.sourceLanguage.observeAsState()
    val targetLanguage by vm.targetLanguage.observeAsState()

    val resultList by vm.resultList.observeAsState()
    val translateProgress by vm.progress.observeAsState()

    val softKeyboardController = LocalSoftwareKeyboardController.current
    Row( // 语种选择
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        LanguageSelect(
            language = sourceLanguage!!,
            languages = allLanguages,
            updateLanguage = {
                vm.sourceLanguage.value = it
                DataSaverUtils.saveData(Consts.KEY_SOURCE_LANGUAGE, it.id)
            }
        )
        ExchangeButton { // 源语言 和 目标语言 交换
            // 省略具体逻辑
        }
        LanguageSelect(
            language = targetLanguage!!,
            languages = allLanguages,
            updateLanguage = {
                vm.targetLanguage.value = it
                DataSaverUtils.saveData(Consts.KEY_TARGET_LANGUAGE, it.id)
            }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    InputText(text = transText, updateText = { vm.translateText.value = it }) // 输入框
    Spacer(modifier = Modifier.height(12.dp))
    TranslateButton(translateProgress!!.toInt()) { // 翻译按钮
      	// 一些必要的判断之后
        vm.translate() // 开始翻译
    }
    Spacer(modifier = Modifier.height(18.dp))
    TranslationList(resultList!!, showSnackbar) // 结果列表
}
```

其中主体翻译功能在`ViewModel`中，内部使用`Flow`创建翻译任务并逐一翻译，在每一个任务执行完成后更新结果（翻译成功显示结果、失败显示错误原因）和进度。

除翻译外，`MainViewModel`同时承担了加载各引擎（内置、插件）及存储选中引擎的任务，其中耗时任务多使用`Flow`+`Coroutine`实现，确保前台页面的流畅。鉴于项目数据加载并不复杂，并未单独抽象出`Repository`。

翻译结果的展示为`LazyColumn`，对于单个结果，如果其含有详细翻译，则会显示展开按钮，点击后以`Markdown`形式展示详细结果，做到和普通结果相互补充。



除翻译页面外，ui还包括了其余各页面，其中设置页面基于开源库`ComposeDataSaver`实现数据持久化保存。对于其内嵌子页面（第三方库、结果排序）使用嵌套导航（定义于com/funny/translation/translate/AppNavigation.kt）并有页面过渡动画效果（基于开源库`accompanist-navigation-animation`）。

其中：

- 第三方库页面使用`json`储存于`assets`，读取后使用`Gson`解析为`OpenSourceLibraryInfo`实体类。之后使用`LazyColumn`进行展示
- 悬浮窗基于开源库`EasyFloat`，使用View体系。允许在任意界面输入翻译内容并即时翻译。使用`LiveData`的观察者模式，当翻译配置（源语言、目标语言、翻译文本）变化时即可自动翻译。此外支持一键打开应用主体页面（基于Intent获取数据）并即时翻译，以获得更详尽的翻译结果。
- <img src="http://img.funnysaltyfish.fun/i/2022/05/06/6274f2808584a.jpg" alt="1" style="zoom:25%;" />

不再赘述。



### codeeditor

应用包含插件体系，故内建插件编辑器。提供插件的编辑、调试、导出功能，并提供实时补全、代码高亮、主题切换等。主要View基于开源项目`sora-editor`（顺带一提，它的作者现在高三~）

本module为独立部分，与主翻译页面独立，可单独改造为独立APP。代码结构如下：

![image-20220426125847414](http://img.funnysaltyfish.fun/i/2022/04/26/62677c0fc811b.png)

其中：

- base：提供基本类
- config： 配置
- extensions： 拓展方法
- vm： ActivityViewModel
- ui 页面及逻辑

主体代码位于`ui`包下，如下：

![image-20220426130125939](http://img.funnysaltyfish.fun/i/2022/04/26/62677ca6c5ccd.png)

其中：

- editor：代码编辑器
- runner：插件运行结果

#### editor

editor为代码编辑器部分，主体使用`AndroidView`的方式加载`CodeEditor`，并通过`update`和`listener`与Compose交互。右上角菜单提供了撤销/回做、保存/导出、更改主题、参数模拟、查看文档的功能，帮助插件开发。其中，文件保存通过`SAF`框架实现，以`ActivityResultLauncher`方式执行并设置回调。

<img src="http://img.funnysaltyfish.fun/i/2022/05/06/6274f252d7e6d.jpg" alt="2" style="zoom:33%;" />

点击右上角运行图标即可运行代码，并进入`runner`页面



#### runner

runner为代码运行的结果展示，其对应`ViewModel`负责运行插件代码（通过`base-core/JSEngine`）并通过实现`DebugTarget`接口输出调试信息





### jet_setting

设置页面的小组件，包含单选项、跳转项、仅文本展示项等，类似于传统Android中的各种Preference。

设置项的本地保存基于`ComposeDataSaver`实现，通过代理形式简化持久化流程。

