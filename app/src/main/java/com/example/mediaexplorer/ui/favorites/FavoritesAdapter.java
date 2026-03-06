package com.example.mediaexplorer.ui.favorites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.local.FavoriteMovieEntity;

import java.util.ArrayList;
import java.util.List;

// Adapter для отображения списка избранных фильмов
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.VH> {

    // Интерфейс для обработки действий пользователя
    public interface Listener {
        void onClick(FavoriteMovieEntity movie);        // открытие фильма
        void onLongClickDelete(FavoriteMovieEntity movie); // удаление из избранного
    }

    private final Listener listener;

    // Список элементов RecyclerView
    private final List<FavoriteMovieEntity> items = new ArrayList<>();

    public FavoritesAdapter(Listener listener) {
        this.listener = listener;
    }

    // Обновление списка данных
    public void setItems(List<FavoriteMovieEntity> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Создание карточки элемента списка
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        FavoriteMovieEntity m = items.get(position);

        // Название фильма
        h.txtTitle.setText(m.title != null ? m.title : "");

        // Загрузка постера через Glide
        if (m.posterPath != null && !m.posterPath.trim().isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w500" + m.posterPath)
                    .into(h.imgPoster);
        }

        // Отображение статуса фильма
        String statusLabel = statusToLabel(m.userStatus);
        if (statusLabel == null) {
            h.txtStatus.setVisibility(View.GONE);
        } else {
            h.txtStatus.setVisibility(View.VISIBLE);
            h.txtStatus.setText(statusLabel);
        }

        // Отображение emoji-реакции пользователя
        if (m.emojiReaction == null || m.emojiReaction.trim().isEmpty()) {
            h.txtEmoji.setVisibility(View.GONE);
        } else {
            h.txtEmoji.setVisibility(View.VISIBLE);
            h.txtEmoji.setText(m.emojiReaction);
        }

        // Клик — открыть экран фильма
        h.itemView.setOnClickListener(v -> listener.onClick(m));

        // Долгий клик — удалить из избранного
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClickDelete(m);
            return true;
        });
    }

    // Преобразование статуса в текст для UI
    private String statusToLabel(String s) {
        if (s == null) return null;

        switch (s) {
            case "TO_WATCH": return "🎬 Позже";
            case "WATCHED": return "✅ Просмотрено";
            case "FAVORITE": return "❤️ Любимое";
            default: return null;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder — хранит ссылки на элементы карточки
    static class VH extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        TextView txtTitle;
        TextView txtStatus;
        TextView txtEmoji;

        VH(@NonNull View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtEmoji = itemView.findViewById(R.id.txtEmoji);
        }
    }
}