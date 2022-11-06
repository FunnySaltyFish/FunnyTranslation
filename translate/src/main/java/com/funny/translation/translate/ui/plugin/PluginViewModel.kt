package com.funny.translation.translate.ui.plugin

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.helper.coroutine.Coroutine.Companion.async
import com.funny.translation.helper.lazyPromise
import com.funny.translation.js.JsEngine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.config.JsConfig
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.network.service.PluginService
import com.funny.translation.translate.utils.SortResultUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PluginViewModel : ViewModel() {
    companion object {
        private const val TAG = "PluginVM"
    }
    private val pluginService : PluginService

        get() = TransNetwork.pluginService

    private val _onlinePlugins by lazyPromise(viewModelScope){
        pluginService.getOnlinePlugins()
    }

    suspend fun getOnlinePlugins(): List<JsBean> {
        return _onlinePlugins.await()
    }

    val plugins : Flow<List<JsBean>>
        get() {
            return appDB.jsDao.getAllJs()
        }

    fun deletePlugin(jsBean: JsBean){
        viewModelScope.launch(Dispatchers.IO) {
            appDB.jsDao.deleteJsByName(jsBean.fileName)
        }
    }

    fun updatePlugin(jsBean: JsBean){
        viewModelScope.launch(Dispatchers.IO) {
            val origin = appDB.jsDao.queryJsByName(jsBean.fileName)
            if(origin != null){
                jsBean.id = origin.id
            }
            appDB.jsDao.updateJs(jsBean)
        }
    }

    /**
     * 根据jsBean判断这个在线插件是否已经被安装/需要升级
     * @param jsBean JsBean
     */

     fun checkPluginState(jsBean: JsBean) : MutableState<OnlinePluginState>{
        val state = mutableStateOf(OnlinePluginState.NotInstalled)
        async(viewModelScope) {
            appDB.jsDao.queryJsByName(jsBean.fileName)
        }.onSuccess { data ->
            if(data!=null){
                state.value = if(data.version < jsBean.version) OnlinePluginState.OutDated else OnlinePluginState.Installed
            }
        }
        return state
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
                    installOrUpdatePlugin(jsBean, successCall, failureCall)
                },{
                    failureCall("插件加载时出错！请联系插件开发者解决！")
                }
            )
        }
    }

    fun installOrUpdatePlugin(jsBean: JsBean, successCall: (String) -> Unit, failureCall: (String) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            if(jsBean.minSupportVersion <= JsConfig.JS_ENGINE_VERSION){
                if(appDB.jsDao.queryJsByName(jsBean.fileName)!=null){ //更新
                    updatePlugin(jsBean)
                    if(JsConfig.JS_ENGINE_VERSION != jsBean.targetSupportVersion){
                        successCall("更新成功！[请注意，新插件最佳版本与当前引擎版本有所差异，可能有兼容性问题]")
                    }else successCall("更新成功！")
                }else{
                    appDB.jsDao.insertJs(jsBean)
                    SortResultUtils.addNew(jsBean.fileName)
                    if(JsConfig.JS_ENGINE_VERSION != jsBean.targetSupportVersion){
                        successCall("添加成功！[请注意，插件最佳版本与当前引擎版本有所差异，可能有兼容性问题]")
                    }else successCall("添加成功！")
                }
            }else{
                failureCall("插件版本与引擎核心不兼容，请联系插件开发者解决！")
            }
        }
    }
}