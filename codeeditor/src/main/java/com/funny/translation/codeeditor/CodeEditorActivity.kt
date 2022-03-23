package com.funny.translation.codeeditor

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.funny.translation.codeeditor.ui.AppNavigation
import com.funny.translation.codeeditor.ui.theme.CodeEditorTheme

class CodeEditorActivity : ComponentActivity() {
    private val TAG = "CodeEditorActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Home()
        }
//        setContentView(TextView(this).apply {
//            text = "年买的"
//            setTextColor(resources.getColor(R.color.purple_700))
//        })
        Log.d(TAG, "onCreate: finish setContent")
    }
}

@Preview("Home")
@Composable
fun Home(){
    //Text(text = "尼玛的",modifier = Modifier.fillMaxSize(),fontSize = 40.sp)
    CodeEditorTheme {
        AppNavigation()
//        Box(modifier = Modifier
//            .fillMaxWidth()
//            .height(64.dp)
//            .background(Color.Black)){
//
//        }
    }
}