/*
 * This file is part of TinyWeatherForecastGermany.
 *
 * Copyright (c) 2020, 2021 Pawel Dube
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.kaffeemitkoffein.tinyweatherforecastgermany;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;

public class AreaContentProvider extends ContentProvider {

    static final String AUTHORITY = "de.kaffeemitkoffein.tinyweatherforecastgermany.areas";
    static final String DATASERVICE = "areas";
    static final String URL_AREADATA = "content://" + AUTHORITY + "/" + DATASERVICE;
    static final Uri URI_AREADATA = Uri.parse(URL_AREADATA);

    private AreaDatabaseHelper areaDatabaseHelper;
    private SQLiteDatabase sqLiteDatabase;

    private Context context;

    public void setContext(Context context){
        this.context = context;
    }

    @Override
    public boolean onCreate() {
        if (context!=null){
            areaDatabaseHelper = new AreaContentProvider.AreaDatabaseHelper(context);
        } else {
            if (getContext().getApplicationContext()==null){
            }
            areaDatabaseHelper = new AreaContentProvider.AreaDatabaseHelper(getContext().getApplicationContext());
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        areaDatabaseHelper = new AreaDatabaseHelper(context);
        sqLiteDatabase = areaDatabaseHelper.getReadableDatabase();
        Cursor c = sqLiteDatabase.query(AreaContentProvider.AreaDatabaseHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, null);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return "text/plain";
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        sqLiteDatabase = areaDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.enableWriteAheadLogging();
        sqLiteDatabase.insert(AreaContentProvider.AreaDatabaseHelper.TABLE_NAME, null, contentValues);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int i = 0;
        sqLiteDatabase = areaDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.enableWriteAheadLogging();
        i = sqLiteDatabase.delete(AreaContentProvider.AreaDatabaseHelper.TABLE_NAME, selection, selectionArgs);
        return i;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        sqLiteDatabase = areaDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.enableWriteAheadLogging();
        return sqLiteDatabase.update(AreaContentProvider.AreaDatabaseHelper.TABLE_NAME, contentValues, selection, selectionArgs);
    }

    public static class AreaDatabaseHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "areas";
        public static final String TABLE_NAME = "tables";
        public static final String KEY_id = "id";
        public static final String KEY_warncellid = "warncellid";
        public static final String KEY_warncenter = "warncenter";
        public static final String KEY_type = "type";
        public static final String KEY_name = "name";
        public static final String KEY_polygonstring = "polygonstring";


        public static final String SQL_COMMAND_CREATE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_id + " INTEGER PRIMARY KEY ASC,"
                + KEY_warncellid + " TEXT,"
                + KEY_warncenter + " TEXT,"
                + KEY_type + " INTEGER,"
                + KEY_name + " TEXT,"
                + KEY_polygonstring + " TEXT"
                + ");";

        public static final String SQL_COMMAND_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public AreaDatabaseHelper(Context c) {
            super(c, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SQL_COMMAND_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE);
            onCreate(sqLiteDatabase);
        }
    }

    private final static String serial_serparator = "_,_";

    private String serializeString(ArrayList<String> s) {
        return TextUtils.join(serial_serparator, s);
    }

    public static ArrayList<String> deSerializeString(String s) {
        String[] results = TextUtils.split(s, serial_serparator);
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < results.length; i++) {
            list.add(results[i]);
        }
        return list;
    }

    public static final String KEY_warncellid = "warncellid";
    public static final String KEY_warncenter = "warncenter";
    public static final String KEY_type = "type";
    public static final String KEY_name = "name";
    public static final String KEY_polygonstring = "polygonstring";


    public ContentValues getContentValuesFromArea(Areas.Area area) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AreaDatabaseHelper.KEY_warncellid, area.warncellID);
        contentValues.put(AreaDatabaseHelper.KEY_warncenter, area.warncenter);
        contentValues.put(AreaDatabaseHelper.KEY_type, area.type);
        contentValues.put(AreaDatabaseHelper.KEY_name, area.name);
        contentValues.put(AreaDatabaseHelper.KEY_polygonstring, area.polygonString);
        return contentValues;
    }

    public Areas.Area getAreaFromCursor(Cursor c) {
        if (c == null) {
            return null;
        } else {
            Areas.Area area = new Areas.Area();
            area.warncellID = c.getString(c.getColumnIndex(AreaDatabaseHelper.KEY_warncellid));
            area.warncenter = c.getString(c.getColumnIndex(AreaDatabaseHelper.KEY_warncenter));
            area.type = c.getInt(c.getColumnIndex(AreaDatabaseHelper.KEY_type));
            area.name = c.getString(c.getColumnIndex(AreaDatabaseHelper.KEY_name));
            area.polygonString = c.getString(c.getColumnIndex(AreaDatabaseHelper.KEY_polygonstring));
            return area;
        }
    }

    public String getAreaNameFromCursor(Cursor c) {
        if (c == null) {
            return null;
        } else {
            return c.getString(c.getColumnIndex(AreaDatabaseHelper.KEY_name));
        }
    }

    public void writeArea(Context c, Areas.Area area) {
        ContentResolver contentResolver = c.getApplicationContext().getContentResolver();
        contentResolver.insert(AreaContentProvider.URI_AREADATA, getContentValuesFromArea(area));
    }

    public static int checkForDatabaseUpgrade(Context c) {
        AreaContentProvider.AreaDatabaseHelper areaDatabaseHelper = new AreaContentProvider.AreaDatabaseHelper(c);
        SQLiteDatabase sqLiteDatabase = areaDatabaseHelper.getWritableDatabase();
        int i = sqLiteDatabase.getVersion();
        sqLiteDatabase.close();
        return i;
        // this should have triggered the update.
    }
}