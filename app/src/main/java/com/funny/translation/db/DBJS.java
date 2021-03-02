package com.funny.translation.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBJS extends SQLiteOpenHelper {
    public static final Object lock = new Object();
    public static final String DB_NAME = "js.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "dbJS";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FILE_NAME = "fileName";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_ABOUT = "about";
    public static final String COLUMN_VERSION = "version";
    public static final String COLUMN_ENABLED = "enabled";


    public static final int COLUMN_ID_INDEX = 0;
    public static final int COLUMN_FILENAME_INDEX = 1;
    public static final int COLUMN_CODE_INDEX = 2;
    public static final int COLUMN_VERSION_INDEX = 3;
    public static final int COLUMN_AUTHOR_INDEX = 4;
    public static final int COLUMN_ABOUT_INDEX = 5;
    public static final int COLUMN_ENABLED_INDEX = 6;

    Context ctx;
    public DBJS(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        synchronized (lock){
            String createTable = String.format("create table if not exists %s(%s integer primary key autoincrement,%s text,%s text,%s integer,%s text,%s text,%s integer);",
                    TABLE_NAME,COLUMN_ID,COLUMN_FILE_NAME,COLUMN_CODE,COLUMN_VERSION,COLUMN_AUTHOR,COLUMN_ABOUT,COLUMN_ENABLED);
            try{
                sqLiteDatabase.execSQL(createTable);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
