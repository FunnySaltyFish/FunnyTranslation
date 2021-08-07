package com.funny.translation.codeeditor.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.funny.translation.codeeditor.R
import com.funny.translation.codeeditor.base.BaseFragment
import com.funny.translation.codeeditor.databinding.CodeRunFragmentBinding

class CodeRunFragment() : BaseFragment(R.layout.code_run_fragment) {
    lateinit var codeRunFragmentBinding : CodeRunFragmentBinding
    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
//        codeRunFragmentBinding = DataBindingUtil.setContentView()
//        codeRunFragmentBinding.codeRunOutputTv.text = "测试一下"
        view.findViewById<TextView>(R.id.code_run_output_tv).text = "测试一下"
    }
}