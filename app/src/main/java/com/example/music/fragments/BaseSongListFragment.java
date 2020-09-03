package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.adapters.SongListAdapter;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;

import java.util.LinkedList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BaseSongListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BaseSongListFragment extends Fragment implements MenuItem.OnActionExpandListener{

    // TODO: Rename and change types of parameters
    public static final String SONG_POSSITION = "song_possion";
    public LinkedList<Song> mSongList = new LinkedList<>();
    public SongListAdapter mAdapter;

    public SongData mSongData;
    public RecyclerView mRecyclerView;
    private BroadcastReceiver mReceiver;

    public BaseSongListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BaseSongListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BaseSongListFragment newInstance(String param1, String param2) {
        BaseSongListFragment fragment = new BaseSongListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSongData = new SongData(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d("TAG", "onStart: ");
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return false;
    }

}