package com.funnt.trans.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.funnt.trans.compose.ui.theme.FunnyTransComposeTheme
import com.funnt.trans.compose.ui.theme.MaterialColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FunnyTransComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialColors.Orange400) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FunnyTransComposeTheme {
        Greeting("Android")
    }
}