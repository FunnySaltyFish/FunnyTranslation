package com.funny.translation.translate.ui.plugin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.js.JsEngine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.config.JsConfig
import com.funny.translation.translate.database.appDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PluginViewModel : ViewModel() {
    val plugins : Flow<List<JsBean>>
        get() {
            return appDB.jsDao.getAllJs()
        }

    fun deletePlugin(jsBean: JsBean){
        viewModelScope.launch(Dispatchers.IO) {
            appDB.jsDao.deleteJs(jsBean)
        }
    }

    fun updatePlugin(jsBean: JsBean){
        viewModelScope.launch(Dispatchers.IO) {
            appDB.jsDao.updateJs(jsBean)
        }
    }

    fun importPlugin(
        code: String,
        successCall: (String) -> Unit,
        failureCall: (String) -> Unit
    ){
        val jsBean = JsBean(code = code)
        val jsEngine = JsEngine(jsBean)
        viewModelScope.launch(Dispatchers.IO) {
            jsEngine.loadBasicConfigurations(
                {
                    // Log.d(TAG, "onActivityResult: min:${jsBean.minSupportVersion} max:${jsBean.maxSupportVersion}")
                    if(jsBean.minSupportVersion<= JsConfig.JS_ENGINE_VERSION&&jsBean.maxSupportVersion>= JsConfig.JS_ENGINE_VERSION){
                        appDB.jsDao.insertJs(jsBean)
                        successCall("添加成功！")
                    }else{
                        failureCall("插件版本与软件核心不兼容，请与开发者联系解决！")
                    }
                },{
                    failureCall("插件加载时出错！请联系插件开发者解决！")
                }
            )
        }

    }
}