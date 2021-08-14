package com.funny.translation.codeeditor.ui.runner

import android.util.Log
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.funny.translation.codeeditor.R
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
private const val TAG = "CodeRunner"

@Composable
fun ComposeCodeRunner(
    navController: NavController,
    activityCodeViewModel: ActivityCodeViewModel
){
    val viewModel : CodeRunnerViewModel = viewModel()
    SideEffect {
        Debug.addTarget(DefaultDebugTarget)
    }
    Scaffold(
        topBar = { CodeRunnerTopBar(
            backAction = {
                navController.navigateUp()
            }
        ) }
    ) {
        CodeRunnerText(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(8.dp),
            viewModel = viewModel,
            activityCodeViewModel = activityCodeViewModel
        )
    }
}

@Composable
fun CodeRunnerTopBar(
    backAction : ()->Unit
){
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.code_run))
        },
        navigationIcon = {
            IconButton(onClick = backAction) {
                Icon(Icons.Filled.ArrowBack,"Back")
            }
        }
    )
}

@Composable
fun CodeRunnerText(
    modifier: Modifier,
    viewModel: CodeRunnerViewModel,
    activityCodeViewModel: ActivityCodeViewModel
) {
    val code = activityCodeViewModel.codeState.value
    LaunchedEffect(key1 = code){
        //Log.d(TAG, "CodeRunnerText: $code")
        viewModel.initJs(code.toString())
    }
    val output = viewModel.outputDebug.observeAsState("")
    Text(
        output.value,
        modifier = modifier
    )
}