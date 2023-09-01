package com.funny.translation.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.funny.translation.core.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permission(
    permission: String = android.Manifest.permission.CAMERA,
    description: String,
    content: @Composable () -> Unit = { }
) {
    val permissionState = rememberPermissionState(permission)
    if (permissionState.status.isGranted){
        content()
    } else {
        Rationale(text = description) {
            permissionState.launchPermissionRequest()
        }
    }
}

@Composable
private fun Rationale(
    text: String,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Don't */ },
        title = {
            Text(text = stringResource(id = R.string.request_permission))
        },
        text = {
            Text(text)
        },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text(stringResource(id = R.string.confirm))
            }
        }
    )
}