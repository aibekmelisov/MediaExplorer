package com.example.mediaexplorer.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieListResponseDto {
    public int page;

    @SerializedName("total_pages")
    public int totalPages;

    public List<MovieDto> results;
}
