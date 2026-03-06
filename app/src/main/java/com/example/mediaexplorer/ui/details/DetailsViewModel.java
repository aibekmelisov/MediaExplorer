package com.example.mediaexplorer.ui.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mediaexplorer.data.local.FavoriteMovieEntity;
import com.example.mediaexplorer.data.remote.dto.CreditsResponseDto;
import com.example.mediaexplorer.data.remote.dto.MovieDetailsDto;
import com.example.mediaexplorer.data.remote.dto.VideoDto;
import com.example.mediaexplorer.data.remote.dto.VideoListResponseDto;
import com.example.mediaexplorer.data.repository.FavoritesRepository;
import com.example.mediaexplorer.data.repository.MoviesRepository;

public class DetailsViewModel extends AndroidViewModel {

    public static final String STATUS_TO_WATCH = "TO_WATCH";
    public static final String STATUS_WATCHED = "WATCHED";
    public static final String STATUS_FAVORITE = "FAVORITE";

    private final MoviesRepository moviesRepo = new MoviesRepository();
    private final FavoritesRepository favRepo;

    private final MutableLiveData<MovieDetailsDto> details = new MutableLiveData<>();
    private final MutableLiveData<CreditsResponseDto> credits = new MutableLiveData<>();
    private final MutableLiveData<String> trailerKey = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isFavorite = new MutableLiveData<>(false);
    private final MutableLiveData<String> comment = new MutableLiveData<>("");
    private final MutableLiveData<Float> userRating = new MutableLiveData<>(0f);

    // NEW
    private final MutableLiveData<String> userStatus = new MutableLiveData<>(STATUS_TO_WATCH);
    private final MutableLiveData<String> emojiReaction = new MutableLiveData<>("");

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public DetailsViewModel(@NonNull Application app) {
        super(app);
        favRepo = new FavoritesRepository(app);
    }

    public LiveData<MovieDetailsDto> details() { return details; }
    public LiveData<CreditsResponseDto> credits() { return credits; }
    public LiveData<String> trailerKey() { return trailerKey; }

    public LiveData<Boolean> isFavorite() { return isFavorite; }
    public LiveData<String> comment() { return comment; }
    public LiveData<Float> userRating() { return userRating; }

