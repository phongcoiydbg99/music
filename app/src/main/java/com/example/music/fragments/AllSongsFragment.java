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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = AllSongsFragment.class.getSimpleName();
    public static final String SONG_POSSITION = "song_possion";
    private static final String MESSAGE_SONG_PLAY_COMPLETE = "message_song_play_complete";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
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
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllSongsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllSongsFragment newInstance(String param1, String param2) {
        AllSongsFragment fragment = new AllSongsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
                Log.d(TAG, "onReceive: song play complete");
                mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(MESSAGE_SONG_PLAY_COMPLETE));
                mediaPlaybackService.play(mSongCurrentPosition);
                updateUI();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlaybackService.MediaPlaybackBinder binder = (MediaPlaybackService.MediaPlaybackBinder) service;
                mediaPlaybackService = binder.getMediaPlaybackService();
                Log.d(TAG, "onServiceConnected()");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        playIntent = new Intent(getActivity(), MediaPlaybackService.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SONG_POSSITION);
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_COMPLETE);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mReceiver, intentFilter);
        getActivity().bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//        getActivity().startService(playIntent);
        ContextCompat.startForegroundService(getContext(),playIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onStop()");
        if (getActivity() != null){
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mReceiver);
            getActivity().stopService(playIntent);
            getActivity().unbindService(serviceConnection);
        }
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
        // hien thanh nghe nhac
        mAdapter.setOnSongItemClickListener(new SongItemClickListener() {
            @Override
            public void onSongItemClick(SongListAdapter.SongViewHolder holder, final int pos) {
                onSongPlay = true;
                mSongCurrentPosition = pos;
                mediaPlaybackService.play(mSongCurrentPosition);
                mediaPlaybackService.setCurrentSongPosition(mSongCurrentPosition);
                isPlaying = true;
                Log.d(TAG, "onSongItemClick: " + isPlaying);
                Toast.makeText(getActivity(), "Play music", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
        // chuyen dang media fragment
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songPlayClickListener != null)
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

    public boolean isPlaying() {
        return isPlaying;
    }

    public void updateUI() {
        mSongData.setCurrentSongId(mSongCurrentPosition);
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