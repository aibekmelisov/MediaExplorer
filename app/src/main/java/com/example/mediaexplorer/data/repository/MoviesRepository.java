package com.example.mediaexplorer.data.repository;

import androidx.annotation.NonNull;

import com.example.mediaexplorer.data.remote.RetrofitClient;
import com.example.mediaexplorer.data.remote.TmdbApiService;
import com.example.mediaexplorer.data.remote.dto.CreditsResponseDto;
import com.example.mediaexplorer.data.remote.dto.GenreListResponseDto;
import com.example.mediaexplorer.data.remote.dto.MovieDetailsDto;
import com.example.mediaexplorer.data.remote.dto.MovieListResponseDto;
import com.example.mediaexplorer.data.remote.dto.PersonDetailsDto;
import com.example.mediaexplorer.data.remote.dto.PersonMovieCreditsDto;
import com.example.mediaexplorer.data.remote.dto.VideoListResponseDto;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoviesRepository {

    // интерфейс обратного вызова
    public interface RepoCallback<T> {
        void onSuccess(T data);          // успешный результат
        void onError(String message);    // сообщение об ошибке
    }

    private final TmdbApiService api;   // API сервис

    // ссылка на активный поисковый запрос
    private final AtomicReference<Call<MovieListResponseDto>> activeSearchCall =
            new AtomicReference<>(null);

    public MoviesRepository() {
        api = RetrofitClient.getApiService(); // получение API клиента
    }

    // получение списка популярных фильмов
    public void getPopular(int page, String lang,
                           RepoCallback<MovieListResponseDto> cb) {

        api.getPopularMovies(
                RetrofitClient.apiKey(),
                page,
                lang
        ).enqueue(wrap(cb)); // асинхронный запрос
    }

    // поиск фильмов
    public void search(String query,
                       int page,
                       String lang,
                       RepoCallback<MovieListResponseDto> cb) {

        cancelSearch(); // отмена предыдущего поиска

        Call<MovieListResponseDto> call =
                api.searchMovies(RetrofitClient.apiKey(), query, page, lang);

        activeSearchCall.set(call); // сохраняем активный запрос

        call.enqueue(new Callback<MovieListResponseDto>() {

            @Override
            public void onResponse(@NonNull Call<MovieListResponseDto> c,
                                   @NonNull Response<MovieListResponseDto> response) {

                if (c.isCanceled()) return; // запрос отменён

                if (response.isSuccessful() && response.body() != null)
                    cb.onSuccess(response.body());
                else
                    cb.onError("API error: code=" + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<MovieListResponseDto> c,
                                  @NonNull Throwable t) {

                if (c.isCanceled()) return;

                cb.onError("Network: " + t.getMessage());
            }
        });
    }

    // фильтрация фильмов через discover
    public void discover(Integer year,
                         String genresCsv,
                         String sortBy,
                         int page,
                         String lang,
                         RepoCallback<MovieListResponseDto> cb) {

        String safeSort =
                (sortBy == null || sortBy.trim().isEmpty())
                        ? "popularity.desc"
                        : sortBy;

        api.discoverMovies(
                RetrofitClient.apiKey(),
                page,
                lang,
                safeSort,
                year,
                genresCsv
        ).enqueue(wrap(cb)); // отправка запроса
    }

    // получение деталей фильма
    public void details(int id,
                        String lang,
                        RepoCallback<MovieDetailsDto> cb) {

        api.getMovieDetails(
                id,
                RetrofitClient.apiKey(),
                lang
        ).enqueue(wrap(cb));
    }

    // получение актёрского состава
    public void credits(int id,
                        String lang,
                        RepoCallback<CreditsResponseDto> cb) {

        api.getMovieCredits(
                id,
                RetrofitClient.apiKey(),
                lang
        ).enqueue(wrap(cb));
    }

    public void videos(int id, String lang, RepoCallback<VideoListResponseDto> cb) {
        api.getMovieVideos(id, RetrofitClient.apiKey(), lang).enqueue(wrap(cb));
    }

    public void genres(String lang, RepoCallback<GenreListResponseDto> cb) {
        api.getMovieGenres(RetrofitClient.apiKey(), lang).enqueue(wrap(cb));
    }

    private <T> Callback<T> wrap(RepoCallback<T> cb) {
        return new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (response.isSuccessful() && response.body() != null) cb.onSuccess(response.body());
                else cb.onError("API error: code=" + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                cb.onError("Network: " + t.getMessage());
            }
        };
    }


    public void personDetails(int personId, String lang, RepoCallback<PersonDetailsDto> cb) {
        api.getPersonDetails(personId, RetrofitClient.apiKey(), lang).enqueue(wrap(cb));
    }

    public void personMovieCredits(int personId, String lang, RepoCallback<PersonMovieCreditsDto> cb) {
        api.getPersonMovieCredits(personId, RetrofitClient.apiKey(), lang).enqueue(wrap(cb));
    }

    public void getTopRated(int page, String lang, RepoCallback<MovieListResponseDto> cb) {
        api.getTopRatedMovies(RetrofitClient.apiKey(), page, lang).enqueue(wrap(cb));
    }

    public void getTrending(String timeWindow, int page, String lang, RepoCallback<MovieListResponseDto> cb) {
        api.getTrendingMovies(timeWindow, RetrofitClient.apiKey(), page, lang).enqueue(wrap(cb));
    }

    public void getNowPlaying(int page, String lang, RepoCallback<MovieListResponseDto> cb) {
        api.getNowPlayingMovies(RetrofitClient.apiKey(), page, lang).enqueue(wrap(cb));
    }

    public void getUpcoming(int page, String lang, RepoCallback<MovieListResponseDto> cb) {
        api.getUpcomingMovies(RetrofitClient.apiKey(), page, lang).enqueue(wrap(cb));
    }

    public void bestOfMonth(String dateGte, String dateLte, int page, String lang,
                            RepoCallback<MovieListResponseDto> cb) {
        api.discoverMovies(
                RetrofitClient.apiKey(),
                page,
                lang,
                "vote_average.desc",
                null,
                null,
                dateGte,
                dateLte,
                200
        ).enqueue(wrap(cb));
    }

    public void discoverAdvanced(Integer year,
                                 String genresCsv,
                                 String sortBy,
                                 String dateGte,
                                 String dateLte,
                                 Integer voteCountGte,
                                 int page,
                                 String lang,
                                 RepoCallback<MovieListResponseDto> cb) {

        String safeSort = (sortBy == null || sortBy.trim().isEmpty())
                ? "popularity.desc"
                : sortBy;

        api.discoverMovies(
                RetrofitClient.apiKey(),
                page,
                lang,
                safeSort,
                year,
                genresCsv,
                dateGte,
                dateLte,
                voteCountGte
        ).enqueue(wrap(cb));
    }

    public void cancelSearch() {
        Call<MovieListResponseDto> prev = activeSearchCall.getAndSet(null);
        if (prev != null && !prev.isCanceled()) prev.cancel();
    }
}
