package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.adapters.SongListAdapter;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;

import java.util.LinkedList;

public abstract class BaseSongsFragment extends Fragment {
    private static final String TAG = AllSongsFragment.class.getSimpleName();

    protected boolean isPlaying = true;
    protected MediaPlaybackService mediaPlaybackService;
    protected int mSongCurrentPosition = -1;
    protected int mSongCurrentId = -1;
    protected LinkedList<Song> mSongList = new LinkedList<>();
    protected SongListAdapter mAdapter;
    protected SongItemClickListener mSongItemClickListener;
    protected SongData mSongData;
    protected RecyclerView mRecyclerView;

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_COMPLETE) {
                if (intent.getAction() == MediaPlaybackService.SONG_PLAY_COMPLETE) {
                    String state = intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_COMPLETE);
                    if (state == "play_normal") {
                        isPlaying = false;
                    } else isPlaying = true;
                    onReceiverSongComplete();
                }
            }
            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_CHANGE) {
                String state = intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE);
                if (state == "song_state_play") {
                    isPlaying = true;
                } else if (state == "song_state_pause") {
                    isPlaying = false;
                } else {
                    mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE));
                    isPlaying = true;
                }
                onReceiverSongChange();
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSongData = new SongData(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_COMPLETE);
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_CHANGE);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mReceiver);
        }
    }

    public abstract void setSongCurrentPosition(int position);
    public abstract void setSongCurrentId(int id);
    public abstract void onReceiverSongComplete();
    public abstract void onReceiverSongChange();
    public abstract void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService);
    public abstract void setOnSongPlayClickListener(AllSongsFragment.SongPlayClickListener songplayclicklistener);
    public abstract void setOnSongItemClickListener(SongItemClickListener songItemClickListener);
    public abstract void setPlaying(boolean playing);
    public abstract void updateUI();
    public void updateUILand(){
    }
}
