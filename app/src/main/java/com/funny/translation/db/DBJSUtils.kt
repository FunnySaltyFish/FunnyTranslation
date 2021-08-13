package com.funny.translation.db

import android.database.Cursor
import android.util.Log
import com.funny.translation.FunnyApplication
import com.funny.translation.bean.Consts
import com.funny.translation.js.bean.JsBean
import java.util.*

object DBJSUtils{
    private val dbHelper: DBJS
    private const val TAG = "DBJSUtils"
    private var mInstance: DBJSUtils? = null
    val lock = Any()
    fun queryAllEnabledJS(): ArrayList<JsBean> {
        synchronized(lock) {
            val db = dbHelper.readableDatabase
            db!!.beginTransaction()
            val results = ArrayList<JsBean>()
            val querySql =
                "select * from " + DBJS.TABLE_NAME + " where " + DBJS.COLUMN_ENABLED + " = 1;"
            var cursor: Cursor? = null
            try {
                cursor = db.rawQuery(querySql, null)
                while (cursor.moveToNext()) {
                    if (results.size > Consts.MAX_JS_NUMBER) break
                    val jsBean = JsBean(
                        cursor.getInt(DBJS.COLUMN_ID_INDEX),
                        cursor.getString(DBJS.COLUMN_FILENAME_INDEX),
                        cursor.getString(DBJS.COLUMN_CODE_INDEX),
                        cursor.getString(DBJS.COLUMN_AUTHOR_INDEX),
                        cursor.getInt(DBJS.COLUMN_VERSION_INDEX),
                        cursor.getString(DBJS.COLUMN_ABOUT_INDEX),
                        cursor.getInt(DBJS.COLUMN_ENABLED_INDEX)
                    )
                    results.add(jsBean)
                }
                db.setTransactionSuccessful()
                db.endTransaction()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
                if (db != null) {
                    db.close()
                }
            }
            return results
        }
    }

    fun queryAllJS(): ArrayList<JsBean> {
        synchronized(lock) {
            val db = dbHelper.readableDatabase
            db!!.beginTransaction()
            val results = ArrayList<JsBean>()
            val querySql = "select * from " + DBJS.TABLE_NAME
            var cursor: Cursor? = null
            try {
                cursor = db.rawQuery(querySql, null)
                while (cursor.moveToNext()) {
                    if (results.size > Consts.MAX_JS_NUMBER) break
                    val jsBean = JsBean(
                        cursor.getInt(DBJS.COLUMN_ID_INDEX),
                        cursor.getString(DBJS.COLUMN_FILENAME_INDEX),
                        cursor.getString(DBJS.COLUMN_CODE_INDEX),
                        cursor.getString(DBJS.COLUMN_AUTHOR_INDEX),
                        cursor.getInt(DBJS.COLUMN_VERSION_INDEX),
                        cursor.getString(DBJS.COLUMN_ABOUT_INDEX),
                        cursor.getInt(DBJS.COLUMN_ENABLED_INDEX)
                    )
                    results.add(jsBean)
                }
                db.setTransactionSuccessful()
                db.endTransaction()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
                if (db != null) {
                    db.close()
                }
            }
            return results
        }
    }

    fun setJSEnabled(id: Int, enabled: Int) {
        synchronized(lock) {
            val db = dbHelper.writableDatabase
            val set = String.format(
                Locale.CHINA, "UPDATE %s SET %s = %d WHERE %s = %d;",
                DBJS.TABLE_NAME, DBJS.COLUMN_ENABLED, enabled, DBJS.COLUMN_ID, id
            )
            db!!.execSQL(set)
            if (db != null) db.close()
        }
    }

    fun insertJS(jsBean: JsBean) {
        synchronized(lock) {
            val db = dbHelper.writableDatabase
            val insert = java.lang.String.format(
                Locale.CHINA,
                "INSERT INTO %s VALUES (%s, '%s', '%s', %d, '%s', '%s', %d);",
                DBJS.TABLE_NAME,
                null,
                jsBean.fileName,
                jsBean.code,
                jsBean.version,
                jsBean.author,
                jsBean.description,
                jsBean.enabled
            )
            db!!.execSQL(insert)
            if (db != null) db.close()
        }
    }

    fun deleteJS(jsBean: JsBean) {
        synchronized(lock) {
            val db = dbHelper.writableDatabase
            val delete = String.format(
                Locale.CHINA, "DELETE FROM %s WHERE %s = %d;",
                DBJS.TABLE_NAME, DBJS.COLUMN_ID, jsBean.id
            )
            db!!.execSQL(delete)
            if (db != null) db.close()
        }
    }

    val nextID: Int
        get() {
            var currentID = 0
            synchronized(lock) {
                val db = dbHelper.writableDatabase
                val next = String.format(
                    Locale.CHINA, "select * from %s order by %s desc limit 0,1;",
                    DBJS.TABLE_NAME, DBJS.COLUMN_ID
                )
                var cursor: Cursor? = null
                try {
                    cursor = db!!.rawQuery(next, null)
                    if (cursor.moveToNext()) {
                        currentID = cursor.getInt(DBJS.COLUMN_ID_INDEX)
                        Log.i(TAG, "currentID = $currentID")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    cursor?.close()
                }
                db?.close()
            }
            return currentID + 1
        }

    fun close() {
        val db = dbHelper.readableDatabase
        if (db != null && db.isOpen) {
            db.close()
        }
    }


    init {
        dbHelper = DBJS(FunnyApplication.getFunnyContext(), DBJS.DB_NAME, null, DBJS.DB_VERSION)
        try {
            val db = dbHelper.writableDatabase
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}