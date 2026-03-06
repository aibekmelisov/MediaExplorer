package com.example.mediaexplorer.data.remote;

import com.example.mediaexplorer.data.remote.dto.CreditsResponseDto;
import com.example.mediaexplorer.data.remote.dto.GenreListResponseDto;
import com.example.mediaexplorer.data.remote.dto.MovieDetailsDto;
import com.example.mediaexplorer.data.remote.dto.MovieListResponseDto;
import com.example.mediaexplorer.data.remote.dto.VideoListResponseDto;
import com.example.mediaexplorer.data.remote.dto.PersonDetailsDto;
import com.example.mediaexplorer.data.remote.dto.PersonMovieCreditsDto;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmdbApiService {

    @GET("movie/popular")
    Call<MovieListResponseDto> getPopularMovies(
            @Query("api_key") String apiKey,      // ключ API
            @Query("page") int page,              // номер страницы
            @Query("language") String language    // язык ответа
    );

    @GET("movie/{id}")
    Call<MovieDetailsDto> getMovieDetails(
            @Path("id") int id,                   // id фильма
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("search/movie")
    Call<MovieListResponseDto> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query,         // поисковая строка
            @Query("page") int page,
            @Query("language") String language
    );

    @GET("movie/{id}/videos")
    Call<VideoListResponseDto> getMovieVideos(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("movie/{id}/credits")
    Call<CreditsResponseDto> getMovieCredits(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("discover/movie")
    Call<MovieListResponseDto> discoverMovies(
            @Query("api_key") String apiKey,
            @Query("page") int page,
            @Query("language") String language,
            @Query("sort_by") String sortBy,              // сортировка
            @Query("primary_release_year") Integer year,  // фильтр по году
            @Query("with_genres") String genresCsv        // жанры CSV
    );

    @GET("genre/movie/list")
    Call<GenreListResponseDto> getMovieGenres(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("person/{id}")
    Call<PersonDetailsDto> getPersonDetails(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("person/{id}/movie_credits")
    Call<PersonMovieCreditsDto> getPersonMovieCredits(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("movie/top_rated")
    Call<MovieListResponseDto> getTopRatedMovies(
            @Query("api_key") String apiKey,
            @Query("page") int page,
            @Query("language") String language
    );

    @GET("trending/movie/{time_window}")
    Call<MovieListResponseDto> getTrendingMovies(
            @Path("time_window") String timeWindow,
            @Query("api_key") String apiKey,
            @Query("page") int page,
            @Query("language") String language
    );

    @GET("movie/now_playing")
    Call<MovieListResponseDto> getNowPlayingMovies(
            @Query("api_key") String apiKey,
            @Query("page") int page,
            @Query("language") String language
    );

    @GET("movie/upcoming")
    Call<MovieListResponseDto> getUpcomingMovies(
            @Query("api_key") String apiKey,
            @Query("page") int page,
            @Query("language") String language
    );

    @GET("discover/movie")
    Call<MovieListResponseDto> discoverMovies(
            @Query("api_key") String apiKey,
            @Query("page") int page,
            @Query("language") String language,
            @Query("sort_by") String sortBy,
            @Query("primary_release_year") Integer year,
            @Query("with_genres") String genresCsv,
            @Query("primary_release_date.gte") String dateGte,
            @Query("primary_release_date.lte") String dateLte,
            @Query("vote_count.gte") Integer voteCountGte
    );




}
