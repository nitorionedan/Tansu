package com.yafoo.tansu2.Activity;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.yafoo.tansu2.Adapter.AppListAdapter;
import com.yafoo.tansu2.Item.InstalledAppItem;
import com.yafoo.tansu2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yaden on 2017/12/10.
 */

public class AppRegisterActivity extends AppCompatActivity
{
    private ListView applistActivity;
    private ArrayList<InstalledAppItem> items = new ArrayList<>();
    private static ArrayList<InstalledAppItem> s_items = new ArrayList<>();
    private static AppListAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_applist);
        applistActivity = (ListView) findViewById(R.id.applistview);
        setTitle(R.string.title_applist);

        // インストール済アプリを取得
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppList = getPackageManager().queryIntentActivities(mainIntent, 0);

        // インストール済アプリの数だけアイテムを作成する
        for (int i = 0; i < pkgAppList.size(); ++i) {
            String appName = (String) getPackageManager()
                    .getApplicationLabel(pkgAppList.get(i).activityInfo.applicationInfo);
            String packageName = pkgAppList.get(i).activityInfo.packageName;
            items.add(new InstalledAppItem(appName, packageName, false));
        }

        // このアプリを省く
        items.removeIf(item-> item.getPackageName().contains("com.yafoo.tansu2"));

        // 名前を降順で並び替える
        Collections.sort(items, new Comparator<InstalledAppItem>() {
            @Override
            public int compare(InstalledAppItem installedAppItem, InstalledAppItem t1) {
                return installedAppItem.getTitle().compareTo(t1.getTitle());
            }
        });

        adapter = new AppListAdapter(items, this);
        applistActivity.setAdapter(adapter);

        save();
    }

    @Override
    public void onPause() {
        super.onPause();
        save();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // TODO: 実装する
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    // @brief チェックリストと状態を保存
    private void save() {
        s_items.clear(); //< まずは削除
        s_items = (ArrayList<InstalledAppItem>) items.clone();
        for (int i = 0; i < items.size(); ++i) {
            s_items.get(i).setChecked(adapter.isChecked(i));
        }
    }

    public static ArrayList<InstalledAppItem> getCheckedAppList() {
        ArrayList<InstalledAppItem> checkedList = new ArrayList<>();
        for (int i = 0; i < s_items.size(); ++i) {
            if (s_items.get(i).isChecked()) {
                checkedList.add(s_items.get(i));
            }
        }

        return checkedList;
    }

    // @brief インストール済アプリの数を取得
    private int getIinstalledAppCount() {
        // インストール済アプリを取得
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppList = getPackageManager().queryIntentActivities(mainIntent, 0);
        return pkgAppList.size();
    }
}
