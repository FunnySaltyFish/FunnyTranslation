package com.funny.translation.codeeditor

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.funny.data_saver.core.LocalDataSaver
import com.funny.translation.BaseActivity
import com.funny.translation.codeeditor.ui.AppNavigation
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.theme.TransTheme

class CodeEditorActivity : BaseActivity() {
    private val TAG = "CodeEditorActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Home()
        }
    }
}

@Preview("Home")
@Composable
fun Home(){
    CompositionLocalProvider(LocalDataSaver provides DataSaverUtils) {
        TransTheme {
            AppNavigation()
        }
    }

}