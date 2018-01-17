package com.yafoo.tansu2.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yafoo.tansu2.Item.InstalledAppItem;
import com.yafoo.tansu2.R;

import java.util.ArrayList;

/**
 * Created by yaden on 2017/12/10.
 */

public class AppListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<InstalledAppItem> mylist = new ArrayList<>();
    private SharedPreferences.Editor editor;

    public AppListAdapter(ArrayList<InstalledAppItem> itemArray, Context context)
    {
        super();

        mylist = itemArray;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mylist.size();
    }

    @Override
    public InstalledAppItem getItem(int position) {
        return mylist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);

        ViewHolder view = null;
        LayoutInflater inflator = ((Activity) context).getLayoutInflater();

        if (view == null) {
            view = new ViewHolder();
            convertView = inflator.inflate(R.layout.adapter_applist, null);

            view.imageView = (ImageView) convertView.findViewById(R.id.adapterimageview);
            view.nametext = (TextView)convertView.findViewById(R.id.adaptertextview);
            view.tick = (CheckBox)convertView.findViewById(R.id.adaptercheckbox);

            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        editor = sharedPrefs.edit();

        Drawable drawableIcon = null;
        try {
            drawableIcon = context.getPackageManager().getApplicationIcon(mylist.get(position).getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.i("myDEBUG(icon)", e.toString());
        }

        view.imageView.setImageDrawable(drawableIcon);
        view.nametext.setText("" + mylist.get(position).getTitle());
        //view.nametext.setCompoundDrawables(drawableIcon, null, null, null);
        view.tick.setTag(position);
        view.tick.setChecked(sharedPrefs.getBoolean("CheckVal" + position, false));
        view.tick.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            // Here
            // we get the position that we have set for the checkbox using setTag.
            int getPosition = (Integer) compoundButton.getTag();

            mylist.get(getPosition).setChecked(compoundButton.isChecked());

            editor.putBoolean("CheckVal" + position, isChecked);
            editor.commit();

            if (isChecked) {
                // do something here
            } else {
                // code here
            }
        });

        return convertView;
    }

    public boolean isChecked(int position) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean("CheckVal" + position, false);
    }

    static private class ViewHolder {
        public ImageView imageView;
        public TextView nametext;
        public CheckBox tick;
    }
}
