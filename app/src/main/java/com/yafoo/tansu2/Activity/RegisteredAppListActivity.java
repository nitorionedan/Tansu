package com.yafoo.tansu2.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.yafoo.tansu2.Adapter.RegisteredAppAdapter;
import com.yafoo.tansu2.Database.KeywordDatabaseHelper;
import com.yafoo.tansu2.Item.InstalledAppItem;
import com.yafoo.tansu2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yaden on 2017/12/23.
 */

public class RegisteredAppListActivity extends AppCompatActivity {

    public final static String ID_EXTRA_DRAWER = "com.yafoo.tansu2._ID_EXTRA_DRAWER";
    private final static String TAG = "myDEBUG(drawer)";

    private ListView listView;
    private ArrayList<String> registeredPackNames = new ArrayList<>();
    private RegisteredAppAdapter adapter;
    private ArrayList<InstalledAppItem> checkedApps = new ArrayList<>();
    private boolean isUpperDrawer = false;

    private KeywordDatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeredapplist);
        listView = (ListView) findViewById(R.id.listview_drawer);
        setTitle(R.string.title_registeredapp);

        isUpperDrawer = getIntent().getBooleanExtra(ID_EXTRA_DRAWER, false);

        // アクティビティのタイトル変更
        if (isUpperDrawer) {
            setTitle(R.string.title_upperdrawer);
        } else {
            setTitle(R.string.title_underdrawer);
        }

        // データベースインスタンス初期化
        databaseHelper = new KeywordDatabaseHelper(this);
        database = databaseHelper.getWritableDatabase();

        // アプリのチェックリスト
        checkedApps =  getCheckedAppList();

        // 登録アプリがなければメッセージ
        if (checkedApps.size() == 0) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.info_title)
                .setMessage(R.string.info_no_registered_app)
                .setPositiveButton(R.string.button_ok, (dialogInterface, which) -> {
                    finish();
                })
                .show();
        }

        // ポストされている通知のパッケージ名をすべて取得
        for (int i = 0; i < checkedApps.size(); ++i) {
            registeredPackNames.add(checkedApps.get(i).getPackageName());
        }

        adapter = new RegisteredAppAdapter(registeredPackNames, getKeywordListFromDB(), this, isUpperDrawer);
        listView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList<InstalledAppItem> getCheckedAppList() {
        ArrayList<InstalledAppItem> checkedApps = new ArrayList<>();
        checkedApps.clear();

        // インストール済アプリを取得
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppList = getPackageManager().queryIntentActivities(mainIntent, 0);

        // インストール済アプリの数だけアイテムを作成する
        ArrayList<InstalledAppItem> items = new ArrayList<>();
        for (int i = 0; i < pkgAppList.size(); ++i) {
            String appName = "";
            String packageName = "";
            try {
                appName = (String) getPackageManager()
                        .getApplicationLabel(pkgAppList.get(i).activityInfo.applicationInfo);
                packageName = pkgAppList.get(i).activityInfo.packageName;
                items.add(new InstalledAppItem(appName, packageName, false));
            } catch (Exception e) {
                Log.i(TAG, "App Name: " + appName + "\n" +
                "Pack name: " + packageName);
            }
        }

        // このアプリを省く
        items.removeIf(item-> item.getPackageName().contains("com.yafoo.tansu2"));

        // 名前順で並び替え
        Collections.sort(items, (installedAppItem, t1) -> installedAppItem.getTitle().compareTo(t1.getTitle()));

        // チェック状態をロード
        SharedPreferences sharedPrefs = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        for (int i = 0; i < items.size(); ++i) {
            boolean isCheck = sharedPrefs.getBoolean("CheckVal" + i, false);
            items.get(i).setChecked(isCheck);
        }

        // itemsのチェックされた項目のみ取得
        for (int i = 0; i < items.size(); ++i) {
            if (items.get(i).isChecked()) {
                checkedApps.add(items.get(i));
            }
        }

        return checkedApps;
    }

    private ArrayList<String> getKeywordListFromDB() {
        ArrayList<String> keywordList = new ArrayList<>();

        Cursor cursor = database.query(
                KeywordDatabaseHelper.DB_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); ++i) {
            keywordList.add(cursor.getString(0));
            cursor.moveToNext();
        }

        return keywordList;
    }
}