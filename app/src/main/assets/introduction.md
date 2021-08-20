## 更新说明
v2.0.0 beta-5：
◆你不一定关注的更新
—重构 全新的代码编辑器
——使用Jetpack Compose重构
——支持文件打开、保存
——支持即时调试、参数模拟
——支持切换多种主题
——精简API，简化插件编写
—重构 部分代码转为kotlin编写
—修复 因混淆导致的OkHttpUtil不可用问题

◆其他
—这两个版本相隔很久，原因很复杂。我本意用kotlin重写部分内容，写着写着感觉不搭；于是萌生全部用kotlin+MVVM重写的念头；后来又想用Jetpack Compose写页面，然后到最后又放弃了。
—无语子

## 软件说明
◆欢迎使用本软件
Q：为什么会有本软件？
A：很多时候面对翻译工作，因为各种原因，我们总是需要机器翻译。这时候如何找到一家正确而精准的翻译引擎，往往成为令人头疼的问题。于是乎，我就诞生了！

Q：它有什么特点吗？
A：我们允许你在一次翻译过程中对同一个文本采用不同翻译引擎*，并同时翻译成多个语言目标**。这样是不是一次要节省很多时间丫？还有啊，在未来，我们也将提供更多样的翻译形式，满足您的不同需求！

Q：翻译软件那么多，你有什么特别之处吗？
A：除了软件宣传的一对多同时翻译外，我们还支持适用于诗词的逐行对照翻译、适合搞怪的逐字翻译，以及一些有趣的小功能（比如说文本放大啦~）。未来我们会不断进步！

Q：为什么有的引擎翻译很慢/不精准呢？
A：翻译慢是因为接口的调用频率会限制，程序不得不停留一段时间（50-800毫秒)，以保证翻译的完成；不精准的问题，在引擎的接口选择上。加强翻译精度，也是未来软件开发的重点。

Q：说的好棒，但是我找不到功能在哪里？(ﾟoﾟ;
A：要选择翻译引擎和翻译方向，您可以从屏幕右侧向左滑动或者点击右上角三角图标。

Q：可以复制翻译结果吗？
A：可以的，每条翻译结果右下角会有一个复制按钮，点击即可复制到剪切板。

Q：好的好的。咦？你们这个应用会不会乱收集我的数据啊！
A：在这一点上，你可以放心。应用无广告，少权限（目前仅有联网权限实现翻译、获取网络状态权限实现离线判断），是绝对的良心软件，业界少有（偷偷告诉你，作者自己也在用哦）。

Q：哇！没有广告，你们怎么盈利呢？
A：暂无盈利途径。您若喜欢，可点击下方按钮复制赞赏码链接（滑稽味****)，为我投出您宝贵的五毛钱；同时，未来软件可能开启会员制（放心很便宜）以提升翻译体验——

Q：诶？这有什么关系吗？
A：有的。各大主流翻译引擎对使用者采取收费制度。（如有道翻译90天内的500万字符收费204元），而且因为聚合多家引擎，费用成本很高。所以在未来可能适当收费，敬请谅解！

Q：开发者你好实诚啊！
A：哈，谢谢支持！

*：目前支持 金山翻译(简版) 有道翻译(标准版) 百度翻译(标准版) 谷歌翻译(标准)。持续更新中
**：因接口限制，部分语言翻译方向暂不可用

## 鸣谢
●鸣谢以下开源库：
 -[Android Jetpack Library](https://developer.android.google.cn/jetpack/)
 -[SmartSwipe](https://github.com/luckybilly/SmartSwipe/)
 -[EditTextField](https://github.com/opprime/EditTextField)
 -[AndroidVideoCache](https://github.com/danikula/AndroidVideoCache)
 -[CircleProgress](https://github.com/lzyzsd/CircleProgress/)
 -[OkHttp3](https://github.com/square/okhttp)
 -[BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)
 -[android-floating-action-button](https://github.com/zendesk/android-floating-action-button)
 -[legado](https://github.com/gedoor/legado/)
 -[CodeEditor](https://github.com/Rosemoe/CodeEditor/)
 -[FileOperator](https://github.com/javakam/FileOperator)
 -[Markwon](https://github.com/noties/Markwon)

## 版权
Copyright@FunnySaltyFish
2020-2021
All Rights Reserved

