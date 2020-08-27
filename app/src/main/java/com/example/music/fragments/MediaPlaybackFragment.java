package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.services.MediaPlaybackService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MediaPlaybackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaPlaybackFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = MediaPlaybackFragment.class.getSimpleName();
    public static final String ID = "song_id";
    public static final String TITLE = "song_title";
    public static final String ARTIST = "song_artist";
    public static final String DATA = "song_data";
    public static final String DURATION = "song_duration";
    public static final String CURRENT_STREAM_POSSITION = "CURRENT_STEAM_POSSITION";
    public static final String IS_PLAYING = "is_playing";
    public static final String SONG_POSSITON = "song_possion";

    private String mSongCurrentTitle;
    private String mSongCurrentArtist;
    private String mSongCurrentData;
    private long mSongCurrentDuration;
    private long mSongCurrentStreamPossition;
    private boolean isPlaying;
    private int mSongCurrentPosition;

    private Song mSong;
    private SongData mSongData;
    private ImageView mMediaSongImage;
    private ImageView mSongImage;
    private TextView mSongName;
    private TextView mSongArtist;
    private TextView mStartTime;
    private TextView mEndTime;
    private ImageButton mMediaQueueButton;
    private ImageButton mMediaPopupButton;
    private ImageButton mMediaRepeatButton;
    private ImageButton mMediaShuffleButton;
    private ImageButton mMediaLikeButton;
    private ImageButton mMediaSkipPreviousButton;
    private ImageView mMediaPlayButton;
    private ImageButton mMediaSkipNextButton;
    private ImageButton mMediaDislikeButton;
    private SeekBar mMediaSeekBar;
    private View view;
    private boolean mServiceStatus = false;
    private MediaPlaybackService mediaPlaybackService;
    private UpdateSeekBarThread updateSeekBarThread;

    public MediaPlaybackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MediaPlaybackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MediaPlaybackFragment newInstance(String title, String artist, String data, long duration, int pos, long current, boolean isPlaying) {
        MediaPlaybackFragment fragment = new MediaPlaybackFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(ARTIST, artist);
        args.putString(DATA, data);
        args.putLong(DURATION, duration);
        args.putLong(CURRENT_STREAM_POSSITION, current);
        args.putBoolean(IS_PLAYING, isPlaying);
        args.putInt(SONG_POSSITON, pos);
        Log.d(TAG, "newInstance: " + artist);
        fragment.setArguments(args);
        return fragment;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_COMPLETE) {
                mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_COMPLETE));
                if (mediaPlaybackService != null) {
                    mediaPlaybackService.play(mSongCurrentPosition);
                    Song song = mediaPlaybackService.getSongData().getSongAt(mSongCurrentPosition);
                    updateSongCurrentData(song, mSongCurrentPosition, true);
                    updateUI();
                    Log.d(TAG, "song complete: " + song.getTitle() + " " + mSongCurrentPosition);
                }
            }
            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_CHANGE) {
                String state = intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE);
                if (state == "song_state_play") {
                    isPlaying = true;
                    updateUI();
                } else if (state == "song_state_pause") {
                    isPlaying = false;
                    updateUI();
                } else {
                    if(mediaPlaybackService != null){
                        mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE));
                        Log.d(TAG, "onReceive: song play change " + mSongCurrentPosition);
                        Song song = mediaPlaybackService.getSongData().getSongAt(mSongCurrentPosition);
                        updateSongCurrentData(song, mSongCurrentPosition, true);
                        updateUI();
                        mediaPlaybackService.startForegroundService(mSongCurrentPosition,true);
                    }
                }
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        mSongData = new SongData(context);
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
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");

        mServiceStatus = false;
        // gui message toi allsongsFragment khi back
