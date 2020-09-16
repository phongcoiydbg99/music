package com.example.music.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.icu.text.UFormat;
import android.util.Log;
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
import com.example.music.SongData;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;

import java.util.Collection;
import java.util.LinkedList;

import es.claucookie.miniequalizerlibrary.EqualizerView;

@SuppressWarnings("unchecked")
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder> implements Filterable {

    private static final String TAG = SongListAdapter.class.getSimpleName();
    private SongData mSongData;
    private LinkedList<Song> mSongList;
    private LinkedList<Song> mSongListFull;
    private Context mContext;
    private LayoutInflater mInflater;
    private int currentPos;
    private int currentId;
    private boolean isPlaying;
    SongItemClickListener songItemClickListener;
    SongBtnClickListener songBtnClickListener;

    public SongListAdapter(Context context, SongData songData) {
        this.mSongData = songData;
        this.mContext = context;
        this.mSongList = songData.getSongList();
        mSongListFull = new LinkedList<Song>();
        mSongListFull.addAll(mSongList);
        mInflater = LayoutInflater.from(context);
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
        isPlaying = mSongData.isPlaying();
        currentPos = mSongData.getCurrentSongPossition();
        currentId = mSongData.getSongCurrentId();
        Log.d(TAG, "onBindViewHolder: "+ currentId+"*"+isPlaying);
        holder.itemId.setText(String.valueOf(position + 1));
        holder.songItemView.setText(mCurrent.getTitle());
        holder.songDurationView.setText(mCurrent.formattedTime());
        holder.itemId.setVisibility(View.VISIBLE);
        holder.iconPlay.setVisibility(View.INVISIBLE);
        holder.songItemView.setTypeface(null, Typeface.NORMAL);
        holder.itemId.setTypeface(null, Typeface.NORMAL);
        holder.itemId.setTextColor(mContext.getResources().getColor(R.color.song_sublable));
//        Log.d(TAG, "onBindViewHolder: "+ currentPos+"*"+isPlaying);
        if (mCurrent.getId() == currentId)
        {
            holder.itemId.setVisibility(View.INVISIBLE);
            holder.iconPlay.setVisibility(View.VISIBLE);
            if (isPlaying) holder.iconPlay.animateBars();
            else {
                holder.iconPlay.stopBars();
                holder.itemId.setVisibility(View.VISIBLE);
                holder.iconPlay.setVisibility(View.INVISIBLE);
                holder.itemId.setTextColor(mContext.getResources().getColor(R.color.song_lable));
                holder.itemId.setTypeface(null, Typeface.BOLD);
            }
            holder.songItemView.setTypeface(null, Typeface.BOLD);
        }
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: "+position);
                if (songItemClickListener != null) {
                    songItemClickListener.onSongItemClick(holder, mCurrent);
                }
            }
        });
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
                    if (songName.getTitle().toLowerCase().contains(constraint.toString().toLowerCase().trim())) {
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

    public LinkedList<Song> getSongList() {
        return mSongListFull;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        public final RelativeLayout relativeLayout;
        public final TextView itemId;
        public final TextView songItemView;
        public final TextView songDurationView;
        public final ImageButton imageButton;
        public EqualizerView iconPlay;
        final SongListAdapter mAdapter;

        public SongViewHolder(@NonNull final View itemView, SongListAdapter adapter) {
            super(itemView);
            itemId = itemView.findViewById(R.id.song_id);
            songItemView = itemView.findViewById(R.id.song_name);
            songDurationView = itemView.findViewById(R.id.song_duration);
            imageButton = itemView.findViewById(R.id.popup_button);
            relativeLayout = itemView.findViewById(R.id.song_list_item);
            iconPlay = (EqualizerView) itemView.findViewById(R.id.equalizer_view);
            this.mAdapter = adapter;
        }
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setSongList(LinkedList<Song> mSongList) {
        this.mSongList = mSongList;
        mSongListFull = new LinkedList<Song>();
        mSongListFull.addAll(mSongList);
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
