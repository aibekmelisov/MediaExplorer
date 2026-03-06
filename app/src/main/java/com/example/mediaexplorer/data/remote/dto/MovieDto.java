package com.example.mediaexplorer.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieDto {
    public int id;
    public String title;

    @SerializedName("original_title")
    public String originalTitle;

    public String overview;

    @SerializedName("poster_path")
    public String posterPath;

    @SerializedName("release_date")
    public String releaseDate;

    @SerializedName("vote_average")
    public double voteAverage;

    @SerializedName("genre_ids")
    public List<Integer> genreIds;
}