//        Intent intent = new Intent(SONG_POSSITON);
//        intent.putExtra(SONG_POSSITON, String.valueOf(mSongCurrentPosition));
//        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).sendBroadcast(intent);
        // unregister receiver
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mReceiver);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        updateSeekBarThread.exit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mSongCurrentTitle = getArguments().getString(TITLE);
            mSongCurrentArtist = getArguments().getString(ARTIST);
            mSongCurrentData = getArguments().getString(DATA);
            mSongCurrentDuration = getArguments().getLong(DURATION);
            isPlaying = getArguments().getBoolean(IS_PLAYING);
            mSongCurrentPosition = getArguments().getInt(SONG_POSSITON);
            mSongCurrentStreamPossition = getArguments().getLong(CURRENT_STREAM_POSSITION);
            Log.d(TAG, "onCreate: " + mSongCurrentArtist);
        }
        updateSeekBarThread = new UpdateSeekBarThread();
        updateSeekBarThread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        initView();
        updateUI();
        clickView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    public void setMediaPlaybackService(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
    }

    public void setSongCurrentStreamPossition(long mSongCurrentStreamPossition) {
        this.mSongCurrentStreamPossition = mSongCurrentStreamPossition;
        Log.d(TAG, "setSongCurrentStreamPossition: " + mediaPlaybackService.isPlaying());

    }

    public void initView() {
        mMediaSongImage = view.findViewById(R.id.media_song_image);
        mSongImage = view.findViewById(R.id.song_media_image);
        mSongName = view.findViewById(R.id.media_song_name);
        mSongArtist = view.findViewById(R.id.song_author_name);
        mStartTime = view.findViewById(R.id.start_time);
        mEndTime = view.findViewById(R.id.end_time);
        mMediaQueueButton = view.findViewById(R.id.media_queue_button);
        mMediaPopupButton = view.findViewById(R.id.media_popup_button);
        mMediaRepeatButton = view.findViewById(R.id.media_repeat);
        mMediaShuffleButton = view.findViewById(R.id.media_shuffle);
        mMediaLikeButton = view.findViewById(R.id.media_thumb_up);
        mMediaSkipPreviousButton = view.findViewById(R.id.media_skip_previous);
        mMediaPlayButton = view.findViewById(R.id.media_play_button);
        mMediaSkipNextButton = view.findViewById(R.id.media_skip_next);
        mMediaDislikeButton = view.findViewById(R.id.media_thumb_down);
        mMediaSeekBar = view.findViewById(R.id.media_seek_bar);
        mMediaSeekBar.setMax((int) (mSongCurrentDuration));
        mMediaSeekBar.setProgress((int) mSongCurrentStreamPossition);
    }

    public void clickView (){
        mMediaQueueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        mMediaPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + mediaPlaybackService.isPlaying() + " " + String.valueOf(mediaPlaybackService == null));
                if (mediaPlaybackService.isPlaying()) {
                    mediaPlaybackService.pause();
                    isPlaying = false;
                    mMediaPlayButton.setImageResource(R.drawable.ic_play_circle);
                    mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongPosition(),false);
                } else {
                    mediaPlaybackService.start();
                    isPlaying = true;
                    updateSeekBarThread.updateSeekBar();
                    mMediaPlayButton.setImageResource(R.drawable.ic_pause_circle);
                    mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongPosition(),true);
                }
            }
        });

        mMediaSkipNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlaybackService.playNext();
            }
        });

        mMediaSkipPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlaybackService.playPrev();
            }
        });

        mMediaLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mMediaDislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mMediaRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlaybackService.isRepeat()){
                    mediaPlaybackService.setRepeat(false);
                    mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_24);
                }
                else{
                    mediaPlaybackService.setRepeat(true);
                    mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_one_24);
                }
            }
        });

        mMediaShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlaybackService.isShuffle()){
                    mediaPlaybackService.setShuffle(false);
                    mMediaShuffleButton.setImageResource(R.drawable.ic_shuffle);
                }
                else {
                    mediaPlaybackService.setShuffle(true);
                    mMediaShuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_25);
                }
            }
        });

        mMediaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlaybackService != null && fromUser) {
                    mediaPlaybackService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void updateSongCurrentData(Song song, int pos, boolean isplaying) {
        mSongCurrentTitle = song.getTitle();
        mSongCurrentArtist = song.getArtistName();
        mSongCurrentData = song.getData();
        mSongCurrentDuration = song.getDuration();
        isPlaying = isplaying;
        mSongCurrentPosition = pos;
    }

    public void updateUI() {

        mSongName.setText(mSongCurrentTitle);
        mSongArtist.setText(mSongCurrentArtist);
        mStartTime.setText(formattedTime(mSongCurrentStreamPossition));
        mEndTime.setText(formattedTime(mSongCurrentDuration));
        if (isPlaying) {
            mMediaPlayButton.setImageResource(R.drawable.ic_pause_circle);
        } else mMediaPlayButton.setImageResource(R.drawable.ic_play_circle);
        if (mediaPlaybackService != null){
            if (mediaPlaybackService.isRepeat()){
                mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_one_24);
            }
            else{
                mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_24);
            }
            if (mediaPlaybackService.isShuffle()){
                mMediaShuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_25);
            }
            else {
                mMediaShuffleButton.setImageResource(R.drawable.ic_shuffle);
            }
        }
        updateSeekBarThread.updateSeekBar();

        byte[] albumArt = SongData.getAlbumArt(mSongCurrentData);
        Log.d(TAG, String.valueOf("updateUI: " + albumArt == null));
        Log.d(TAG, "updateUI: " + albumArt);
        if (albumArt != null) {
            Glide.with(view.getContext()).asBitmap()
                    .load(albumArt)
                    .into(mSongImage);
            Glide.with(view.getContext()).asBitmap()
                    .load(albumArt)
                    .into(mMediaSongImage);
        } else {
            Glide.with(view.getContext())
                    .load(R.drawable.art_song_default)
                    .into(mSongImage);
            Glide.with(view.getContext())
                    .load(R.drawable.art_song_default)
                    .into(mMediaSongImage);
        }


    }

    public String formattedTime(long duration) {
        String seconds = String.valueOf(duration / 1000 % 60);
        String minutes = String.valueOf(duration / 1000 / 60);
        if (minutes.length() <= 1) minutes = "0" + minutes;
        if (seconds.length() <= 1) seconds = "0" + seconds;
        return minutes + ":" + seconds;
    }

    public class UpdateSeekBarThread extends Thread {
        private Handler handler;

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            handler = new Handler();
            Looper.loop();
        }

        public void updateSeekBar() {
            if (mediaPlaybackService != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "runis: "+  mediaPlaybackService.isPlaying());
                        if (isPlaying) {
                            while (mediaPlaybackService.getmPlayer() != null) {
                                try {
                                    long current = -1;
                                    try {
                                        current = mediaPlaybackService.getCurrentStreamPosition();
                                    } catch (IllegalStateException e) {
//                                    e.printStackTrace();
                                    }
                                    if (getActivity() != null) {
                                        final long finalCurrent = current;
                                        Log.d(TAG, "run: "+  finalCurrent);
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMediaSeekBar.setMax((int) (mediaPlaybackService.getDuration()));
                                                mMediaSeekBar.setProgress((int) (finalCurrent));
                                                mStartTime.setText(formattedTime(finalCurrent));
                                            }
                                        });
                                    }
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        }

        public void exit() {
            handler.getLooper().quit();
        }
    }
}