package com.funny.translation.translate.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import com.funny.translation.Consts
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.activity.ShareActivity
import com.funny.translation.translate.utils.EasyFloatUtils

@RequiresApi(Build.VERSION_CODES.N)
class OpenFloatWindowTileService: TileService() {

    companion object {
        const val TAG = "OpenFWTileService"
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "onStartListening: showing: ${EasyFloatUtils.isShowingFloatBall()}, state: ${qsTile.state}")
        // 检测状态是否匹配
        if (EasyFloatUtils.isShowingFloatBall() && qsTile.state == Tile.STATE_INACTIVE){
            qsTile.state = Tile.STATE_ACTIVE
            qsTile.updateTile()
        }
        else if (!EasyFloatUtils.isShowingFloatBall() && qsTile.state == Tile.STATE_ACTIVE){
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "onClick: ")
        when(qsTile.state){
            Tile.STATE_ACTIVE -> kotlin.run {
                qsTile.state = Tile.STATE_INACTIVE
                EasyFloatUtils.hideAllFloatWindow()
            }
            Tile.STATE_INACTIVE -> kotlin.run {
                qsTile.state = Tile.STATE_ACTIVE
                Intent().apply {
                    setClass(FunnyApplication.ctx, ShareActivity::class.java)
                    action = Consts.INTENT_ACTION_CLICK_FLOAT_WINDOW_TILE
                    putExtra(Consts.INTENT_EXTRA_OPEN_FLOAT_WINDOW, true)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.let {
                    startActivityAndCollapse(it)
                }
            }
        }

        qsTile.updateTile()
    }
}