package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.VerticalSpaceItemDecoration;
import com.example.music.adapters.SongListAdapter;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;

import java.util.LinkedList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllSongsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllSongsFragment extends Fragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final int VERTICAL_ITEM_SPACE = 150;
    private static final String IS_PORTRAIT = "is_portrait";
    private static final String TAG = AllSongsFragment.class.getSimpleName();
    public static final String SONG_POSSITION = "song_possion";

    // TODO: Rename and change types of parameters
    private Boolean isPortrait;
    private LinkedList<Song> mSongList = new LinkedList<>();
    private SongListAdapter mAdapter;
    private SongPlayClickListener songPlayClickListener;
    private SongItemClickListener mSongItemClickListener;

    private SongData mSongData;
    private Song mSong;
    private View view;
    private LinearLayout mLinearLayout;
    private ImageView mSongImage;
    private TextView mSongName;
    private TextView mSongArtist;
    private ImageView mSongPlayBtn;
    private MediaPlaybackService mediaPlaybackService;
    private int mSongCurrentPosition = -1;
    private int mSongCurrentId = -1;
    private boolean isPlaying = true;

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
                Log.d(TAG, "onReceive: song play complete " + mSongCurrentPosition);
                if (mediaPlaybackService != null) {
                    mSongCurrentPosition = mediaPlaybackService.getCurrentSongPosition();
                    if (isPortrait) {
                        updateUI();
                    } else updateUILand();

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
                if (!isPortrait) {
                    Log.d(TAG, "onReceive: song play change " + mSongCurrentPosition);
                    updateUILand();
                } else {
                    Log.d(TAG, "onReceive: song play change " + mSongCurrentPosition);
                    updateUI();
                }
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSongData = new SongData(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            isPortrait = getArguments().getBoolean(IS_PORTRAIT);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SONG_POSSITION);
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_COMPLETE);
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_CHANGE);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onStop()");
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mReceiver);
        }
    }

    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
        Log.d(TAG, "setMediaPlaybackService: " + this.mediaPlaybackService.isPlaying());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: " + mSongCurrentPosition);
        view = inflater.inflate(R.layout.fragment_all_songs, container, false);
        mLinearLayout = view.findViewById(R.id.play_song_layout);
        mSongImage = view.findViewById(R.id.song_image);
        mSongName = view.findViewById(R.id.song_name_play);
        mSongArtist = view.findViewById(R.id.song_artist_name);
        mSongPlayBtn = view.findViewById(R.id.song_play_button);
        RecyclerView mRecyclerView = view.findViewById(R.id.song_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        if (mSongCurrentPosition >= 0) {
            mSongData.setCurrentSongPossition(mSongCurrentPosition);
        }
        mSongData.setPlaying(isPlaying);
        mAdapter = new SongListAdapter(view.getContext(), mSongData);
        mSongList = mAdapter.getSongList();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
//            mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

        if (mAdapter != null) {
            mAdapter.setOnSongItemClickListener(mSongItemClickListener);
        }

        if (mSongCurrentPosition >= 0) {
            if (isPortrait) {
                updateUI();
            } else {
                updateUILand();
            }
        }
        Log.d(TAG, String.valueOf(mediaPlaybackService != null));
        if (mediaPlaybackService != null) {
            mSongCurrentPosition = mediaPlaybackService.getCurrentSongPosition();
            isPlaying = mediaPlaybackService.isPlaying();
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
                songPlayClickListener.onSongPlayClickListener(v, mSong, mSongCurrentPosition, mediaPlaybackService.getCurrentStreamPosition(), mediaPlaybackService.isPlaying());
            }
        });

        // song menu click listener
        mAdapter.setOnSongBtnClickListener(new SongListAdapter.SongBtnClickListener() {
            @Override
            public void onSongBtnClickListener(ImageButton btn, View v, final Song song, final int pos) {
                Toast.makeText(v.getContext(), song.getAlbumName(), Toast.LENGTH_SHORT).show();
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                // Inflate the Popup using XML file.
                popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
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

    public void setSongCurrentPosition(int position) {
        this.mSongCurrentPosition = position;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void updateUILand() {
        mSongData.setCurrentSongPossition(mSongCurrentPosition);
        mSongData.setPlaying(isPlaying);
        mAdapter.setCurrentPos(mSongCurrentPosition);
        mAdapter.notifyDataSetChanged();
        Log.d(TAG, "updateUILand: "+isPlaying);
    }

    public void updateUI() {
        mSongData.setCurrentSongPossition(mSongCurrentPosition);
        mSongData.setPlaying(isPlaying);
        mAdapter.setCurrentPos(mSongCurrentPosition);
        mAdapter.notifyDataSetChanged();
        updatePlaySongLayout(mSongCurrentPosition);
        Log.d(TAG, "updateUI: "+isPlaying);
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

    public void setOnSongPlayClickListener(SongPlayClickListener songPlayClickListener) {
        this.songPlayClickListener = songPlayClickListener;
    }

    public void setOnSongItemClickListener(SongItemClickListener songItemClickListener) {
        mSongItemClickListener = songItemClickListener;
        if (mAdapter != null) {
            mAdapter.setOnSongItemClickListener(songItemClickListener);
        }
    }
}