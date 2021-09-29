package com.funny.translation.translate.bean

import com.funny.translation.translate.utils.RC4

object Consts {
    //错误常量
    const val ERROR_UNKNOWN = "未知错误！翻译失败！"
    const val ERROR_JSON = "Json数据解析错误！"
    const val ERROR_UNSUPPORT_LANGUAGE = "暂不支持的语言翻译形式"
    const val ERROR_DATED_ENGINE = "当前方法已过期！请反馈！"
    const val ERROR_POST = "发送POST请求异常！请检查网络连接！"
    const val ERROR_IO = "IO流错误！"
    const val ERROR_ILLEGAL_DATA = "数据不合法！"
    const val ERROR_ONLY_CHINESE_SUPPORT = "当前翻译模式仅支持中文！"
    const val ERROR_NO_BV_OR_AV = "未检测到有效的Bv号或Av号"

    const val MODE_EACH_LINE = 1

    //百度翻译常量
    private val DEFAULT_BAIDU_APP_ID: String = RC4.decry_RC4("785ebf34dc6aa09ffc4f5726d7bcb14f3f", "27420")
    private val DEFAULT_BAIDU_SECURITY_KEY: String = RC4.decry_RC4("0e36da569e6dd8e3a831377988e8b5373ab87173", "27420")
    const val DEFAULT_BAIDU_SLEEP_TIME = 1000L

    var BAIDU_APP_ID = DEFAULT_BAIDU_APP_ID
    var BAIDU_SECURITY_KEY = DEFAULT_BAIDU_SECURITY_KEY
    var BAIDU_SLEEP_TIME = DEFAULT_BAIDU_SLEEP_TIME
}