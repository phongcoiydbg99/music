package com.example.music;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MusicDB {
    public static final String ID = "_id";
    public static final String ID_PROVIDER = "is_provider";
    public static final String SONG_NAME = "song_name";
    public static final String SONG_DATA = "song_data";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String COUNT_OF_PLAY = "count_of_play";

    private static final String LOG_TAG = "MusicDB";
    public static final String SQLITE_TABLE = "MusicDB";

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    ID + " integer PRIMARY KEY autoincrement," +
                    ID_PROVIDER + "," +
                    SONG_NAME + "," +
                    SONG_DATA + "," +
                    IS_FAVORITE + "," +
                    COUNT_OF_PLAY + "," +
                    " UNIQUE (" + ID_PROVIDER +"));";

    public static void onCreate(SQLiteDatabase db) {
        Log.w(LOG_TAG, DATABASE_CREATE);
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
        onCreate(db);
    }
}
