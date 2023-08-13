package com.funny.translation.translate.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import com.funny.translation.BaseActivity
import com.funny.translation.translate.ui.thanks.AnnualReportScreen

class AnnualReportActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AnnualReportScreen()
        }
    }
}