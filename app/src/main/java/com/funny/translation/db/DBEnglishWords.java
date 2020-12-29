package com.funny.translation.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.funny.translation.FunnyApplication;
import com.funny.translation.utils.StringUtil;

import java.util.ArrayList;

public class DBEnglishWords {
    public static final String DB_NAME = "english_words.db";
    public static final String TABLE_NAME = "WORDS";
    private final int VERSION = 1;

    private final String COLUMN_WORD = "word";
    private final String COLUMN_PHONETIC_SYMBOL = "phonetic_symbols";
    private final String COLUMN_EXPLANATION = "explanation";

    private final int COLUMN_WORD_INDEX = 0;
    private final int COLUMN_PHONETIC_SYMBOL_INDEX = 1;
    private final int COLUMN_EXPLANATION_INDEX = 2;

    private final DBHelper dbHelper;
    private static DBEnglishWords mInstance;

    private final static int MAX_WORDS = 30;

    private DBEnglishWords(){
        dbHelper = new DBHelper(FunnyApplication.getFunnyContext(),DB_NAME,null,VERSION);
    }

    public static DBEnglishWords getInstance(){
        if (mInstance==null){
            mInstance = new DBEnglishWords();
        }
        return mInstance;
    }

    public void init(String words){
        synchronized (dbHelper){
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            try {
//                String createTable = String.format("CREATE TABLE %s (%s TEXT NOT NULL , %s TEXT NOT NULL,%s TEXT NOT NULL);"
//                        , TABLE_NAME, COLUMN_WORD, COLUMN_PHONETIC_SYMBOL, COLUMN_EXPLANATION);
                String createTable = String.format("CREATE VIRTUAL TABLE %s USING fts3(%s TEXT NOT NULL , %s TEXT NOT NULL,%s TEXT NOT NULL);"
                        , TABLE_NAME, COLUMN_WORD, COLUMN_PHONETIC_SYMBOL, COLUMN_EXPLANATION);
                database.execSQL(createTable);
                database.beginTransaction();
                String[] arr = words.split("\n");
                ContentValues contentValues = new ContentValues();
                for (String str : arr) {
                    if (str.length() > 1) {//是字母
                        int first = str.indexOf('[');
                        int last = str.lastIndexOf(']');
                        if (first > 0 && last > 0) {
                            Word word = new Word(
                                    StringUtil.replaceEnglishPunctuation(str.substring(0, first)),
                                    StringUtil.replaceEnglishPunctuation(str.substring(first, last + 1)),
                                    StringUtil.replaceEnglishPunctuation(str.substring(last + 1))
                            );
                            contentValues.clear();
                            contentValues.put(COLUMN_WORD,word.getWord());
                            contentValues.put(COLUMN_PHONETIC_SYMBOL,word.getPhoneticSymbol());
                            contentValues.put(COLUMN_EXPLANATION,word.getExplanation());
                            database.insert(TABLE_NAME,null,contentValues);
//                            String addString = String.format("INSERT INTO %s VALUES ('%s','%s','%s');",
//                                    TABLE_NAME,word.getWord(),word.getPhoneticSymbol(),word.getExplanation());
//                            database.execSQL(addString);
                        }
                    }
                }
                database.setTransactionSuccessful();
                database.endTransaction();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (database!=null){
                    database.close();
                }
            }
        }
    }

    public ArrayList<Word> queryWords(String startsWith){
        synchronized (dbHelper) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            ArrayList<Word> results = new ArrayList<>();
            if (!StringUtil.isValidContent(startsWith)) {
                return results;
            }
            //String tableName = startsWith.substring(0, 1).toUpperCase();
            //String querySql = "select * from " + TABLE_NAME + " where " + COLUMN_WORD + " like " + "'" + startsWith + "%';";
            String querySql = "select * from " + TABLE_NAME + " where " + COLUMN_WORD + " match '" + startsWith +"*';";
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(querySql, null);
                while (cursor.moveToNext()) {
                    if(results.size()>MAX_WORDS)break;
                    Word word = new Word(
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

    private void insertOneWord(String tableName,Word word){
        synchronized (dbHelper) {
            try (SQLiteDatabase database = dbHelper.getWritableDatabase()) {
                String sql = String.format("INSERT INTO %s (%s,%s,%s);", tableName, word.getWord(), word.getPhoneticSymbol(), word.getExplanation());
                database.execSQL(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class Word{
        String word;
        String phoneticSymbol;
        String explanation;

        public Word(String word, String phoneticSymbol, String explanation) {
            this.word = word;
            this.phoneticSymbol = phoneticSymbol;
            this.explanation = explanation;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getPhoneticSymbol() {
            return phoneticSymbol;
        }

        public void setPhoneticSymbol(String phoneticSymbol) {
            this.phoneticSymbol = phoneticSymbol;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }


    }

}
