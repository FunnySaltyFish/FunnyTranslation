package com.funny.translation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.funny.translation.databinding.ActivityCodeBinding;
import com.funny.translation.fragements.CodeEditorViewModel;
import com.funny.translation.jetpack.ActivityCodeViewModel;
import com.funny.translation.utils.ApplicationUtil;

public class CodeActivity extends BaseActivity{
    ActivityCodeViewModel activityCodeViewModel;
    CodeEditorViewModel codeEditorViewModel;

    ActivityCodeBinding activityCodeBinding;

    NavController navController;

    Resources re;

    boolean hasCreatedMenu = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCodeBinding = DataBindingUtil.setContentView(this,R.layout.activity_code);
        setSupportActionBar(activityCodeBinding.toolbar);

        //activityCodeBinding.toolbar.inflateMenu(R.menu.menu_code_editor);

        re = getResources();

        activityCodeViewModel = new ViewModelProvider(this,new ViewModelProvider.NewInstanceFactory()).get(ActivityCodeViewModel.class);
        codeEditorViewModel = new ViewModelProvider(this).get(CodeEditorViewModel.class);

        activityCodeBinding.setData(activityCodeViewModel);
        activityCodeBinding.setLifecycleOwner(this);


        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.actvity_code_nav_fragment);
        navController = navHostFragment.getNavController();
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (!hasCreatedMenu) return;
                int id = destination.getId();
                if(id==R.id.codeRunFragment){
                    activityCodeBinding.toolbar.getMenu().findItem(R.id.menu_code_editor_debug).setVisible(false);
                }else{
                    activityCodeBinding.toolbar.getMenu().findItem(R.id.menu_code_editor_debug).setVisible(true);
                }
            }
        });
        NavigationUI.setupActionBarWithNavController(this,navController);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_code_editor,menu);
        hasCreatedMenu = true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_code_editor_debug){
            navController.navigate(R.id.action_codeEditorFragment_to_codeRunFragment);
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(true){
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("您确定退出吗？您所进行的编辑将不会被保存。")
                        .setCancelable(false)
                        .setPositiveButton("确定", (dialog, which) -> {
                            finish();
                        })
                        .setNegativeButton("取消",null)
                        .create();
                alertDialog.show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
