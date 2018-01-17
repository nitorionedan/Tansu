package com.yafoo.tansu2.Activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.yafoo.tansu2.Adapter.KeywordRegisterAdapter;
import com.yafoo.tansu2.Database.KeywordDatabaseHelper;
import com.yafoo.tansu2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by expriment on 2017/12/30.
 */

public class KeywordRegisterActivity extends AppCompatActivity {
    private static final String TAG = "myDEBUG(kdRegister)";

    private ListView listView;
    private ArrayList<String> keywordList = new ArrayList<>();
    private Context context;
    private KeywordRegisterAdapter keywordRegisterAdapter;

    // データベース
    private KeywordDatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keywordregister);
        setTitle(R.string.title_keywordregister);

        context = this;
        listView = (ListView) findViewById(R.id.listview_keyword);
        databaseHelper = new KeywordDatabaseHelper(this);

        if (getKeywordCount() == 0) {
            Toast.makeText(context, R.string.toast_no_keyword, Toast.LENGTH_LONG);
        }

        // データベース
        keywordList = getKeywordListFromDB();

        // アダプター
        keywordRegisterAdapter = new KeywordRegisterAdapter(this, this, keywordList);

        // 登録ボタン
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.keywordregister_fab);
        fab.setOnClickListener(view -> {
            // キーワード入力
            EditText editText = new EditText(KeywordRegisterActivity.this);

            new AlertDialog.Builder(KeywordRegisterActivity.this)
                    .setView(editText)
                    // 登録ボタン
                    .setPositiveButton(R.string.button_add, (dialogInterface, i) -> {
                        writeKeywordToDB(editText.getText().toString());
                        refactorListView();
                    })
                    .setNegativeButton(R.string.button_cancel, null)
                    .show();
        });

        // クリックイベント
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            Toast.makeText(context, R.string.button_ok, Toast.LENGTH_SHORT).show();
            Log.i(TAG, Integer.toString(position));
        });

        listView.setAdapter(keywordRegisterAdapter);
    }

    private ArrayList<String> getKeywordListFromDB() {
        ArrayList<String> keywordList = new ArrayList<>();

        database = databaseHelper.getReadableDatabase();
        cursor = database.query(
                KeywordDatabaseHelper.DB_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        // データベースから取得
        for (int i = 0; i < cursor.getCount(); ++i) {
            keywordList.add(cursor.getString(0));
            cursor.moveToNext();
        }

        cursor.close();
        database.close();

        // 名前を降順で並び替える
        Collections.sort(keywordList, (keyword, t1) -> keyword.compareTo(t1));

        return keywordList;
    }

    private void writeKeywordToDB(String keyword) {
        String whereClause = "keyword=?";
        String[] whereArgs = { keyword };
        database = databaseHelper.getReadableDatabase();
        cursor = database.query(
                KeywordDatabaseHelper.DB_TABLE,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        // 重複したキーワードがあれば
        if (cursor.getCount() > 0) {
            Toast.makeText(context, R.string.toast_registered_keyword, Toast.LENGTH_LONG).show();
            cursor.close();
            database.close();
            return;
        }

        cursor.close();
        database.close();

        // 登録処理
        database = databaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            final int columnCount = 1;
            final ContentValues values = new ContentValues(columnCount);
            values.clear();
            values.put("keyword", keyword);

            database.insert(KeywordDatabaseHelper.DB_TABLE, null, values);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        } finally {
            database.endTransaction();
        }
        database.close();
    }

    private int getKeywordCount() {
        int count = 0;
        database = databaseHelper.getReadableDatabase();
        cursor = database.query(
                KeywordDatabaseHelper.DB_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
        );
        count = cursor.getCount();
        cursor.close();
        database.close();

        return count;
    }

    private void refactorListView() {
        finish();
        startActivity(getIntent());
    }

    public static void refresh(Activity activity, Context context) {
        activity.finish();
        Intent intent = new Intent(context, KeywordRegisterActivity.class);
        activity.startActivity(intent);
    }
}
