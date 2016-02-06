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

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;

import org.sufficientlysecure.rootcommands.Shell;

import java.io.IOException;

import sssemil.com.hostsaway.R;
import sssemil.com.hostsaway.util.Constants;
import sssemil.com.hostsaway.util.Log;
import sssemil.com.hostsaway.util.WebserverUtils;

public class WebserverFragment extends SherlockFragment {
    private Activity mActivity;
    private ToggleButton mWebserverToggle;

    private Shell mRootShell;

    /**
     * Inflate the layout for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.webserver_fragment, container, false);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        mWebserverToggle = (ToggleButton) mActivity
                .findViewById(R.id.webserver_fragment_toggle_button);

        try {
            mRootShell = Shell.startRootShell();
        } catch (Exception e) {
            Log.e(Constants.TAG, "Problem starting root shell!", e);
        }

        // set togglebutton checked if webserver is running
        if (WebserverUtils.isWebserverRunning(mRootShell)) {
            mWebserverToggle.setChecked(true);
        } else {
            mWebserverToggle.setChecked(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            if (mRootShell != null) {
                mRootShell.close();
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "Problem while closing shell!", e);
        }
    }

    /**
     * Button Action to start or stop webserver
     *
     * @param view
     */
    public void webserverOnClick(View view) {
        if (mWebserverToggle.isChecked()) {
            WebserverUtils.startWebserver(mActivity, mRootShell);
        } else {
            WebserverUtils.stopWebserver(mActivity, mRootShell);
        }
    }
}
