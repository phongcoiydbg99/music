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
import com.example.music.fragments.BaseSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;


public abstract class LayoutController implements AllSongsFragment.SongPlayClickListener, SongItemClickListener, BaseSongsFragment.SongRemoveFavoriteListener, MediaPlaybackFragment.SongIsFavorClickListener {

    private static final String TAG = LayoutController.class.getSimpleName();
    public static final String LAST_SONG_POS_EXTRA = "last_song_pos_extra";
    public static final String LAST_SONG_DURATION_EXTRA = "last_song_duration_extra";
    public static final String LAST_SONG_ISPLAYING_EXTRA = "last_song_isplaying_extra";
    public static final String LAST_SONG_IS_REPEAT_EXTRA = "last_song_is_repeat_extra";
    public static final String LAST_SONG_IS_SHUFFLE_EXTRA = "last_song_is_shuffle_extra" ;
    public static final String LAST_SONG_ID_EXTRA = "last_song_id_extra";


    protected AppCompatActivity mActivity;
    protected BaseSongsFragment mBaseSongsFragment;
    protected MediaPlaybackFragment mMediaPlaybackFragment;
    protected Boolean isFavorite;

    public LayoutController(AppCompatActivity activity) {
        mActivity = activity;
    }
    public MediaPlaybackService mediaPlaybackService;
    public boolean isConnected = false;

    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
    }

    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: "+ mediaPlaybackService.getCurrentSongIndex());
        int pos = mediaPlaybackService.getCurrentSongIndex() != -1 ?  mediaPlaybackService.getCurrentSongIndex() : 0;
        boolean isPlaying = mediaPlaybackService != null && mediaPlaybackService.isPlaying();
        long currentStreamPos = mediaPlaybackService != null ?  mediaPlaybackService.getCurrentStreamPosition() : 0;
        outState.putInt(LAST_SONG_POS_EXTRA, pos );
        outState.putLong(LAST_SONG_DURATION_EXTRA, currentStreamPos);
        outState.putBoolean(LAST_SONG_ISPLAYING_EXTRA, isPlaying);
    }

    public abstract void onCreate(Bundle savedInstanceState, int songPos,int songId, long songDuration, boolean isPlaying);
    public abstract void onCreateFavorite();
    public abstract void onCreateAllSong();
    public abstract void onConnection();
}
