package com.example.music.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.music.MusicDB;
import com.example.music.MusicProvider;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.adapters.SongListAdapter;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllSongsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllSongsFragment extends BaseSongsFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final int VERTICAL_ITEM_SPACE = 150;
    private static final String IS_PORTRAIT = "is_portrait";
    private static final String TAG = AllSongsFragment.class.getSimpleName();


    // TODO: Rename and change types of parameters

    public AllSongsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AllSongsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllSongsFragment newInstance(Boolean isPortrait) {
        AllSongsFragment fragment = new AllSongsFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_PORTRAIT, isPortrait);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        mSongData = new SongData(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        if (getArguments() != null) {
            isPortrait = getArguments().getBoolean(IS_PORTRAIT);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void updatePopupMenu(View v, final Song song, final int pos) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        // Inflate the Popup using XML file.
        int id = song.getId();
        final Uri uri = Uri.parse(MusicProvider.CONTENT_URI + "/" + id);
        final Cursor cursor = getContext().getContentResolver().query(uri, null, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            Log.d(TAG, "updatePopupMenu: "+cursor.getInt(cursor.getColumnIndex(MusicDB.IS_FAVORITE)));
            if (cursor.getInt(cursor.getColumnIndex(MusicDB.IS_FAVORITE)) == 2) {
                popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
            } else  popup.getMenuInflater().inflate(R.menu.menu_popup_add, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    ContentValues values = new ContentValues();
                    if (item.getItemId() == R.id.action_add_songs) {
                        values.put(MusicDB.IS_FAVORITE, 2);
                        Toast.makeText(getActivity().getApplicationContext(), "Add Favorite", Toast.LENGTH_SHORT).show();
                    } else if (item.getItemId() == R.id.action_remove_songs) {
                        values.put(MusicDB.IS_FAVORITE, 0);
                        Toast.makeText(getActivity().getApplicationContext(), "Remove Favorite", Toast.LENGTH_SHORT).show();
                    }
                    getContext().getContentResolver().update(uri, values, null, null);
                    return false;
                }
            });
        }
        cursor.close();
        popup.show();

    }

    @Override
    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
        this.isPlaying = this.mediaPlaybackService.isPlaying();
        Log.d(TAG, "setMediaPlaybackService: " + this.mediaPlaybackService.isPlaying());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
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

    @Override
    public void onReceiverSongComplete() {
        if (mediaPlaybackService != null) {
            mSongCurrentIndex = mediaPlaybackService.getCurrentSongIndex();
            mSongCurrentId = mediaPlaybackService.getCurrentSongId();
            updateUI();
        }
    }

    @Override
    public void onReceiverSongChange() {
        updateUI();
    }

    @Override
    public void updateAdapter() {
        mSongData.setSongCurrentId(mSongCurrentId);
        mSongData.setPlaying(isPlaying);
        mAdapter = new SongListAdapter(view.getContext(), mSongData);
        mSongList = mAdapter.getSongList();
        if (mSongList.size() <= 0) {
            mTextView.setText(R.string.text_list_song);
            mTextView.setVisibility(View.VISIBLE);
        } else mTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void refresh() {

    }

    public interface SongPlayClickListener {
        void onSongPlayClickListener(View v, Song song, long current, boolean isPlaying);
    }

}