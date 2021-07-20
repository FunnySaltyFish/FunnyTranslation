package com.funny.translation.fragments;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.funny.translation.R;
import com.funny.translation.code.JavaScriptSyntaxManager;
import com.funny.translation.databinding.CodeEditorFragmentBinding;
import com.funny.translation.jetpack.ActivityCodeViewModel;
import com.funny.translation.utils.FileUtil;

import java.io.IOException;

public class CodeEditorFragment extends Fragment {

    CodeEditorViewModel mViewModel;
    ActivityCodeViewModel activityCodeViewModel;
    CodeEditorFragmentBinding fragmentBinding;

    Resources re;

    public static CodeEditorFragment newInstance() {
        return new CodeEditorFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fragmentBinding = DataBindingUtil.inflate(inflater,R.layout.code_editor_fragment,container,false);
        return fragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        re=getResources();

        mViewModel = new ViewModelProvider(this).get(CodeEditorViewModel.class);
        activityCodeViewModel = new ViewModelProvider(requireActivity()).get(ActivityCodeViewModel.class);


        // TODO: Use the ViewModel
        fragmentBinding.setData(mViewModel);
        fragmentBinding.setLifecycleOwner(this);

        initCodeEditor();
    }

    private void initCodeEditor(){
        mViewModel.setKeywords(re.getStringArray(R.array.js_keywords));

        int layoutId = R.layout.view_code_suggestion_item;
        int tvId = R.id.view_code_suggestion_item_tv;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),layoutId,tvId,mViewModel.getKeywords().getValue());

        fragmentBinding.codeView.setAdapter(adapter);
        fragmentBinding.codeView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                activityCodeViewModel.setCode(s.toString());
            }
        });

        try {
            fragmentBinding.codeView.setText(FileUtil.getAssetsData(requireActivity(),"js_temp.js"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JavaScriptSyntaxManager.applyMonokaiTheme(requireContext(),fragmentBinding.codeView);
    }

}