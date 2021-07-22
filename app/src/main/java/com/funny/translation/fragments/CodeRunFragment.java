package com.funny.translation.fragments;

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

import com.funny.translation.CodeActivity;
import com.funny.translation.R;
import com.funny.translation.bean.Consts;
import com.funny.translation.databinding.CodeRunFragmentBinding;
import com.funny.translation.jetpack.ActivityCodeViewModel;
import com.funny.translation.js.JS;
import com.funny.translation.js.JSEngine;
import com.funny.translation.js.JSException;
import com.funny.translation.js.TranslationCustom;
import com.funny.translation.translation.TranslationException;
import com.funny.translation.translation.TranslationResult;

import java.lang.ref.WeakReference;

public class CodeRunFragment extends Fragment {

    private CodeRunViewModel mViewModel;
    private ActivityCodeViewModel activityCodeViewModel;
    CodeRunFragmentBinding codeRunFragmentBinding;

    public CodeRunHandler handler;

    String TAG =  "CodeRunFragment";

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CodeRunViewModel.class);
        //mViewModel.getOutput().observe(requireActivity(), s -> Log.i(TAG,"receive:"+s));

        activityCodeViewModel = new ViewModelProvider(requireActivity()).get(ActivityCodeViewModel.class);

        codeRunFragmentBinding.setData(mViewModel);
        codeRunFragmentBinding.setLifecycleOwner(this);

        handler = new CodeRunHandler((CodeActivity)getActivity(),mViewModel);

        new CodeRunThread(this,mViewModel).start();
    }

    public void appendOutput(String s){
        Message msg = Message.obtain();
        msg.what = Consts.MESSAGE_CODE_RUN_UPDATE_OUTPUT;
        msg.obj = s;
        handler.sendMessage(msg);
    }

    public void appendDivider(){
        handler.sendEmptyMessage(Consts.MESSAGE_CODE_RUN_UPDATE_DIVIDER);
    }

    public void clearOutput(){
        handler.sendEmptyMessage(Consts.MESSAGE_CODE_RUN_CLEAR_OUTPUT);
    }

    class CodeRunThread extends Thread{
        CodeRunFragment codeRunFragment;
        CodeRunViewModel codeRunViewModel;
        public CodeRunThread(CodeRunFragment codeRunFragment,CodeRunViewModel codeRunViewModel){
            this.codeRunFragment = codeRunFragment;
            this.codeRunViewModel = codeRunViewModel;
        }

        @Override
        public void run() {
            super.run();
            codeRunFragment.clearOutput();
            codeRunFragment.appendOutput("开始加载JS...");
            try {
                codeRunViewModel.reloadJS(activityCodeViewModel.getCode().getValue());
            } catch (JSException e) {
                e.printStackTrace();
                codeRunFragment.appendOutput("加载JS错误，具体原因："+e.getMessage());
                codeRunFragment.appendOutput("结束执行！");
                return;
            }
            codeRunFragment.appendOutput("JS加载完成！");
            codeRunFragment.appendDivider();

            JS js = codeRunViewModel.jsEngine.getValue().js;
            codeRunFragment.appendOutput("作者："+js.author);
            codeRunFragment.appendOutput("关于："+js.about);
            codeRunFragment.appendOutput("版本："+js.version);
            codeRunFragment.appendDivider();

            codeRunFragment.appendOutput("开始执行JS...");

            //重新初始化这个语言名称，后续需要删除
            Consts.LANGUAGE_NAMES = getResources().getStringArray(R.array.languages);
            codeRunFragment.appendOutput(String.format("默认参数：{\n\tsourceString:%s,\n\tsourceLanguage:%s,\n\ttargetLanguage:%s\n}",
                    mViewModel.sourceString,
                    Consts.LANGUAGE_NAMES[mViewModel.sourceLanguage],
                    Consts.LANGUAGE_NAMES[mViewModel.targetLanguage]
            ));
            codeRunFragment.appendDivider();

            JSEngine mJSEngine = mViewModel.jsEngine.getValue();
            CodeRunTranslationCustom translationCustom = new CodeRunTranslationCustom(mViewModel.sourceString, mViewModel.sourceLanguage, mViewModel.targetLanguage);
            translationCustom.setCodeRunFragment(codeRunFragment);
            translationCustom.setJSEngine(mJSEngine);
            try {
                translationCustom.translate(Consts.MODE_NORMAL);
            } catch (TranslationException e) {
                e.printStackTrace();
                codeRunFragment.appendOutput("翻译过程中出错，具体原因是："+e.getMessage());
                codeRunFragment.appendOutput("结束执行！");
                return;
            }

            codeRunFragment.appendOutput("执行完毕！");
        }
    }

    static class CodeRunHandler extends Handler{
        WeakReference<CodeActivity> codeActivityWeakReference;
        CodeRunViewModel viewModel;
        public CodeRunHandler(CodeActivity activity,CodeRunViewModel codeRunViewModel){
            codeActivityWeakReference = new WeakReference<>(activity);
            viewModel = codeRunViewModel;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(viewModel!=null){
                if(msg.what == Consts.MESSAGE_CODE_RUN_UPDATE_OUTPUT){
                    viewModel.appendOutput((String)msg.obj);
                }else if(msg.what == Consts.MESSAGE_CODE_RUN_CLEAR_OUTPUT){
                    viewModel.clearOutput();
                }else if(msg.what == Consts.MESSAGE_CODE_RUN_UPDATE_DIVIDER){
                    viewModel.appendDividerLine();
                }
            }
        }
    }

    static class CodeRunTranslationCustom extends TranslationCustom{
        CodeRunFragment codeRunFragment;

        public CodeRunTranslationCustom(String sourceString, short sourceLanguage, short targetLanguage) {
            super(sourceString, sourceLanguage, targetLanguage);
        }

        public void setCodeRunFragment(CodeRunFragment codeRunFragment) {
            this.codeRunFragment = codeRunFragment;
        }

        @Override
        public String madeURL() {
            codeRunFragment.appendOutput("开始执行 madeURL 方法...");
            String url = super.madeURL();
            codeRunFragment.appendOutput("执行完毕，获得的结果是："+url+"\n");
            return url;
        }

        @Override
        public String getBasicText(String url) throws TranslationException {
            String result = "";
            try {
                codeRunFragment.appendOutput("开始执行 getBasicText 方法...");
                result = super.getBasicText(url);
            }catch (Exception e){
                codeRunFragment.appendOutput("发生错误！原因是："+e.getMessage());
            }
            codeRunFragment.appendOutput("执行完毕，获得的结果是："+result+"\n");
            return result;
        }

        @Override
        public TranslationResult getFormattedResult(String basicText) throws TranslationException {
            codeRunFragment.appendOutput("开始执行 getFormattedResult 方法...");
            TranslationResult result = getResult();
            try{
                result = super.getFormattedResult(basicText);
                codeRunFragment.appendOutput("执行完毕，获得的结果是："+result.toString()+"\n");
            }catch (Exception e){
                codeRunFragment.appendOutput("发生错误！原因是："+e.getMessage());
            }
            return result;
        }
    }

}