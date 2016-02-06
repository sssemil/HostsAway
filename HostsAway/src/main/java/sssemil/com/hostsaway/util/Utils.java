/*
 * Copyright (C) 2011-2012 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * This file is part of HostsAway.
 * 
 * HostsAway is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HostsAway is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HostsAway.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package sssemil.com.hostsaway.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.sufficientlysecure.rootcommands.RootCommands;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;

import java.util.List;
import java.util.concurrent.ExecutionException;

import sssemil.com.hostsaway.R;
import sssemil.com.hostsaway.helper.PreferenceHelper;

public class Utils {

    /**
     * Check if Android is rooted, check for su binary and busybox and display possible solutions if
     * they are not available
     *
     * @param activity
     * @return true if phone is rooted
     */
    public static boolean isAndroidRooted(final Activity activity) {
        boolean rootAccess = false;

        // root check can be disabled for debugging in emulator
        if (Constants.DEBUG_DISABLE_ROOT_CHECK) {
            rootAccess = true;
        } else {
            if (RootCommands.rootAccessGiven()) {
                rootAccess = true;
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setCancelable(false);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setTitle(activity.getString(R.string.no_root_title));

                // build view from layout
                LayoutInflater factory = LayoutInflater.from(activity);
                final View dialogView = factory.inflate(R.layout.no_root_dialog, null);
                builder.setView(dialogView);

                builder.setNeutralButton(activity.getResources().getString(R.string.button_exit),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.finish(); // finish current activity, means exiting app
                            }
                        }
                );

                AlertDialog alert = builder.create();
                alert.show();
            }
        }

        return rootAccess;
    }

    /**
     * Show reboot question
     *
     * @param titleR   resource id of title string
     * @param messageR resource id of message string
     */
    public static void rebootQuestion(final Context context, int titleR, int messageR) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleR);
        builder.setIcon(android.R.drawable.ic_dialog_info);

        // build view from layout
        LayoutInflater factory = LayoutInflater.from(context);
        final View dialogView = factory.inflate(R.layout.reboot_dialog, null);

        // set text in view based on given resource id
        TextView text = (TextView) dialogView.findViewById(R.id.reboot_dialog_text);
        text.setText(context.getString(messageR));

        builder.setView(dialogView);

        builder.setPositiveButton(context.getString(R.string.button_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // set preference to never show reboot dialog again if checkbox is checked
                        CheckBox checkBox = (CheckBox) dialogView
                                .findViewById(R.id.reboot_dialog_checkbox);
                        if (checkBox.isChecked()) {
                            PreferenceHelper.setNeverReboot(context, true);
                        }

                        try {
                            Shell rootShell = Shell.startRootShell();

                            Toolbox tb = new Toolbox(rootShell);
                            tb.reboot(Toolbox.REBOOT_REBOOT);
                        } catch (Exception e) {
                            Log.e(Constants.TAG, "Problem with rebooting", e);
                        }
                    }
                }
        );
        builder.setNegativeButton(context.getString(R.string.button_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // set preference to never show reboot dialog again if checkbox is checked
                        CheckBox checkBox = (CheckBox) dialogView
                                .findViewById(R.id.reboot_dialog_checkbox);
                        if (checkBox.isChecked()) {
                            PreferenceHelper.setNeverReboot(context, true);
                        }

                        dialog.dismiss();
                    }
                }
        );
        AlertDialog question = builder.create();

        question.show();
    }

    /**
     * Checks if Android is online
     *
     * @param context
     * @return returns true if online
     */
    public static boolean isAndroidOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the application is installed on the SD card. See
     * http://stackoverflow.com/questions/
     * 5814474/how-can-i-find-out-if-my-app-is-installed-on-sd-card
     *
     * @return <code>true</code> if the application is installed on the sd card
     */
    @SuppressLint("SdCardPath")
    public static boolean isInstalledOnSdCard(Context context) {
        // check for API level 8 and higher
        if (VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                ApplicationInfo ai = pi.applicationInfo;
                return (ai.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE;
            } catch (NameNotFoundException e) {
                // ignore
            }
        }

        // check for API level 7 (rooted devices) - check files dir
        try {
            String filesDir = context.getFilesDir().getAbsolutePath();
            if (filesDir.startsWith("/data/")) {
                return false;
            } else if (filesDir.contains("/mnt/") || filesDir.contains("/sdcard/")) {
                return true;
            }
        } catch (Throwable e) {
            // ignore
        }

        return false;
    }

    /**
     * Checks if HostsAway is in foreground, see
     * http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
     *
     * @param context
     * @return
     */
    public static boolean isInForeground(Context context) {
        AsyncTask<Context, Void, Boolean> foregroundCheckTask = new AsyncTask<Context, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Context... params) {
                final Context context = params[0].getApplicationContext();
                return isAppOnForeground(context);
            }

            private boolean isAppOnForeground(Context context) {
                ActivityManager activityManager = (ActivityManager) context
                        .getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
                if (appProcesses == null) {
                    return false;
                }
                final String packageName = context.getPackageName();
                for (RunningAppProcessInfo appProcess : appProcesses) {
                    if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                            && appProcess.processName.equals(packageName)) {
                        return true;
                    }
                }
                return false;
            }
        };

        boolean foreground = false;
        try {
            foreground = foregroundCheckTask.execute(context).get();
        } catch (InterruptedException e) {
            Log.e(Constants.TAG, "IsInForeground InterruptedException", e);
        } catch (ExecutionException e) {
            Log.e(Constants.TAG, "IsInForeground ExecutionException", e);
        }

        return foreground;
    }
}
