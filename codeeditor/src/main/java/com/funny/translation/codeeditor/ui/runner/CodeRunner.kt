package com.funny.translation.codeeditor.ui.runner

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.funny.translation.codeeditor.R
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel

@Composable
fun ComposeCodeRunner(
    navController: NavController,
    activityCodeViewModel: ActivityCodeViewModel
){
    Scaffold(
        topBar = { CodeRunnerTopBar(
            backAction = {
                navController.navigateUp()
            }
        ) }
    ) {
        CodeRunnerText(
            modifier = Modifier.fillMaxWidth(),
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
    activityCodeViewModel: ActivityCodeViewModel
) {
    Text(
        activityCodeViewModel.codeState.value.toString(),
        modifier = modifier
    )
}