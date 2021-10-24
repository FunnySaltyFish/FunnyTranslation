package com.funny.translation.js.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.funny.translation.trans.Language
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "table_js")
@TypeConverters(LanguageListConverter::class)
data class JsBean(
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0,
    var fileName : String = "Plugin",
    var code : String = "",
    var author : String = "Author",
    var version : Int = 1,
    var description : String = "",
    var enabled : Int = 1,
    var minSupportVersion : Int = 2,
    var maxSupportVersion : Int = 2,
    var isOffline : Boolean = false,
    var debugMode : Boolean = true,
    var supportLanguages: List<Language> = arrayListOf()
){
    fun toSQL() = """
        insert into table_js($id,$fileName,$code,$author,$version,$description,$enabled,
        $minSupportVersion,$maxSupportVersion,$isOffline,$debugMode)
        )
    """.trimIndent()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JsBean

        if (id != other.id) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + fileName.hashCode()
        return result
    }


}

class LanguageListConverter{
    @TypeConverter
    fun languagesToJson(languages : List<Language>) : String = Gson().toJson(languages)

    @TypeConverter
    fun jsonToLanguages(json : String) : List<Language> {
        val typeToken = object : TypeToken<List<Language>>(){}.type
        return Gson().fromJson(json, typeToken)
    }
}