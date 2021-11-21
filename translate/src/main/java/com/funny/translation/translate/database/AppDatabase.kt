package com.funny.translation.translate.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.funny.translation.js.JsDao
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.bean.LanguageListConverter
import com.funny.translation.translate.FunnyApplication

val appDB by lazy{
    AppDatabase.createDatabase()
}

@Database(entities = [JsBean::class], version = 3)
@TypeConverters(LanguageListConverter::class)
abstract class AppDatabase : RoomDatabase(){
    abstract val jsDao : JsDao

    companion object{
        fun createDatabase() =
            Room.databaseBuilder(FunnyApplication.ctx, AppDatabase::class.java,"app_db.db")
                .addCallback(callback)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build()

        /**
         * 迁移记录：
         * 1->2:更新Google翻译插件
         * 2->3:添加 targetSupportVersion 字段
         */
        private val MIGRATION_1_2 = object : Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("delete from table_js")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2,3){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("delete from table_js where id=1")
                database.execSQL("alter table table_js add column targetSupportVersion integer not null default 4")
            }
        }

        private val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }

}