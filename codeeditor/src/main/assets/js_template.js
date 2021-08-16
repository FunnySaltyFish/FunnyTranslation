/*FunnyTranslation JS Engine Start*/
let FunnyJS = {
    // 插件基本配置
    // 插件作者
    "author":"张三",
    // 插件描述。如果该描述是Markdown类型，请在开头添加：[Markdown]
    "description":"这是一个插件",
    // 插件版本。插件的更新依赖于此
    "version":1,
    // 最低支持的插件引擎版本
    "mixSupportVersion":2,
    // 最高支持的插件引擎版本
    "maxSupportVersion":999,
    // 插件名称
    "name":"示例插件",
    // 是否开启Debug模式
    "debugMode":true,

    // 下面是插件需要回调的方法
    // 详见 https://www.yuque.com/funnysaltyfish/vzmuud
    "madeURL":function(){return ""},
    "isOffline":function(){return true;},
	"getBasicText":function(url){return "";},
	"getFormattedResult":function(text){
        result.setBasicResult(text);
        return result;
	}
}
/*FunnyTranslation JS Engine End*/