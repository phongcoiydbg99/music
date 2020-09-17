package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.adapters.SongListAdapter;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;

import java.util.LinkedList;

public abstract class BaseSongsFragment extends Fragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private static final String TAG = BaseSongsFragment.class.getSimpleName();

    protected boolean isPlaying = true;
    protected MediaPlaybackService mediaPlaybackService;
    protected int mSongCurrentIndex = -1;
    protected int mSongCurrentPosition = -1;
    protected int mSongCurrentId = -1;
    protected Boolean isPortrait;
    protected Boolean isFavorite = false;
    protected TextView mTextView;
    protected LinkedList<Song> mSongList = new LinkedList<>();
    protected SongListAdapter mAdapter;
    protected SongItemClickListener mSongItemClickListener;
    protected SongData mSongData;
    protected RecyclerView mRecyclerView;
    protected AllSongsFragment.SongPlayClickListener songPlayClickListener;
    protected SongRemoveFavoriteListener songRemoveFavoriteListener;
    protected View view;
    protected RelativeLayout mRelativeLayout;
    protected ImageView mSongImage;
    protected TextView mSongName;
    protected TextView mSongArtist;
    protected ImageView mSongPlayBtn;
    protected Song mSong;


    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_COMPLETE) {
                if (intent.getAction() == MediaPlaybackService.SONG_PLAY_COMPLETE) {
                    String state = intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_COMPLETE);
                    isPlaying = !state.equals("play_done");
                    onReceiverSongComplete();
                }
            }
            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_CHANGE) {
                String state = intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE);
                if (state == "chance_data"){
                    refresh();
                } else {
                    if (state == "song_state_play") {
                        isPlaying = true;
                    } else if (state == "song_state_pause") {
                        isPlaying = false;
                    } else {
                        mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE));
                        mSongCurrentIndex = mediaPlaybackService.getCurrentSongIndex();
                        isPlaying = true;
                    }
                    onReceiverSongChange();
                }
            }

        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
        mSongData = new SongData(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_COMPLETE);
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_CHANGE);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_all_songs, container, false);
        mRelativeLayout = view.findViewById(R.id.play_song_layout);
        mSongImage = view.findViewById(R.id.song_image);
        mSongName = view.findViewById(R.id.song_name_play);
        mSongArtist = view.findViewById(R.id.song_artist_name);
        mSongPlayBtn = view.findViewById(R.id.song_play_button);
        mTextView = view.findViewById(R.id.text_favorite_song);
        mRecyclerView = view.findViewById(R.id.song_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        updateAdapter();

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
//            mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

        if (mAdapter != null) {
            mAdapter.setOnSongItemClickListener(mSongItemClickListener);
        }

        if (mediaPlaybackService != null && mSongCurrentId >= 0) {
            mSongCurrentIndex = mediaPlaybackService.getCurrentSongIndex();
            isPlaying = mediaPlaybackService.isPlaying();
            updateUI();
        }
        if (mSongCurrentId >= 0) {
            updateUI();
        }
        mSongPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlaybackService.isPlaying()) {
                    mediaPlaybackService.pause();
                    mSongPlayBtn.setImageResource(R.drawable.ic_media_play_light);
                    mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongIndex(), false);
                    isPlaying = false;
                    mSongData.setPlaying(isPlaying);
                    mAdapter.notifyDataSetChanged();
                } else {
                    if (mediaPlaybackService.isFirst()) {
                        mediaPlaybackService.play(mediaPlaybackService.getCurrentSongIndex());
                        mediaPlaybackService.setFirst(false);
                    } else
                        mediaPlaybackService.start();
                    mSongPlayBtn.setImageResource(R.drawable.ic_media_pause_light);
                    mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongIndex(), true);
                    isPlaying = true;
                    mSongData.setPlaying(isPlaying);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        // chuyen dang media fragment
        mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songPlayClickListener != null)
                isPlaying = mediaPlaybackService.isPlaying();
                int mCurrentStreamPosition = mediaPlaybackService.getCurrentStreamPosition() > mSong.getDuration() ? 0 : mediaPlaybackService.getCurrentStreamPosition();
                songPlayClickListener.onSongPlayClickListener(v, mSong, mCurrentStreamPosition, mediaPlaybackService.isPlaying());
            }
        });

        // song menu click listener
        mAdapter.setOnSongBtnClickListener(new SongListAdapter.SongBtnClickListener() {
            @Override
            public void onSongBtnClickListener(ImageButton btn, View v, final Song song, final int pos) {
                updatePopupMenu(v,song,pos);
            }
        });
        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mReceiver);
        }
    }

    protected abstract void updatePopupMenu(View v,Song song, int pos);

    public abstract void onReceiverSongComplete();

    public abstract void onReceiverSongChange();

    public abstract void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService);

    public abstract void updateAdapter();

    public abstract void refresh();

    public void setSongListPlay(){
        mediaPlaybackService.setSongList(mSongData.getSongList());
        mediaPlaybackService.setCurrentSongIndex(mediaPlaybackService.getCurrentSongPosition());
    }

    public interface SongRemoveFavoriteListener{
        void onSongRemoveFavoriteListener();
    }

    public void setOnSongRemoveFavoriteListener(SongRemoveFavoriteListener songPlayClickListener) {
        this.songRemoveFavoriteListener = songPlayClickListener;
    }

    public void setOnSongPlayClickListener(AllSongsFragment.SongPlayClickListener songPlayClickListener) {
        this.songPlayClickListener = songPlayClickListener;
    }

    public void setOnSongItemClickListener(SongItemClickListener songItemClickListener) {
        mSongItemClickListener = songItemClickListener;
        if (mAdapter != null) {
            mAdapter.setOnSongItemClickListener(songItemClickListener);
        }
    }

    public void setStateMusic(int index, int id, boolean playing){
        mSongCurrentIndex = index;
        mSongCurrentId = id;
        isPlaying = playing;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    public void updateUI() {
        if (mSongData != null) {
            mSongData.setPlaying(isPlaying);
            if (mediaPlaybackService != null){
                mSongCurrentId = mediaPlaybackService.getCurrentSongId();
            }
            mSongData.setSongCurrentId(mSongCurrentId);
            int pos = (mSongCurrentIndex - 2) > 0 ? (mSongCurrentIndex - 2) : 0;
            mRecyclerView.scrollToPosition(pos);
            mAdapter.notifyDataSetChanged();
            SongData songData = new SongData(getActivity().getApplicationContext());
            Song song = songData.getSongId(mSongCurrentId);
            Log.d(TAG, "updateUI: "+ mSongCurrentIndex);
            if (isPortrait) updatePlaySongLayout(song);
        }
    }

    public void updatePlaySongLayout(Song mSong) {
        this.mSong = mSong;
        mRelativeLayout.setVisibility(View.VISIBLE);
        mSongName.setText(mSong.getTitle());
        mSongArtist.setText(mSong.getArtistName());
        if (isPlaying) {
            mSongPlayBtn.setImageResource(R.drawable.ic_media_pause_light);
        } else mSongPlayBtn.setImageResource(R.drawable.ic_media_play_light);
        Bitmap albumArt = SongData.getAlbumArt(mSong.getData());
        if (albumArt != null) {
            mSongImage.setImageBitmap(albumArt);
        } else {
            mSongImage.setImageResource(R.drawable.art_song_default);
        }
    }
}
