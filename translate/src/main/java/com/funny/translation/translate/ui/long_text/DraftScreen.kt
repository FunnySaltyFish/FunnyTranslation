package com.funny.translation.translate.ui.long_text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.compose.loading.DefaultEmpty
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.database.Draft
import com.funny.translation.translate.ui.main.SwipeToDismissItem
import com.funny.translation.translate.ui.widget.CommonPage

private const val TAG = "DraftScreen"

@Composable
fun DraftScreen() {
    CommonPage(title = stringResource(id = R.string.drafts)) {
        val vm: DraftViewModel = viewModel()
        val list by vm.draftList.collectAsState(initial = emptyList())
        val navController = LocalNavController.current
        LazyColumn {
            if (list.isNotEmpty()) {
                items(list, key = { it.id }) { draft ->
                    DraftItem(task = draft, onClick = {
                         navController.navigateToTextEdit(
                             TextEditorAction.UpdateDraft(draft.id, draft.content)
                         )
                    }, deleteTaskAction = vm::deleteDraft)
                }
            } else {
                item {
                    DefaultEmpty()
                }
            }
        }
    }
}

@Composable
private fun DraftItem(
    modifier: Modifier = Modifier,
    task: Draft,
    onClick: () -> Unit,
    deleteTaskAction: (Draft) -> Unit
) {
    SwipeToDismissItem(
        modifier = modifier,
        onDismissed = {
            deleteTaskAction(task)
        }
    ) {
        ListItem(
            modifier = Modifier.clickable(onClick = onClick),
            headlineContent = {
                Text(text = task.remark)
            },
            supportingContent = {
                Text(text = task.content, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        )
    }
}