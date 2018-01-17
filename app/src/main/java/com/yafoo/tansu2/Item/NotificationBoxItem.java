package com.yafoo.tansu2.Item;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Date;

/**
 * Created by expriment on 2018/01/11.
 */

public class NotificationBoxItem {
    private String title = "";
    private String text = "";
    private String subText = "";
    private String dateText = "";
    private String packageName = "";
    private Drawable icon = null;
    private int year = 0, month = 0, day = 0;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    public void setDate(int y, int m, int d) {
        year = y;
        month = m+1; // なぜか1月が０月なので
        day = d;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getSubText() {
        return subText;
    }

    public String getDateText () {
        return dateText;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }
}
