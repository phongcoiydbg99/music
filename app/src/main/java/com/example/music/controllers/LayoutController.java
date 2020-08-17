package com.example.music.controllers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music.fragments.AllSongsFragment;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;


public abstract class LayoutController implements AllSongsFragment.SongPlayClickListener {
    public static final String LAST_ITEM_TITLE_EXTRA = "last_item_title";
    private static final String TAG = LayoutController.class.getSimpleName();

    protected AppCompatActivity mActivity;
    protected AllSongsFragment mAllSongsFragment;
    
    public LayoutController(AppCompatActivity activity) {
        mActivity = activity;
    }
    public MediaPlaybackService mediaPlaybackService;
    public Intent playIntent;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlaybackService.MediaPlaybackBinder binder = (MediaPlaybackService.MediaPlaybackBinder) service;
            mediaPlaybackService = binder.getMediaPlaybackService();
            Log.d(TAG,"onServiceConnected()");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    public ServiceConnection getServiceConnection()
    {
        return serviceConnection;
    }
    public void onStart()
    {
        playIntent = new Intent(mActivity, MediaPlaybackService.class);
        Log.d(TAG,"onStart()");
        playIntent.setAction("");
        mActivity.bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        mActivity.startService(playIntent);
    }

    public void onDestroy()
    {
        Log.d(TAG,"onStop()");
        mActivity.stopService(playIntent);
        mActivity.unbindService(serviceConnection);
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
