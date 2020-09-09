
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
    private int mCurrentSongPossion;
    private int mCurrentSongId;

    public PortLayoutController(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, int songPos, int songId, long songDuration, boolean isPlaying, boolean isRepeat, boolean isShuffle) {
        if (mActivity.findViewById(R.id.fragment_all_songs) != null) {
            // Create a new Fragment to be placed in the activity layout
            Log.d(TAG, "onCreate: " + songPos);
            isFavorite = false;
            mCurrentSongPossion = songPos;
            mCurrentSongId = songId;
            mBaseSongsFragment = AllSongsFragment.newInstance(true);
            mBaseSongsFragment.setOnSongPlayClickListener(this);
            mBaseSongsFragment.setOnSongItemClickListener(this);
            this.isPlaying = isPlaying;
            mBaseSongsFragment.setSongCurrentPosition(songPos);
            mBaseSongsFragment.setSongCurrentId(songId);
            mBaseSongsFragment.setPlaying(isPlaying);
            // Add the fragment to the 'fragment_container' FrameLayout
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mBaseSongsFragment).commit();
        }
    }

    @Override
    public void onCreateFavorite() {
        isFavorite = true;
        mBaseSongsFragment = FavoriteSongsFragment.newInstance(true);
        mBaseSongsFragment.setOnSongItemClickListener(this);
        mBaseSongsFragment.setOnSongPlayClickListener(this);
        mBaseSongsFragment.setOnSongRemoveFavoriteListener(this);
        mBaseSongsFragment.setMediaPlaybackService(mediaPlaybackService);
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_all_songs, mBaseSongsFragment).addToBackStack(null).commit();
    }

    @Override
    public void onCreateAllSong() {
        isFavorite = false;
        mBaseSongsFragment = AllSongsFragment.newInstance(true);
        mBaseSongsFragment.setOnSongPlayClickListener(this);
        mBaseSongsFragment.setOnSongItemClickListener(this);
        mBaseSongsFragment.setMediaPlaybackService(mediaPlaybackService);
        mBaseSongsFragment.setSongCurrentId(mediaPlaybackService.getCurrentSongId());
        mBaseSongsFragment.setSongCurrentPosition(mediaPlaybackService.getCurrentSongPosition());
        mBaseSongsFragment.setPlaying(mediaPlaybackService.isPlaying());
        mediaPlaybackService.setSongList(SongData.getAllSongs(mActivity));
        mediaPlaybackService.setCurrentSongIndex(mediaPlaybackService.getCurrentSongPosition());
        // Add the fragment to the 'fragment_container' FrameLayout
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_all_songs, mBaseSongsFragment).commit();
    }

    @Override
    public void onConnection() {
        if (isConnected) {
            mBaseSongsFragment.setMediaPlaybackService(mediaPlaybackService);
            isPlaying = mediaPlaybackService.isPlaying();
            Log.d(TAG, "onConnection: " + mediaPlaybackService.isPlaying());
            if (mCurrentSongPossion >= 0)
            {
                mediaPlaybackService.setSongList(SongData.getAllSongs(mActivity));
                mediaPlaybackService.setCurrentSongIndex(mediaPlaybackService.getCurrentSongPosition());
                mediaPlaybackService.startForegroundService(mCurrentSongPossion, isPlaying);
            }

            if (isPlaying) {
                mBaseSongsFragment.setPlaying(true);
                mBaseSongsFragment.setSongCurrentPosition(mCurrentSongPossion);
                mBaseSongsFragment.setSongCurrentId(mCurrentSongId);
                Log.d(TAG, "onConnection: ");
                Toast.makeText(mActivity, "Play music", Toast.LENGTH_SHORT).show();
                mBaseSongsFragment.updateUI();
            }
        }

    }

    @Override
    public void onSongPlayClickListener(View v, Song song, int pos, long current, boolean isPlaying) {
        Log.d(TAG, "onSongPlayClick: " + isPlaying);
        if (isConnected) {
            mMediaPlaybackFragment = MediaPlaybackFragment.newInstance(true, song.getTitle(), song.getArtistName(), song.getData(), song.getDuration(), pos, current, isPlaying);
            mMediaPlaybackFragment.setMediaPlaybackService(mediaPlaybackService);
            mMediaPlaybackFragment.setOnSongIsFavorClickListener(this);
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mMediaPlaybackFragment).addToBackStack(null).commit();
            mActivity.getSupportActionBar().hide();
        }
    }

    @Override
    public void onSongItemClick(SongListAdapter.SongViewHolder holder, Song song) {
        int pos = holder.getAdapterPosition();
        if (isFavorite) {
            mediaPlaybackService.setSongList(SongData.getFavorAllSongs(mActivity));
            Log.d(TAG, "onSongItemClick: " + SongData.getFavorAllSongs(mActivity).size());
        } else mediaPlaybackService.setSongList(SongData.getAllSongs(mActivity));

        mediaPlaybackService.play(song);
        mediaPlaybackService.startForegroundService(pos, true);
        mediaPlaybackService.setCurrentSongPosition(song.getPos());
        mediaPlaybackService.setCurrentSongIndex(pos);
        mediaPlaybackService.setCurrentSongId(song.getId());

        mBaseSongsFragment.setSongCurrentPosition(song.getPos());
        mBaseSongsFragment.setSongCurrentId(song.getId());
        mBaseSongsFragment.setPlaying(true);
        Log.d(TAG, "onSongItemClick: " + mediaPlaybackService.getCurrentSongId());
        Toast.makeText(mActivity, "Play music", Toast.LENGTH_SHORT).show();
        mBaseSongsFragment.updateUI();
    }

    @Override
    public void onSongRemoveFavoriteListener() {
        mBaseSongsFragment.refresh();
    }

    @Override
    public void onSongIsFavorClickListener() {
        mBaseSongsFragment.refresh();
    }
}
