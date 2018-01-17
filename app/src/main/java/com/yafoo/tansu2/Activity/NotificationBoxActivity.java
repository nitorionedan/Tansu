package com.yafoo.tansu2.Activity;

/**
 * Created by yaden on 2017/12/23.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yafoo.tansu2.Adapter.NotificationBoxAdapter;
import com.yafoo.tansu2.Database.KeywordDatabaseHelper;
import com.yafoo.tansu2.Database.NotificationDatabaseHelper;
import com.yafoo.tansu2.Item.InstalledAppItem;
import com.yafoo.tansu2.Item.NotificationBoxItem;
import com.yafoo.tansu2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yaden on 2017/11/30.
 */

public class NotificationBoxActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {
    public static final String ID_EXTRA_PACKNAME = "com.yafoo.tansu2._ID_EXTRA_PACKNAME";
    public static final String ID_EXTRA_ISUPPER = "com.yafoo.tansu2.ID_EXTRA_ISUPPER";
    public static final String ID_EXTRA_KEYWORD = "com.yafoo.tansu2._ID_EXTRA_KEYWORD";
    private static final String TAG = "myDEBUG(notifBox)";

    private ListView listView;
    private NotificationBoxAdapter adapter;

    private ArrayList<Column> columns = new ArrayList<>();
    private ArrayList<Column> checkedColumns = new ArrayList<>();
    private ArrayList<InstalledAppItem> checkedApps = new ArrayList<>();
    private ArrayList<String> keywordList = null;

