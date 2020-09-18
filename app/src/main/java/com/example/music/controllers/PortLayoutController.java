
package com.example.music.controllers;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.music.MusicDB;
import com.example.music.MusicProvider;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.activities.ActivityMusic;
import com.example.music.adapters.SongListAdapter;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.BaseSongsFragment;
import com.example.music.fragments.FavoriteSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;


public class PortLayoutController extends LayoutController {
    public static final String TAG = "PortLayoutController";
    private boolean isPlaying;
    private int mSongCurrentIndex;
    private int mCurrentSongId;

    public PortLayoutController(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, int songPos, int songId, long songDuration, boolean isPlaying) {
        if (mActivity.findViewById(R.id.fragment_all_songs) != null) {
            // Create a new Fragment to be placed in the activity layout
            Log.d(TAG, "onCreate: " + songPos);
            isFavorite = false;
            mSongCurrentIndex = songPos;
            mCurrentSongId = songId;
            mBaseSongsFragment = AllSongsFragment.newInstance(true);
            setListener();
            this.isPlaying = isPlaying;
            mBaseSongsFragment.setStateMusic(songPos, songId, isPlaying);
            // Add the fragment to the 'fragment_container' FrameLayout
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mBaseSongsFragment).commit();
        }
    }

    @Override
    public void onCreateFavorite() {
        isFavorite = true;
        mBaseSongsFragment = FavoriteSongsFragment.newInstance(true);
        setListener();
        mBaseSongsFragment.setMediaPlaybackService(mediaPlaybackService);
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_all_songs, mBaseSongsFragment).commit();
    }

    @Override
    public void onCreateAllSong() {
        isFavorite = false;
        mBaseSongsFragment = AllSongsFragment.newInstance(true);
        setListener();
        mBaseSongsFragment.setMediaPlaybackService(mediaPlaybackService);
        mBaseSongsFragment.setStateMusic(mediaPlaybackService.getCurrentSongPosition(), mediaPlaybackService.getCurrentSongId(), mediaPlaybackService.isPlaying());
        mBaseSongsFragment.setFavorite(isFavorite);
        mediaPlaybackService.setSongList(SongData.getAllSongs(mActivity));
        mediaPlaybackService.setCurrentSongIndex(mediaPlaybackService.getCurrentSongPosition());
        // Add the fragment to the 'fragment_container' FrameLayout
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_all_songs, mBaseSongsFragment).commit();
    }

    @Override
    public void onConnection() {
        mBaseSongsFragment.setMediaPlaybackService(mediaPlaybackService);
        isPlaying = mediaPlaybackService.isPlaying();
        Log.d(TAG, "onConnection: " + mSongCurrentIndex);
        mediaPlaybackService.setSongList(SongData.getAllSongs(mActivity));
        mediaPlaybackService.setCurrentSongIndex(SongData.getSongIndex(mediaPlaybackService.getSongList(), mCurrentSongId));
        mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongIndex(), isPlaying);
        mBaseSongsFragment.setStateMusic(mSongCurrentIndex, mCurrentSongId, isPlaying);
        if (isPlaying)
            mBaseSongsFragment.updateUI();

    }

    @Override
    public void onSongPlayClickListener(View v, Song song, long current, boolean isPlaying) {
        Log.d(TAG, "onSongPlayClick: " + mediaPlaybackService.getCurrentSongIndex());
        mMediaPlaybackFragment = MediaPlaybackFragment.newInstance(true, song.getTitle(), song.getArtistName(), song.getData(), song.getDuration(), current, isPlaying);
        mMediaPlaybackFragment.setMediaPlaybackService(mediaPlaybackService);
        mMediaPlaybackFragment.setOnSongIsFavorClickListener(this);
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_all_songs, mMediaPlaybackFragment).addToBackStack(null).commit();
        mActivity.getSupportActionBar().hide();
    }

    @Override
    public void onSongItemClick(SongListAdapter.SongViewHolder holder, Song song) {
        int pos = holder.getAdapterPosition();
        if (isFavorite) {
            mediaPlaybackService.setSongList(SongData.getFavorAllSongs(mActivity));
            Log.d(TAG, "onSongItemClick: " + SongData.getFavorAllSongs(mActivity).size());
        } else mediaPlaybackService.setSongList(SongData.getAllSongs(mActivity));
        mediaPlaybackService.play(song);
        int index = (!isFavorite) ? song.getPos() : SongData.getSongIndex(SongData.getFavorAllSongs(mActivity), song.getId());
        Log.d(TAG, "onSongItemClick: " + index);
        mediaPlaybackService.startForegroundService(index, true);
        mediaPlaybackService.setStateMusic(song.getPos(), index, song.getId());
        mBaseSongsFragment.setStateMusic(index, song.getId(), true);
        mBaseSongsFragment.setFavorite(isFavorite);
        Log.d(TAG, "onSongItemClick: " + mediaPlaybackService.getCurrentSongId());
        mBaseSongsFragment.updateUI();
    }

    @Override
    public void onSongRemoveFavoriteListener() {
        mBaseSongsFragment.refresh();
    }

    @Override
    public void onSongIsFavorClickListener() {
        mBaseSongsFragment.setSongListPlay();
        mBaseSongsFragment.refresh();
    }

    public void setListener() {
        mBaseSongsFragment.setOnSongPlayClickListener(this);
        mBaseSongsFragment.setOnSongItemClickListener(this);
        mBaseSongsFragment.setOnSongRemoveFavoriteListener(this);
    }
}
