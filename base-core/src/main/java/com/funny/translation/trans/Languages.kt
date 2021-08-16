package com.funny.translation.trans

enum class Language(val id : Int) {
    AUTO(0),
    CHINESE(1),
    ENGLISH(2),
    JAPANESE(3),
    KOREAN(4),
    FRENCH(5),
    RUSSIAN(6),
    GERMANY(7),
    WENYANWEN(8),
    THAI(9)
}

fun findLanguageById(id : Int) = Language.values().filter { it.id == id }[0]
