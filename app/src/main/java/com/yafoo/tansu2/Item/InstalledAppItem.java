package com.yafoo.tansu2.Item;

import android.provider.ContactsContract;

import java.util.Date;

/**
 * Created by yaden on 2017/12/10.
 * @brief This is Item class for AppRegisterActivity class.
 */

public class InstalledAppItem
{
    private String packageName = "";
    private String title = "";
    private String bigtext = "";
    private int year, month, day;
    private String date = "";
    private boolean checked = false;

    public InstalledAppItem(String title, String packageName, boolean checked) {
        this.title = title;
        this.packageName = packageName;
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getTitle(){
        return title;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setTitle(String title){
        this.title = title;
    }
}
