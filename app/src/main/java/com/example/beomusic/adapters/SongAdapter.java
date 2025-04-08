package com.example.beomusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beomusic.R;
import com.example.beomusic.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songs = new ArrayList<>();
    private Context context;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song);
        void onMoreClick(Song song, View view);
    }

    public SongAdapter(Context context, OnSongClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong;
        TextView tvSongName;
        TextView tvArtistName;
        ImageButton btnMore;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.imgSong);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            btnMore = itemView.findViewById(R.id.btnMore);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSongClick(songs.get(position));
                }
            });

            btnMore.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMoreClick(songs.get(position), v);
                }
            });
        }

        void bind(Song song) {
            tvSongName.setText(song.getTitle());
            tvArtistName.setText(song.getArtist());

            // Sử dụng Glide để load ảnh
            Glide.with(context)
                    .load(song.getThumbnailUrl())
                    .into(imgSong);
        }
    }
}