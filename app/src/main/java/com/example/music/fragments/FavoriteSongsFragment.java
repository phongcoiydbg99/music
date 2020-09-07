package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.music.MusicDB;
import com.example.music.MusicProvider;
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
public class FavoriteSongsFragment extends BaseSongsFragment implements MenuItem.OnActionExpandListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = AllSongsFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String SONG_POSSITION = "song_possion";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
        mSongList = mSongData.getSongListFavor();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite_songs, container, false);
        mRecyclerView = view.findViewById(R.id.song_recyclerview);
        if (mediaPlaybackService != null){
            mSongCurrentId = mediaPlaybackService.getCurrentSongId();
            Song song = mSongData.getSongFavorId(mediaPlaybackService.getCurrentSongId());
            if (song != null) {
                mSongCurrentPosition = mSongData.getCurrentSongPossition();
            } else mSongCurrentPosition = -1;
            mSongData.setSongCurrentId(mediaPlaybackService.getCurrentSongId());
            mSongData.setPlaying(mediaPlaybackService.isPlaying());
        }
        mAdapter = new SongListAdapter(view.getContext(), mSongData);
        mAdapter.setSongList(mSongData.getSongListFavor());
        mAdapter.setOnSongItemClickIdListener(mSongItemClickIdListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mAdapter.setOnSongBtnClickListener(new SongListAdapter.SongBtnClickListener() {
            @Override
            public void onSongBtnClickListener(ImageButton btn, View v, final Song song, final int pos) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                // Inflate the Popup using XML file.
                popup.getMenuInflater().inflate(R.menu.menu_popup_favorite, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_remove_songs) {
                            int id = mediaPlaybackService.getCurrentSongId();
                            Uri uri = Uri.parse(MusicProvider.CONTENT_URI + "/" + id);
                            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null,
                                    null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                ContentValues values = new ContentValues();
                                values.put(MusicDB.IS_FAVORITE, 0);
                                getContext().getContentResolver().update(uri, values, null, null);
                                mSongData.setSongListFavor(SongData.getFavorAllSongs(getActivity().getApplicationContext()));
                                mAdapter.setSongList(mSongData.getSongListFavor());
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity().getApplicationContext(), cursor.getString(cursor.getColumnIndex(MusicDB.TITLE)), Toast.LENGTH_SHORT).show();
                            }
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setOnSongItemClickIdListener(SongListAdapter.SongItemClickIdListener songItemClickListener) {
        mSongItemClickIdListener = songItemClickListener;
        if (mAdapter != null) {
            mAdapter.setOnSongItemClickIdListener(songItemClickListener);
        }
    }

    @Override
    public void updateUI() {
        mSongData.setSongCurrentId(mSongCurrentId);
        mSongData.setCurrentSongPossition(mSongCurrentPosition);
        mSongData.setPlaying(isPlaying);
        mAdapter.setCurrentPos(mSongCurrentPosition);
        mRecyclerView.scrollToPosition(mSongCurrentPosition);
        mAdapter.notifyDataSetChanged();
    }

    public MediaPlaybackService getMediaPlaybackService() {
        return mediaPlaybackService;
    }

    @Override
    public void setSongCurrentPosition(int position) {
        mSongCurrentPosition = position;
    }

    @Override
    public void setSongCurrentId(int id) {
        mSongCurrentId = id;
    }

    @Override
    public void onReceiverSongComplete() {
        if (mediaPlaybackService != null) {
            Song song = mSongData.getSongFavorId(mediaPlaybackService.getCurrentSongId());
            mSongCurrentId = mediaPlaybackService.getCurrentSongId();
            if (song != null) {
                mSongCurrentPosition = mSongData.getCurrentSongPossition();
            } else  mSongCurrentPosition = -1;
            updateUI();
        }
    }

    @Override
    public void onReceiverSongChange() {
        Song song = mSongData.getSongFavorId(mediaPlaybackService.getCurrentSongId());
        mSongCurrentId = mediaPlaybackService.getCurrentSongId();
        Log.d(TAG, String.valueOf("onReceive: "+song == null));
        if (song != null) {
            mSongCurrentPosition = mSongData.getCurrentSongPossition();
        } else  mSongCurrentPosition = -1;
        updateUI();
    }

    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
    }

    @Override
    public void setOnSongPlayClickListener(AllSongsFragment.SongPlayClickListener songplayclicklistener) {

    }

    @Override
    public void setOnSongItemClickListener(SongItemClickListener songItemClickListener) {

    }

    @Override
    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
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