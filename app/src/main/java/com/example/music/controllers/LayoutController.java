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

    private static final String TAG = LayoutController.class.getSimpleName();
    public static final String LAST_SONG_POS_EXTRA = "last_song_id_extra";
    public static final String LAST_SONG_DURATION_EXTRA = "last_song_duration_extra";
    public static final String LAST_SONG_ISPLAYING_EXTRA = "last_song_isplaying_extra";
    public static final String LAST_SONG_IS_REPEAT_EXTRA = "last_song_is_repeat_extra";
    public static final String LAST_SONG_IS_SHUFFLE_EXTRA = "last_song_is_shuffle_extra" ;


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

    public void onSaveInstanceState(Bundle outState) {
        int pos = mediaPlaybackService.getCurrentSongPosition() != -1 ?  mediaPlaybackService.getCurrentSongPosition() : 0;
        boolean isPlaying = mediaPlaybackService != null ? mediaPlaybackService.isPlaying() : false;
        boolean isRepeat = mediaPlaybackService != null ? mediaPlaybackService.isRepeat() : false;
        boolean isShuffle = mediaPlaybackService != null ? mediaPlaybackService.isShuffle() : false;
        long currentStreamPos = mediaPlaybackService != null ?  mediaPlaybackService.getCurrentStreamPosition() : 0;
        outState.putInt(LAST_SONG_POS_EXTRA, pos );
        outState.putLong(LAST_SONG_DURATION_EXTRA, currentStreamPos);
        outState.putBoolean(LAST_SONG_ISPLAYING_EXTRA, isPlaying);
        outState.putBoolean(LAST_SONG_IS_REPEAT_EXTRA, isRepeat);
        outState.putBoolean(LAST_SONG_IS_SHUFFLE_EXTRA, isShuffle);
    }
    
    public abstract void onCreate(Bundle savedInstanceState, int songPos, long songDuration, boolean isPlaying, boolean isRepeat, boolean isShuffle);
    public abstract void onConnection();
}
