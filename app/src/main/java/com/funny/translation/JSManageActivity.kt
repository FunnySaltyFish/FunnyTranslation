package com.funny.translation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.funny.translation.bean.Consts
import com.funny.translation.codeeditor.CodeEditorActivity
import com.funny.translation.db.DBJSUtils
import com.funny.translation.db.DBJSUtils.deleteJS
import com.funny.translation.db.DBJSUtils.insertJS
import com.funny.translation.db.DBJSUtils.nextID
import com.funny.translation.db.DBJSUtils.queryAllJS
import com.funny.translation.js.JSException
import com.funny.translation.js.JsEngine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.utils.ApplicationUtil
import com.funny.translation.utils.FileUtil
import com.funny.translation.widget.JSManageAdapter
import com.getbase.floatingactionbutton.FloatingActionButton
import java.io.IOException

class JSManageActivity : BaseActivity() {
    var rv: RecyclerView? = null
    lateinit var adapter: JSManageAdapter
    var btnImportFromLocal: FloatingActionButton? = null
    var btnNewFile: FloatingActionButton? = null
    var jsDetailDialog: AlertDialog? = null
    var deleteJSDialog: AlertDialog? = null
    val TAG = "JSManageActivity"
    var hasChanged = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_js_manage)
        rv = findViewById(R.id.js_manage_rv)
        adapter = JSManageAdapter(R.layout.view_js_manage_rv_item).apply {
            setList(queryAllJS())
            addChildClickViewIds(R.id.view_js_manage_rv_item_check)
            setOnItemChildClickListener { adapter, view, position ->
                val jsManageAdapter = adapter as JSManageAdapter
                when (view.id) {
                    R.id.view_js_manage_rv_item_check -> {
                        val dbjsUtils = DBJSUtils
                        val jsBean = jsManageAdapter.data[position]
                        jsBean.enabled = 1 - jsBean.enabled
                        dbjsUtils.setJSEnabled(jsBean.id, jsBean.enabled)
                        hasChanged = true
                    }
                }
            }
            setOnItemClickListener { adapter, view, position ->
                val jsBean = (adapter as JSManageAdapter).getItem(position)
                showJSDetailDialog(jsBean)
            }
            isUseEmpty = true
            setEmptyView(R.layout.view_js_manage_rv_empty)
        }

        rv?.adapter = adapter
        rv?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        initFAB()
    }

    private fun initFAB() {
        btnImportFromLocal = findViewById(R.id.js_manage_import_from_local)
        btnImportFromLocal?.setOnClickListener { view: View? -> startChooseFile() }
        btnNewFile = findViewById(R.id.js_manage_new_file)
        btnNewFile?.setOnClickListener { view: View? ->
            //moveToActivity(CodeActivity.class);
            moveToActivity(CodeEditorActivity::class.java)
        }
    }

    private fun startChooseFile() {
        //通过系统的文件浏览器选择一个文件
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        //筛选，只显示可以“打开”的结果，如文件(而不是联系人或时区列表)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        //过滤只显示图像类型文件
        //intent.setType("text/javascript");
        intent.type = "application/javascript"
        startActivityForResult(intent, Consts.ACTIVITY_JS_MANAGE)
    }

    private fun showJSDetailDialog(jsBean: JsBean) {
        jsDetailDialog = AlertDialog.Builder(this@JSManageActivity)
            .setTitle(jsBean.fileName)
            .setMessage(java.lang.String.format("关于：\n%s", jsBean.description))
            .setNegativeButton("删除") { dialog, which -> showDeleteJSDialog(jsBean) }
            .create()
        jsDetailDialog?.show()
    }

    private fun showDeleteJSDialog(jsBean: JsBean) {
        deleteJSDialog = AlertDialog.Builder(this@JSManageActivity)
            .setTitle("警告")
            .setMessage(String.format("您确定要删除插件【%s】吗？", jsBean.fileName))
            .setNegativeButton("取消", null)
            .setPositiveButton("确定") { _, _ ->
                deleteJS(jsBean)
                Log.i(TAG, "当前准备删除的js ID为：" + jsBean.id)
                adapter.remove(jsBean)
                ApplicationUtil.print(baseContext, "已删除！")
                hasChanged = true
            }
            .setCancelable(false)
            .create()
        deleteJSDialog!!.show()
    }

    override fun onBackPressed() {
        val backIntent = Intent()
        backIntent.putExtra("hasChanged", hasChanged)
        setResult(Consts.ACTIVITY_JS_MANAGE, backIntent)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == Consts.ACTIVITY_JS_MANAGE && resultCode == RESULT_OK) {
            val uri: Uri?
            if (resultData != null) {
                // 获取选择文件Uri
                uri = resultData.data
                if (uri != null) {
                    try {
                        val code = FileUtil.readTextFromUri(this, uri)
                        //Log.i(TAG,"加载进来的code是：\n"+code);
                        val jsBean = JsBean(code = code)
                        jsBean.id = nextID
                        val jsEngine = JsEngine(jsBean)
                        jsEngine.loadBasicConfigurations()
                        adapter.addData(jsEngine.jsBean)
                        insertJS(jsEngine.jsBean)
                        hasChanged = true
                        ApplicationUtil.print(this, "添加成功！")
                    } catch (e: IOException) {
                        e.printStackTrace()
                        ApplicationUtil.print(this, "添加插件时发生IO流错误，添加失败。")
                    } catch (e: JSException) {
                        e.printStackTrace()
                        ApplicationUtil.print(this, "添加插件时插件本身产生错误，原因是：" + e.message)
                    }
                }
            }
        }
    }
}