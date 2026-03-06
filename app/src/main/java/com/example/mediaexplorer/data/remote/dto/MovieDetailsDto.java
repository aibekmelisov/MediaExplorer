package com.example.mediaexplorer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieDetailsDto {
    public int id;
    public String title;

    public String overview;

    @SerializedName("poster_path")
    public String posterPath;

    @SerializedName("release_date")
    public String releaseDate;

    @SerializedName("vote_average")
    public double voteAverage;

    public List<GenreDto> genres;

}
