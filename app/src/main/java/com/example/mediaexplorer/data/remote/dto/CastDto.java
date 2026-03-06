package com.example.mediaexplorer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CastDto {
    public int id; // person_id


    public String name;
    public String character;

    @SerializedName("profile_path")
    public String profilePath;
}
