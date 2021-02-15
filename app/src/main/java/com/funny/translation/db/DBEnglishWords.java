package com.funny.translation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.funny.translation.FunnyApplication;
import com.funny.translation.utils.FileUtil;
import com.funny.translation.utils.StringUtil;

import java.io.File;
import java.io.IOException;

import static com.funny.translation.db.DBEnglishWordsUtils.*;

public class DBEnglishWords extends SQLiteOpenHelper {
    Context ctx;
    public DBEnglishWords(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.ctx=context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        synchronized (lock) {
            try {
                String words = FileUtil.getAssetsData(ctx, "english_words.txt");
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
                            contentValues.put(COLUMN_WORD, word.getWord());
                            contentValues.put(COLUMN_PHONETIC_SYMBOL, word.getPhoneticSymbol());
                            contentValues.put(COLUMN_EXPLANATION, word.getExplanation());
                            database.insert(TABLE_NAME, null, contentValues);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(database!=null) {
                    database.setTransactionSuccessful();
                    database.endTransaction();
                }

            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        synchronized (lock) {
            if (newVersion == 2) {//版本 2
                if (hasCreatedDB(DB_NAME)) {
                    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                }
                onCreate(sqLiteDatabase);
            }
        }
    }

    public static boolean hasCreatedDB(String dbName){
        File file = new File(FunnyApplication.getFunnyContext().getDatabasePath(dbName).getPath());
        return file.exists();
    }

    public static class Word{
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
