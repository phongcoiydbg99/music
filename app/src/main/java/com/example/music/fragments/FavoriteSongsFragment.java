package com.example.music.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public LinkedList<Song> mSongList = new LinkedList<>();
    public SongListAdapter mAdapter;

    public SongData mSongData;
    public RecyclerView mRecyclerView;
    private MediaPlaybackService mediaPlaybackService;
    private SongItemClickListener mSongItemClickListener;
    private SongListAdapter.SongItemClickIdListener mSongItemClickIdListener;

    public FavoriteSongsFragment() {
        // Required empty public constructor
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite_songs, container, false);
        mRecyclerView = view.findViewById(R.id.song_recyclerview);
        Song song = mSongData.getSongFavorId(mediaPlaybackService.getCurrentSongId());
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