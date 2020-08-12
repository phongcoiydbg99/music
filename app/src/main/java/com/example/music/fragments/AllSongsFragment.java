package com.example.music.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.LinkedList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllSongsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllSongsFragment extends Fragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final int VERTICAL_ITEM_SPACE = 150;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LinkedList<Song> mSongList = new LinkedList<>();
    private RecyclerView mRecyclerView;
    private SongListAdapter mAdapter;
    private SongData mSongData;
    private Fragment fragment;
    private LinearLayout mLinearLayout;
    private ImageView mSongImage;
    private TextView mSongName;
    private TextView mSongArtist;
    private ImageButton mSongPlayBtn;
    private boolean songPlay = false;

    private SongItemClickListener mSongItemClickListener;

    public AllSongsFragment() {}

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSongData = new SongData(context);
        System.out.println("asdasdasd");
//        if (context instanceof SongPlayClickListener) {
//            songPlayClickListener = (SongPlayClickListener) context;
//        } else {
//            throw new ClassCastException(context.toString()
//                    + " must implemenet AllSongsFragment.SongPlayClickListener");
//        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
        System.out.println("Heloo");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_all_songs, container, false);

        mLinearLayout = view.findViewById(R.id.play_song_layout);
        mSongImage = view.findViewById(R.id.song_image);
        mSongName = view.findViewById(R.id.song_name_play);
        mSongArtist = view.findViewById(R.id.song_artist_name);
        mSongPlayBtn = view.findViewById(R.id.song_play_button);

        mRecyclerView = view.findViewById(R.id.song_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        mSongList = mSongData.getSongList();
        if ( mSongList.size() > 0)
        {
            mAdapter = new SongListAdapter(view.getContext(), mSongList);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
//            mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        }
        if (mAdapter != null) {
            mAdapter.setOnSongItemClickListener(mSongItemClickListener);
        }

        // song menu click listener
        mAdapter.setOnSongBtnClickListener(new SongListAdapter.SongBtnClickListener() {
            @Override
            public void onSongBtnClickListener(ImageButton btn, View v, final Song song, final int pos) {
                Toast.makeText(v.getContext(),song.getAlbumName(),Toast.LENGTH_SHORT).show();
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

    public void setOnSongItemClickListener(SongItemClickListener songItemClickListener) {
        mSongItemClickListener = songItemClickListener;
        if (mAdapter != null) {
            mAdapter.setOnSongItemClickListener(songItemClickListener);
        }
    }

    public void updatePlaySongLayout(Context context,Song song)
    {
        mSongName.setText(song.getTitle());
        mSongArtist.setText(song.getArtistName());
        byte[] albumArt = SongData.getAlbumArt(song.getData());
        if (albumArt != null) {
            Glide.with(context).asBitmap()
                    .load(albumArt)
                    .into(mSongImage);
        } else {
            Glide.with(context)
                    .load(R.drawable.background_transparent)
                    .into(mSongImage);
        }
    }

}