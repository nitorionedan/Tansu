package com.yafoo.tansu2.Database;

/**
 * Created by yaden on 2017/12/23.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by yaden on 2017/12/04.
 */

public class KeywordDatabaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = "myDatabaseHelper";
    public static final String DB_NAME = "tansuDB.db";
    public static final String DB_TABLE = "keywordTable";
    public static final int DB_VERSION = 4; //< ver3, 2017.12.27
    private Context context;

    // データベースを作成、または開く、管理するための処理
    public KeywordDatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        // テーブルの作成
        sqLiteDatabase.execSQL (
            "CREATE TABLE IF NOT EXISTS " + DB_TABLE + " (keyword text)"
        );
        Log.i(TAG, "テーブル作ったで");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer)
    {
        // 古いバージョンのテーブルが存在するならこれを削除
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
        // 新規
        onCreate(sqLiteDatabase);
        Log.i(TAG, "onUpgrade()");
    }
}
