package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
import android.widget.ImageButton;
import android.widget.TextView;
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
public class FavoriteSongsFragment extends BaseSongsFragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = FavoriteSongsFragment.class.getSimpleName();
    private static final String IS_PORTRAIT = "is_portrait";
    // TODO: Rename and change types of parameters

    public FavoriteSongsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FavoriteSongsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteSongsFragment newInstance(Boolean isPortrait) {
        FavoriteSongsFragment fragment = new FavoriteSongsFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_PORTRAIT, isPortrait);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) isPortrait = true;
        else isPortrait = false;
        Log.d(TAG, "onConfigurationChanged: ");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isPortrait = getArguments().getBoolean(IS_PORTRAIT);
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

        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void updatePopupMenu(View v, final Song song, final int pos) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        // Inflate the Popup using XML file.
        popup.getMenuInflater().inflate(R.menu.menu_popup_favorite, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_remove_songs) {
                    if (songRemoveFavoriteListener != null){
                        Uri uri = Uri.parse(MusicProvider.CONTENT_URI + "/" + song.getId());
                        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null,
                                null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            ContentValues values = new ContentValues();
                            values.put(MusicDB.IS_FAVORITE, 1);
                            getContext().getContentResolver().update(uri, values, null, null);
                            refresh();
                            if (song.getId() == mediaPlaybackService.getCurrentSongId()) {
                                mSongData.setSongList(SongData.getAllSongs(getActivity().getApplicationContext()));
                                mediaPlaybackService.setCurrentSongIndex(mediaPlaybackService.getCurrentSongPosition());
                                mediaPlaybackService.setSongList(mSongData.getSongList());
                            } else {
                                if (isFavorite == true) {
                                    if (pos < mediaPlaybackService.getCurrentSongIndex()){
                                        int index = mediaPlaybackService.getCurrentSongIndex()-1;
                                        if (index < 0) index = 0;
                                        if (index >= mSongData.getSongListFavor().size()) index =  mSongData.getSongListFavor().size() - 1;
                                        mediaPlaybackService.setCurrentSongIndex(index);
                                    }
                                    mediaPlaybackService.setSongList(mSongData.getSongListFavor());
                                    Log.d(TAG, "onMenuItemClick: "+mSongData.getSongListFavor().size());
                                }
                            }
                            Toast.makeText(getActivity().getApplicationContext(), "Remove Favorite", Toast.LENGTH_SHORT).show();
                            songRemoveFavoriteListener.onSongRemoveFavoriteListener();
                        }
                        cursor.close();
                    }

                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public void onReceiverSongComplete() {
        if (mediaPlaybackService != null) {
            Song song = mSongData.getSongFavorId(mediaPlaybackService.getCurrentSongId());
            mSongCurrentId = mediaPlaybackService.getCurrentSongId();
//            if (song != null) {
//                mSongCurrentPosition = mSongData.getCurrentSongPossition();
//            } else mSongCurrentPosition = -1;
            updateUI();
        }
    }

    @Override
    public void onReceiverSongChange() {
        if (mediaPlaybackService != null){
            Song song = mSongData.getSongFavorId(mediaPlaybackService.getCurrentSongId());
            mSongCurrentId = mediaPlaybackService.getCurrentSongId();
            Log.d(TAG, String.valueOf("onReceive: " + song == null));
//            if (song != null) {
//                mSongCurrentPosition = mSongData.getCurrentSongPossition();
//            } else mSongCurrentPosition = -1;
            updateUI();
        }
    }

    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
    }


    @Override
    public void updateAdapter() {
        if (mediaPlaybackService != null) {
            mSongCurrentId = mediaPlaybackService.getCurrentSongId();
            Song song = mSongData.getSongId(mediaPlaybackService.getCurrentSongId());
//            if (song != null) {
//                mSongCurrentPosition = mSongData.getCurrentSongPossition();
//            } else mSongCurrentPosition = -1;
            mSongData.setSongCurrentId(mediaPlaybackService.getCurrentSongId());
            mSongData.setPlaying(mediaPlaybackService.isPlaying());
        }
        mAdapter = new SongListAdapter(view.getContext(), mSongData);
        mSongList = SongData.getFavorAllSongs(getContext());
        mAdapter.setSongList(mSongList);
        if (mSongList.size() <= 0)
            mTextView.setVisibility(View.VISIBLE);
        else mTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void refresh() {
        mSongData.setSongListFavor(SongData.getFavorAllSongs(getActivity().getApplicationContext()));
        mAdapter.setSongList(mSongData.getSongListFavor());
        if (mSongData.getSongListFavor().size() <= 0)
            mTextView.setVisibility(View.VISIBLE);
        else mTextView.setVisibility(View.INVISIBLE);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.getFilter().filter(newText);
        return false;
    }
}