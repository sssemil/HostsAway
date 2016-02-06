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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import sssemil.com.hostsaway.R;
import sssemil.com.hostsaway.provider.HostsAwayContract.RedirectionList;

public class RedirectionCursorAdapter extends SimpleCursorAdapter {

    public RedirectionCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to,
                                    int flags) {
        super(context, layout, c, from, to, flags);
    }

    /**
     * Bind cursor to view using the checkboxes
     */
    @Override
    public void bindView(View v, Context context, Cursor cursor) {
        CheckBox cBox = (CheckBox) v.findViewById(R.id.checkbox_list_two_checkbox);
        TextView hostnameTextView = (TextView) v.findViewById(R.id.checkbox_list_two_text);
        TextView ipTextView = (TextView) v.findViewById(R.id.checkbox_list_two_subtext);

        if (cBox != null) {
            // bind cursor position to tag of list item
            int cursorPosition = cursor.getPosition();
            cBox.setTag("checkbox_" + cursorPosition);
            hostnameTextView.setTag("hostname_" + cursorPosition);
            ipTextView.setTag("ip_" + cursorPosition);

            int enabledCol = cursor.getColumnIndexOrThrow(RedirectionList.ENABLED);
            String enabled = cursor.getString(enabledCol);

            if (Integer.parseInt(enabled) == 1) {
                cBox.setChecked(true);
            } else {
                cBox.setChecked(false);
            }

            // set hostname
            int hostnameCol = cursor.getColumnIndexOrThrow(RedirectionList.HOSTNAME);
            String hostname = cursor.getString(hostnameCol);
            hostnameTextView.setText(hostname);

            // set ip
            int ipCol = cursor.getColumnIndexOrThrow(RedirectionList.IP);
            String ip = cursor.getString(ipCol);
            ipTextView.setText(ip);
        }
    }

}