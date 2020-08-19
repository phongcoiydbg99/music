package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.provider.MediaStore;
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
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = AllSongsFragment.class.getSimpleName();
    public static final String SONG_POSSITION = "song_possion";
    private static final String MESSAGE_SONG_PLAY_COMPLETE = "message_song_play_complete";

    // TODO: Rename and change types of parameters
    private Boolean isPortrait;
    private LinkedList<Song> mSongList = new LinkedList<>();
    private RecyclerView mRecyclerView;
    private SongListAdapter mAdapter;
    private SongPlayClickListener songPlayClickListener;
    private SongItemClickListener mSongItemClickListener;

    private SongData mSongData;
    private Song mSong;
    private Fragment fragment;
    private View view;
    private LinearLayout mLinearLayout;
    private ImageView mSongImage;
    private TextView mSongName;
    private TextView mSongArtist;
    private ImageView mSongPlayBtn;
    private boolean songPlay = false;
    private MediaPlaybackService mediaPlaybackService;
    private int visible = View.GONE;
    private int mSongCurrentPosition = -1;
    private boolean onSongPlay = false;
    ServiceConnection serviceConnection;
    Intent playIntent;
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
            if (intent.getAction() == SONG_POSSITION) {
                if (onSongPlay) {
                    Log.d(TAG, "onReceive: on song play " + onSongPlay);
                    mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(SONG_POSSITION));
                    isPlaying = mediaPlaybackService.isPlaying();
                    updateUI();
                }
            }
            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_COMPLETE) {
                mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(MESSAGE_SONG_PLAY_COMPLETE));
                Log.d(TAG, "onReceive: song play complete " + mSongCurrentPosition);
                if (mediaPlaybackService != null){
                    mediaPlaybackService.play(mSongCurrentPosition);
                    if (isPortrait){
                        updateUI();
                    } else updateUILand();

                }
            }
            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_CHANGE) {
                mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE));
                Log.d(TAG, "onReceive: song play change " + mSongCurrentPosition);
                if (!isPortrait){updateUILand();}
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
        if (getActivity() != null){
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mReceiver);
        }
    }

    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
        Log.d(TAG, "setMediaPlaybackService: "+ this.mediaPlaybackService.isPlaying());
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
        mRecyclerView = view.findViewById(R.id.song_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mSongList = mSongData.getSongList();
        if (mSongList.size() > 0) {
            if (mSongCurrentPosition >= 0) mSongData.setCurrentSongPossition(mSongCurrentPosition);
            mAdapter = new SongListAdapter(view.getContext(), mSongData);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
//            mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        }

        mSongPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlaybackService.isPlaying()) {
                    mediaPlaybackService.pause();
                    isPlaying = false;
                    mSongPlayBtn.setImageResource(R.drawable.ic_media_play_light);
                } else {
                    mediaPlaybackService.start();
                    isPlaying = true;

                    mSongPlayBtn.setImageResource(R.drawable.ic_media_pause_light);
                }
                ;
            }
        });
        mAdapter.setOnSongItemClickListener(mSongItemClickListener);
        // chuyen dang media fragment
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songPlayClickListener != null)
                    Log.d(TAG, String.valueOf(mediaPlaybackService == null));
                    isPlaying = mediaPlaybackService.isPlaying();
                    songPlayClickListener.onSongPlayClickListener(v, mSong, mSongCurrentPosition,mediaPlaybackService.getCurrentStreamPosition(), mediaPlaybackService.isPlaying());
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

    public SongData getSongData() {
        return mSongData;
    }

    public Song getSong() {
        return mSongData.getSongAt(mSongCurrentPosition);
    }

    public int getSongCurrentPosition() {
        return mSongCurrentPosition;
    }

    public void setSongCurrentPosition(int mSongCurrentPosition) {
        this.mSongCurrentPosition = mSongCurrentPosition;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void setOnSongPlay(boolean onSongPlay) {
        this.onSongPlay = onSongPlay;
    }

    public void updateUILand() {
        mSongData.setCurrentSongPossition(mSongCurrentPosition);
        mAdapter.setCurrentPos(mSongCurrentPosition);
        mAdapter.notifyDataSetChanged();
    }

    public void updateUI() {
        mSongData.setCurrentSongPossition(mSongCurrentPosition);
        mAdapter.setCurrentPos(mSongCurrentPosition);
        mAdapter.notifyDataSetChanged();
        updatePlaySongLayout(mSongCurrentPosition);
    }

    public void updatePlaySongLayout(int pos) {
        visible = View.VISIBLE;
        mLinearLayout.setVisibility(visible);
        mSong = mSongList.get(pos);
        mSongName.setText(mSong.getTitle());
        mSongArtist.setText(mSong.getArtistName());
        if (isPlaying)
        {
            mSongPlayBtn.setImageResource(R.drawable.ic_media_pause_light);
        } else  mSongPlayBtn.setImageResource(R.drawable.ic_media_play_light);;
        byte[] albumArt = SongData.getAlbumArt(mSong.getData());
        if (albumArt != null) {
            Glide.with(view.getContext()).asBitmap()
                    .load(albumArt)
                    .into(mSongImage);
        } else {
            Glide.with(view.getContext())
                    .load(R.drawable.background_transparent)
                    .into(mSongImage);
        }
    }

    public interface SongPlayClickListener {
        void onSongPlayClickListener(View v, Song song, int pos,long current, boolean isPlaying);
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