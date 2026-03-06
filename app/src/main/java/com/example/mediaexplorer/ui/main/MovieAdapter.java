package com.example.mediaexplorer.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.remote.dto.MovieDto;
import com.example.mediaexplorer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieVH> {

    // список фильмов для отображения
    private final List<MovieDto> items = new ArrayList<>();

    // обработчик клика по элементу
    private final OnMovieClickListener listener;

    public MovieAdapter(OnMovieClickListener listener) {
        this.listener = listener;
    }

    // интерфейс обработки нажатия на фильм
    public interface OnMovieClickListener {
        void onMovieClick(MovieDto movie);
    }

    // установка нового списка фильмов
    public void setItems(List<MovieDto> newItems) {
        items.clear();                 // очищаем текущие данные
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();        // обновляем RecyclerView
    }

    // добавление дополнительных элементов (например при пагинации)
    public void addItems(List<MovieDto> more) {
        if (more == null || more.isEmpty()) return;

        int start = items.size();      // позиция вставки
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public MovieVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // создание layout элемента списка
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);

        return new MovieVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieVH holder, int position) {

        MovieDto m = items.get(position);

        // формирование URL постера
        String url = m.posterPath != null
                ? Constants.TMDB_IMAGE_BASE_URL + m.posterPath
                : null;

        // загрузка изображения через Glide
        Glide.with(holder.imgPoster.getContext())
                .load(url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imgPoster);

        // обработка клика по карточке фильма
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMovieClick(m);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();           // количество элементов
    }

    // ViewHolder элемента списка
    static class MovieVH extends RecyclerView.ViewHolder {

        ImageView imgPoster;

        MovieVH(@NonNull View itemView) {
            super(itemView);

            // ссылка на ImageView постера
            imgPoster = itemView.findViewById(R.id.imgPoster);
        }
    }
}