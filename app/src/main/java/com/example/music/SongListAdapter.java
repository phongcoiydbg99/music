package com.example.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder> {

    private LinkedList<String> mSongList;
    private Context mContext;
    private LayoutInflater mInflater;

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

    public SongListAdapter (Context context, LinkedList<String> songList)
    {
        this.mContext = context;
        this.mSongList = songList;
        mInflater = LayoutInflater.from(context);
    }
}
