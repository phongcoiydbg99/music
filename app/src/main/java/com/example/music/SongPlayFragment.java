package com.example.music;

import android.content.Intent;
import android.media.Image;
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

import java.util.LinkedList;

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
    private String songName ;
    private int songImageResource;
    private RelativeLayout mRelativeLayout;
    private ImageView mSongImage;
    private TextView mSongName;
    private ImageButton mSongPlayBtn;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SongPlayFragment(){}
    public SongPlayFragment(String name, int resource) {
        // Required empty public constructor
        this.songName = name;
        this.songImageResource = resource;
    }

    public static SongPlayFragment newInstance(String name, int resource) {
        return new SongPlayFragment(name,resource);
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
        mSongPlayBtn = view.findViewById(R.id.song_play_button);

        mSongName.setText(songName);
        mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(),"AAHAHAHAH",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(view.getContext(),MediaPlaybackActivity.class);
                startActivity(intent);
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
}