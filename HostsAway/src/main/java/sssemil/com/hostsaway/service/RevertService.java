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

package sssemil.com.hostsaway.service;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import org.sufficientlysecure.rootcommands.Shell;

import java.io.FileOutputStream;

import sssemil.com.hostsaway.R;
import sssemil.com.hostsaway.helper.PreferenceHelper;
import sssemil.com.hostsaway.helper.ResultHelper;
import sssemil.com.hostsaway.ui.BaseActivity;
import sssemil.com.hostsaway.util.ApplyUtils;
import sssemil.com.hostsaway.util.Constants;
import sssemil.com.hostsaway.util.Log;
import sssemil.com.hostsaway.util.StatusCodes;

public class RevertService extends WakefulIntentService {
    private Context mService;

    public RevertService() {
        super("HostsAwayRevertService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mService = this;

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Asynchronous background operations of service, with wakelock
     */
    @Override
    public void doWakefulWork(Intent intent) {
        // disable buttons
        BaseActivity.setButtonsDisabledBroadcast(mService, true);

        try {
            Shell rootShell = Shell.startRootShell();
            int revertResult = revert(rootShell);
            rootShell.close();

            Log.d(Constants.TAG, "revert result: " + revertResult);

            // enable buttons
            BaseActivity.setButtonsDisabledBroadcast(mService, false);

            ResultHelper.showNotificationBasedOnResult(mService, revertResult, null);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Problem while reverting!", e);
        }
    }

    /**
     * Reverts to default hosts file
     *
     * @return Status codes REVERT_SUCCESS or REVERT_FAIL
     */
    private int revert(Shell shell) {
        BaseActivity.setStatusBroadcast(mService, getString(R.string.status_reverting),
                getString(R.string.status_reverting_subtitle), StatusCodes.CHECKING);

        // build standard hosts file
        try {
            FileOutputStream fos = mService.openFileOutput(Constants.HOSTS_FILENAME,
                    Context.MODE_PRIVATE);

            // default localhost
            String localhost = Constants.LOCALHOST_IPv4 + " " + Constants.LOCALHOST_HOSTNAME
                    + Constants.LINE_SEPERATOR + Constants.LOCALHOST_IPv6 + " "
                    + Constants.LOCALHOST_HOSTNAME;
            fos.write(localhost.getBytes());
            fos.close();

            // copy build hosts file with RootTools, based on target from preferences
            if (PreferenceHelper.getApplyMethod(mService).equals("writeToSystem")) {

                ApplyUtils.copyHostsFile(mService, Constants.ANDROID_SYSTEM_ETC_HOSTS, shell);
            } else if (PreferenceHelper.getApplyMethod(mService).equals("writeToDataData")) {

                ApplyUtils.copyHostsFile(mService, Constants.ANDROID_DATA_DATA_HOSTS, shell);
            } else if (PreferenceHelper.getApplyMethod(mService).equals("writeToData")) {

                ApplyUtils.copyHostsFile(mService, Constants.ANDROID_DATA_HOSTS, shell);
            } else if (PreferenceHelper.getApplyMethod(mService).equals("customTarget")) {

                ApplyUtils.copyHostsFile(mService, PreferenceHelper.getCustomTarget(mService),
                        shell);
            }

            // delete generated hosts file after applying it
            mService.deleteFile(Constants.HOSTS_FILENAME);

            // set status to disabled
            BaseActivity.updateStatusDisabled(mService);

            return StatusCodes.REVERT_SUCCESS;
        } catch (Exception e) {
            Log.e(Constants.TAG, "Exception", e);

            return StatusCodes.REVERT_FAIL;
        }
    }
}
