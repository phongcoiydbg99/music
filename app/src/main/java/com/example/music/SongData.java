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
    private LinkedList<Song> mSongListFavor;
    private int mCurrentSongPossition;
    private int mSongCurrentId;
    private boolean isPlaying;
    private static String sortOrder = "song_title ASC";

    public int getmSongCurrentId() {
        return mSongCurrentId;
    }

    public void setmSongCurrentId(int mSongCurrentId) {
        this.mSongCurrentId = mSongCurrentId;
    }

    public int getCurrentSongPossition() {
        return mCurrentSongPossition;
    }

    public void setCurrentSongPossition(int mCurrentSongPossition) {
        this.mCurrentSongPossition = mCurrentSongPossition;
    }


    public SongData(Context context) {
        mSongList = getAllSongs(context);
        mSongListFavor = getFavorAllSongs(context);
        mCurrentSongPossition = -1;
        mSongCurrentId = -1;
        mContext = context;
    }

    public Song getRandomSong() {
        Random r = new Random();
        return mSongList.get(r.nextInt(mSongList.size() - 1));
    }
    public int getRandomSongPos() {
        Random r = new Random();
        return r.nextInt(mSongList.size() - 1);
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

    public Song getSongId(int id){
        int pos = 0;
        if (mSongList.size() > 0) {
            for(Song song: mSongList){
                if (song.getId() == id)
                {
                    mCurrentSongPossition = pos;
                    return song;
                }
                pos++;
            }
        }
        return null;
    }
    public static LinkedList<Song> getAllSongs(Context context) {

        LinkedList<Song> songList = new LinkedList<>();
        LinkedList<Song> songListFavor = new LinkedList<>();
        int pos = 0;
        int posFavor = 0;
        Uri uri =  Uri.parse(String.valueOf(MusicProvider.CONTENT_URI));;
        String[] projection = {
                MusicDB.ID,
                MusicDB.ID_PROVIDER,
                MusicDB.TITLE,
                MusicDB.ARTIST,
                MusicDB.DURATION,
                MusicDB.DATA,
                MusicDB.IS_FAVORITE,
                MusicDB.COUNT_OF_PLAY
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int id_provider = cursor.getInt(1);
                String title = cursor.getString(2);
                String artistName = cursor.getString(3);
                long duration = cursor.getInt(4);
                String data = cursor.getString(5);
                int is_fravorite = cursor.getInt(6);
                String count_of_play = cursor.getString(7);
                Song song = new Song(pos,id_provider,title,artistName,data,duration);
                Log.d("TAG", "Data: "+ id + " Album: " + is_fravorite);
                songList.add(song);
                if (is_fravorite == 2 ) {
                    songListFavor.add(song);
                    posFavor++;
                }
                pos++;
            }
            cursor.close();
        }
        return  songList;
    }
    public static LinkedList<Song> getFavorAllSongs(Context context) {

        LinkedList<Song> songListFavor = new LinkedList<>();
        int pos = 0;
        int posFavor = 0;
        Uri uri =  Uri.parse(String.valueOf(MusicProvider.CONTENT_URI));;
        String[] projection = {
                MusicDB.ID,
                MusicDB.ID_PROVIDER,
                MusicDB.TITLE,
                MusicDB.ARTIST,
                MusicDB.DURATION,
                MusicDB.DATA,
                MusicDB.IS_FAVORITE,
                MusicDB.COUNT_OF_PLAY
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int id_provider = cursor.getInt(1);
                String title = cursor.getString(2);
                String artistName = cursor.getString(3);
                long duration = cursor.getInt(4);
                String data = cursor.getString(5);
                int is_fravorite = cursor.getInt(6);
                String count_of_play = cursor.getString(7);
                Log.d("TAG", "Data: "+ id + " Album: " + is_fravorite);
                if (is_fravorite == 2 ) {
                    Song song = new Song(posFavor,id_provider,title,artistName,data,duration);
                    songListFavor.add(song);
                    posFavor++;
                }
            }
            cursor.close();
        }
        return  songListFavor;
    }
    public static byte[] getAlbumArt(String uri)
    {
        byte[] albumArt = new byte[0];
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(uri);
            albumArt = mediaMetadataRetriever.getEmbeddedPicture();
            mediaMetadataRetriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return albumArt;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public LinkedList<Song> getSongListFavor() {
        return mSongListFavor;
    }
}
