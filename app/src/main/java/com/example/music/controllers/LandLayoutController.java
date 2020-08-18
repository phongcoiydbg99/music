
package com.example.music.controllers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;
import com.example.music.fragments.SongPlayFragment;

public class LandLayoutController extends LayoutController {
    private static final String TAG = "LayoutController";
    private MediaPlaybackFragment mMediaPlaybackFragment;
    private SongData mSongData;
    private Song mSong;

    public LandLayoutController(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, int songId) {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (mActivity.findViewById(R.id.contentAllSongs_land) != null) {
            Log.d(TAG, "onCreate: " + songId);
            mSongData = new SongData(mActivity.getApplicationContext());
            if (songId < 0) mSong = mSongData.getSongId(60);
            else mSong = mSongData.getSongId(songId);
            Log.d(TAG, "onCreate: " + mSong.getTitle());
            mMediaPlaybackFragment = MediaPlaybackFragment.newInstance(mSong.getTitle(),mSong.getArtistName(),mSong.getData(),mSong.getDuration(),mSongData.getCurrentSongPossition(),3,true);
            Bundle args = new Bundle();
            args.putInt(LAST_SONG_ID_EXTRA, songId);
            mMediaPlaybackFragment.setArguments(args);

            // Create a new Fragment to be placed in the activity layout
            mAllSongsFragment = new AllSongsFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mAllSongsFragment)
                    .replace(R.id.fragment_media, mMediaPlaybackFragment)
                    .commit();
        }
    }

    @Override
    public void onSongPlayClickListener(View v, Song song, int pos, long current, boolean isPlaying) {

    }


//    @Override
//    public void onSongItemClick(View v, int pos) {
//        Toast.makeText(mActivity, "Play music", Toast.LENGTH_SHORT).show();
////        SongPlayFragment songPlayFragment = new SongPlayFragment();
////        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
////        transaction.replace(R.id.fragment_play, songPlayFragment).commit();
//    }


//    @Override
//    public void onNewClick(NewItem item) {
//        Bundle args = newBundleFromNewItem(item);
//        mNewContentFragment.update(args);
//    }
}
