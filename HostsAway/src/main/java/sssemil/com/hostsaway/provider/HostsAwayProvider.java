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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Arrays;

import sssemil.com.hostsaway.provider.HostsAwayContract.Blacklist;
import sssemil.com.hostsaway.provider.HostsAwayContract.HostsSources;
import sssemil.com.hostsaway.provider.HostsAwayContract.RedirectionList;
import sssemil.com.hostsaway.provider.HostsAwayContract.Whitelist;
import sssemil.com.hostsaway.provider.HostsAwayDatabase.Tables;
import sssemil.com.hostsaway.util.Constants;
import sssemil.com.hostsaway.util.Log;

public class HostsAwayProvider extends ContentProvider {
    private static final int HOSTS_SOURCES = 100;
    private static final int HOSTS_SOURCES_ID = 101;
    private static final int WHITELIST = 200;
    private static final int WHITELIST_ID = 201;
    private static final int BLACKLIST = 300;
    private static final int BLACKLIST_ID = 301;
    private static final int REDIRECTION_LIST = 400;
    private static final int REDIRECTION_LIST_ID = 401;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private HostsAwayDatabase mHostsAwayDatabase;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri} variations supported by
     * this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = HostsAwayContract.CONTENT_AUTHORITY;

        // Hosts sources
        matcher.addURI(authority, HostsAwayContract.PATH_HOSTS_SOURCES, HOSTS_SOURCES);
        matcher.addURI(authority, HostsAwayContract.PATH_HOSTS_SOURCES + "/#", HOSTS_SOURCES_ID);

        // Whitelist
        matcher.addURI(authority, HostsAwayContract.PATH_WHITELIST, WHITELIST);
        matcher.addURI(authority, HostsAwayContract.PATH_WHITELIST + "/#", WHITELIST_ID);

        // Blacklist
        matcher.addURI(authority, HostsAwayContract.PATH_BLACKLIST, BLACKLIST);
        matcher.addURI(authority, HostsAwayContract.PATH_BLACKLIST + "/#", BLACKLIST_ID);

        // Redirection list
        matcher.addURI(authority, HostsAwayContract.PATH_REDIRECTION_LIST, REDIRECTION_LIST);
        matcher.addURI(authority, HostsAwayContract.PATH_REDIRECTION_LIST + "/#", REDIRECTION_LIST_ID);

        return matcher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mHostsAwayDatabase = new HostsAwayDatabase(context);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOSTS_SOURCES:
                return HostsSources.CONTENT_TYPE;
            case HOSTS_SOURCES_ID:
                return HostsSources.CONTENT_ITEM_TYPE;
            case WHITELIST:
                return Whitelist.CONTENT_TYPE;
            case WHITELIST_ID:
                return Whitelist.CONTENT_ITEM_TYPE;
            case BLACKLIST:
                return Blacklist.CONTENT_TYPE;
            case BLACKLIST_ID:
                return Blacklist.CONTENT_ITEM_TYPE;
            case REDIRECTION_LIST:
                return RedirectionList.CONTENT_TYPE;
            case REDIRECTION_LIST_ID:
                return RedirectionList.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(Constants.TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");

        final SQLiteDatabase db = mHostsAwayDatabase.getWritableDatabase();

        Uri rowUri = null;
        long rowId = -1;
        try {
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case HOSTS_SOURCES:
                    rowId = db.insertOrThrow(Tables.HOSTS_SOURCES, null, values);
                    rowUri = HostsSources.buildUri(Long.toString(rowId));
                    break;
                case WHITELIST:
                    rowId = db.insertOrThrow(Tables.WHITELIST, null, values);
                    rowUri = Whitelist.buildUri(Long.toString(rowId));
                    break;
                case BLACKLIST:
                    rowId = db.insertOrThrow(Tables.BLACKLIST, null, values);
                    rowUri = Blacklist.buildUri(Long.toString(rowId));
                    break;
                case REDIRECTION_LIST:
                    rowId = db.insertOrThrow(Tables.REDIRECTION_LIST, null, values);
                    rowUri = RedirectionList.buildUri(Long.toString(rowId));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        } catch (SQLiteConstraintException e) {
            Log.e(Constants.TAG, "Constraint exception on insert! Entry already existing?");
        }

        // notify of changes in db
        getContext().getContentResolver().notifyChange(uri, null);

        return rowUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Log.v(Constants.TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mHostsAwayDatabase.getReadableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOSTS_SOURCES:
                qb.setTables(Tables.HOSTS_SOURCES);
                break;
            case WHITELIST:
                qb.setTables(Tables.WHITELIST);
                break;
            case BLACKLIST:
                qb.setTables(Tables.BLACKLIST);
                break;
            case REDIRECTION_LIST:
                qb.setTables(Tables.REDIRECTION_LIST);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // notify through cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.v(Constants.TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");

        final SQLiteDatabase db = mHostsAwayDatabase.getWritableDatabase();

        int count = 0;
        try {
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case HOSTS_SOURCES_ID:
                    count = db.update(Tables.HOSTS_SOURCES, values,
                            buildDefaultSelection(uri, selection), selectionArgs);
                    break;
                case WHITELIST_ID:
                    count = db.update(Tables.WHITELIST, values, buildDefaultSelection(uri, selection),
                            selectionArgs);
                    break;
                case BLACKLIST_ID:
                    count = db.update(Tables.BLACKLIST, values, buildDefaultSelection(uri, selection),
                            selectionArgs);
                    break;
                case REDIRECTION_LIST_ID:
                    count = db.update(Tables.REDIRECTION_LIST, values,
                            buildDefaultSelection(uri, selection), selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        } catch (SQLiteConstraintException e) {
            Log.e(Constants.TAG, "Constraint exception on update! Entry already existing?");
        }

        // notify of changes in db
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(Constants.TAG, "delete(uri=" + uri + ")");

        final SQLiteDatabase db = mHostsAwayDatabase.getWritableDatabase();

        int count;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOSTS_SOURCES_ID:
                count = db.delete(Tables.HOSTS_SOURCES, buildDefaultSelection(uri, selection),
                        selectionArgs);
                break;
            case WHITELIST_ID:
                count = db.delete(Tables.WHITELIST, buildDefaultSelection(uri, selection),
                        selectionArgs);
                break;
            case BLACKLIST_ID:
                count = db.delete(Tables.BLACKLIST, buildDefaultSelection(uri, selection),
                        selectionArgs);
                break;
            case REDIRECTION_LIST_ID:
                count = db.delete(Tables.REDIRECTION_LIST, buildDefaultSelection(uri, selection),
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // notify of changes in db
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    /**
     * Build default selection statement. If no extra selection is specified only build where clause
     * with rowId
     *
     * @param uri
     * @param selection
     * @return
     */
    private String buildDefaultSelection(Uri uri, String selection) {
        String rowId = uri.getPathSegments().get(1);
        String where = "";
        if (!TextUtils.isEmpty(selection)) {
            where = " AND (" + selection + ")";
        }

        return BaseColumns._ID + "=" + rowId + where;
    }
}