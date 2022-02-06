package com.funny.translation.translate.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.bean.AppConfig
import com.yhao.floatwindow.FloatWindow
import com.yhao.floatwindow.PermissionListener
import com.yhao.floatwindow.Screen
import com.funny.translation.translate.R
import com.yhao.floatwindow.MoveType

object FloatWindowUtils {
    fun initScreenSize(activity: Activity){
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        AppConfig.SCREEN_WIDTH = displayMetrics.widthPixels
        AppConfig.SCREEN_HEIGHT = displayMetrics.heightPixels
    }

    fun initFloatingWindow(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.layout_float_window, null)

        FloatWindow
            .with(FunnyApplication.ctx)
            .setView(view)
            .setWidth(AppConfig.SCREEN_WIDTH * 9 / 10)
            .setHeight(300)
            .setX(AppConfig.SCREEN_WIDTH * 1 / 20)
            .setY(100)
            .setDesktopShow(true)
            .setPermissionListener(object : PermissionListener {
                override fun onSuccess() {

                }

                override fun onFail() {
                    AppConfig.INIT_FLOATING_WINDOW = false
                    Toast.makeText(context, "悬浮窗权限授予失败，悬浮窗无法使用",Toast.LENGTH_SHORT).show()
                }
            })
            .setMoveType(MoveType.active)
            .build()

        AppConfig.INIT_FLOATING_WINDOW = true
    }

    fun showFloatWindow(){
        if (AppConfig.INIT_FLOATING_WINDOW) FloatWindow.get().show()
    }

    fun hideFloatWindow(){
        if (AppConfig.INIT_FLOATING_WINDOW)FloatWindow.get().hide()
    }

    @Composable
    fun TransFloatWindow() {
        var text by remember {
            mutableStateOf("")
        }
        Column {
            OutlinedTextField(value = text, onValueChange = {
                text = it
            })
        }
    }
}