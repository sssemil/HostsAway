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

package sssemil.com.hostsaway.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.wakeful.WakefulIntentService;

import org.sufficientlysecure.rootcommands.Shell;

import sssemil.com.hostsaway.R;
import sssemil.com.hostsaway.helper.PreferenceHelper;
import sssemil.com.hostsaway.service.DailyListener;
import sssemil.com.hostsaway.util.Constants;
import sssemil.com.hostsaway.util.Log;
import sssemil.com.hostsaway.util.Utils;
import sssemil.com.hostsaway.util.WebserverUtils;

public class PrefsActivity extends SherlockPreferenceActivity {
    private Context mActivity;
    private ActionBar mActionBar;

    private EditTextPreference mCustomTarget;
    private CheckBoxPreference mUpdateCheckDaily;
    private CheckBoxPreference mWebserverOnBoot;

    /**
     * Menu Items
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in Action Bar clicked; go home
                Intent intent = new Intent(this, BaseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;
        mActionBar = getSupportActionBar();

        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        getPreferenceManager().setSharedPreferencesName(Constants.PREFS_NAME);
        addPreferencesFromResource(R.xml.preferences);

        /*
         * Listen on click of update daily pref, register UpdateService if enabled,
         * setOnPreferenceChangeListener is not used because it is executed before setting the
         * preference value, this would lead to a false check in UpdateListener
         */
        Preference UpdateDailyPref = findPreference(getString(R.string.pref_update_check_daily_key));
        UpdateDailyPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (PreferenceHelper.getUpdateCheckDaily(mActivity)) {
                    WakefulIntentService.scheduleAlarms(new DailyListener(), mActivity, false);
                } else {
                    WakefulIntentService.cancelAlarms(mActivity);
                }
                return false;
            }

        });

        /* Start webserver if pref is enabled */
        Preference WebserverEnabledPref = findPreference(getString(R.string.pref_webserver_enabled_key));
        WebserverEnabledPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Shell rootShell;
                try {
                    rootShell = Shell.startRootShell();

                    if (newValue.equals(true)) {
                        // start webserver
                        WebserverUtils.startWebserver(mActivity, rootShell);
                    } else {
                        // stop webserver
                        WebserverUtils.stopWebserver(mActivity, rootShell);
                    }

                    rootShell.close();
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Problem while starting/stopping webserver!", e);
                }

                return true;
            }
        });

        // find custom target edit
        mCustomTarget = (EditTextPreference) getPreferenceScreen().findPreference(
                getString(R.string.pref_custom_target_key));

        // enable custom target pref on create if enabled in apply method
        if (PreferenceHelper.getApplyMethod(mActivity).equals("customTarget")) {
            mCustomTarget.setEnabled(true);
        } else {
            mCustomTarget.setEnabled(false);
        }

        /* enable custom target pref if enabled in apply method */
        Preference customTargetPref = findPreference(getString(R.string.pref_apply_method_key));
        customTargetPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals("customTarget")) {
                    mCustomTarget.setEnabled(true);
                } else {
                    mCustomTarget.setEnabled(false);
                }
                return true;
            }
        });

        /*
         * Disable update check daily and webserver on boot when installed on sd card. See
         * http://developer.android.com/guide/appendix/install-location.html why
         */
        mUpdateCheckDaily = (CheckBoxPreference) getPreferenceScreen().findPreference(
                getString(R.string.pref_update_check_daily_key));
        mWebserverOnBoot = (CheckBoxPreference) getPreferenceScreen().findPreference(
                getString(R.string.pref_webserver_on_boot_key));

        if (Utils.isInstalledOnSdCard(this)) {
            mUpdateCheckDaily.setEnabled(false);
            mWebserverOnBoot.setEnabled(false);
            mUpdateCheckDaily.setSummary(R.string.pref_sdcard_problem);
            mWebserverOnBoot.setSummary(R.string.pref_sdcard_problem);
        } else {
            mUpdateCheckDaily.setEnabled(true);
            mWebserverOnBoot.setEnabled(true);
            mUpdateCheckDaily.setSummary(R.string.pref_update_check_daily_summary);
            mWebserverOnBoot.setSummary(R.string.pref_webserver_on_boot_summary);
        }

    }
}