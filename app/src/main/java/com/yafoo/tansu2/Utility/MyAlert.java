package com.yafoo.tansu2.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;

/**
 * Created by yaden on 2017/12/03.
 */

public class MyAlert
{
    public static void makeAlert(Context context, String title, String msg)
    {
        AlertDialog.Builder alertDiaLog = new AlertDialog.Builder(context);
        alertDiaLog.setTitle(title);
        alertDiaLog.setMessage(msg);
        alertDiaLog.setPositiveButton("OK", null);
        alertDiaLog.show();
    }

    public static void showNotificationInfo(final Context context, final String title, final String text, final String packageName)
    {
        Drawable icon = null;
        try {
            icon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new AlertDialog
                .Builder(context)
                .setTitle(title)
                .setMessage(text)
                .setIcon(icon)
                .setPositiveButton(
                        "LAUNCH APP",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int width) {
                                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                                context.startActivity(launchIntent);
                            }
                        }
                )
                .setNegativeButton(
                        "CLOSE",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int width) {
                            }
                        }
                )
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void showNotificationInfo(final Context context, final String title, final StatusBarNotification sbn)
    {
        Drawable icon = null;
        try {
            icon = context.getPackageManager().getApplicationIcon(sbn.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new AlertDialog
                .Builder(context)
                .setTitle(title)
                .setMessage(sbn.getNotification().tickerText.toString())
                .setIcon(icon)
                .setPositiveButton(
                        "LAUNCH APP",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int width) {
                                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(sbn.getPackageName());
                                context.startActivity(launchIntent);
                            }
                        }
                )
                .setNegativeButton(
                        "CLOSE",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int width) {
                            }
                        }
                )
                .show();
    }
}
