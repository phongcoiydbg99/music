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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.music.MusicHelper;
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
 * Use the {@link AllSongsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllSongsFragment extends BaseSongsFragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final int VERTICAL_ITEM_SPACE = 150;
    private static final String IS_PORTRAIT = "is_portrait";
    private static final String TAG = AllSongsFragment.class.getSimpleName();


    // TODO: Rename and change types of parameters
    protected Boolean isPortrait;
    private SongPlayClickListener songPlayClickListener;
    private Song mSong;
    private View view;
    private LinearLayout mLinearLayout;
    private ImageView mSongImage;
    private TextView mSongName;
    private TextView mSongArtist;
    private ImageView mSongPlayBtn;

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
    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
        this.isPlaying = this.mediaPlaybackService.isPlaying();
        Log.d(TAG, "setMediaPlaybackService: " + this.mediaPlaybackService.isPlaying());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: " + isPlaying);
        view = inflater.inflate(R.layout.fragment_all_songs, container, false);
        mLinearLayout = view.findViewById(R.id.play_song_layout);
        mSongImage = view.findViewById(R.id.song_image);
        mSongName = view.findViewById(R.id.song_name_play);
        mSongArtist = view.findViewById(R.id.song_artist_name);
        mSongPlayBtn = view.findViewById(R.id.song_play_button);
        mRecyclerView = view.findViewById(R.id.song_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mSongData.setCurrentSongPossition(mSongCurrentPosition);
        mSongData.setSongCurrentId(mSongCurrentId);
        mSongData.setPlaying(isPlaying);

        mAdapter = new SongListAdapter(view.getContext(), mSongData);
        mSongList = mAdapter.getSongList();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
//            mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

        if (mAdapter != null) {
            mAdapter.setOnSongItemClickListener(mSongItemClickListener);
        }

        Log.d(TAG, String.valueOf(mediaPlaybackService != null));
        if (mediaPlaybackService != null && mSongCurrentPosition >= 0) {
            mSongCurrentPosition = mediaPlaybackService.getCurrentSongPosition();
            isPlaying = mediaPlaybackService.isPlaying();
            updateUI();

        }
        if (mSongCurrentPosition >= 0) {
            updateUI();
        }
        mSongPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: start" + mediaPlaybackService.isPlaying());
                if (mediaPlaybackService.isPlaying()) {
                    mediaPlaybackService.pause();
                    mSongPlayBtn.setImageResource(R.drawable.ic_media_play_light);
                    mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongPosition(), false);
                    isPlaying = false;
                    mSongData.setPlaying(isPlaying);
                    mAdapter.notifyDataSetChanged();
                } else {
                    if (mediaPlaybackService.isFirst()) {
                        mediaPlaybackService.play(mSongCurrentPosition);
                        mediaPlaybackService.setFirst(false);
                    } else
                        mediaPlaybackService.start();
                    mSongPlayBtn.setImageResource(R.drawable.ic_media_pause_light);
                    mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongPosition(), true);
                    isPlaying = true;
                    mSongData.setPlaying(isPlaying);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        // chuyen dang media fragment
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songPlayClickListener != null)
                    Log.d(TAG, String.valueOf(mediaPlaybackService == null));
                isPlaying = mediaPlaybackService.isPlaying();
                int mCurrentStreamPosition = mediaPlaybackService.getCurrentStreamPosition() > mSong.getDuration() ? 0 : mediaPlaybackService.getCurrentStreamPosition();
                Log.d(TAG, "onClick: " + String.valueOf(mediaPlaybackService.getDuration()) + " " + mSong.getDuration());
                songPlayClickListener.onSongPlayClickListener(v, mSong, mSongCurrentPosition, mCurrentStreamPosition, mediaPlaybackService.isPlaying());
            }
        });

        // song menu click listener
        mAdapter.setOnSongBtnClickListener(new SongListAdapter.SongBtnClickListener() {
            @Override
            public void onSongBtnClickListener(ImageButton btn, View v, final Song song, final int pos) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                // Inflate the Popup using XML file.
                popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_add_songs) {
                            int id = mSongData.getSongAt(pos).getId();
                            Uri uri = Uri.parse(MusicProvider.CONTENT_URI + "/" + id);
                            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null,
                                    null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                ContentValues values = new ContentValues();
                                values.put(MusicDB.IS_FAVORITE, 2);
                                getContext().getContentResolver().update(uri, values, null, null);
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
    public void setSongCurrentPosition(int position) {
        this.mSongCurrentPosition = position;
    }

    @Override
    public void setSongCurrentId(int id) {
        mSongCurrentId = id;
    }

    @Override
    public void onReceiverSongComplete() {
        if (mediaPlaybackService != null) {
            mSongCurrentPosition = mediaPlaybackService.getCurrentSongPosition();
            mSongCurrentId = mediaPlaybackService.getCurrentSongId();
            updateUI();
        }
    }

    @Override
    public void onReceiverSongChange() {
        updateUI();
    }

    @Override
    public void setOnSongPlayClickListener(SongPlayClickListener songPlayClickListener) {
        this.songPlayClickListener = songPlayClickListener;
    }

    @Override
    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public void updateUI() {
        mSongData.setCurrentSongPossition(mSongCurrentPosition);
        mSongData.setPlaying(isPlaying);
        if (mediaPlaybackService != null) mSongData.setSongCurrentId(mediaPlaybackService.getCurrentSongId());
        mAdapter.setCurrentPos(mSongCurrentPosition);
        mRecyclerView.scrollToPosition(mSongCurrentPosition);
        mAdapter.notifyDataSetChanged();
        if (isPortrait) updatePlaySongLayout(mSongCurrentPosition);
        Log.d(TAG, "updateUI: " + isPlaying);
    }

    public void updatePlaySongLayout(int pos) {
        int visible = View.VISIBLE;
        mLinearLayout.setVisibility(visible);
        mSong = mSongList.get(pos);

        mSongName.setText(mSong.getTitle());
        mSongArtist.setText(mSong.getArtistName());
        if (isPlaying) {
            mSongPlayBtn.setImageResource(R.drawable.ic_media_pause_light);
        } else mSongPlayBtn.setImageResource(R.drawable.ic_media_play_light);
        byte[] albumArt = SongData.getAlbumArt(mSong.getData());
        if (albumArt != null) {
            Glide.with(view.getContext()).asBitmap()
                    .load(albumArt)
                    .into(mSongImage);
        } else {
            Glide.with(view.getContext())
                    .load(R.drawable.art_song_default)
                    .into(mSongImage);
        }
    }

    public interface SongPlayClickListener {
        void onSongPlayClickListener(View v, Song song, int pos, long current, boolean isPlaying);
    }


    @Override
    public void setOnSongItemClickListener(SongItemClickListener songItemClickListener) {
        mSongItemClickListener = songItemClickListener;
        if (mAdapter != null) {
            mAdapter.setOnSongItemClickListener(songItemClickListener);
        }
    }
}