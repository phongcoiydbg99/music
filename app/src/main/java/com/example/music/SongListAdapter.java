package com.example.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;
import java.util.LinkedList;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder> implements Filterable {

    private LinkedList<String> mSongList;
    private LinkedList<String> mSongListFull;
    private Context mContext;
    private LayoutInflater mInflater;

    public SongListAdapter (Context context, LinkedList<String> songList)
    {
        this.mContext = context;
        this.mSongList = songList;
        this.mSongListFull = songList;
        mInflater = LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.song_list_item, parent, false);
        return new SongViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        String mCurrent = mSongList.get(position);
        holder.itemId.setText(String.valueOf(position));
        holder.songItemView.setText(mCurrent);

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

            LinkedList<String> filteredList = new LinkedList<>();

            if (constraint.toString().isEmpty()) {
                filteredList.addAll(mSongListFull);
            } else {
                for (String songName : mSongListFull)
                {
                    if (songName.toLowerCase().contains(constraint.toString().toLowerCase().trim()))
                    {
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
            mSongList.addAll((Collection<? extends String>) results.values);
            notifyDataSetChanged();

        }
    };

    class SongViewHolder extends RecyclerView.ViewHolder{
        public final TextView itemId;
        public final TextView songItemView;
        public final ImageButton imageButton;
        final SongListAdapter mAdapter;
        public SongViewHolder(@NonNull final View itemView, SongListAdapter adapter) {
            super(itemView);
            itemId = itemView.findViewById(R.id.song_id);
            songItemView = itemView.findViewById(R.id.song_name);
            imageButton = itemView.findViewById(R.id.popup_button);
            this.mAdapter = adapter;

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(itemView.getContext(),"Asd",Toast.LENGTH_SHORT).show();
                    PopupMenu popup = new PopupMenu(itemView.getContext(), v);
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

        }
    }


}
