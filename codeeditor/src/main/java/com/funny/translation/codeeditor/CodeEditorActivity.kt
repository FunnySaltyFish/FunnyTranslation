package com.funny.translation.codeeditor

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.funny.translation.codeeditor.base.BaseActivity
import com.funny.translation.codeeditor.databinding.ActivityCodeBinding
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import com.funny.translation.codeeditor.vm.CodeEditorViewModel

class CodeEditorActivity : BaseActivity(){
    private val activityCodeViewModel by viewModels<ActivityCodeViewModel>()
    val codeEditorViewModel by viewModels<CodeEditorViewModel>()

    lateinit var activityCodeBinding : ActivityCodeBinding

    lateinit var nav: NavController

    var hasCreatedMenu = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCodeBinding = DataBindingUtil.setContentView(this, R.layout.activity_code)
        setSupportActionBar(activityCodeBinding.toolbar)

        activityCodeBinding.toolbar.inflateMenu(R.menu.menu_code_editor);
        activityCodeBinding.data = activityCodeViewModel
        activityCodeBinding.lifecycleOwner = this


        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.activity_code_nav_fragment) as NavHostFragment
        nav = navHostFragment.navController
        nav.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                if (!hasCreatedMenu) return
                val id: Int = destination.id
                if (id == R.id.codeRunFragment) {
                    activityCodeBinding.toolbar.menu.findItem(R.id.menu_code_editor_debug).isVisible =
                        false
                } else {
                    activityCodeBinding.toolbar.menu.findItem(R.id.menu_code_editor_debug).isVisible =
                        true
                }
            }
        })
        NavigationUI.setupActionBarWithNavController(this, nav)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_code_editor, menu)
        hasCreatedMenu = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_code_editor_debug) {
            nav.navigate(R.id.action_codeEditorFragment_to_codeRunFragment)
        }
        return (NavigationUI.onNavDestinationSelected(item, nav)
                || super.onOptionsItemSelected(item))
    }

    override fun onSupportNavigateUp(): Boolean {
        return nav.navigateUp()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (true) {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("您确定退出吗？您所进行的编辑将不会被保存。")
                    .setCancelable(false)
                    .setPositiveButton(
                        "确定"
                    ) { dialog: DialogInterface?, which: Int -> finish() }
                    .setNegativeButton("取消", null)
                    .create()
                alertDialog.show()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}