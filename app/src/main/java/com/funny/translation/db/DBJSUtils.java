package com.funny.translation.db;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.funny.translation.FunnyApplication;
import com.funny.translation.bean.Consts;
import com.funny.translation.js.JS;

import java.util.ArrayList;
import java.util.Locale;

import static com.funny.translation.db.DBJS.*;
public class DBJSUtils {
    private final DBJS dbHelper;
    private static DBJSUtils mInstance;

    public static final Object lock=new Object();

    private DBJSUtils(){
        dbHelper = new DBJS(FunnyApplication.getFunnyContext(),DB_NAME,null,DB_VERSION);
        try{
            SQLiteDatabase db=dbHelper.getWritableDatabase();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static DBJSUtils getInstance(){
        if (mInstance==null){
            mInstance = new DBJSUtils();
        }
        return mInstance;
    }

    public ArrayList<JS> queryAllEnabledJS(){
        synchronized (lock) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            db.beginTransaction();
            ArrayList<JS> results = new ArrayList<>();
            String querySql = "select * from " + TABLE_NAME + " where " + COLUMN_ENABLED + " = 1;";
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(querySql, null);
                while (cursor.moveToNext()) {
                    if(results.size()> Consts.MAX_JS_NUMBER)break;
                    JS js = new JS(
                            cursor.getInt(COLUMN_ID_INDEX),
                            cursor.getString(COLUMN_FILENAME_INDEX),
                            cursor.getString(COLUMN_CODE_INDEX),
                            cursor.getInt(COLUMN_VERSION_INDEX),
                            cursor.getString(COLUMN_AUTHOR_INDEX),
                            cursor.getString(COLUMN_ABOUT_INDEX),
                            cursor.getInt(COLUMN_ENABLED_INDEX)
                    );
                    results.add(js);
                }
                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            }
            return results;
        }
    }

    public ArrayList<JS> queryAllJS(){
        synchronized (lock) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            db.beginTransaction();
            ArrayList<JS> results = new ArrayList<>();
            String querySql = "select * from " + TABLE_NAME;
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(querySql, null);
                while (cursor.moveToNext()) {
                    if(results.size()> Consts.MAX_JS_NUMBER)break;
                    JS js = new JS(
                            cursor.getInt(COLUMN_ID_INDEX),
                            cursor.getString(COLUMN_FILENAME_INDEX),
                            cursor.getString(COLUMN_CODE_INDEX),
                            cursor.getInt(COLUMN_VERSION_INDEX),
                            cursor.getString(COLUMN_AUTHOR_INDEX),
                            cursor.getString(COLUMN_ABOUT_INDEX),
                            cursor.getInt(COLUMN_ENABLED_INDEX)
                    );
                    results.add(js);
                }
                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            }
            return results;
        }
    }

    public void setJSEnabled(int id,int enabled){
        synchronized (lock){
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String set = String.format(Locale.CHINA,"UPDATE %s SET %s = %d WHERE %s = %d;",
                    TABLE_NAME,COLUMN_ENABLED,enabled,COLUMN_ID,id);
            db.execSQL(set);
            if(db!=null)db.close();
        }
    }

    public void insertJS(JS js){
        synchronized (lock){
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String insert = String.format(Locale.CHINA,"INSERT INTO %s VALUES (%s, '%s', '%s', %d, '%s', '%s', %d);",
                    TABLE_NAME,null,js.fileName,js.code,js.version,js.author,js.about,js.enabled);
            db.execSQL(insert);
            if(db!=null)db.close();
        }
    }

    public void close(){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        if(db!=null&&db.isOpen()){
            db.close();
        }
    }
}
