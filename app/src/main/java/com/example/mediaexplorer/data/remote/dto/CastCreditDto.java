package com.example.mediaexplorer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CastCreditDto {
    public int id; // movie id
    public String title;

    @SerializedName("poster_path")
    public String posterPath;

    @SerializedName("release_date")
    public String releaseDate;

    public String character;

    @SerializedName("vote_average")
    public float voteAverage;
}