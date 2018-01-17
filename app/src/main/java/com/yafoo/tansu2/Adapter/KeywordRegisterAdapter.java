package com.yafoo.tansu2.Adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yafoo.tansu2.Activity.KeywordRegisterActivity;
import com.yafoo.tansu2.Database.KeywordDatabaseHelper;
import com.yafoo.tansu2.R;

import java.util.ArrayList;

/**
 * Created by expriment on 2017/12/30.
 */

public class KeywordRegisterAdapter extends BaseAdapter {
    private static String TAG = "myDEBUG(kdRegister)";
    private Context context;
    private Activity activity;
    private ArrayList<String> keywordList;

    private SQLiteDatabase database;
    private KeywordDatabaseHelper databaseHelper;

    public KeywordRegisterAdapter(Context context, Activity activity, ArrayList<String> keywordList) {
        super();

        this.context = context;
        this.activity = activity;
        this.keywordList = (ArrayList<String>) keywordList.clone();
        databaseHelper = new KeywordDatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
    }

    @Override
    public int getCount() {
        return keywordList.size();
    }

    @Override
    public String getItem(int position) {
        return keywordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.adapter_keywordregister, null);

            viewHolder.textView = (TextView) convertView.findViewById(R.id.adapter_keywordregister_text);
            viewHolder.deleteButton = (ImageButton) convertView.findViewById(R.id.adapter_keyword_deletebutton);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // TextVIew
        viewHolder.textView.setText(keywordList.get(position));
        viewHolder.textView.setOnLongClickListener(view -> {
            return false;
        });
        viewHolder.textView.setOnClickListener(view -> {
        });

        // DeleteButton
        ViewHolder finalViewHolder = viewHolder;
        viewHolder.deleteButton.setOnClickListener(View -> {
            deleteKeywordFromDB(finalViewHolder.textView.getText().toString());
            KeywordRegisterActivity.refresh(activity, context);
        });

        return convertView;
    }

    public void deleteKeywordFromDB(String keyword) {
        database.execSQL("DELETE FROM " + KeywordDatabaseHelper.DB_TABLE +
                " WHERE keyword = " + "\"" + keyword + "\"");
    }

    static private class ViewHolder {
        public TextView textView;
        public ImageButton deleteButton;
    }
}
