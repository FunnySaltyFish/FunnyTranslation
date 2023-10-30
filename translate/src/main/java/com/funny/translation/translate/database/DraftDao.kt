package com.funny.translation.translate.database


import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Stable
@Keep
@Entity(tableName = "table_drafts")
data class Draft(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo
    val content: String,
    @ColumnInfo
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo
    val remark: String = ""
)

@Dao
interface DraftDao {
    // getAll
    @Query("select * from table_drafts order by timestamp desc")
    fun getAll(): Flow<List<Draft>>

    // upsert
    @Upsert
    fun upsert(draft: Draft)

    // delete
    @Delete
    fun delete(draft: Draft)
}