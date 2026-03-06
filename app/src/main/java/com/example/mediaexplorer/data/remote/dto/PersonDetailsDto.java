package com.example.mediaexplorer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class PersonDetailsDto {
    public int id;
    public String name;

    @SerializedName("profile_path")
    public String profilePath;

    public String biography;

    @SerializedName("birthday")
    public String birthday;

    @SerializedName("place_of_birth")
    public String placeOfBirth;
}