
package com.example.music.controllers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.adapters.SongListAdapter;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;


public class PortLayoutController extends LayoutController {
    public static final String TAG = "PortLayoutController";
    private SongData songData;
    private boolean isPlaying;
    public PortLayoutController(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, int songPos, long songDuration, boolean isPlaying, boolean isRepeat, boolean isShuffle) {
        if (mActivity.findViewById(R.id.fragment_all_songs) != null) {
            // Create a new Fragment to be placed in the activity layout
            Log.d(TAG, "onCreate: "+ songPos);
            mAllSongsFragment = AllSongsFragment.newInstance(true);
            mAllSongsFragment.setOnSongPlayClickListener(this);
            mAllSongsFragment.setOnSongItemClickListener(this);
            this.isPlaying = isPlaying;
            mAllSongsFragment.setSongCurrentPosition(songPos);
            mAllSongsFragment.setPlaying(isPlaying);
            songData = new SongData(mActivity);
            // Add the fragment to the 'fragment_container' FrameLayout
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mAllSongsFragment).commit();
        }
    }

    @Override
    public void onConnection() {
        if (isConnected) {
            mAllSongsFragment.setMediaPlaybackService(mediaPlaybackService);
            if (isPlaying) {
                mAllSongsFragment.setPlaying(true);
                mAllSongsFragment.setSongCurrentPosition(mediaPlaybackService.getCurrentSongPosition());
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
                    .replace(R.id.fragment_all_songs, mediaPlaybackFragment).addToBackStack(null).commit();
            mActivity.getSupportActionBar().hide();
        }
    }

    @Override
    public void onSongItemClick(SongListAdapter.SongViewHolder holder, int pos) {
        mAllSongsFragment.setOnSongPlay(true);
        mAllSongsFragment.setSongCurrentPosition(pos);
        mediaPlaybackService.play(pos);
        mediaPlaybackService.startForegroundService(pos,true);
        mAllSongsFragment.setPlaying(true);
        Log.d(TAG, "onSongItemClick: " );
        Toast.makeText(mActivity, "Play music", Toast.LENGTH_SHORT).show();
        mAllSongsFragment.updateUI();
    }
}
