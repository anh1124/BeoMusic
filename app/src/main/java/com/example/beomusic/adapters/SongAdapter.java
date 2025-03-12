package com.example.beomusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beomusic.R;
import com.example.beomusic.models.Song;

import java.util.List;

/**
 * Adapter for displaying songs in a RecyclerView
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songs;
    private final SongClickListener listener;

    /**
     * Interface for song click events
     */
    public interface SongClickListener {
        void onSongClick(Song song);
        void onSongOptionsClick(Song song, View view);
    }

    /**
     * Constructor
     * @param songs List of songs to display
     * @param listener Click listener
     */
    public SongAdapter(List<Song> songs, SongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    /**
     * Update the list of songs
     * @param newSongs New list of songs
     */
    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
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

    /**
     * ViewHolder for song items
     */
    class SongViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSongTitle;
        private final TextView tvArtist;
        private final TextView tvDuration;
        private final ImageButton btnOptions;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnOptions = itemView.findViewById(R.id.btnOptions);

            // Set click listener for the item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSongClick(songs.get(position));
                }
            });

            // Set click listener for the options button
            btnOptions.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSongOptionsClick(songs.get(position), btnOptions);
                }
            });
        }

        /**
         * Bind song data to views
         * @param song Song to bind
         */
        public void bind(Song song) {
            tvSongTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());

            // Format duration (assuming duration is in seconds)
            int minutes = song.getDuration() / 60;
            int seconds = song.getDuration() % 60;
            tvDuration.setText(String.format("%d:%02d", minutes, seconds));
        }
    }
}
