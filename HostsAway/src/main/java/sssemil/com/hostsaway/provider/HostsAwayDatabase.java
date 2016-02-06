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

package sssemil.com.hostsaway.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import sssemil.com.hostsaway.provider.HostsAwayContract.BlacklistColumns;
import sssemil.com.hostsaway.provider.HostsAwayContract.HostsSourcesColumns;
import sssemil.com.hostsaway.provider.HostsAwayContract.RedirectionListColumns;
import sssemil.com.hostsaway.provider.HostsAwayContract.WhitelistColumns;
import sssemil.com.hostsaway.util.Constants;
import sssemil.com.hostsaway.util.Log;

public class HostsAwayDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "adaway.db";
    private static final int DATABASE_VERSION = 12;
    private static final String CREATE_HOSTS_SOURCES = "CREATE TABLE IF NOT EXISTS "
            + Tables.HOSTS_SOURCES + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + HostsSourcesColumns.URL + " TEXT UNIQUE, " + HostsSourcesColumns.LAST_MODIFIED_LOCAL
            + " INTEGER, " + HostsSourcesColumns.LAST_MODIFIED_ONLINE + " INTEGER, "
            + HostsSourcesColumns.ENABLED + " INTEGER)";
    private static final String CREATE_WHITELIST = "CREATE TABLE IF NOT EXISTS " + Tables.WHITELIST
            + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WhitelistColumns.HOSTNAME + " TEXT UNIQUE, " + WhitelistColumns.ENABLED + " INTEGER)";
    private static final String CREATE_BLACKLIST = "CREATE TABLE IF NOT EXISTS " + Tables.BLACKLIST
            + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BlacklistColumns.HOSTNAME + " TEXT UNIQUE, " + BlacklistColumns.ENABLED + " INTEGER)";
    private static final String CREATE_REDIRECTION_LIST = "CREATE TABLE IF NOT EXISTS "
            + Tables.REDIRECTION_LIST + "(" + BaseColumns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + RedirectionListColumns.HOSTNAME
            + " TEXT UNIQUE, " + RedirectionListColumns.IP + " TEXT, "
            + RedirectionListColumns.ENABLED + " INTEGER)";

    HostsAwayDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public long insertHostsSource(SQLiteStatement insertStmt, String url) {
        insertStmt.bindString(1, url);
        insertStmt.bindLong(2, 0); // last_modified_local starts at 0
        insertStmt.bindLong(3, 0); // last_modified_online starts at 0
        insertStmt.bindString(4, "1"); // default is enabled
        return insertStmt.executeInsert();
    }

    private void insertDefaultHostsSources(SQLiteDatabase db) {
        // fill default hosts sources
        SQLiteStatement insertStmt;
        String insertHostsSources = "INSERT OR IGNORE INTO " + Tables.HOSTS_SOURCES
                + "(url, last_modified_local, last_modified_online, enabled) VALUES (?, ?, ?, ?)";
        insertStmt = db.compileStatement(insertHostsSources);


        //TODO help user
        // http://winhelp2002.mvps.org/hosts.htm
        //insertHostsSource(insertStmt, "http://winhelp2002.mvps.org/hosts.txt");

        // http://hosts-file.net
        //insertHostsSource(insertStmt, "http://hosts-file.net/ad_servers.txt");

        // http://pgl.yoyo.org/adservers/
        //insertHostsSource(insertStmt,
        //        "http://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&showintro=0&mimetype=plaintext");

        // HostsAway's own mobile hosts
        //insertHostsSource(insertStmt, "https://adaway.org/hosts.txt");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(Constants.TAG, "Creating database...");

        db.execSQL(CREATE_HOSTS_SOURCES);
        db.execSQL(CREATE_WHITELIST);
        db.execSQL(CREATE_BLACKLIST);
        db.execSQL(CREATE_REDIRECTION_LIST);

        insertDefaultHostsSources(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(Constants.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
    }

    public interface Tables {
        String HOSTS_SOURCES = "hosts_sources";
        String WHITELIST = "whitelist";
        String BLACKLIST = "blacklist";
        String REDIRECTION_LIST = "redirection_list";
    }
}
