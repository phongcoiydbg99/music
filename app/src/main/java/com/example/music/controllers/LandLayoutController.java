
package com.example.music.controllers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.adapters.SongListAdapter;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.BaseSongsFragment;
import com.example.music.fragments.FavoriteSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;

public class LandLayoutController extends LayoutController {
    private static final String TAG = "LandLayoutController";
    private SongData mSongData;
    private Song mSong;
    private boolean mIsPlaying;
    private int mCurrentSongPossion;
    private int mSongCurrentStreamPossition;
    private int mCurrentSongId;

    public LandLayoutController(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, int songPos, int songId, long songDuration, boolean isPlaying) {
        if (mActivity.findViewById(R.id.contentAllSongs_land) != null) {
            if (songPos < 0) songPos = 0;
            mCurrentSongPossion = songPos;
            mIsPlaying = isPlaying;
            mSongCurrentStreamPossition = (int) songDuration;
            isFavorite = false;
            mSongData = new SongData(mActivity.getApplicationContext());
            mSong = mSongData.getSongAt(songPos);
            if (songDuration > mSong.getDuration()) mSongCurrentStreamPossition = 0;
            mCurrentSongId = mSong.getId();
            Log.d(TAG, "onCreate: " + " * " + " " + isPlaying);

            mMediaPlaybackFragment = MediaPlaybackFragment.newInstance(false, mSong.getTitle(), mSong.getArtistName(), mSong.getData(), mSong.getDuration(), mCurrentSongPossion, mSongCurrentStreamPossition, mIsPlaying);
            mMediaPlaybackFragment.setOnSongIsFavorClickListener(this);
            // Create a new Fragment to be placed in the activity layout
            mBaseSongsFragment = AllSongsFragment.newInstance(false);
            mBaseSongsFragment.setStateMusic(mCurrentSongPossion, mCurrentSongId, mIsPlaying);

            setListener();

            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mBaseSongsFragment)
                    .replace(R.id.fragment_media, mMediaPlaybackFragment)
                    .commit();
        }
    }

    @Override
    public void onCreateFavorite() {
        isFavorite = true;
        mBaseSongsFragment = FavoriteSongsFragment.newInstance(false);
        mBaseSongsFragment.setMediaPlaybackService(mediaPlaybackService);
        Log.d(TAG, "onCreateFavorite: " + mediaPlaybackService.getCurrentSongId());
        setListener();
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_all_songs, mBaseSongsFragment).commit();
    }

    @Override
    public void onCreateAllSong() {
        isFavorite = false;
        mBaseSongsFragment = AllSongsFragment.newInstance(false);
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
        Log.d(TAG, "onConnection: " + mSongCurrentStreamPossition + " " + mediaPlaybackService.getDuration());
        Log.d(TAG, "onConnection: " + mCurrentSongPossion + " " + mediaPlaybackService.getCurrentStreamPosition());
        Log.d(TAG, "onConnection: " + mCurrentSongId);
        if (isConnected) {

            mBaseSongsFragment.setMediaPlaybackService(mediaPlaybackService);
            mMediaPlaybackFragment.setMediaPlaybackService(mediaPlaybackService);

            if (mCurrentSongPossion >= 0) {
                mediaPlaybackService.setStateMusic(mCurrentSongPossion, mCurrentSongPossion, mCurrentSongId);
                mediaPlaybackService.startForegroundService(mCurrentSongPossion, mIsPlaying);
            }

            mBaseSongsFragment.setStateMusic(mCurrentSongPossion, mCurrentSongId, mIsPlaying);
            mBaseSongsFragment.updateUI();
            mMediaPlaybackFragment.updateSongCurrentData(mSongData.getSongAt(mCurrentSongPossion), mCurrentSongPossion, mIsPlaying);
            mMediaPlaybackFragment.updateUI();

        }
    }

    @Override
    public void onSongPlayClickListener(View v, Song song, int pos, long current, boolean isPlaying) {
        if (isConnected) {
            Log.d(TAG, "onSongPlayClickListener: " + song.getTitle());
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
        mediaPlaybackService.setStateMusic(song.getPos(), holder.getAdapterPosition(), song.getId());
        Log.d(TAG, "onSongItemClick: " + holder.getAdapterPosition());
        mBaseSongsFragment.setStateMusic(holder.getAdapterPosition(), mediaPlaybackService.getCurrentSongId(), true);
        mBaseSongsFragment.setFavorite(isFavorite);
        mBaseSongsFragment.updateUI();

        mediaPlaybackService.startForegroundService(pos, true);
        if (isConnected)
            mMediaPlaybackFragment.updateSongCurrentData(mediaPlaybackService.getSongList().get(pos), pos, true);
        mMediaPlaybackFragment.setSongCurrentStreamPossition(0);
        mMediaPlaybackFragment.updateUI();
    }

    @Override
    public void onSongIsFavorClickListener() {
        mBaseSongsFragment.refresh();
        mBaseSongsFragment.setSongListPlay();
    }

    @Override
    public void onSongRemoveFavoriteListener() {
        mMediaPlaybackFragment.updateUI();
    }

    public void setListener() {
        mBaseSongsFragment.setOnSongPlayClickListener(this);
        mBaseSongsFragment.setOnSongItemClickListener(this);
        mBaseSongsFragment.setOnSongRemoveFavoriteListener(this);
    }

}
