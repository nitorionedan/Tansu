package com.yafoo.tansu2.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yafoo.tansu2.Activity.NotificationBoxActivity;
import com.yafoo.tansu2.R;

import java.util.ArrayList;

/**
 * Created by yaden on 2017/12/24.
 */

public class RegisteredAppAdapter extends BaseAdapter {

    private static final String TAG = "myDEBUG(register)";
    private Context context;
    private ArrayList<String> registeredPackNameList;
    private ArrayList<String> keywordList = new ArrayList<>();
    private boolean isUpperDrawer = false;

    public RegisteredAppAdapter(ArrayList<String> registeredPackNameList, ArrayList<String> keywordList, Context context, boolean isUpperDrawer) {
        super();

        this.registeredPackNameList = (ArrayList<String>) registeredPackNameList.clone();
        this.keywordList = (ArrayList<String>) keywordList.clone();
        this.context = context;
        this.isUpperDrawer = isUpperDrawer;
    }

    @Override
    public int getCount() {
        return registeredPackNameList.size();
    }

    @Override
    public Object getItem(int i) {
        return registeredPackNameList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.adapter_registeredapp, null);

            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.register_adapter_imageview);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.register_adapter_textview);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // ImageView設定 -------------------------------------------------------------
        Drawable drawableIcon = null;
        try {
            drawableIcon = context.getPackageManager().getApplicationIcon(registeredPackNameList.get(position));
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
        viewHolder.imageView.setImageDrawable(drawableIcon);

        // TextView設定 -------------------------------------------------------------
        // アプリ名を取得
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;

        try {
            ai = pm.getApplicationInfo(registeredPackNameList.get(position), 0);
        } catch (Exception e) {
            ai = null;
        }

        final String appName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        viewHolder.textView.setText(appName);
        // ClickListenerを設定
        viewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NotificationBoxActivity.class);

                Bundle extras = new Bundle();
                extras.putString(NotificationBoxActivity.ID_EXTRA_PACKNAME, registeredPackNameList.get(position));
                extras.putBoolean(NotificationBoxActivity.ID_EXTRA_ISUPPER, isUpperDrawer);
                extras.putStringArrayList(NotificationBoxActivity.ID_EXTRA_KEYWORD, keywordList);

                intent.putExtras(extras);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    static private class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }
}
