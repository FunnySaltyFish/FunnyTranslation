package com.funny.translation.translate.database

import androidx.annotation.Keep
import androidx.paging.PagingSource
import androidx.room.*
import com.funny.translation.helper.JsonX
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationResult
import com.funny.translation.translate.findLanguageById

@Keep
@Entity(tableName = "table_trans_favorite")
data class TransFavoriteBean(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo
    val sourceString: String,
    @ColumnInfo
    val resultText: String,
    @ColumnInfo
    val sourceLanguageId: Int,
    @ColumnInfo
    val targetLanguageId: Int,
    @ColumnInfo
    val engineName: String,
    @ColumnInfo
    val time: Long = System.currentTimeMillis(),
) {
    companion object {
        fun fromTransResult(transResult: TranslationResult, sourceString: String, sourceLanguageId: Int) =
            TransFavoriteBean(
                sourceString = sourceString,
                resultText = transResult.basicResult.trans,
                sourceLanguageId = sourceLanguageId,
                targetLanguageId = transResult.targetLanguage?.id ?: Language.AUTO.id,
                engineName = transResult.engineName,
                time = System.currentTimeMillis(),
            )
    }
}

@Dao
interface TransFavoriteDao {
    @Query("select * from table_trans_favorite order by id desc")
    fun queryAllPaging(): PagingSource<Int, TransFavoriteBean>

    @Insert
    fun insertTransFavorite(transFavoriteBean: TransFavoriteBean)

    @Delete
    fun deleteTransFavorite(transFavoriteBean: TransFavoriteBean)

    @Query("""select count(1) from table_trans_favorite where 
        sourceString=:sourceString and resultText=:resultText and sourceLanguageId=:sourceLanguageId 
        and targetLanguageId=:targetLanguageId and engineName=:engineName""")
    fun count(sourceString: String, resultText: String, sourceLanguageId: Int, targetLanguageId: Int, engineName: String): Int
}

