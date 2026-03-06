package com.example.mediaexplorer.ui.actor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.remote.dto.CastCreditDto;
import com.example.mediaexplorer.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;

// Экран подробной информации об актере
public class ActorDetailsActivity extends AppCompatActivity {

    // Ключ для передачи id актёра через Intent
    public static final String EXTRA_PERSON_ID = "person_id";

    // Элементы интерфейса
    private MaterialToolbar toolbar;
    private ImageView imgActor;
    private TextView txtName, txtMeta, txtBio, txtFilmographyTitle, txtError;
    private ProgressBar progressBar;
    private RecyclerView recyclerFilmography;

    // Адаптер фильмографии
    private FilmographyAdapter adapter;

    // ViewModel (MVVM)
    private ActorViewModel vm;

    // id актёра
    private int personId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actor_details);

        // Привязка элементов интерфейса
        toolbar = findViewById(R.id.toolbar);
        imgActor = findViewById(R.id.imgActor);
        txtName = findViewById(R.id.txtName);
        txtMeta = findViewById(R.id.txtMeta);
        txtBio = findViewById(R.id.txtBio);
        txtFilmographyTitle = findViewById(R.id.txtFilmographyTitle);
        txtError = findViewById(R.id.txtError);
        progressBar = findViewById(R.id.progressBar);
        recyclerFilmography = findViewById(R.id.recyclerFilmography);

        // Кнопка "назад" на toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Адаптер списка фильмов актёра
        adapter = new FilmographyAdapter(movie -> {
            if (movie == null || movie.id <= 0) return;

            // Переход на экран деталей фильма
            Intent i = new Intent(this, com.example.mediaexplorer.ui.details.DetailsActivity.class);
            i.putExtra(com.example.mediaexplorer.ui.details.DetailsActivity.EXTRA_MOVIE_ID, movie.id);
            startActivity(i);
        });

        // Настройка RecyclerView
        recyclerFilmography.setLayoutManager(new LinearLayoutManager(this));
        recyclerFilmography.setAdapter(adapter);

        // Получаем id актёра из Intent
        personId = getIntent().getIntExtra(EXTRA_PERSON_ID, -1);

        // Проверка корректности id
        if (personId <= 0) {
            Toast.makeText(this, "person_id не пришёл", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Получение ViewModel
        vm = new ViewModelProvider(this).get(ActorViewModel.class);

        // Наблюдение за состоянием загрузки
        vm.loading().observe(this,
                l -> progressBar.setVisibility(Boolean.TRUE.equals(l) ? View.VISIBLE : View.GONE));

        // Наблюдение за ошибками
        vm.error().observe(this, e -> {
            if (e == null || e.trim().isEmpty()) {
                txtError.setVisibility(View.GONE);
            } else {
                txtError.setVisibility(View.VISIBLE);
                txtError.setText(e);
            }
        });

        // Наблюдение за данными актёра
        vm.person().observe(this, p -> {
            if (p == null) return;

            txtName.setText(p.name != null ? p.name : "Актёр");
            toolbar.setTitle(p.name != null ? p.name : "Актёр");

            // Формирование строки метаданных (дата рождения + место рождения)
            String meta = "";
            if (p.birthday != null && !p.birthday.trim().isEmpty())
                meta += "Дата рождения: " + p.birthday;

            if (p.placeOfBirth != null && !p.placeOfBirth.trim().isEmpty()) {
                if (!meta.isEmpty()) meta += "  •  ";
                meta += p.placeOfBirth;
            }

            txtMeta.setText(meta.isEmpty() ? "-" : meta);

            // Биография актёра
            txtBio.setText(
                    (p.biography != null && !p.biography.trim().isEmpty())
                            ? p.biography
                            : "Биография отсутствует"
            );

            // Загрузка фотографии актёра через Glide
            String url = p.profilePath != null
                    ? Constants.TMDB_IMAGE_BASE_URL + p.profilePath
                    : null;

            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgActor);
        });

        // Наблюдение за фильмографией актёра
        vm.credits().observe(this, c -> {
            if (c == null || c.cast == null || c.cast.isEmpty()) {
                txtFilmographyTitle.setText("Фильмография: -");
                adapter.setItems(null);
            } else {
                txtFilmographyTitle.setText("Фильмография");
                adapter.setItems(c.cast);
            }
        });

        // Загрузка данных актёра
        vm.load(personId);
    }
}