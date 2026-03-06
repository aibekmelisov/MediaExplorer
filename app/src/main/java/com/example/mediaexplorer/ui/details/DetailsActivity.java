package com.example.mediaexplorer.ui.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.remote.dto.MovieDetailsDto;
import com.example.mediaexplorer.ui.actor.ActorDetailsActivity;
import com.example.mediaexplorer.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class DetailsActivity extends AppCompatActivity {

    // Ключ для передачи id фильма через Intent
    public static final String EXTRA_MOVIE_ID = "movie_id";

    // Верхняя панель
    private MaterialToolbar toolbar;

    // Основные поля с информацией о фильме
    private ImageView imgPoster;
    private TextView txtTitle, txtMeta, txtGenres, txtOverview;

    // Состояние загрузки и ошибки
    private ProgressBar progressBar;
    private TextView txtError;

    // Список актёров (горизонтальный RecyclerView)
    private RecyclerView recyclerCast;
    private CastAdapter castAdapter;

    // Кнопки действий
    private MaterialButton btnShare, btnFavorite, btnTrailer;

    // Чипы статуса фильма в избранном (посмотреть/просмотрено/любимое)
    private ChipGroup chipGroupStatus;
    private Chip chipToWatch, chipWatched, chipFavTag;

    // Блок пользовательского отзыва (emoji + рейтинг + комментарий)
    private View cardUserReview;
    private TextView emojiLove, emojiOk, emojiWow, emojiBoring, emojiSkull;
    private RatingBar ratingBarUser;
    private EditText edtComment;
    private MaterialButton btnSaveComment;

    // Ссылка на выбранную emoji (чтобы подсветить выбор)
    private View selectedEmojiView;

    // ViewModel для загрузки данных и работы с избранным (MVVM)
    private DetailsViewModel vm;

    // id выбранного фильма
    private int movieId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Получаем id фильма, который передал предыдущий экран
        movieId = getIntent().getIntExtra(EXTRA_MOVIE_ID, -1);

        // Если id не пришёл, закрываем экран (иначе нечего загружать)
        if (movieId <= 0) {
            Toast.makeText(this, "movie_id не пришёл", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Подключаем элементы интерфейса
        bindViews();

        // Настройка toolbar, списка актёров и обработчиков нажатий
        setupToolbar();
        setupCast();
        setupClicks();

        // Создаём ViewModel и подписываемся на данные
        vm = new ViewModelProvider(this).get(DetailsViewModel.class);
        observeVm();

        // Загружаем все данные фильма: детали, актёров, трейлеры, статус избранного
        vm.loadAll(movieId);
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);

        imgPoster = findViewById(R.id.imgPoster);
        txtTitle = findViewById(R.id.txtTitle);
        txtMeta = findViewById(R.id.txtMeta);
        txtGenres = findViewById(R.id.txtGenres);
        txtOverview = findViewById(R.id.txtOverview);

        progressBar = findViewById(R.id.progressBar);
        txtError = findViewById(R.id.txtError);

        recyclerCast = findViewById(R.id.recyclerCast);

        btnShare = findViewById(R.id.btnShare);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnTrailer = findViewById(R.id.btnTrailer);

        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        chipToWatch = findViewById(R.id.chipToWatch);
        chipWatched = findViewById(R.id.chipWatched);
        chipFavTag = findViewById(R.id.chipFavTag);

        cardUserReview = findViewById(R.id.cardUserReview);

        emojiLove = findViewById(R.id.emojiLove);
        emojiOk = findViewById(R.id.emojiOk);
        emojiWow = findViewById(R.id.emojiWow);
        emojiBoring = findViewById(R.id.emojiBoring);
        emojiSkull = findViewById(R.id.emojiSkull);

        ratingBarUser = findViewById(R.id.ratingBarUser);
        edtComment = findViewById(R.id.edtComment);
        btnSaveComment = findViewById(R.id.btnSaveComment);

        // На старте скрываем блоки, которые доступны только после добавления в избранное
        cardUserReview.setVisibility(View.GONE);
        chipGroupStatus.setVisibility(View.GONE);
        chipToWatch.setVisibility(View.GONE); // подстраховка (в XML может быть GONE)
    }


    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupCast() {
        castAdapter = new CastAdapter(actor -> {
            if (actor == null || actor.id <= 0) {
                Toast.makeText(this, "Не удалось открыть актёра", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, ActorDetailsActivity.class);
            i.putExtra(ActorDetailsActivity.EXTRA_PERSON_ID, actor.id);
            startActivity(i);
        });

        recyclerCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerCast.setAdapter(castAdapter);
    }

    private void setupClicks() {
        btnShare.setOnClickListener(v -> shareMovie());
        btnTrailer.setOnClickListener(v -> vm.loadTrailerKey(movieId));

        btnFavorite.setOnClickListener(v -> {
            String text = edtComment.getText() != null ? edtComment.getText().toString().trim() : "";
            float rating = ratingBarUser.getRating();
            vm.toggleFavorite(movieId, text, rating);
        });

        btnSaveComment.setOnClickListener(v -> {
            if (!Boolean.TRUE.equals(vm.isFavorite().getValue())) {
                Toast.makeText(this, "Сначала добавь в избранное", Toast.LENGTH_SHORT).show();
                return;
            }
            String text = edtComment.getText() != null ? edtComment.getText().toString().trim() : "";
            vm.saveComment(movieId, text);
        });

        ratingBarUser.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (!fromUser) return;
            if (!Boolean.TRUE.equals(vm.isFavorite().getValue())) {
                Toast.makeText(this, "Сначала добавь в избранное", Toast.LENGTH_SHORT).show();
                bar.setRating(0f);
                return;
            }
            vm.saveRating(movieId, rating);
        });

        // Чипы: реагируем только когда chip стал checked (а не просто нажали)
        chipToWatch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) return;
            if (!Boolean.TRUE.equals(vm.isFavorite().getValue())) {
                Toast.makeText(this, "Сначала добавь в избранное", Toast.LENGTH_SHORT).show();
                return;
            }
            vm.saveStatus(movieId, DetailsViewModel.STATUS_TO_WATCH);
        });

        chipWatched.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) return;
            if (!Boolean.TRUE.equals(vm.isFavorite().getValue())) {
                Toast.makeText(this, "Сначала добавь в избранное", Toast.LENGTH_SHORT).show();
                return;
            }
            vm.saveStatus(movieId, DetailsViewModel.STATUS_WATCHED);
        });

        chipFavTag.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) return;
            if (!Boolean.TRUE.equals(vm.isFavorite().getValue())) {
                Toast.makeText(this, "Сначала добавь в избранное", Toast.LENGTH_SHORT).show();
                return;
            }
            vm.saveStatus(movieId, DetailsViewModel.STATUS_FAVORITE);
        });

        // Эмодзи
        emojiLove.setOnClickListener(v -> onEmojiClicked(v, "😍"));
        emojiOk.setOnClickListener(v -> onEmojiClicked(v, "😐"));
        emojiWow.setOnClickListener(v -> onEmojiClicked(v, "🤯"));
        emojiBoring.setOnClickListener(v -> onEmojiClicked(v, "😴"));
        emojiSkull.setOnClickListener(v -> onEmojiClicked(v, "💀"));
    }

    private void observeVm() {
        vm.loading().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE)
        );

        vm.error().observe(this, err -> {
            if (err == null || err.trim().isEmpty()) {
                txtError.setVisibility(View.GONE);
            } else {
                txtError.setVisibility(View.VISIBLE);
                txtError.setText(err);
            }
        });

        vm.details().observe(this, d -> {
            if (d == null) return;
            renderDetails(d);
        });

        vm.credits().observe(this, credits -> {
            if (credits != null) castAdapter.setItems(credits.cast);
        });

        vm.trailerKey().observe(this, key -> {
            if (key == null || key.trim().isEmpty()) return;
            String url = "https://www.youtube.com/watch?v=" + key.trim();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        // Главное: показываем “низ” только если в избранном
        vm.isFavorite().observe(this, this::renderFavoriteUi);

        vm.comment().observe(this, c -> {
            if (c == null) c = "";
            String current = edtComment.getText() != null ? edtComment.getText().toString() : "";
            if (!c.equals(current)) edtComment.setText(c);
        });

        vm.userRating().observe(this, r -> {
            if (r == null) r = 0f;
            ratingBarUser.setRating(r);
        });

        vm.userStatus().observe(this, this::applyStatusToChips);
        vm.emojiReaction().observe(this, this::applyEmojiUI);
    }

    private void renderDetails(MovieDetailsDto d) {
        txtTitle.setText(d.title != null ? d.title : "Без названия");
        txtOverview.setText(d.overview != null ? d.overview : "Нет описания");

        String meta = "Рейтинг: " + d.voteAverage + "  •  Дата: " + (d.releaseDate != null ? d.releaseDate : "-");
        txtMeta.setText(meta);

        if (d.genres == null || d.genres.isEmpty()) {
            txtGenres.setText("Жанры: -");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < d.genres.size(); i++) {
                if (d.genres.get(i) == null || d.genres.get(i).name == null) continue;
                if (sb.length() > 0) sb.append(", ");
                sb.append(d.genres.get(i).name);
            }
            txtGenres.setText("Жанры: " + sb);
        }

        String posterUrl = (d.posterPath != null) ? Constants.TMDB_IMAGE_BASE_URL + d.posterPath : null;
        Glide.with(this)
                .load(posterUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imgPoster);
    }

    private void renderFavoriteUi(Boolean fav) {
        boolean isFav = Boolean.TRUE.equals(fav);

        btnFavorite.setIconResource(isFav ? R.drawable.ic_favorite_24 : R.drawable.ic_favorite_border_24);

        // НИЗ (статус + отзыв) показываем ТОЛЬКО когда в избранном
        chipGroupStatus.setVisibility(isFav ? View.VISIBLE : View.GONE);
        cardUserReview.setVisibility(isFav ? View.VISIBLE : View.GONE);

        // chipToWatch у тебя в XML "gone", но когда избранное, мы его показываем
        chipToWatch.setVisibility(isFav ? View.VISIBLE : View.GONE);

        // когда убрали из избранного, чистим
        if (!isFav) {
            edtComment.setText("");
            ratingBarUser.setRating(0f);
            applyEmojiUI("");
            applyStatusToChips(DetailsViewModel.STATUS_TO_WATCH);
        }
    }

    // ---------- STATUS ----------
    private void applyStatusToChips(String status) {
        if (status == null || status.trim().isEmpty()) status = DetailsViewModel.STATUS_TO_WATCH;

        // чтобы не было “залипания” слушателей, просто setChecked
        if (DetailsViewModel.STATUS_WATCHED.equals(status)) {
            chipWatched.setChecked(true);
        } else if (DetailsViewModel.STATUS_FAVORITE.equals(status)) {
            chipFavTag.setChecked(true);
        } else {
            chipToWatch.setChecked(true);
        }
    }

    // ---------- EMOJI ----------
    private void onEmojiClicked(View v, String emoji) {
        if (!Boolean.TRUE.equals(vm.isFavorite().getValue())) {
            Toast.makeText(this, "Сначала добавь в избранное", Toast.LENGTH_SHORT).show();
            return;
        }
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        highlightEmoji(v);
        vm.saveEmoji(movieId, emoji);
    }

    private void applyEmojiUI(String emoji) {
        if (emoji == null) emoji = "";
        emoji = emoji.trim();

        setEmojiInactive(emojiLove);
        setEmojiInactive(emojiOk);
        setEmojiInactive(emojiWow);
        setEmojiInactive(emojiBoring);
        setEmojiInactive(emojiSkull);

        selectedEmojiView = null;
        if (emoji.isEmpty()) return;

        if ("😍".equals(emoji)) highlightEmoji(emojiLove);
        else if ("😐".equals(emoji)) highlightEmoji(emojiOk);
        else if ("🤯".equals(emoji)) highlightEmoji(emojiWow);
        else if ("😴".equals(emoji)) highlightEmoji(emojiBoring);
        else if ("💀".equals(emoji)) highlightEmoji(emojiSkull);
    }

    private void highlightEmoji(View v) {
        setEmojiInactive(emojiLove);
        setEmojiInactive(emojiOk);
        setEmojiInactive(emojiWow);
        setEmojiInactive(emojiBoring);
        setEmojiInactive(emojiSkull);

        selectedEmojiView = v;
        if (selectedEmojiView instanceof TextView) {
            selectedEmojiView.setAlpha(1f);
            selectedEmojiView.setScaleX(1.15f);
            selectedEmojiView.setScaleY(1.15f);

            selectedEmojiView.setBackgroundResource(R.drawable.bg_emoji_selected);
        }
    }

    private void setEmojiInactive(TextView tv) {
        if (tv == null) return;
        tv.setAlpha(0.7f);
        tv.setScaleX(1f);
        tv.setScaleY(1f);
        tv.setBackground(null);
    }

    // ---------- SHARE ----------
    private void shareMovie() {
        String url = "https://www.themoviedb.org/movie/" + movieId;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(intent, "Поделиться фильмом"));
    }
}