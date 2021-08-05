package com.funny.translation.codeeditor.base

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

open class BaseActivity : AppCompatActivity() , CoroutineScope by MainScope() {

}