package com.funny.translation;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.funny.translation.databinding.ActivityCodeBinding;
import com.funny.translation.fragements.CodeEditorViewModel;
import com.funny.translation.jetpack.ActivityCodeViewModel;

public class CodeActivity extends BaseActivity{
    ActivityCodeViewModel activityCodeViewModel;
    CodeEditorViewModel codeEditorViewModel;

    ActivityCodeBinding activityCodeBinding;

    Resources re;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCodeBinding = DataBindingUtil.setContentView(this,R.layout.activity_code);
        re = getResources();
        activityCodeViewModel = new ViewModelProvider(this,new ViewModelProvider.NewInstanceFactory()).get(ActivityCodeViewModel.class);
        codeEditorViewModel = new ViewModelProvider(this).get(CodeEditorViewModel.class);

        activityCodeBinding.setData(activityCodeViewModel);
        activityCodeBinding.setLifecycleOwner(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_code_editor,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        NavController navController = Navigation.findNavController(this,R.id.actvity_code_nav_fragment);
        if(id == R.id.menu_code_editor_debug){
            navController.navigate(R.id.action_codeEditorFragment_to_codeRunFragment);
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }
}
