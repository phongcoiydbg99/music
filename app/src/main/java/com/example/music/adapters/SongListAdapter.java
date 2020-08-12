package com.example.music.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.icu.text.UFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.interfaces.SongItemClickListener;

import java.util.Collection;
import java.util.LinkedList;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder> implements Filterable {

    private LinkedList<Song> mSongList;
    private LinkedList<Song> mSongListFull;
    private Context mContext;
    private LayoutInflater mInflater;
    private int currentPos;
    SongItemClickListener songItemClickListener;
    SongBtnClickListener songBtnClickListener;

    public SongListAdapter(Context context, LinkedList<Song> songList) {
        this.mContext = context;
        this.mSongList = songList;
        mSongListFull = new LinkedList<Song>();
        mSongListFull.addAll(mSongList);
        mInflater = LayoutInflater.from(context);
        currentPos = -1;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.song_list_item, parent, false);
        return new SongViewHolder(mItemView, this);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongViewHolder holder, final int position) {
        final Song mCurrent = mSongList.get(position);
        holder.itemId.setText(String.valueOf(position + 1));
        holder.songItemView.setText(mCurrent.getTitle());
        holder.songDurationView.setText(mCurrent.formattedTime());
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (songItemClickListener != null) {
                    songItemClickListener.onSongItemClick(view, position);
//                    System.out.println(prePos);
//                    mCurrent.setPlaying(true);
//                    mSongList.set(position,mCurrent);
//                    if (prePos != position && prePos >= 0) {
//                        Song preSong = mSongList.get(prePos);
//                        preSong.setPlaying(false);
//                        mSongList.set(prePos,preSong);
//                    }
//                    for(Song song : mSongList)
//                    System.out.print(" "+song.isPlaying());
//                    System.out.println("");
//                    prePos = position;
                    currentPos = position;
                    notifyDataSetChanged();
                    System.out.println(currentPos);
                }
            }
        });
        if (currentPos == position && currentPos >= 0){
            System.out.println(currentPos);
            holder.itemId.setVisibility(View.INVISIBLE);
            holder.iconPlay.setVisibility(View.VISIBLE);
            holder.songItemView.setTypeface(null, Typeface.BOLD);
        }

        holder.imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (songBtnClickListener != null) {
                    songBtnClickListener.onSongBtnClickListener(holder.imageButton, view, mCurrent, position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            LinkedList<Song> filteredList = new LinkedList<>();

            if (constraint.toString().isEmpty()) {
                filteredList.addAll(mSongListFull);
            } else {
                for (Song songName : mSongListFull) {
                    if (songName.getAlbumName().toLowerCase().contains(constraint.toString().toLowerCase().trim())) {
                        filteredList.addLast(songName);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mSongList.clear();
            mSongList.addAll((Collection<? extends Song>) results.values);
            notifyDataSetChanged();

        }
    };

    class SongViewHolder extends RecyclerView.ViewHolder {
        public final RelativeLayout relativeLayout;
        public final TextView itemId;
        public final TextView songItemView;
        public final TextView songDurationView;
        public final ImageButton imageButton;
        public final ImageView iconPlay;
        final SongListAdapter mAdapter;

        public SongViewHolder(@NonNull final View itemView, SongListAdapter adapter) {
            super(itemView);
            itemId = itemView.findViewById(R.id.song_id);
            songItemView = itemView.findViewById(R.id.song_name);
            songDurationView = itemView.findViewById(R.id.song_duration);
            imageButton = itemView.findViewById(R.id.popup_button);
            relativeLayout = itemView.findViewById(R.id.song_list_item);
            iconPlay = itemView.findViewById(R.id.icon_play);
            this.mAdapter = adapter;
        }
    }


    public interface SongBtnClickListener {
        void onSongBtnClickListener(ImageButton btn, View v, Song song, int pos);
    }

    public void setOnSongItemClickListener(SongItemClickListener songItemClickListener) {
        this.songItemClickListener = songItemClickListener;
    }

    public void setOnSongBtnClickListener(SongBtnClickListener songBtnClickListener) {
        this.songBtnClickListener = songBtnClickListener;
    }
}
