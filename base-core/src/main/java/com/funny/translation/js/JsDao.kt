package com.funny.translation.js

import androidx.lifecycle.LiveData
import androidx.room.*
import com.funny.translation.js.bean.JsBean
import kotlinx.coroutines.flow.Flow

@Dao
interface JsDao {
    @Query("select * from table_js")
    fun getAllJs() : Flow<List<JsBean>>

    @Query("select * from table_js where enabled > 0")
    fun getEnabledJs() : Flow<List<JsBean>>

    @Query("select count(*) from table_js")
    fun getJsCount() : Int

    @Query("select * from table_js where fileName=(:name)")
    fun queryJsByName(name : String) : JsBean?

    @Insert
    fun insertJs(jsBean: JsBean)

    @Insert
    fun insertJsList(jsBeans: List<JsBean>)

    @Delete
    fun deleteJs(jsBean: JsBean)

    @Query("delete from table_js where fileName = (:fileName)")
    fun deleteJsByName(fileName: String)

    @Update
    fun updateJs(jsBean: JsBean)
}