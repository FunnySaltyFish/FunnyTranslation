package com.funny.translation.translate.ui.long_text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.translate.ui.widget.ExpandMoreButton
import com.funny.translation.ui.FixedSizeIcon


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
    content: @Composable (expanded: Boolean) -> Unit,
) {
    var expand by rememberStateOf(value = false)
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
        RichTooltipBox(
            text = {
                Text(text = helpText, style = MaterialTheme.typography.bodySmall)
            },
        ) {
            FixedSizeIcon(
                Icons.Default.QuestionMark, "", tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp).offset(4.dp, (-0).dp).tooltipAnchor()
            )
        }
        if (expandable) {
            ExpandMoreButton(modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),expand = expand, onClick = {
                expand = !expand
            })
        }
    }
    content(expand)
}