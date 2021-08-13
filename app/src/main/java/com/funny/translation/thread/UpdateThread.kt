package com.funny.translation.thread

import com.funny.translation.MainActivity
import com.funny.translation.utils.UpdateUtil
import com.funny.translation.utils.ApplicationUtil
import java.lang.Exception

class UpdateThread(var ctx: MainActivity) : Thread() {
    var isShowResult = false
    var haveNewVersion = false
    var hasError = false //联网获取翻译时是否错误
    var ErrorMessage: String? = ""
    override fun run() {
        // TODO: Implement this method
        super.run()
        val t = Thread { // TODO: Implement this method
            try {
                UpdateUtil.updateDescription = UpdateUtil.getUpdateDescription()
            } catch (e: Exception) {
                e.printStackTrace()
                hasError = true
                haveNewVersion = false
                ErrorMessage = e.message
            }
        }
        t.start()
        //long curTime=System.currentTimeMillis();
        //System.out.println("t开始");
        try {
            t.join() //确保网络访问执行完后再继续
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        haveNewVersion = UpdateUtil.checkNewVersion(ctx)
        //System.out.println("t结束，花费"+(System.currentTimeMillis()-curTime));
        while (!isShowResult) { //还没有完成
            if (ctx.isFree) { //主Activity闲着
                ctx.runOnUiThread { // TODO: Implement this method
                    if (haveNewVersion) {
                        ctx.showUpdateDialog()
                    } else {
                        if (hasError) {
                            ApplicationUtil.print(ctx, "出错!原因是：$ErrorMessage")
                        } else {
                            ApplicationUtil.print(ctx, "自动更新检测完毕，当前已是最新版本！")
                        }
                    }
                }
                isShowResult = true
            } else {
                try {
                    sleep(1000)
                    //System.out.println("试图展示更新dialog——");
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}