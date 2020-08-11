package com.example.music.fragments;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.activities.MediaPlaybackActivity;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.interfaces.SongPlayClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SongPlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SongPlayFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Song mSong;
    private int mSongPos;
    private RelativeLayout mRelativeLayout;
    private ImageView mSongImage;
    private TextView mSongName;
    private TextView mSongArtist;
    private ImageButton mSongPlayBtn;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SongPlayClickListener songPlayClickListener;

    public SongPlayFragment(){}
    public SongPlayFragment(Song song, int pos) {
        // Required empty public constructor
        this.mSong = song;
        this.mSongPos = pos;
    }

    public static SongPlayFragment newInstance(Song song, int pos) {
        return new SongPlayFragment(song,pos);
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SongPlayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SongPlayFragment newInstance(String param1, String param2) {
        SongPlayFragment fragment = new SongPlayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_song_play, container, false);
        mRelativeLayout = view.findViewById(R.id.layout_song_play);
        mSongImage = view.findViewById(R.id.song_image);
        mSongName = view.findViewById(R.id.song_name_play);
        mSongArtist = view.findViewById(R.id.song_artist_name);
        mSongPlayBtn = view.findViewById(R.id.song_play_button);

//        mSongName.setText(mSong.getTitle());
//        mSongArtist.setText(mSong.getArtistName());
//        byte[] albumArt = getAlbumArt(mSong.getData());
//        if (albumArt != null)
//        {
//            Glide.with(view.getContext()).asBitmap()
//                    .load(albumArt)
//                    .into(mSongImage);
//        }
//        else
//        {
//            Glide.with(view.getContext())
//                    .load(R.drawable.background_transparent)
//                    .into(mSongImage);
//        }
        mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(),"AAHAHAHAH",Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(view.getContext(), MediaPlaybackActivity.class);
//                intent.putExtra("pos",mSongPos);
//                startActivity(intent);
                if (songPlayClickListener != null)
                {
                    songPlayClickListener.onSongPlayClick(v,0);
                }
            }
        });

        mSongPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(),"Buton",Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
    private byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri);
        byte[] albumArt = mediaMetadataRetriever.getEmbeddedPicture();
        mediaMetadataRetriever.release();
        return albumArt;
    }
    public void setOnSongPlayClickListener(SongPlayClickListener songPlayClickListener) {
        this.songPlayClickListener = songPlayClickListener;
    }
}