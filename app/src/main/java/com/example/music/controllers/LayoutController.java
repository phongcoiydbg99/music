package com.example.music.controllers;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music.fragments.AllSongsFragment;
import com.example.music.interfaces.SongItemClickListener;


public abstract class LayoutController implements SongItemClickListener {
    public static final String LAST_ITEM_TITLE_EXTRA = "last_item_title";
    
    protected AppCompatActivity mActivity;
    protected AllSongsFragment mAllSongsFragment;
    
    public LayoutController(AppCompatActivity activity) {
        mActivity = activity;
    }
    
//    protected Bundle newBundleFromNewItem(NewItem item) {
//        Bundle args = new Bundle();
//        args.putString(NewContentFragment.TITLE_EXTRA, item.title);
//        args.putString(NewContentFragment.DESCRIPTION_EXTRA, item.description);
//        args.putString(NewContentFragment.DATE_EXTRA, item.date);
//        args.putString(NewContentFragment.CONTENT_ENCODED_EXTRA, item.contentEncoded);
//        args.putString(NewContentFragment.IMAGE_URL_EXTRA, item.imageUrl);
//        return args;
//    }
    
    public void onSaveInstanceState(Bundle outState) {
//        outState.putString(LAST_ITEM_TITLE_EXTRA, mNewsListFragment.getCurrentItemTitle());
        outState.putString(LAST_ITEM_TITLE_EXTRA, "mNewsListFragment.getCurrentItemTitle()");
    }
    
    public abstract void onCreate(Bundle savedInstanceState, String currentItemTitle);
}
