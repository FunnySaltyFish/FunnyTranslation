package com.funny.translation.translate.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.funny.translation.js.JsDao
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.bean.LanguageListConverter
import com.funny.translation.translate.FunnyApplication

val appDB by lazy{
    AppDatabase.createDatabase()
}

@Database(entities = [JsBean::class, TransHistoryBean::class], version = 4, autoMigrations = [])
@TypeConverters(LanguageListConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase(){
    abstract val jsDao : JsDao
    abstract val transHistoryDao: TransHistoryDao

    companion object{
        fun createDatabase() =
            Room.databaseBuilder(FunnyApplication.ctx, AppDatabase::class.java,"app_db.db")
                .addCallback(callback)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .build()

        /**
         * 迁移记录：
         * 1->2:更新Google翻译插件
         * 2->3:添加 targetSupportVersion 字段
         * 3->4:新增表 table_trans_history
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

        private val MIGRATION_3_4 = object : Migration(3,4){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""create table if not exists table_trans_history(
                    |id integer primary key autoincrement not null,
                    |sourceString text not null,
                    |sourceLanguageId integer not null default 0,
                    |targetLanguageId integer not null default 0,
                    |engineNames text not null default '[]',
                    |time integer not null)""".trimMargin())
            }
        }

        private val MIGRATION_4_5 = object : Migration(4,5){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""create table if not exists table_temp(
                    |id integer primary key autoincrement not null,
                    |sourceString text not null,
                    |sourceLanguageId integer not null default 0,
                    |targetLanguageId integer not null default 0,
                    |engineNames text not null default '[]',
                    |lastTime integer not null,
                    |times integer not null default 1)""".trimMargin())
                database.execSQL("""""")
            }
        }

        private val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }

}