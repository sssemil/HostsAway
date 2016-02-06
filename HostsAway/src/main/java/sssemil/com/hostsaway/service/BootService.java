/*
 * Copyright (C) 2011-2013 Dominik Schürmann <dominik@dominikschuermann.de>
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

import android.app.IntentService;
import android.content.Intent;

import sssemil.com.hostsaway.util.Constants;
import sssemil.com.hostsaway.util.Log;
import sssemil.com.hostsaway.util.WebserverUtils;

public class BootService extends IntentService {

    public BootService() {
        super("HostsAwayBootService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // start webserver
        Log.d(Constants.TAG, "BootService: onHandleIntent");
        WebserverUtils.startWebserverOnBoot(getApplicationContext());
    }

}