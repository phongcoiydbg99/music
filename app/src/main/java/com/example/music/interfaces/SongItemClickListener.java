package com.example.music.interfaces;

import android.view.View;

import com.example.music.adapters.SongListAdapter;


public interface SongItemClickListener {
    void onSongItemClick(SongListAdapter.SongViewHolder holder, int pos);
}
