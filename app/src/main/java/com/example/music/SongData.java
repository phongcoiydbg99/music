package com.example.music;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SongData {
    private static SongData songData;
    private Context mContext;
    private LinkedList<Song> mSongList;

//    private SongData(Context context) {
//        mContext = context.getApplicationContext();
//        mSongList = querySongs();
//    }


//    public static SongData get(Context context) {
//        if (songData == null) {
//            songData = new SongData(context);
//        }
//        return songData;
//    }

//    public Song getSong(long id) {
//        SongCursorWrapper cursorWrapper = querySong("_id=" + id, null);
//        try {
//            if (cursorWrapper.getCount() != 0) {
//                cursorWrapper.moveToFirst();
//                return cursorWrapper.getSong();
//            }
//            return Song.EMPTY();
//        } finally {
//            cursorWrapper.close();
//        }
//    }

    public Song getRandomSong() {
        Random r = new Random();
        return mSongList.get(r.nextInt(mSongList.size() - 1));
    }

    public Song getNextSong(Song currentSong) {
        try {
            return mSongList.get(mSongList.indexOf(currentSong) + 1);
        } catch (Exception e) {
            return getRandomSong();
        }
    }


    public Song getPreviousSong(Song currentSong) {
        try {
            return mSongList.get(mSongList.indexOf(currentSong) - 1);
        } catch (Exception e) {
            return getRandomSong();
        }
    }

    public List<Song> getmSongList() {
        return mSongList;
    }

//    public List<Song> querySongs() {
//        List<Song> songList = new ArrayList();
//        SongCursorWrapper cursor = querySong(null, null);
//        try {
//            cursor.moveToFirst();
//            do {
//                Song song = cursor.getSong();
//                song = cursor.getSong();
//                song.setAlbumArt(getAlbumUri(song.getAlbumId()).toString());
//                mSongList.add(song);
//            } while (cursor.moveToNext());
//        } finally {
//            cursor.close();
//        }
//        return songList;
//    }
}
