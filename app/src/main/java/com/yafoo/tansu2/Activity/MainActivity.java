package com.yafoo.tansu2.Activity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yafoo.tansu2.Database.KeywordDatabaseHelper;
import com.yafoo.tansu2.Database.NotificationDatabaseHelper;
import com.yafoo.tansu2.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String TAG = "myDEBUG";

    private static SQLiteDatabase database;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(Color.BLACK);
        context = this;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // データベースのテーブル作成
        SQLiteDatabase sqLiteDatabase = new NotificationDatabaseHelper(this).getWritableDatabase();
        sqLiteDatabase.execSQL (
                "CREATE TABLE IF NOT EXISTS " + NotificationDatabaseHelper.DB_TABLE + " (keyword text)"
        );
        sqLiteDatabase.close();
        sqLiteDatabase = new KeywordDatabaseHelper(this).getWritableDatabase();
        sqLiteDatabase.execSQL (
                "CREATE TABLE IF NOT EXISTS " + KeywordDatabaseHelper.DB_TABLE + " (keyword text)"
        );
        sqLiteDatabase.close();

        // Upper Drawer
        ImageButton upperDrawer = (ImageButton) findViewById(R.id.upperdrawer_button);
        upperDrawer.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisteredAppListActivity.class);
            intent.putExtra(RegisteredAppListActivity.ID_EXTRA_DRAWER, true);
            startActivity(intent);
        });

        // Under drawer
        ImageButton underDrawer = (ImageButton) findViewById(R.id.underdrawer_button);
        underDrawer.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisteredAppListActivity.class);
            intent.putExtra(RegisteredAppListActivity.ID_EXTRA_DRAWER, false);
            startActivity(intent);
        });

        try {
            // もしNotification accessが有効であれば
            if (Settings.Secure.getString(this.getContentResolver(),
                    "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
            } else {
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_warning)
                        .setTitle(R.string.attention_title)
                        .setMessage(R.string.attention_notificationaccess)
                        .setNeutralButton(R.string.button_notificationaccess, (dialogInterface, which) -> {
                            // Notification accessまで行く
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                            startActivity(intent);
                        })
                        .setNegativeButton(R.string.button_close, null)
                        .show();
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            Toast.makeText(context, R.string.toast_error_open_notificationaccess, Toast.LENGTH_SHORT).show();
        }

        // データベースオブジェクト作成
        NotificationDatabaseHelper databaseHelper = new NotificationDatabaseHelper(this);
        database = databaseHelper.getWritableDatabase();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notificationaccess) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        } else if (id == R.id.nav_cleardata) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.attention_title)
                    .setMessage(R.string.ques_delete)
                    .setPositiveButton(R.string.button_yes, (dialogInterface, which) -> {
                        database.execSQL("DELETE FROM " + NotificationDatabaseHelper.DB_TABLE);
                        Toast.makeText(MainActivity.this, R.string.toast_delete, Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton(R.string.button_no, null)
                    .show();
        } else if (id == R.id.nav_applist) {
            Intent intent = new Intent(MainActivity.this, AppRegisterActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_keywordlist) {
            Intent intent = new Intent(MainActivity.this, KeywordRegisterActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            // アラート用TextViewの設定
            TextView textView = new TextView(this);
            textView.setText(R.string.help_textview);
            textView.setVerticalScrollBarEnabled(true);
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
            textView.setFocusable(true);
            textView.setPadding(30, 0, 30, 0);

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_help_outline)
                    .setTitle(R.string.help_title)
                    .setView(textView)
                    .setPositiveButton(R.string.button_close, null)
                    .show();
        } else if (id == R.id.nav_info) {
            TextView textView = new TextView(this);
            textView.setGravity(Gravity.CENTER);
            final SpannableString spannableString = new SpannableString(getText(R.string.info_developper));
            Linkify.addLinks(spannableString, Linkify.EMAIL_ADDRESSES);
            textView.setText(spannableString);
            textView.setTextSize(16);
            textView.setMovementMethod(LinkMovementMethod.getInstance());

            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_info_outline)
                    .setTitle(R.string.info_title)
                    .setView(textView)
                    .setNegativeButton(R.string.button_close, null)
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
