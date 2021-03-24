package com.funny.translation.fragements;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.funny.translation.R;
import com.funny.translation.databinding.CodeRunFragmentBinding;
import com.funny.translation.jetpack.ActivityCodeViewModel;
import com.funny.translation.js.JS;
import com.funny.translation.js.JSException;

public class CodeRunFragment extends Fragment {

    private CodeRunViewModel mViewModel;
    private ActivityCodeViewModel activityCodeViewModel;
    CodeRunFragmentBinding codeRunFragmentBinding;

    static Handler handler;

    public static CodeRunFragment newInstance() {
        return new CodeRunFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        codeRunFragmentBinding = DataBindingUtil.inflate(inflater,R.layout.code_run_fragment, container, false);
        return codeRunFragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CodeRunViewModel.class);
        activityCodeViewModel = new ViewModelProvider(getActivity()).get(ActivityCodeViewModel.class);

        codeRunFragmentBinding.setData(mViewModel);
        codeRunFragmentBinding.setLifecycleOwner(this);


        new CodeRunThread(mViewModel).start();
    }

    class CodeRunThread extends Thread{
        CodeRunViewModel mViewModel;
        public CodeRunThread(CodeRunViewModel codeRunViewModel){
            this.mViewModel = codeRunViewModel;
        }

        @Override
        public void run() {
            super.run();
            mViewModel.clearOutput();
            mViewModel.appendOutput("开始加载JS...");
            try {
                mViewModel.reloadJS(activityCodeViewModel.getCode().getValue());
            } catch (JSException e) {
                e.printStackTrace();
                mViewModel.appendOutput("加载JS错误。具体原因："+e.getMessage());
            }
            mViewModel.appendOutput("JS加载完成！");
            mViewModel.appendDividerLine();

            JS js = mViewModel.jsEngine.getValue().js;
            mViewModel.appendOutput("作者："+js.author);
            mViewModel.appendOutput("关于："+js.about);
            mViewModel.appendOutput("版本："+js.version);
            mViewModel.appendDividerLine();

            mViewModel.appendOutput("开始执行JS...");
        }
    }

}