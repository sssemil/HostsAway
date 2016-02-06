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

import android.app.Application;

import org.sufficientlysecure.rootcommands.RootCommands;

import sssemil.com.hostsaway.helper.PreferenceHelper;
import sssemil.com.hostsaway.util.Constants;
import sssemil.com.hostsaway.util.Log;

public class HostsAwayApplication extends Application {

    @Override
    public void onCreate() {

        // workaround for http://code.google.com/p/android/issues/detail?id=20915
        try {
            Log.d(Constants.TAG, "Setting workaround for AsyncTask...");
            Class.forName("android.os.AsyncTask");
        } catch (Exception e) { // silently catch all
        }

        // Set Debug level based on preference
        if (PreferenceHelper.getDebugEnabled(this)) {
            Constants.DEBUG = true;
            Log.d(Constants.TAG, "Debug set to true by preference!");
            // set RootCommands to debug mode based on HostsAway
            RootCommands.DEBUG = Constants.DEBUG;
        } else {
            Constants.DEBUG = false;
            RootCommands.DEBUG = Constants.DEBUG;
        }

        super.onCreate();
    }

}
