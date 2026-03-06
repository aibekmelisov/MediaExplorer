package com.example.mediaexplorer.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mediaexplorer.data.remote.dto.MovieListResponseDto;
import com.example.mediaexplorer.data.repository.MoviesRepository;

public class MainViewModel extends ViewModel {

    private final MoviesRepository repo = new MoviesRepository();

    private final MutableLiveData<MovieListResponseDto> pageData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private boolean filterMode = false;
    private Integer year = null;
    private String genresCsv = null;

    // NEW: sort
    private String sortBy = "popularity.desc";

    public LiveData<MovieListResponseDto> pageData() { return pageData; }
    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }

    public void setFilter(Integer year, String genresCsv) {
        this.year = year;
        this.genresCsv = genresCsv;
        this.filterMode = (year != null) || (genresCsv != null);
    }

    public void clearFilter() {
        this.year = null;
        this.genresCsv = null;
        this.filterMode = false;
    }

    // NEW
    public void setSortBy(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) this.sortBy = "popularity.desc";
        else this.sortBy = sortBy;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void loadPage(int page) {
        loading.setValue(true);
        error.setValue(null);

        MoviesRepository.RepoCallback<MovieListResponseDto> cb = new MoviesRepository.RepoCallback<MovieListResponseDto>() {
            @Override
            public void onSuccess(MovieListResponseDto data) {
                loading.postValue(false);
                pageData.postValue(data);
            }

            @Override
            public void onError(String msg) {
                loading.postValue(false);
                error.postValue(msg);
            }
        };


        repo.discover(year, genresCsv, sortBy, page, "ru-RU", cb);
    }
}