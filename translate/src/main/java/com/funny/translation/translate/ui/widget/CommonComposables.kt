package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.funny.translation.helper.SimpleAction
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R

/**
 * CommonPage，有一个 TopBar 以及剩余内容，被 Column 包裹
 * @param modifier Modifier
 * @param title String?
 * @param navController NavHostController
 * @param navigationIcon [@androidx.compose.runtime.Composable] Function0<Unit>
 * @param actions TopBar 的 actions
 * @param content 主要内容
 */
@Composable
fun CommonPage(
    modifier: Modifier = Modifier,
    title: String? = null,
    navController: NavHostController = LocalNavController.current,
    navigationIcon: @Composable () -> Unit = { CommonNavBackIcon(navController) },
    actions: @Composable RowScope.() -> Unit = { },
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonTopBar(title = title, navigationIcon = navigationIcon, navController = navController, actions = actions)
        content()
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    modifier: Modifier = Modifier,
    title: String?,
    navController: NavHostController = LocalNavController.current,
    navigationIcon: @Composable () -> Unit = { CommonNavBackIcon(navController) },
    actions: @Composable RowScope.() -> Unit = { },
) {
    TopAppBar(
        modifier = modifier,
        title = {
            if (title != null) {
                Text(text = title, Modifier.padding(start = 12.dp))
            }
        },
        navigationIcon = navigationIcon,
        actions = {
            actions()
            Spacer(modifier = Modifier.width(12.dp))
        }
    )
}

@Composable
fun CommonNavBackIcon(
    navController: NavHostController = LocalNavController.current,
    navigateBackAction: SimpleAction = { navController.popBackStack() }
) {
    IconButton(onClick = navigateBackAction) {
        Icon(
            Icons.Default.ArrowBack,
            contentDescription = stringResource(id = R.string.back)
        )
    }
}