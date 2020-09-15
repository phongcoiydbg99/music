package com.example.music.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.MusicDB;
import com.example.music.MusicProvider;
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
    public static final String IS_PORTRAIT = "is_portait";
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
    private int mSongCurrentId = -1;

    private boolean isPortrait = true;
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
    private MediaPlaybackService mediaPlaybackService;
    private UpdateSeekBarThread updateSeekBarThread;
    private SongIsFavorClickListener mSongIsFavorClickListener;

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
    public static MediaPlaybackFragment newInstance(Boolean isPortrait, String title, String artist, String data, long duration, int pos, long current, boolean isPlaying) {
        MediaPlaybackFragment fragment = new MediaPlaybackFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_PORTRAIT, isPortrait);
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
                String state = intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_COMPLETE);
                isPlaying = !state.equals("play_done");
                if (mediaPlaybackService != null) {
                    mSongCurrentPosition = mediaPlaybackService.getCurrentSongPosition();
                    if (mSongCurrentPosition < 0) mSongCurrentPosition = 0;
                    Song song = mediaPlaybackService.getSongData().getSongAt(mSongCurrentPosition);
                    mSongCurrentStreamPossition = mediaPlaybackService.getCurrentStreamPosition();
                    updateSongCurrentData(song, mSongCurrentPosition, isPlaying);
                    updateUI();
                    Log.d(TAG, "song complete: " + isPlaying + " " + mSongCurrentPosition);
                }
            }
            if (intent.getAction() == MediaPlaybackService.SONG_PLAY_CHANGE) {
                String state = intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE);
                if (mediaPlaybackService != null)
                    mSongCurrentStreamPossition = mediaPlaybackService.getCurrentStreamPosition();
                if (state == "song_state_play") {
                    isPlaying = true;
                    mMediaPlayButton.setImageResource(R.drawable.ic_pause_circle);
                } else if (state == "song_state_pause") {
                    isPlaying = false;
                    mMediaPlayButton.setImageResource(R.drawable.ic_play_circle);
                } else {
                    if (mediaPlaybackService != null) {
                        mSongCurrentPosition = Integer.parseInt(intent.getStringExtra(MediaPlaybackService.MESSAGE_SONG_PLAY_CHANGE));
                        Log.d(TAG, "onReceive: song play change " + mSongCurrentPosition);
                        Song song = mediaPlaybackService.getSongData().getSongAt(mSongCurrentPosition);
                        updateSongCurrentData(song, mSongCurrentPosition, true);
                        updateUI();
                        mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongIndex(), true);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            isPortrait = args.getBoolean(IS_PORTRAIT);
            mSongCurrentTitle = getArguments().getString(TITLE);
            mSongCurrentArtist = getArguments().getString(ARTIST);
            mSongCurrentData = getArguments().getString(DATA);
            mSongCurrentDuration = getArguments().getLong(DURATION);
            isPlaying = getArguments().getBoolean(IS_PLAYING);
            mSongCurrentPosition = getArguments().getInt(SONG_POSSITON);
            mSongCurrentStreamPossition = getArguments().getLong(CURRENT_STREAM_POSSITION);
            Log.d(TAG, "onCreate: " + mSongCurrentPosition);
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
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_COMPLETE);
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_CHANGE);
        intentFilter.addAction(MediaPlaybackService.SONG_PLAY_CHANGE);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        // unregister receiver
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mReceiver);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        updateSeekBarThread.exit();
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
        mMediaSeekBar.setMax((int) (mSongCurrentDuration/1000));
        mMediaSeekBar.setProgress((int) mSongCurrentStreamPossition/1000);
    }

    public void clickView() {
        mMediaQueueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        mMediaPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + mediaPlaybackService.isPlaying() + " " + (mediaPlaybackService == null));
                if (mediaPlaybackService.isPlaying()) {
                    mediaPlaybackService.pause();
                    isPlaying = false;
                    mediaPlaybackService.sendMessageChangeState("song_state_pause");
                    mMediaPlayButton.setImageResource(R.drawable.ic_play_circle);
                    mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongIndex(), false);
                } else {
                    if (mediaPlaybackService.isFirst()) {
                        Log.d(TAG, "clickView: " + mediaPlaybackService.getCurrentSongIndex());
                        mediaPlaybackService.play(mediaPlaybackService.getCurrentSongIndex());
                        mediaPlaybackService.setFirst(false);
                    } else
                        mediaPlaybackService.start();
                    isPlaying = true;
                    mediaPlaybackService.sendMessageChangeState("song_state_play");
                    updateSeekBarThread.updateSeekBar();
                    mMediaPlayButton.setImageResource(R.drawable.ic_pause_circle);
                    mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongIndex(), true);
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
                mSongCurrentId = mediaPlaybackService.getCurrentSongId();
                Uri uri = Uri.parse(MusicProvider.CONTENT_URI + "/" + mSongCurrentId);
                Cursor cursor = getContext().getContentResolver().query(uri, null, null, null,
                        null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    if (cursor.getInt(cursor.getColumnIndex(MusicDB.IS_FAVORITE)) == 0 || cursor.getInt(cursor.getColumnIndex(MusicDB.IS_FAVORITE)) == 1) {
                        ContentValues values = new ContentValues();
                        values.put(MusicDB.IS_FAVORITE, 2);
                        getContext().getContentResolver().update(uri, values, null, null);
                        Toast.makeText(getActivity().getApplicationContext(), "Add Favorite", Toast.LENGTH_SHORT).show();
                        mMediaDislikeButton.setImageResource(R.drawable.ic_thumb_down);
                        mMediaLikeButton.setImageResource(R.drawable.ic_thumb_up_black);
                        if (mSongIsFavorClickListener != null) {
                            mSongIsFavorClickListener.onSongIsFavorClickListener();
                        }
                    }
                }
            }
        });

        mMediaDislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSongIsFavorClickListener != null) {
                    mSongCurrentId = mediaPlaybackService.getCurrentSongId();
                    Log.d(TAG, "onClick: " + mSongCurrentId);
                    Uri uri = Uri.parse(MusicProvider.CONTENT_URI + "/" + mSongCurrentId);
                    Cursor cursor = getContext().getContentResolver().query(uri, null, null, null,
                            null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        if (cursor.getInt(cursor.getColumnIndex(MusicDB.IS_FAVORITE)) == 2) {
                            ContentValues values = new ContentValues();
                            values.put(MusicDB.IS_FAVORITE, 1);
                            getContext().getContentResolver().update(uri, values, null, null);
                            Toast.makeText(getActivity().getApplicationContext(), "Remove Favorite", Toast.LENGTH_SHORT).show();
                            mMediaDislikeButton.setImageResource(R.drawable.ic_thumb_down_black);
                            mMediaLikeButton.setImageResource(R.drawable.ic_thumb_up);
                            mSongIsFavorClickListener.onSongIsFavorClickListener();
                        }
                    }
                }
            }
        });

        mMediaRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int repeat = mediaPlaybackService.isRepeat();
                if (repeat == MediaPlaybackService.REPEAT) {
                    mediaPlaybackService.setRepeat(MediaPlaybackService.NORMAL);
                    mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_24);
                } else if (repeat == MediaPlaybackService.REPEAT_ALL) {
                    mediaPlaybackService.setRepeat(MediaPlaybackService.REPEAT);
                    mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_one_24);
                } else {
                    mediaPlaybackService.setRepeat(MediaPlaybackService.REPEAT_ALL);
                    mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_all_24);
                }
            }
        });

        mMediaShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int shuffle = mediaPlaybackService.isShuffle();
                if (shuffle == MediaPlaybackService.SHUFFLE) {
                    mediaPlaybackService.setShuffle(MediaPlaybackService.NORMAL);
                    mMediaShuffleButton.setImageResource(R.drawable.ic_shuffle);
                } else {
                    mediaPlaybackService.setShuffle(MediaPlaybackService.SHUFFLE);
                    mMediaShuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_25);
                }
            }
        });

        mMediaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlaybackService != null && fromUser) {
                    if (mediaPlaybackService.isFirst()) {
                        try {
                            mediaPlaybackService.play(mediaPlaybackService.getCurrentSongIndex());
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "onProgressChanged: "+mediaPlaybackService.isPlaying());
                        mediaPlaybackService.setFirst(false);
                        mediaPlaybackService.startForegroundService(mediaPlaybackService.getCurrentSongPosition(), true);
                        isPlaying = true;
                        mMediaPlayButton.setImageResource(R.drawable.ic_pause_circle);
                        updateSeekBarThread.updateSeekBar();
                    }
                    mediaPlaybackService.seekTo(progress*1000);
                }
                mStartTime.setText(formattedTime(progress*1000));
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
        if (mediaPlaybackService != null) {
            int repeat = mediaPlaybackService.isRepeat();
            if (repeat == MediaPlaybackService.REPEAT) {
                mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_one_24);
            } else if (repeat == MediaPlaybackService.REPEAT_ALL) {
                mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_all_24);
            } else {
                mMediaRepeatButton.setImageResource(R.drawable.ic_baseline_repeat_24);
            }

            int shuffle = mediaPlaybackService.isShuffle();
            if (shuffle != MediaPlaybackService.SHUFFLE) {
                mMediaShuffleButton.setImageResource(R.drawable.ic_shuffle);
            } else {
                mMediaShuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_25);
            }
            mSongCurrentId = mediaPlaybackService.getCurrentSongId();
            Uri uri = Uri.parse(MusicProvider.CONTENT_URI + "/" + mSongCurrentId);
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null,
                    null);
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getInt(cursor.getColumnIndex(MusicDB.IS_FAVORITE)) != 2) {
                        mMediaDislikeButton.setImageResource(R.drawable.ic_thumb_down_black);
                        mMediaLikeButton.setImageResource(R.drawable.ic_thumb_up);
                } else {
                        mMediaDislikeButton.setImageResource(R.drawable.ic_thumb_down);
                        mMediaLikeButton.setImageResource(R.drawable.ic_thumb_up_black);
                }
            }
        }
        updateSeekBarThread.updateSeekBar();

        Bitmap albumArt = SongData.getAlbumArt(mSongCurrentData);
        if (albumArt != null) {
            mSongImage.setImageBitmap(albumArt);
            mMediaSongImage.setImageBitmap(albumArt);
        } else {
            mSongImage.setImageResource(R.drawable.art_song_default);
            mMediaSongImage.setImageResource(R.drawable.art_song_default);
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
                        Log.d(TAG, "runis: " + mediaPlaybackService.isPlaying());
                        if (isPlaying) {
                            while (mediaPlaybackService.getPlayer() != null) {
                                try {
                                    long current = -1;
                                    try {
                                        current = mediaPlaybackService.getCurrentStreamPosition();
                                    } catch (IllegalStateException e) {
//                                    e.printStackTrace();
                                    }
                                    if (getActivity() != null) {
                                        final long finalCurrent = current;
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMediaSeekBar.setMax((int) (mediaPlaybackService.getDuration()/1000));
                                                mMediaSeekBar.setProgress((int) (finalCurrent/1000));

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

    public interface SongIsFavorClickListener {
        void onSongIsFavorClickListener();
    }


    public void setOnSongIsFavorClickListener(SongIsFavorClickListener songItemClickListener) {
        mSongIsFavorClickListener = songItemClickListener;
    }

}