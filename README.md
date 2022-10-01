## FunnyTranslation

### 介绍

本软件是一款翻译软件，旧版基于Java语言，在传统View体系下开发；自2.0.0起逐步改用Kotlin开发，自2.1.0起软件架构重写，大部分代码均用Kotlin编写，页面完全改用Jetpack Compose。软件由2.1.0起决定开源。

本应用有以下特点：

- Jetpack Compose + MVVM + Kotlin Coroutine + Flow + Room 
- 多 module 设计，公共模块与项目模块分离
- 支持多引擎同步翻译，支持插件开发、下载、更新、导入、导出等，高拓展性
- 充分利用 Kotlin 语言特性，如延迟懒加载、类代理、Coroutine、Flow、密闭类、拓展方法、拓展属性、反射等 
- 基于 Rhino JS 设计并实现完整的 JavaScript 插件加载、运行、调试环境，提供插件开发一条龙支持 
- 适配 Android11文件权限，适配双语、暗黑模式、横屏/平板页面，开发、使用并公开发布三个开源库

应用逐步适配 Android13，具体包括：  
- 支持单独设置应用语言

您可以在以下途径获取最新版本：
- [酷安](https://www.coolapk.com/apk/com.funny.translation)
- [此仓库](/translate/release/translate-release.apk)

考虑到本应用的Star逐渐增多，且在某些地方一定程度上作为学习的项目，我最近也在加强它“一个可参考的开源项目”的属性。  

如果您希望阅读代码，我建议您从 [主Activity](translate/src/main/java/com/funny/translation/translate/TransActivity.kt) 开始

如有帮助， **欢迎star** ！

### 截图

#### 运行截图

| 图片                                                         | 图片                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| <img src="./screenshot/1.png" alt="Screenshot_2021-11-07-22-37-33-814_com.funny.tran" style="zoom:33%;" /> | <img src="./screenshot/2.png" alt="Screenshot_2021-11-07-22-39-18-201_com.funny.tran" style="zoom:33%;" /> |
| <img src="./screenshot/3.png" alt="Screenshot_2021-11-07-22-40-16-339_com.funny.tran" style="zoom:33%;" /> | <img src="./screenshot/4.png" alt="IMG_20211107_223720" style="zoom:33%;" /> |





### 源代码简介

作为开源项目来说，您可以从以下方面了解它



#### 模块

- **translate： 主体的翻译页面**
- **base-core：基础模块，定义了基本Bean，以API形式引入第三方模块提供给其他部分**
- **codeeditor：代码编辑器页面**
- **login: 登录、注册页面**
- **jet_setting_core：设置页面的基本组件**
- editor、language-base、language-universal：来源于开源项目[sora-editor](https://github.com/Rosemoe/sora-editor)，代码编辑器 View
- buildSrc：依赖版本管理
- app/library：旧版应用全部模块

您可以参阅[此文档](./detail_introduction.md)以了解各模块详细代码组成




#### 运行前准备

- 您需要使用 [Android Studio](https://developer.android.google.cn/studio/)  **2020.3.1 及以上版本**

- 为了安全起见，开源部分不包括有关签名信息的`signing.properties`文件，如需打 Release 包请您补全此文件

  - **signing.properties**

  - 位于根目录下

    ```bash
    // 如果需要打release包，请在项目根目录下自行添加此文件
    /**
     *  STORE_FILE=yourAppStroe.keystore
        STORE_PASSWORD=yourStorePwd
        KEY_ALIAS=yourKeyAlias
        KEY_PASSWORD=yourAliasPwd
     */
    ```
  
    

### 致谢

- 页面设计参考自 酷安@江戸川コナン（已授权）
- 宣传图来自 酷安@松川吖
- 感谢所有赞助过项目的小伙伴们！



如果此项目对您有帮助，欢迎不吝**Star**~