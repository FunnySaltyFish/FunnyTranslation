package com.funny.translation.translate.ui.long_text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltipBox
import androidx.compose.material3.RichTooltipState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.widget.ExpandMoreButton
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.launch


/**
 * 可展开的 Category
 * @receiver ColumnScope
 * @param title String
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColumnScope.Category(
    title: String,
    helpText: String = "helpText",
    expandable: Boolean = true,
    defaultExpand: Boolean = false,
    extraRowContent: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (expanded: Boolean) -> Unit,
) {
    var expand by rememberStateOf(value = defaultExpand)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        val tooltipState = remember {  RichTooltipState() }
        val scope = rememberCoroutineScope()
        RichTooltipBox(
            text = {
                Text(text = helpText, style = MaterialTheme.typography.bodySmall)
            },
            tooltipState = tooltipState,
            action = {
                // Close
                TextButton(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End), onClick = {
                    scope.launch {
                        tooltipState.dismiss()
                    }
                }) {
                    Text(text = stringResource(id = R.string.close))
                }
            }
        ) {
            FixedSizeIcon(
                Icons.Default.QuestionMark, "", tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(14.dp)
                    .offset(4.dp, (-0).dp)
                    .tooltipAnchor()
            )
        }
        Row(modifier = Modifier
            .weight(1f)
            .padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (extraRowContent != null) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.labelSmall
                ) {
                    extraRowContent()
                }
            }
        }
        if (expandable) {
            ExpandMoreButton(modifier = Modifier,expand = expand, onClick = {
                expand = !expand
            })
        }
    }
    content(expand)
}