    public LiveData<String> userStatus() { return userStatus; }
    public LiveData<String> emojiReaction() { return emojiReaction; }

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }

    public void loadAll(int movieId) {
        loading.setValue(true);
        error.setValue(null);

        moviesRepo.details(movieId, "ru-RU", new MoviesRepository.RepoCallback<MovieDetailsDto>() {
            @Override public void onSuccess(MovieDetailsDto data) {
                details.postValue(data);
                loading.postValue(false);
            }
            @Override public void onError(String msg) {
                loading.postValue(false);
                error.postValue(msg);
            }
        });

        moviesRepo.credits(movieId, "ru-RU", new MoviesRepository.RepoCallback<CreditsResponseDto>() {
            @Override public void onSuccess(CreditsResponseDto data) { credits.postValue(data); }
            @Override public void onError(String msg) { }
        });

        favRepo.isFavorite(movieId, new FavoritesRepository.RepoCallback<Boolean>() {
            @Override public void onSuccess(Boolean data) { isFavorite.postValue(Boolean.TRUE.equals(data)); }
            @Override public void onError(String msg) { }
        });

        favRepo.getComment(movieId, new FavoritesRepository.RepoCallback<String>() {
            @Override public void onSuccess(String data) { comment.postValue(data != null ? data : ""); }
            @Override public void onError(String msg) { }
        });

        favRepo.getRating(movieId, new FavoritesRepository.RepoCallback<Float>() {
            @Override public void onSuccess(Float data) { userRating.postValue(data != null ? data : 0f); }
            @Override public void onError(String msg) { }
        });

        // NEW: status + emoji
        favRepo.getStatus(movieId, new FavoritesRepository.RepoCallback<String>() {
            @Override public void onSuccess(String data) {
                userStatus.postValue(data != null && !data.trim().isEmpty() ? data : STATUS_TO_WATCH);
            }
            @Override public void onError(String msg) { }
        });

        favRepo.getEmoji(movieId, new FavoritesRepository.RepoCallback<String>() {
            @Override public void onSuccess(String data) { emojiReaction.postValue(data != null ? data : ""); }
            @Override public void onError(String msg) { }
        });
    }

    public void toggleFavorite(int movieId, String typedComment, float typedRating) {
        MovieDetailsDto d = details.getValue();
        if (d == null) {
            error.postValue("Детали ещё не загрузились");
            return;
        }

        boolean currentlyFav = Boolean.TRUE.equals(isFavorite.getValue());

        if (currentlyFav) {
            isFavorite.postValue(false);

            favRepo.delete(movieId, new FavoritesRepository.RepoCallback<Void>() {
                @Override public void onSuccess(Void data) {
                    comment.postValue("");
                    userRating.postValue(0f);
                    userStatus.postValue(STATUS_TO_WATCH);
                    emojiReaction.postValue("");
                }
                @Override public void onError(String msg) {
                    error.postValue(msg);
                    isFavorite.postValue(true); // откат
                }
            });
        } else {
            isFavorite.postValue(true);

            FavoriteMovieEntity e = new FavoriteMovieEntity();
            e.id = d.id;
            e.title = d.title;
            e.posterPath = d.posterPath;
            e.releaseDate = d.releaseDate;
            e.voteAverage = d.voteAverage;
            e.overview = d.overview;

            e.comment = typedComment;
            e.userRating = typedRating;

            String st = userStatus.getValue();
            e.userStatus = (st == null || st.trim().isEmpty()) ? STATUS_TO_WATCH : st;
            String em = emojiReaction.getValue();
            e.emojiReaction = (em == null) ? "" : em;

            favRepo.insert(e, new FavoritesRepository.RepoCallback<Void>() {
                @Override public void onSuccess(Void data) {
                    comment.postValue(typedComment);
                    userRating.postValue(typedRating);
                }
                @Override public void onError(String msg) {
                    error.postValue(msg);
                    isFavorite.postValue(false); // откат
                }
            });
        }
    }

    public void saveComment(int movieId, String text) {
        if (!Boolean.TRUE.equals(isFavorite.getValue())) return;

        favRepo.updateComment(movieId, text, new FavoritesRepository.RepoCallback<Void>() {
            @Override public void onSuccess(Void data) { comment.postValue(text); }
            @Override public void onError(String msg) { error.postValue(msg); }
        });
    }

    public void saveRating(int movieId, float rating) {
        if (!Boolean.TRUE.equals(isFavorite.getValue())) return;

        favRepo.updateRating(movieId, rating, new FavoritesRepository.RepoCallback<Void>() {
            @Override public void onSuccess(Void data) { userRating.postValue(rating); }
            @Override public void onError(String msg) { error.postValue(msg); }
        });
    }

    // NEW: сохранить статус (работает только если уже в избранном)
    public void saveStatus(int movieId, String status) {
        userStatus.postValue(status);
        if (!Boolean.TRUE.equals(isFavorite.getValue())) return;

        favRepo.updateStatus(movieId, status, new FavoritesRepository.RepoCallback<Void>() {
            @Override public void onSuccess(Void data) { }
            @Override public void onError(String msg) { error.postValue(msg); }
        });
    }

    // NEW: сохранить эмодзи
    public void saveEmoji(int movieId, String emoji) {
        emojiReaction.postValue(emoji);
        if (!Boolean.TRUE.equals(isFavorite.getValue())) return;

        favRepo.updateEmoji(movieId, emoji, new FavoritesRepository.RepoCallback<Void>() {
            @Override public void onSuccess(Void data) { }
            @Override public void onError(String msg) { error.postValue(msg); }
        });
    }

    public void loadTrailerKey(int movieId) {
        moviesRepo.videos(movieId, "ru-RU", new MoviesRepository.RepoCallback<VideoListResponseDto>() {
            @Override public void onSuccess(VideoListResponseDto data) {
                if (data == null || data.results == null) {
                    error.postValue("Трейлер не найден");
                    return;
                }
                for (VideoDto v : data.results) {
                    if (v != null && v.key != null && v.site != null
                            && "YouTube".equalsIgnoreCase(v.site)
                            && v.key.trim().length() > 0) {
                        trailerKey.postValue(v.key.trim());
                        return;
                    }
                }
                error.postValue("Трейлер не найден");
            }

            @Override public void onError(String msg) { error.postValue(msg); }
        });
    }
}