package com.example.mediaexplorer.ui.actor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.remote.dto.CastCreditDto;
import com.example.mediaexplorer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FilmographyAdapter extends RecyclerView.Adapter<FilmographyAdapter.VH> {

    public interface OnMovieClickListener {
        void onMovieClick(CastCreditDto movie);
    }

    private final List<CastCreditDto> items = new ArrayList<>();
    private final OnMovieClickListener listener;

    public FilmographyAdapter(OnMovieClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CastCreditDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filmography, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CastCreditDto m = items.get(position);

        h.txtTitle.setText(m.title != null ? m.title : "-");
        String sub = "";
        if (m.releaseDate != null && !m.releaseDate.trim().isEmpty()) sub += m.releaseDate;
        if (m.character != null && !m.character.trim().isEmpty()) {
            if (!sub.isEmpty()) sub += "  •  ";
            sub += "Роль: " + m.character;
        }
        h.txtSub.setText(sub.isEmpty() ? "-" : sub);

        String url = m.posterPath != null ? Constants.TMDB_IMAGE_BASE_URL + m.posterPath : null;
        Glide.with(h.imgPoster.getContext())
                .load(url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(h.imgPoster);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMovieClick(m);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle, txtSub;

        VH(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSub = itemView.findViewById(R.id.txtSub);
        }
    }
}