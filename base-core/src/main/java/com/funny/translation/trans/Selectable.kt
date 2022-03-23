package com.funny.translation.trans

interface Selectable {
    var selected : Boolean
}

class SelectableImpl : Selectable{
    override var selected: Boolean = true
}