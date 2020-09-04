package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.adapters.SongListAdapter;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;

import java.util.LinkedList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoriteSongsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteSongsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = AllSongsFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String SONG_POSSITION = "song_possion";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public LinkedList<Song> mSongList = new LinkedList<>();
    public SongListAdapter mAdapter;
    private boolean isPlaying = true;

    public SongData mSongData;
    public RecyclerView mRecyclerView;
    private MediaPlaybackService mediaPlaybackService;
    private SongItemClickListener mSongItemClickListener;
    private SongListAdapter.SongItemClickIdListener mSongItemClickIdListener;

    public FavoriteSongsFragment() {
        // Required empty public constructor
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getStringExtra(SONG_POSSITION));
//            if (intent.getAction() == SONG_POSSITION) {
//
//                    Log.d(TAG, "onReceive: on song play " + mediaPlaybackService.isPlaying());
//                    mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(SONG_POSSITION));
//                    isPlaying = mediaPlaybackService.isPlaying();
//                    updateUI();
//            }
            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_COMPLETE) {
                String state = intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_COMPLETE);
                if (state == "play_normal") {
                    isPlaying = false;
                } else isPlaying = true;
                if (mediaPlaybackService != null) {
                }
            }
            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_CHANGE) {
                String state = intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE);
                if (state == "song_state_play") {
                    isPlaying = true;
                } else if (state == "song_state_pause") {
                    isPlaying = false;
                } else {
                    isPlaying = true;
                }
//                if ()
//                updateUi();
            }
        }
    };
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoriteSongsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteSongsFragment newInstance(String param1, String param2) {
        FavoriteSongsFragment fragment = new FavoriteSongsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mSongData = new SongData(getActivity().getApplicationContext());
        mSongList = mSongData.getSongListFavor();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SONG_POSSITION);
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_COMPLETE);
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_CHANGE);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite_songs, container, false);
        mRecyclerView = view.findViewById(R.id.song_recyclerview);
        Song song = mSongData.getSongFavorId(mediaPlaybackService.getCurrentSongId());
        mSongData.setPlaying(mediaPlaybackService.isPlaying());
        mAdapter = new SongListAdapter(view.getContext(), mSongData);
        mAdapter.setSongList(mSongData.getSongListFavor());
        mAdapter.setOnSongItemClickIdListener(mSongItemClickIdListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        return view;
    }

    public void updateUi(int mSongCurrentPosition, boolean isPlaying ){
        mSongData.setCurrentSongPossition(mSongCurrentPosition);
        mSongData.setPlaying(isPlaying);
        mAdapter.setCurrentPos(mSongCurrentPosition);
        mRecyclerView.scrollToPosition(mSongCurrentPosition);
        mAdapter.notifyDataSetChanged();
    }

    public void setOnSongItemClickIdListener(SongListAdapter.SongItemClickIdListener songItemClickListener) {
        mSongItemClickIdListener = songItemClickListener;
        if (mAdapter != null) {
            mAdapter.setOnSongItemClickIdListener(songItemClickListener);
        }
    }

    public MediaPlaybackService getMediaPlaybackService() {
        return mediaPlaybackService;
    }

    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
    }
}