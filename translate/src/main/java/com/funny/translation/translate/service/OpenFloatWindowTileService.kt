package com.funny.translation.translate.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.funny.translation.helper.Log
import com.funny.translation.translate.TransActivityIntent
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
                TransActivityIntent.OpenFloatWindow.asIntent().let {
                    startActivityAndCollapse(it)
                }
            }
        }

        qsTile.updateTile()
    }
}