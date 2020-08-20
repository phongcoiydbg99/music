package com.example.music.controllers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.music.Song;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;


public abstract class LayoutController implements AllSongsFragment.SongPlayClickListener, SongItemClickListener {

    public static final String LAST_SONG_POS_EXTRA = "last_song_id_extra";
    public static final String LAST_SONG_DURATION_EXTRA = "last_song_duration_extra";
    public static final String LAST_SONG_ISPLAYING_EXTRA = "last_song_isplaying_extra";
    private static final String TAG = LayoutController.class.getSimpleName();


    protected AppCompatActivity mActivity;
    protected AllSongsFragment mAllSongsFragment;

    public LayoutController(AppCompatActivity activity) {
        mActivity = activity;
    }
    public MediaPlaybackService mediaPlaybackService;
    public Intent playIntent;
    public boolean isConnected = false;

    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    protected Bundle newBundleFromNewItem(Song song) {
        Bundle args = new Bundle();
        args.putString(MediaPlaybackFragment.ID, String.valueOf(song.getId()));
        args.putString(MediaPlaybackFragment.TITLE, song.getTitle());
        args.putString(MediaPlaybackFragment.ARTIST, song.getArtistName());
        args.putString(MediaPlaybackFragment.DATA, song.getData());
        args.putString(MediaPlaybackFragment.DURATION, String.valueOf(song.getDuration()));
        return args;
    }

    public void onSaveInstanceState(Bundle outState) {
        int pos = mediaPlaybackService.getCurrentSongPosition() != -1 ?  mediaPlaybackService.getCurrentSongPosition() : 0;
        long currentStreamPos = mediaPlaybackService != null ?  mediaPlaybackService.getCurrentStreamPosition() : 0;
        boolean isPlaying = mediaPlaybackService == null || mediaPlaybackService.isPlaying();
        outState.putInt(LAST_SONG_POS_EXTRA, pos );
        outState.putLong(LAST_SONG_DURATION_EXTRA, currentStreamPos);
        outState.putBoolean(LAST_SONG_ISPLAYING_EXTRA, isPlaying);
    }
    
    public abstract void onCreate(Bundle savedInstanceState, int songPos, long songDuration, boolean isPlaying);
    public abstract void onConnection();
}
