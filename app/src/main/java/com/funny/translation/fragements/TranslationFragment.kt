package com.funny.translation.fragements

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.funny.translation.R

class TranslationFragment : Fragment() {

    companion object {
        fun newInstance() = TranslationFragment()
    }

    private lateinit var viewModel: TranslationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.translation_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TranslationViewModel::class.java)
        // TODO: Use the ViewModel
    }

}