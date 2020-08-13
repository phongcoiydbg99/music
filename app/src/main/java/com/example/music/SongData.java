package com.example.music;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SongData {
    private Context mContext;
    private LinkedList<Song> mSongList;
    private int mCurrentSongId;

    public int getCurrentSongId() {
        return mCurrentSongId;
    }

    public void setCurrentSongId(int mCurrentSongId) {
        this.mCurrentSongId = mCurrentSongId;
    }


    public SongData(Context context) {
        mSongList = getAllSongs(context);
        mCurrentSongId = -1;
        mContext = context;
    }

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

    public LinkedList<Song> getSongList() {
        return mSongList;
    }

    public Song getSongAt(int pos)
    {
        return mSongList.get(pos);
    }
    public int getCount() {
        return mSongList.size();
    }

    public static LinkedList<Song> getAllSongs(Context context) {
        LinkedList<Song> songList = new LinkedList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int trackNumber = cursor.getInt(1);
                long duration = cursor.getInt(2);
                String title = cursor.getString(3);
                String artistName = cursor.getString(4);
                String composer = cursor.getString(5);
                String albumName = cursor.getString(6);
                String data = cursor.getString(7);

                Song song = new Song(id,title,artistName,composer,albumName,data,trackNumber,duration);
                Log.d("TAG", "Data: "+ data + " Album: " + albumName);
                songList.add(song);
            }
            cursor.close();
        }
        return  songList;
    }
    public static byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri);
        byte[] albumArt = mediaMetadataRetriever.getEmbeddedPicture();
        mediaMetadataRetriever.release();
        return albumArt;
    }
}
