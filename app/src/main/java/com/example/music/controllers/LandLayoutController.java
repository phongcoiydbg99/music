
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
import com.example.music.fragments.MediaPlaybackFragment;

public class LandLayoutController extends LayoutController {
    private static final String TAG = "LandLayoutController";
    private MediaPlaybackFragment mMediaPlaybackFragment;
    private SongData mSongData;
    private Song mSong;
    private boolean mIsPlaying;
    private int mCurrentSongPossion;
    private int mSongCurrentStreamPossition;

    public LandLayoutController(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, int songPos, long songDuration, boolean isPlaying, boolean isRepeat, boolean isShuffle) {
        if (mActivity.findViewById(R.id.contentAllSongs_land) != null) {
            Log.d(TAG, "onCreate: " +" * "+ songPos+" "+isPlaying);
            mCurrentSongPossion = songPos;
            mIsPlaying = isPlaying;
            mSongCurrentStreamPossition = (int) songDuration;
            mSongData = new SongData(mActivity.getApplicationContext());
            if (songPos < 0) songPos = 0;
            mSong = mSongData.getSongAt(songPos);
            mMediaPlaybackFragment = MediaPlaybackFragment.newInstance(mSong.getTitle(),mSong.getArtistName(),mSong.getData(),mSong.getDuration(),songPos,songDuration,isPlaying);

            // Create a new Fragment to be placed in the activity layout
            mAllSongsFragment = AllSongsFragment.newInstance(false);
            mAllSongsFragment.setSongCurrentPosition(songPos);
            mAllSongsFragment.setOnSongPlayClickListener(this);
            mAllSongsFragment.setOnSongItemClickListener(this);
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mAllSongsFragment)
                    .replace(R.id.fragment_media, mMediaPlaybackFragment)
                    .commit();
        }
    }

    @Override
    public void onConnection() {
        Log.d(TAG, "onConnection: "+ mSongCurrentStreamPossition+" "+mSong.getDuration());
        if (isConnected){
            mAllSongsFragment.setMediaPlaybackService(mediaPlaybackService);
            mMediaPlaybackFragment.setMediaPlaybackService(mediaPlaybackService);
            if (mCurrentSongPossion >= 0)
            mediaPlaybackService.startForegroundService(mCurrentSongPossion,mIsPlaying);
            if (mIsPlaying) {
                mAllSongsFragment.setSongCurrentPosition(mCurrentSongPossion);
                mAllSongsFragment.setPlaying(true);
                mAllSongsFragment.updateUILand();
                mMediaPlaybackFragment.updateSongCurrentData(mSongData.getSongAt(mCurrentSongPossion),mCurrentSongPossion,true);
                mMediaPlaybackFragment.updateUI();
            }
        }
    }

    @Override
    public void onSongPlayClickListener(View v, Song song, int pos, long current, boolean isPlaying) {
        Toast.makeText(mActivity, "Play music "+isConnected, Toast.LENGTH_SHORT).show();
        if (isConnected) {
//            Bundle args = newBundleFromNewItem(song);
//            mediaPlaybackFragment.setArguments(args);
            Log.d(TAG, "onSongPlayClickListener: "+ song.getTitle());
//            mActivity.getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_media, mMediaPlaybackFragment).commit();
        }
    }

    @Override
    public void onSongItemClick(SongListAdapter.SongViewHolder holder, int pos) {
        mAllSongsFragment.setSongCurrentPosition(pos);
        mediaPlaybackService.play(pos);
        mAllSongsFragment.setPlaying(true);
        mAllSongsFragment.updateUILand();
        Log.d(TAG, "onSongItemClick: " );
        mediaPlaybackService.startForegroundService(pos,true);
        if (isConnected)
        mMediaPlaybackFragment.updateSongCurrentData(mSongData.getSongAt(pos),pos,true);
        mMediaPlaybackFragment.setSongCurrentStreamPossition(0);
        mMediaPlaybackFragment.updateUI();
    }
}
