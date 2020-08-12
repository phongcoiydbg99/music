
package com.example.music.controllers;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.music.R;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;
import com.example.music.fragments.SongPlayFragment;

public class LandLayoutController extends LayoutController {
    private MediaPlaybackFragment mMediaPlaybackFragment;

    public LandLayoutController(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, String currentItemTitle) {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (mActivity.findViewById(R.id.contentAllSongs_land) != null) {
            mMediaPlaybackFragment = new MediaPlaybackFragment();
            Bundle args = new Bundle();
            args.putString(LAST_ITEM_TITLE_EXTRA, currentItemTitle);
            mMediaPlaybackFragment.setArguments(args);
            
            // Create a new Fragment to be placed in the activity layout
            mAllSongsFragment = new AllSongsFragment();
            mAllSongsFragment.setOnSongItemClickListener(this);
//            mNewsListFragment.setLoadCallback(mNewContentFragment);

            // Add the fragment to the 'fragment_container' FrameLayout
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_all_songs, mAllSongsFragment)
                    .replace(R.id.fragment_media, mMediaPlaybackFragment)
                    .commit();
        }
    }

    @Override
    public void onSongItemClick(View v, int pos) {
        Toast.makeText(mActivity, "Play music", Toast.LENGTH_SHORT).show();
//        SongPlayFragment songPlayFragment = new SongPlayFragment();
//        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.fragment_play, songPlayFragment).commit();
    }


//    @Override
//    public void onNewClick(NewItem item) {
//        Bundle args = newBundleFromNewItem(item);
//        mNewContentFragment.update(args);
//    }
}
