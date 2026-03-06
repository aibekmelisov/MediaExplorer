package com.example.mediaexplorer.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.local.FavoriteMovieEntity;
import com.example.mediaexplorer.ui.details.DetailsActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

// Экран "Избранное": список сохранённых фильмов + фильтрация по статусу
public class FavoritesActivity extends AppCompatActivity {

    // UI: список, индикатор загрузки, сообщение об ошибке, пустой экран
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtError;
    private View emptyState;

    // Adapter и ViewModel (MVVM)
    private FavoritesAdapter adapter;
    private FavoritesViewModel vm;

    // Чипы фильтра: все / хочу посмотреть / просмотрено / любимое
    private Chip chipAll, chipToWatch, chipWatched, chipFavorite;

    private ChipGroup chipGroupFilter;

    // Текущий фильтр (null = показываем все фильмы)
    private String currentFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Toolbar + кнопка "назад"
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Привязка элементов интерфейса
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        txtError = findViewById(R.id.txtError);
        emptyState = findViewById(R.id.emptyState);

        chipAll = findViewById(R.id.chipAll);
        chipToWatch = findViewById(R.id.chipToWatch);
        chipWatched = findViewById(R.id.chipWatched);
        chipFavorite = findViewById(R.id.chipFavorite);

        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        // Переключение фильтра по выбранному чипу
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) return;

            int id = checkedIds.get(0);

            if (id == R.id.chipAll) {
                currentFilter = null;          // показать все
                vm.load();
            } else if (id == R.id.chipToWatch) {
                currentFilter = "TO_WATCH";    // хочу посмотреть
                vm.loadByStatus(currentFilter);
            } else if (id == R.id.chipWatched) {
                currentFilter = "WATCHED";     // просмотрено
                vm.loadByStatus(currentFilter);
            } else if (id == R.id.chipFavorite) {
                currentFilter = "FAVORITE";    // любимое
                vm.loadByStatus(currentFilter);
            }
        });

        // Начальное состояние: показываем все избранные фильмы
        chipGroupFilter.check(R.id.chipAll);

        // Adapter: открытие деталей фильма + удаление из избранного по долгому нажатию
        adapter = new FavoritesAdapter(new FavoritesAdapter.Listener() {
            @Override
            public void onClick(FavoriteMovieEntity movie) {
                Intent intent = new Intent(FavoritesActivity.this, DetailsActivity.class);
                intent.putExtra(DetailsActivity.EXTRA_MOVIE_ID, movie.id);
                startActivity(intent);
            }

            @Override
            public void onLongClickDelete(FavoriteMovieEntity movie) {
                new AlertDialog.Builder(FavoritesActivity.this)
                        .setTitle("Удалить из избранного?")
                        .setMessage(movie.title != null ? movie.title : "Фильм")
                        .setPositiveButton("Удалить", (d, w) ->
                                vm.delete(movie.id, currentFilter)) // удаляем и обновляем текущий фильтр
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        });

        // Сетка 3 колонки (как галерея)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        // ViewModel
        vm = new ViewModelProvider(this).get(FavoritesViewModel.class);

        // Показ/скрытие индикатора загрузки
        vm.loading().observe(this, loading ->
                progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));

        // Отображение ошибок (и уведомление через Toast)
        vm.error().observe(this, err -> {
            if (err == null || err.trim().isEmpty()) {
                txtError.setVisibility(View.GONE);
            } else {
                txtError.setVisibility(View.VISIBLE);
                txtError.setText(err);
                Toast.makeText(this, err, Toast.LENGTH_LONG).show();
            }
        });

        // Обновление списка избранного
        vm.items().observe(this, list -> {
            adapter.setItems(list);

            // Если список пустой, показываем empty-state
            boolean isEmpty = (list == null || list.isEmpty());
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            if (isEmpty) txtError.setVisibility(View.GONE);
        });

        // Дополнительно: быстрые клики по чипам (дублируют ChipGroup)
        chipAll.setOnClickListener(v -> vm.load());
        chipToWatch.setOnClickListener(v -> vm.loadByStatus("TO_WATCH"));
        chipWatched.setOnClickListener(v -> vm.loadByStatus("WATCHED"));
        chipFavorite.setOnClickListener(v -> vm.loadByStatus("FAVORITE"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // При возвращении на экран обновляем данные (после изменений в DetailsActivity)
        if (currentFilter == null) vm.load();
        else vm.loadByStatus(currentFilter);
    }
}