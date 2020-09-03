package com.example.music;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class MusicHelper {
    public static Song getSong(Context context, int songPos){
        String[] projectionDB = {
                MusicDB.ID,
                MusicDB.ID_PROVIDER,
                MusicDB.TITLE,
                MusicDB.ARTIST,
                MusicDB.DURATION,
                MusicDB.DATA,
                MusicDB.IS_FAVORITE,
                MusicDB.COUNT_OF_PLAY
        };
        Uri uri = Uri.parse(MusicProvider.CONTENT_URI + "/" + songPos);
        Cursor cursor = context.getContentResolver().query(uri, projectionDB, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            int id_provider = cursor.getInt(1);
            String title = cursor.getString(2);
            String artistName = cursor.getString(3);
            long duration = cursor.getInt(4);
            String data = cursor.getString(5);
            String is_fravorite = cursor.getString(6);
            String count_of_play = cursor.getString(7);
            return  new Song(0,id_provider,title,artistName,data,duration);
        }
        return null;
    }
}
