package com.example.music.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MediaPlaybackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaPlaybackFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SONG_POSSITON = "song_possion";
    private Song mSong;
    // TODO: Rename and change types of parameters
    private int mSongPos;
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
    private ImageButton mMediaPlayButton;
    private ImageButton mMediaSkipNextButton;
    private ImageButton mMediaDislikeButton;

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
    public static MediaPlaybackFragment newInstance(int pos) {
        MediaPlaybackFragment fragment = new MediaPlaybackFragment();
        Bundle args = new Bundle();
        args.putInt(SONG_POSSITON, pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSongData = new SongData(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSongPos = getArguments().getInt(SONG_POSSITON);
        }
        mSong = mSongData.getSongAt(mSongPos);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        initView(view);
        updateUI(view);
        return view;
    }

    public void initView(View view) {
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
    }

    public void updateUI(View view) {
        mSongName.setText(mSong.getTitle());
        mSongArtist.setText(mSong.getArtistName());
        mStartTime.setText("00:00");
        mEndTime.setText(formattedTime(mSong.getDuration()));
        byte[] albumArt = SongData.getAlbumArt(mSong.getData());
        if (albumArt != null) {
            Glide.with(view.getContext()).asBitmap()
                    .load(albumArt)
                    .into(mSongImage);
            Glide.with(view.getContext()).asBitmap()
                    .load(albumArt)
                    .into(mMediaSongImage);
        } else {
            Glide.with(view.getContext())
                    .load(R.drawable.background_transparent)
                    .into(mSongImage);
            Glide.with(view.getContext())
                    .load(R.drawable.background_transparent)
                    .into(mMediaSongImage);
        }

        mMediaQueueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    public String formattedTime(long duration) {
        String seconds = String.valueOf(mSong.getDuration() % 60);
        String minutes = String.valueOf(mSong.getDuration() / 60);
        if (minutes.length() <= 1) minutes = "0" + minutes;
        if (seconds.length() <= 1) seconds = "0" + seconds;
        return minutes +":"+seconds;
    }
}