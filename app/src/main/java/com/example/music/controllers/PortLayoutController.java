
package com.example.music.controllers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.adapters.SongListAdapter;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;


public class PortLayoutController extends LayoutController {
    public static final String TAG = "PortLayoutController";
    private boolean isPlaying;
    private int mCurrentSongPossion;

    public PortLayoutController(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, int songPos, long songDuration, boolean isPlaying, boolean isRepeat, boolean isShuffle) {
        if (mActivity.findViewById(R.id.fragment_all_songs) != null) {
            // Create a new Fragment to be placed in the activity layout
            Log.d(TAG, "onCreate: "+ songPos);
            mCurrentSongPossion = songPos;
            mAllSongsFragment = AllSongsFragment.newInstance(true);
            mAllSongsFragment.setOnSongPlayClickListener(this);
            mAllSongsFragment.setOnSongItemClickListener(this);
            this.isPlaying = isPlaying;
            mAllSongsFragment.setSongCurrentPosition(songPos);
            mAllSongsFragment.setPlaying(isPlaying);
            // Add the fragment to the 'fragment_container' FrameLayout
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mAllSongsFragment).commit();
        }
    }

    @Override
    public void onConnection() {
        if (isConnected) {
            mAllSongsFragment.setMediaPlaybackService(mediaPlaybackService);
            isPlaying = mediaPlaybackService.isPlaying();
            Log.d(TAG, "onConnection: "+mediaPlaybackService.isPlaying());
            if (mCurrentSongPossion >= 0)
            mediaPlaybackService.startForegroundService(mCurrentSongPossion,isPlaying);
            if (isPlaying) {
                mAllSongsFragment.setPlaying(true);
                mAllSongsFragment.setSongCurrentPosition(mCurrentSongPossion);
                Log.d(TAG, "onConnection: " );
                Toast.makeText(mActivity, "Play music", Toast.LENGTH_SHORT).show();
                mAllSongsFragment.updateUI();
            }
        }

    }

    @Override
    public void onSongPlayClickListener(View v, Song song, int pos,long current, boolean isPlaying) {
        Log.d(TAG, "onSongPlayClick: " + isPlaying);
        if (isConnected) {
            MediaPlaybackFragment mediaPlaybackFragment = MediaPlaybackFragment.newInstance(song.getTitle(), song.getArtistName(), song.getData(), song.getDuration(), pos, current, isPlaying);
            mediaPlaybackFragment.setMediaPlaybackService(mediaPlaybackService);
            mActivity.getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_all_songs, mediaPlaybackFragment).addToBackStack(null).commit();
            mActivity.getSupportActionBar().hide();
        }
    }

    @Override
    public void onSongItemClick(SongListAdapter.SongViewHolder holder, int pos) {
        mAllSongsFragment.setSongCurrentPosition(pos);
        mediaPlaybackService.play(pos);
        mediaPlaybackService.startForegroundService(pos,true);
        mAllSongsFragment.setPlaying(true);
        Log.d(TAG, "onSongItemClick: " );
        Toast.makeText(mActivity, "Play music", Toast.LENGTH_SHORT).show();
        mAllSongsFragment.updateUI();
    }
}