    private boolean isUpperDrawer = false;
    private String appNameForSearch;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificationbox);
        listView = (ListView) findViewById(R.id.listview_notificationbox);

        // パッケージ名、キーワード、段の種類を取得
        Bundle extras = getIntent().getExtras();
        appNameForSearch = extras.getString(ID_EXTRA_PACKNAME);
        isUpperDrawer = extras.getBoolean(ID_EXTRA_ISUPPER);
        keywordList = extras.getStringArrayList(ID_EXTRA_KEYWORD);

        checkedApps = getCheckList();

        // タイトル設定
        if (isUpperDrawer){
            setTitle(R.string.title_stared_notifbox);
        } else {
            setTitle(R.string.title_all_notifbox);
        }

        // 溜まった通知がないとき
        if (getNotificationCount() == 0) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_info_outline)
                    .setTitle(R.string.info_title)
                    .setMessage(R.string.info_no_notification)
                    .setPositiveButton(R.string.button_ok, (dialogInterface, which) -> finish())
                    .show();
            return;
        }

        // 登録すみパッケージ名を取得
        ArrayList<String> checkedPackage = new ArrayList<>();
        for (int i = 0; i < checkedApps.size(); ++i) {
            checkedPackage.add(checkedApps.get(i).getPackageName());
        }

        // もしチェックがされていなければ
        final int chekedAppCount = checkedPackage.size();
        if (chekedAppCount == 0) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_info_outline)
                    .setTitle(R.string.info_title)
                    .setMessage(R.string.info_no_registered_app)
                    .setNegativeButton(R.string.button_close, (dialog, which) -> finish())
                    .setNeutralButton(R.string.button_register, (dialog, which) -> {
                        finish();
                        Intent intent = new Intent(NotificationBoxActivity.this, AppRegisterActivity.class);
                        startActivity(intent);
                    })
                    .show();
            return;
        }

        // 上段のときキーワードが無ければ
        if (isUpperDrawer && keywordList.size() == 0) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_info_outline)
                    .setTitle(R.string.info_title)
                    .setMessage(R.string.info_no_stared_notification)
                    .setPositiveButton(R.string.button_ok, (dialogInterface, which) -> finish())
                    .show();
            return;
        }

        // 溜まった通知を取得
        if (isUpperDrawer) {
            getNotificationByKeywrodFromDB();
        } else {
            getNotificationFromDB();
        }

        // 日付で並び替え
        Collections.sort(checkedColumns, (checkedColumn, t1) ->
                t1.date.compareTo(checkedColumn.date)
        );

        Collections.reverse(columns);

        // 通知の情報を整理
        ArrayList<NotificationBoxItem> notifItems = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            NotificationBoxItem notifItem = new NotificationBoxItem();

            // アプリ名を取得
            ApplicationInfo ai;
            try {
                ai = getPackageManager().getApplicationInfo(columns.get(i).packageName, 0);
            } catch (Exception e) {
                ai = null;
            }
            final String appName = (String) (ai != null ? getPackageManager().getApplicationLabel(ai) : "(unknown app)");
            // アイコン生成
            Drawable drawableIcon = null;
            try {
                drawableIcon = getPackageManager().getApplicationIcon(columns.get(i).packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.i("myDEBUG(icon)", e.toString());
            }

            notifItem.setTitle(appName);
            notifItem.setIcon(drawableIcon);
            notifItem.setText(columns.get(i).tickerText);
            notifItem.setDateText(columns.get(i).date);
            notifItem.setPackageName(columns.get(i).packageName);
            notifItem.setDate(columns.get(i).year, columns.get(i).month, columns.get(i).day);
            notifItems.add(notifItem);
        }

        // アダプター
        adapter = new NotificationBoxAdapter(this, this, notifItems);

        // リストビュー
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            showAdvancedNotification(notifItems, position);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(NotificationBoxActivity.this, ((TextView) view).getText(), Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList<InstalledAppItem> getCheckList() {
        ArrayList<InstalledAppItem> checkedApps = new ArrayList<>();

        // インストール済アプリを取得
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppList = getPackageManager().queryIntentActivities(mainIntent, 0);

        // インストール済アプリの数だけアイテムを作成する
        ArrayList<InstalledAppItem> items = new ArrayList<>();
        for (int i = 0; i < pkgAppList.size(); ++i) {
            String appName = (String) getPackageManager()
                    .getApplicationLabel(pkgAppList.get(i).activityInfo.applicationInfo);
            String packageName = pkgAppList.get(i).activityInfo.packageName;
            items.add(new InstalledAppItem(appName, packageName, false));
        }

        // このアプリを省く
        items.removeIf(item-> item.getPackageName().contains("com.yafoo.tansu2"));

        // 名前順で並び替え
        Collections.sort(items, (installedAppItem, t1) ->
                installedAppItem.getTitle().compareTo(t1.getTitle()));

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

        return  checkedApps;
    }

    // すべて通知を取得する
    private void getNotificationByKeywrodFromDB() {
        // データベースオブジェクト作成
        NotificationDatabaseHelper databaseHelper = new NotificationDatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor;

        // WHERE文発行
        String whereClause = "packagename = ? AND (";
        for (int i = 0; i < keywordList.size(); ++i) {
            if (i != 0) { // 先頭以外なら
                whereClause += "OR ";
            }
            whereClause += "tickertext LIKE ?";
        }
        whereClause += ")";

        ArrayList<String> whereArgs = new ArrayList<>();

        whereArgs.add(appNameForSearch);

        for (int i = 0; i < keywordList.size(); ++i) {
            whereArgs.add("%" + keywordList.get(i) + "%");
        }

        // データベースからテーブルを読み込む
        cursor = database.query(
                NotificationDatabaseHelper.DB_TABLE,
                null,
                whereClause,
                whereArgs.toArray(new String[whereArgs.size()]),
                null,
                null,
                null
        );

        // カーソルの位置を先頭に移動
        cursor.moveToFirst();

        if (cursor.getCount() == 0) { //< 重要な通知が無かった場合
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_info_outline)
                    .setTitle(R.string.info_title)
                    .setMessage(R.string.info_no_stared_notification)
                    .setPositiveButton(R.string.button_ok, (dialogInterface, i) -> finish())
                    .show();
        }

        final int RECODE_ID = 0;
        final int RECODE_PACKAGENAME = 1;
        final int RECODE_TICKERTEXT = 2;
        final int RECODE_YEAR = 3;
        final int RECODE_MONTH = 4;
        final int RECODE_DAY = 5;
        final int RECODE_DATE = 6;

        for(int i = 0; i < cursor.getCount(); ++i) {
            try {
                // cursor内のレコードのデータをString型に変換
                Column columnHolder = new Column();
                columnHolder.id = cursor.getString(RECODE_ID);
                columnHolder.packageName = cursor.getString(RECODE_PACKAGENAME);
                columnHolder.tickerText = cursor.getString(RECODE_TICKERTEXT);
                columnHolder.year = cursor.getInt(RECODE_YEAR);
                columnHolder.month = cursor.getInt(RECODE_MONTH);
                columnHolder.day = cursor.getInt(RECODE_DAY);
                columnHolder.date = cursor.getString(RECODE_DATE);
                columns.add(columnHolder);
                cursor.moveToNext();
            } catch (Exception e) {
                Log.i(TAG, e.toString());
                Log.i(TAG, Integer.toString(cursor.getCount()));
            }
        }
        // カーソルを閉じる
        cursor.close();
    }

    // 通知をキーワードで抽出する
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getNotificationFromDB() {
        // データベースオブジェクト作成
        NotificationDatabaseHelper databaseHelper = new NotificationDatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        // WHERE文発行
        String whereClause = "packagename=?";
        String[] whereArgs = { appNameForSearch };

        // データベースからテーブルを読み込む
        Cursor cursor = database.query(
                NotificationDatabaseHelper.DB_TABLE,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        // カーソルの位置を先頭に移動
        cursor.moveToFirst();

        if (cursor.getCount() == 0) { //< 重要な通知が無かった場合
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_info_outline)
                    .setTitle(R.string.info_title)
                    .setMessage(R.string.info_no_notification)
                    .setPositiveButton(R.string.button_ok, (dialogInterface, i) -> finish())
                    .show();
        }

        final int RECODE_ID = 0;
        final int RECODE_PACKAGENAME = 1;
        final int RECODE_TICKERTEXT = 2;
        final int RECODE_YEAR = 3;
        final int RECODE_MONTH = 4;
        final int RECODE_DAY = 5;
        final int RECODE_DATE = 6;

        for(int i = 0; i < cursor.getCount(); ++i) {
            try {
                // cursor内のレコードのデータをString型に変換
                Column columnHolder = new Column();
                columnHolder.id = cursor.getString(RECODE_ID);
                columnHolder.packageName = cursor.getString(RECODE_PACKAGENAME);
                columnHolder.tickerText = cursor.getString(RECODE_TICKERTEXT);
                columnHolder.year = cursor.getInt(RECODE_YEAR);
                columnHolder.month = cursor.getInt(RECODE_MONTH);
                columnHolder.day = cursor.getInt(RECODE_DAY);
                columnHolder.date = cursor.getString(RECODE_DATE);
                columns.add(columnHolder);
                cursor.moveToNext();
            } catch (Exception e) {
                Log.i(TAG, e.toString());
                Log.i(TAG, Integer.toString(cursor.getCount()));
            }
        }

        // カーソルを閉じる
        cursor.close();
    }

    private int getNotificationCount() {
        KeywordDatabaseHelper databaseHelper = new KeywordDatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        // WHERE文発行
        String whereClause = "packagename=?";
        String[] whereArgs = { appNameForSearch };

        // データベースからテーブルを読み込む
        Cursor cursor = database.query(
                NotificationDatabaseHelper.DB_TABLE,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return cursor.getCount();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showAdvancedNotification(ArrayList<NotificationBoxItem> notifItems, int position) {
        // ハイパーリンクを有効にする
        final SpannableString spannableString = new SpannableString(notifItems.get(position).getText());
        Linkify.addLinks(spannableString, Linkify.ALL);

        // アラート用TextViewの設定
        TextView textView = new TextView(this);
        textView.setText(spannableString);
        textView.setVerticalScrollBarEnabled(true);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setScrollBarFadeDuration(100);
        textView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        textView.setFocusable(true);
        textView.setPadding(30, 0, 30, 0);

        // アイコン生成
        Drawable drawableIcon = null;
        try {
            drawableIcon = getPackageManager().getApplicationIcon(notifItems.get(position).getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.i("myDEBUG(icon)", e.toString());
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setIcon(drawableIcon)
                .setTitle(notifItems.get(position).getTitle())
                .setView(textView)
                .setPositiveButton(R.string.button_launch, (dialog, which) -> {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(notifItems.get(position).getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton(R.string.button_close, null)
                .show();
    }

    private class Column {
        public String id;
        public String packageName;
        public String tickerText;
        public int year, month, day;
        public String date;
    }
}