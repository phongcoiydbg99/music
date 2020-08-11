
package com.example.music.controllers;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.example.music.R;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;
import com.example.music.fragments.SongPlayFragment;


public class PortLayoutController extends LayoutController {
    public PortLayoutController(AppCompatActivity activity) {
        super(activity);
    }
    private SongPlayFragment songPlayFragment;
    @Override
    public void onCreate(Bundle savedInstanceState, String currentItemTitle) {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (mActivity.findViewById(R.id.fragment_all_songs) != null) {
            // Create a new Fragment to be placed in the activity layout
            mAllSongsFragment = new AllSongsFragment();
            mAllSongsFragment.setOnSongItemClickListener(this);

            // Add the fragment to the 'fragment_container' FrameLayout
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mAllSongsFragment).commit();
        }
    }

    @Override
    public void onSongItemClick(View v, int pos) {
        Toast.makeText(mActivity, "Play music", Toast.LENGTH_SHORT).show();
        songPlayFragment = new SongPlayFragment();
        songPlayFragment.setOnSongPlayClickListener(this);
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_play, songPlayFragment).commit();
    }

    @Override
    public void onSongPlayClick(View v, int pos) {
//        Toolbar toolbar = mActivity.findViewById(R.id.toolbar);
        MediaPlaybackFragment mediaPlaybackFragment = new MediaPlaybackFragment();
        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.full, mediaPlaybackFragment).addToBackStack(null).commit();
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
