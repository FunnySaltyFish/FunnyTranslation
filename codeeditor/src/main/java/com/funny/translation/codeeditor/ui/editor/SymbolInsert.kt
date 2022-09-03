package com.funny.translation.codeeditor.ui.editor

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.unit.dp
import io.github.rosemoe.editor.widget.SymbolChannel

data class Symbol(val show:String,val insert:String)
const val TAG = "SymbolInsert"

@Composable
fun ComposeSymbolInsertItem(
    symbolChannel: SymbolChannel,
    symbol: Symbol
){
    Text(
        text = symbol.show,
        modifier = Modifier.fillMaxHeight()
            .clickable { symbolChannel.insertSymbol(symbol.insert, 1) }
            .padding(PaddingValues(horizontal = 12.dp,vertical = 0.dp))
    )
}