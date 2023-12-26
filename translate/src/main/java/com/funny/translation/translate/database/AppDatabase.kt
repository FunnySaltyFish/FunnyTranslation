package com.funny.translation.translate.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.funny.compose.ai.bean.ChatMessage
import com.funny.translation.helper.Log
import com.funny.translation.js.JsDao
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.bean.LanguageListConverter
import com.funny.translation.translate.FunnyApplication

val appDB by lazy{
    AppDatabase.createDatabase()
}

@Database(
    entities = [JsBean::class, TransHistoryBean::class, TransFavoriteBean::class, LongTextTransTask::class, Draft::class, ChatMessage::class],
    version = 10,
    autoMigrations = []
)
@TypeConverters(
    LanguageListConverter::class, StringListConverter::class,
    TermListConverter::class, EditablePromptConverter::class,
    IntListConverter::class,
)
abstract class AppDatabase : RoomDatabase(){
    abstract val jsDao : JsDao
    abstract val transHistoryDao: TransHistoryDao
    abstract val transFavoriteDao: TransFavoriteDao
    abstract val longTextTransDao: LongTextTransDao
    abstract val draftDao: DraftDao
    abstract val chatHistoryDao: ChatHistoryDao

    companion object{
        // 获取当前时间，以毫秒为单位
        private const val now = "CAST(strftime('%s', 'now') AS INTEGER) * 1000"
        private const val TAG = "AppDatabase"

        fun createDatabase() =
            Room.databaseBuilder(FunnyApplication.ctx, AppDatabase::class.java,"app_db.db")
                .addCallback(callback)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .addMigrations(MIGRATION_6_7)
                .addMigrations(MIGRATION_7_8)
                .addMigrations(MIGRATION_8_9)
                .addMigrations(MIGRATION_9_10)
                .build()

        abstract class CustomMigration(startVersion: Int, endVersion: Int): Migration(startVersion, endVersion) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "migrate: try to migrate from $startVersion to $endVersion")
            }
        }

        /**
         * 迁移记录：
         * 1->2:更新Google翻译插件
         * 2->3:添加 targetSupportVersion 字段
         * 3->4:新增表 table_trans_history
         * 4->5:去除无用的主键 id，解决一些id重复导致的bug
         * 5-6:新增表 table_trans_favorite
         * 6-7:新增表 table_long_text_trans_tasks
         * 7-8:新增表 table_draft，为 table_long_text_trans_tasks 添加一列 “备注”
         * 8-9:为 table_long_text_trans_tasks 添加 createTime 和 updateTime
         */
        private val MIGRATION_1_2 = object : CustomMigration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                super.migrate(database)
                database.execSQL("delete from table_js")
            }
        }

        private val MIGRATION_2_3 = object : CustomMigration(2,3){
            override fun migrate(database: SupportSQLiteDatabase) {
                super.migrate(database)
                database.execSQL("delete from table_js where id=1")
                database.execSQL("alter table table_js add column targetSupportVersion integer not null default 4")
            }
        }

        private val MIGRATION_3_4 = object : CustomMigration(3,4){
            override fun migrate(database: SupportSQLiteDatabase) {
                super.migrate(database)
                database.execSQL("""create table if not exists table_trans_history(
                    |id integer primary key autoincrement not null,
                    |sourceString text not null,
                    |sourceLanguageId integer not null default 0,
                    |targetLanguageId integer not null default 0,
                    |engineNames text not null default '[]',
                    |time integer not null)""".trimMargin())
            }
        }

        private val MIGRATION_4_5 = object : CustomMigration(4,5){
            override fun migrate(database: SupportSQLiteDatabase) {
                super.migrate(database)
                database.execSQL("CREATE TABLE IF NOT EXISTS `table_js_temp` (`id` INTEGER NOT NULL,`fileName` TEXT PRIMARY KEY NOT NULL, `code` TEXT NOT NULL, `author` TEXT NOT NULL, `version` INTEGER NOT NULL, `description` TEXT NOT NULL, `enabled` INTEGER NOT NULL, `minSupportVersion` INTEGER NOT NULL, `maxSupportVersion` INTEGER NOT NULL, `targetSupportVersion` INTEGER NOT NULL, `isOffline` INTEGER NOT NULL, `debugMode` INTEGER NOT NULL, `supportLanguages` TEXT NOT NULL);")
                database.execSQL("insert into table_js_temp select * from table_js;")
                database.execSQL("drop table table_js;")
                database.execSQL("ALTER TABLE table_js_temp RENAME TO table_js;")
            }
        }

        // create table_trans_favorite
        private val MIGRATION_5_6 = object : CustomMigration(5, 6){
            override fun migrate(database: SupportSQLiteDatabase) {
                super.migrate(database)
                database.execSQL("""create table if not exists table_trans_favorite(
                    |id integer primary key autoincrement not null,
                    |sourceString text not null,
                    |resultText text not null,
                    |sourceLanguageId integer not null default 0,
                    |targetLanguageId integer not null default 0,
                    |engineName text not null default '未知引擎',
                    |time integer not null)""".trimMargin())
            }
        }

        // create table_long_text_trans_tasks
        private val MIGRATION_6_7 = object : CustomMigration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                super.migrate(database)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `table_long_text_trans_tasks` " +
                            "(`id` TEXT PRIMARY KEY NOT NULL, `chatBotId` INTEGER NOT NULL, `sourceText` TEXT NOT NULL, " +
                            "`resultText` TEXT NOT NULL, `prompt` TEXT NOT NULL, `allCorpus` TEXT NOT NULL, " +
                            "`sourceTextSegments` TEXT NOT NULL, `resultTextSegments` TEXT NOT NULL, " +
                            "`translatedLength` INTEGER NOT NULL)"
                )
            }
        }

        // create table_draft
        private val MIGRATION_7_8 = object : CustomMigration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                super.migrate(database)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `table_drafts` " +
                            "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `remark` TEXT NOT NULL DEFAULT '')"
                )
                // 为 table_long_text_trans_tasks 添加一列 “备注”
                database.execSQL("ALTER TABLE table_long_text_trans_tasks ADD COLUMN `remark` TEXT NOT NULL DEFAULT ''")
            }
        }

        // 为 table_long_text_trans_tasks 添加 createTime 和 updateTime
        private val MIGRATION_8_9 = object : CustomMigration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                super.migrate(database)
                // 由于新增列无法设置默认值，所以先创建列，再更新值，再创建触发器
                database.execSQL(
                    "ALTER TABLE table_long_text_trans_tasks ADD COLUMN `createTime` INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE table_long_text_trans_tasks ADD COLUMN `updateTime` INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "UPDATE table_long_text_trans_tasks SET createTime = $now, updateTime = $now"
                )
                database.execSQL("""
                    -- 创建触发器
                    CREATE TRIGGER update_long_trans_tasks_trigger AFTER UPDATE OF chatBotId, sourceText, resultText, prompt, allCorpus, sourceTextSegments, resultTextSegments, remark ON table_long_text_trans_tasks
                    BEGIN
                        UPDATE table_long_text_trans_tasks SET updateTime = $now WHERE id = NEW.id;
                    END;
                """.trimIndent())
            }
        }

        // 创建 table_chat_history
        private val MIGRATION_9_10 = object : CustomMigration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                super.migrate(database)
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `table_chat_history` " +
                            "(`id` TEXT PRIMARY KEY NOT NULL, `botId` INTEGER NOT NULL, `conversationId` TEXT NOT NULL, " +
                            "`sender` TEXT NOT NULL, `content` TEXT NOT NULL, `type` INTEGER NOT NULL, `error` TEXT, " +
                            "`timestamp` INTEGER NOT NULL)"
                )
            }
        }

        private val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }

}