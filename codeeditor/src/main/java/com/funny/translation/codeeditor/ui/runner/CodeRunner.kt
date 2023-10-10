@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.translation.codeeditor.ui.runner

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.funny.translation.codeeditor.R
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.ui.FixedSizeIcon

private const val TAG = "CodeRunner"

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ComposeCodeRunner(
    navController: NavController,
    activityCodeViewModel: ActivityCodeViewModel
){
    val viewModel : CodeRunnerViewModel = viewModel()
    val verticalScrollState = rememberScrollState()
    DisposableEffect(key1 = TAG){
        Debug.addTarget(DefaultDebugTarget)
        onDispose {
            Debug.removeTarget(DefaultDebugTarget)
        }
    }
    Scaffold(
        topBar = { CodeRunnerTopBar(
            backAction = {
                navController.navigateUp()
            }
        ) }
    ) {
        SelectionContainer(modifier = Modifier.padding(it)) {
            CodeRunnerText(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(8.dp)
                    .verticalScroll(verticalScrollState),
                viewModel = viewModel,
                activityCodeViewModel = activityCodeViewModel
            )
        }
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
                FixedSizeIcon(Icons.Filled.ArrowBack,"Back")
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
    val code by activityCodeViewModel.codeState
    var shouldExecuteCode by activityCodeViewModel.shouldExecuteCode

    LaunchedEffect(shouldExecuteCode){
        //Log.d(TAG, "CodeRunnerText: $code")
        if (shouldExecuteCode) {
            viewModel.clearDebug()
            viewModel.initJs(activityCodeViewModel,code.toString())
            shouldExecuteCode = false
        }
    }

    val output = viewModel.outputDebug
    Text(
        output.value,
        modifier = modifier,
    )
}