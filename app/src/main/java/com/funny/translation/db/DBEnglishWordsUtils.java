package com.funny.translation.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.funny.translation.FunnyApplication;
import com.funny.translation.utils.StringUtil;

import java.util.ArrayList;

public class DBEnglishWordsUtils {
    public static final String DB_NAME = "english_words.db";
    public static final String TABLE_NAME = "WORDS";
    //1 :第一版
    //2 :更新了部分单词的内容，防止单词过长
    public final int VERSION = 2;


    public final static String COLUMN_WORD = "word";
    public final static String COLUMN_PHONETIC_SYMBOL = "phonetic_symbols";
    public final static String COLUMN_EXPLANATION = "explanation";

    public final static int COLUMN_WORD_INDEX = 0;
    public final static int COLUMN_PHONETIC_SYMBOL_INDEX = 1;
    public final static int COLUMN_EXPLANATION_INDEX = 2;

    private final DBEnglishWords dbHelper;
    private static DBEnglishWordsUtils mInstance;

    public static Object lock=new Object();

    private final static int MAX_WORDS = 30;

    private DBEnglishWordsUtils(){
        dbHelper = new DBEnglishWords(FunnyApplication.getFunnyContext(),DB_NAME,null,VERSION);
        try{
            SQLiteDatabase db=dbHelper.getWritableDatabase();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static DBEnglishWordsUtils getInstance(){
        if (mInstance==null){
            mInstance = new DBEnglishWordsUtils();
        }
        return mInstance;
    }

    public boolean shouldUpdate(){
        return VERSION>dbHelper.getWritableDatabase().getVersion();
    }

    public ArrayList<DBEnglishWords.Word> queryWords(String startsWith){
        synchronized (lock) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            ArrayList<DBEnglishWords.Word> results = new ArrayList<>();
            if (!StringUtil.isValidContent(startsWith)) {
                return results;
            }
            String querySql = "select * from " + TABLE_NAME + " where " + COLUMN_WORD + " match '" + startsWith +"*';";
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(querySql, null);
                while (cursor.moveToNext()) {
                    if(results.size()>MAX_WORDS)break;
                    DBEnglishWords.Word word = new DBEnglishWords.Word(
                            cursor.getString(COLUMN_WORD_INDEX),
                            cursor.getString(COLUMN_PHONETIC_SYMBOL_INDEX),
                            cursor.getString(COLUMN_EXPLANATION_INDEX)
                    );
                    results.add(word);
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

    public void close(){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        if(db!=null&&db.isOpen()){
            db.close();
        }
    }
}
