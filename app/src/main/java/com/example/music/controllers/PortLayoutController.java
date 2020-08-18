
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
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;


public class PortLayoutController extends LayoutController {
    public static final String TAG = "PortLayoutController";
    public PortLayoutController(AppCompatActivity activity) {
        super(activity);
    }

    private SongData songData;
    @Override
    public void onCreate(Bundle savedInstanceState, int songId) {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (mActivity.findViewById(R.id.fragment_all_songs) != null) {
            // Create a new Fragment to be placed in the activity layout
            mAllSongsFragment = new AllSongsFragment();
            mAllSongsFragment.setOnSongPlayClickListener(this);
            Log.d("TAGG", "onCreate: ");
            songData = new SongData(mActivity);
            // Add the fragment to the 'fragment_container' FrameLayout
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mAllSongsFragment).commit();
        }
    }

    @Override
    public void onSongPlayClickListener(View v, Song song, int pos,long current, boolean isPlaying) {
        Log.d(TAG, "onSongPlayClick: " + isPlaying);

        MediaPlaybackFragment mediaPlaybackFragment = MediaPlaybackFragment.newInstance(song.getTitle(),song.getArtistName(),song.getData(),song.getDuration(),pos,current,isPlaying);
        Bundle args = newBundleFromNewItem(song);
        mediaPlaybackFragment.setArguments(args);
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_all_songs, mediaPlaybackFragment).addToBackStack(null).commit();
        mActivity.getSupportActionBar().hide();
    }

//    @Override
//    public void onNewClick(NewItem item) {
//        // Create fragment and give it an argument specifying the article it should show
//        NewContentFragment newFragment = new NewContentFragment();
//        Bundle args = newBundleFromNewItem(item);
//        newFragment.setArguments(args);
//
//        FragmentTransaction transaction = mActivity.getSupportFragmentManager()
//                .beginTransaction();
//
//        // Replace whatever is in the fragment_container view with this fragment,
//        // and add the transaction to the back stack so the user can navigate back
//        transaction.replace(R.id.fragment_container, newFragment);
//        transaction.addToBackStack(null);
//
//        // Commit the transaction
//        transaction.commit();
//    }
}
