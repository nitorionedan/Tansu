package com.yafoo.tansu2.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.yafoo.tansu2.Activity.MainActivity;
import com.yafoo.tansu2.Database.KeywordDatabaseHelper;
import com.yafoo.tansu2.Database.NotificationDatabaseHelper;
import com.yafoo.tansu2.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by yaden on 2017/11/30.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MyNotificationListener extends NotificationListenerService {
    private static final String TAG = "myDEBUG(NLS)";

    // データベース関連
    private NotificationDatabaseHelper databaseHelper = new NotificationDatabaseHelper(this);
    private SQLiteDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();

        database = databaseHelper.getWritableDatabase();
        database.execSQL (
                "CREATE TABLE IF NOT EXISTS " + KeywordDatabaseHelper.DB_TABLE + " (keyword text)"
        );

        final int notifId = R.string.service_name;
        showNotification(notifId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return super.onBind(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "onPosted()------------------------------------");

        try
        {
            String notiText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString() + "\n" +
                    sbn.getNotification().extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();
            Calendar cal = Calendar.getInstance();
            Date date = new Date();
            writeToDB(
                    sbn.getId(),
                    sbn.getPackageName(),
                    notiText,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH),
                    date.toString(),
                    true
            );
            loggingNotificationInfomation(sbn);
        }
        catch (Exception e)
        {
            Log.i(TAG, e.toString());
            Log.i(TAG, "Failed to write to DB...");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "onRemoved()------------------------------------");
        loggingNotificationInfomation(sbn);
    }

    @Override
    public void onListenerConnected() {
        Log.i(TAG, "NLS is on connected!");
    }

    @Override
    public void onListenerDisconnected() {
        Log.i(TAG, "NLS is on disconnected...");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void loggingNotificationInfomation(StatusBarNotification sbn) {
        Log.i(TAG, "title:\n" + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE));
        Log.i(TAG, "subText:\n" + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_SUB_TEXT));
        Log.i(TAG, "bigText:\n" + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
        Calendar cal = Calendar.getInstance();
        Log.i(TAG, "date:\n" + Integer.toString(cal.get(Calendar.YEAR)) + "/" + Integer.toString(cal.get(Calendar.MONTH)) + "/" + Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
        Log.i(TAG, "date(Date):\n" + new Date().toString());
//        Log.i(TAG, "extraText:" + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT));
//        Log.i(TAG, "summaryText:" + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT));
//        Log.i(TAG, "infoText:" + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_INFO_TEXT));
//        Log.i(TAG, "titleBig:" + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE_BIG));
//        Log.i(TAG, "conversationTitle:" + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE));
    }

    private void showNotification(int id) {
        // Tansuからの通知をタップしたき起動させる設定を施す
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // ステータスバーへ表示
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("アプリからの通知を集めています");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_tansu);
        builder.setLargeIcon(bitmap);
        builder.setSmallIcon(R.drawable.icon_tansu);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(contentIntent);
        startForeground(id, builder.build());
    }

    // @brief データベースへの書き込み
    // @param[in] editedStrings 書き込む文字列
    private void writeToDB(int id, String packageName, String text, int year, int month, int day, String date, Boolean isUnread) throws Exception {
        database.beginTransaction();
        try {
            final int columCount = 8;
            final ContentValues values = new ContentValues(columCount);
            values.clear();
            values.put("id", id);
            values.put("packagename", packageName);
            values.put("tickertext", text);
            values.put("year", year);
            values.put("month", month);
            values.put("day", day);
            values.put("date", date);
            values.put("isunread", isUnread? 1 : 0);

            database.insert(NotificationDatabaseHelper.DB_TABLE, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
}
