package com.funny.translation.js.extentions

import javax.script.ScriptException

val ScriptException.messageWithDetail
    get() = "第${this.lineNumber}行、第${this.columnNumber}列发生错误：\n${this.message}"