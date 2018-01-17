package com.yafoo.tansu2.Adapter;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yafoo.tansu2.Activity.NotificationBoxActivity;
import com.yafoo.tansu2.Database.NotificationDatabaseHelper;
import com.yafoo.tansu2.Item.NotificationBoxItem;
import com.yafoo.tansu2.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by expriment on 2018/01/11.
 */

public class NotificationBoxAdapter extends BaseAdapter {
    private NotificationBoxActivity activity;
    private Context context;
    private ArrayList<NotificationBoxItem> notificationBoxItems;
    private NotificationDatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public NotificationBoxAdapter(NotificationBoxActivity activity, Context context, ArrayList<NotificationBoxItem> notificationBoxItems) {
        this.activity = activity;
        this.context = context;
        this.notificationBoxItems = (ArrayList<NotificationBoxItem>) notificationBoxItems.clone();
        databaseHelper = new NotificationDatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
    }

    @Override
    public int getCount() {
        return notificationBoxItems.size();
    }

    @Override
    public NotificationBoxItem getItem(int i) {
        return notificationBoxItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.adapter_notificationbox, null);

            viewHolder.icon = (ImageView) convertView.findViewById(R.id.adapter_notificationbox_icon);
            viewHolder.title = (TextView) convertView.findViewById(R.id.adapter_notificationbox_title);
            viewHolder.text = (TextView) convertView.findViewById(R.id.adapter_notificationbox_text);
            viewHolder.date = (TextView) convertView.findViewById(R.id.adapter_notificationbox_date);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.icon.setImageDrawable(notificationBoxItems.get(position).getIcon());
        viewHolder.title.setText(notificationBoxItems.get(position).getTitle());
        viewHolder.text.setText(notificationBoxItems.get(position).getText());
        viewHolder.date.setText(
                notificationBoxItems.get(position).getYear()+"/"+
                notificationBoxItems.get(position).getMonth()+"/"+
                notificationBoxItems.get(position).getDay()
        );

        // 寿命用の色付け設定
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.set(notificationBoxItems.get(position).getYear(), notificationBoxItems.get(position).getMonth(), notificationBoxItems.get(position).getDay());
        end.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH)+1, end.get(Calendar.DAY_OF_MONTH));
        int life = (int) getDifferenceBetweenDates(start, end);
        if (0 <= life && life <= 2) {
            // ATTENTION: 色の変更なし
        } else if (3 <= life && life <= 5) {
            viewHolder.text.setBackgroundColor(context.getColor(R.color.notifLifeLevel2));
        } else if (life == 6) {
            viewHolder.text.setBackgroundColor(context.getColor(R.color.notifLifeLevel3));
        } else { //< 寿命が来た通知
            deleteDeadNotificationFromDB(position);
        }

        return convertView;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getDifferenceBetweenDates(Calendar start, Calendar end) {
        long differenceNum = 0;

        Date startDate = start.getTime();
        Date endDate = end.getTime();
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        differenceNum = diffTime / (1000 * 60 * 60 * 24);

        return differenceNum;
    }

    // @brief 寿命が来た通知を調べるとともにそれを削除する
    private void deleteDeadNotificationFromDB(int position) {
        String whereClause = "tickertext LIKE ?";
        String[] whereArgs = new String[] { notificationBoxItems.get(position).getText() };
        database.delete(NotificationDatabaseHelper.DB_TABLE, whereClause, whereArgs);
    }

    static class ViewHolder {
        public TextView title = null;
        public TextView text = null; //< 本文
        public TextView subText = null;
        public TextView date = null;
        public ImageView icon = null;
    }
}
