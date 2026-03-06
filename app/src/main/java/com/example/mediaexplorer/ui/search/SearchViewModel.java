package com.example.mediaexplorer.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mediaexplorer.data.remote.dto.MovieDto;
import com.example.mediaexplorer.data.remote.dto.MovieListResponseDto;
import com.example.mediaexplorer.data.repository.MoviesRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {

    private final MoviesRepository repo = new MoviesRepository();

    private final MutableLiveData<MovieListResponseDto> pageData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private String query = "";
    private int requestId = 0;

    private final List<MovieDto> allLoadedResults = new ArrayList<>();

    private Integer filterYear = null;
    private Double filterMinRating = null;
    private List<Integer> filterGenreIds = new ArrayList<>();

    public LiveData<MovieListResponseDto> pageData() { return pageData; }
    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }

    public void setQuery(String q) { query = (q == null) ? "" : q.trim(); }
    public String getQuery() { return query; }

    public void newSearchSession() {
        requestId++;
        repo.cancelSearch();
        error.setValue(null);
        allLoadedResults.clear();
    }

    public void setLocalFilters(Integer year, Double minRating, List<Integer> genreIds) {
        filterYear = year;
        filterMinRating = minRating;

        filterGenreIds = new ArrayList<>();
        if (genreIds != null) filterGenreIds.addAll(genreIds);

        publishFiltered();
    }

    public void clearLocalFilters() {
        filterYear = null;
        filterMinRating = null;
        filterGenreIds.clear();
        publishAll();
    }

    private boolean hasLocalFilters() {
        return filterYear != null
                || filterMinRating != null
                || (filterGenreIds != null && !filterGenreIds.isEmpty());
    }

    public void searchPage(int page) {
        final int myId = requestId;

        if (query == null || query.trim().isEmpty()) {
            loading.setValue(false);
            return;
        }

        loading.setValue(true);
        error.setValue(null);

        repo.search(query, page, "ru-RU", new MoviesRepository.RepoCallback<MovieListResponseDto>() {
            @Override
            public void onSuccess(MovieListResponseDto data) {
                if (myId != requestId) return;

                loading.postValue(false);

                if (data != null && data.results != null) {
                    allLoadedResults.addAll(data.results);
                }

                if (hasLocalFilters()) publishFiltered();
                else publishAll();
            }

            @Override
            public void onError(String msg) {
                if (myId != requestId) return;
                loading.postValue(false);
                error.postValue(msg);
            }
        });
    }

    private void publishAll() {
        MovieListResponseDto dto = new MovieListResponseDto();
        dto.results = new ArrayList<>(allLoadedResults);
        pageData.postValue(dto);
    }

    private void publishFiltered() {
        List<MovieDto> filtered = new ArrayList<>();

        for (MovieDto m : allLoadedResults) {
            boolean match = true;

            if (filterYear != null) {
                if (m.releaseDate == null || m.releaseDate.length() < 4) {
                    match = false;
                } else {
                    try {
                        int movieYear = Integer.parseInt(m.releaseDate.substring(0, 4));
                        if (movieYear != filterYear) match = false;
                    } catch (Exception e) {
                        match = false;
                    }
                }
            }

            if (match && filterMinRating != null) {
                if (m.voteAverage < filterMinRating) match = false;
            }

            if (match && filterGenreIds != null && !filterGenreIds.isEmpty()) {
                boolean has = false;

                if (m.genreIds != null) {
                    for (Integer g : filterGenreIds) {
                        if (m.genreIds.contains(g)) {
                            has = true;
                            break;
                        }
                    }
                }

                if (!has) match = false;
            }

            if (match) filtered.add(m);
        }

        MovieListResponseDto dto = new MovieListResponseDto();
        dto.results = filtered;
        pageData.postValue(dto);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repo.cancelSearch();
    }

    public Integer getFilterYear() {
        return filterYear;
    }

    public Double getFilterMinRating() {
        return filterMinRating;
    }

    public List<Integer> getFilterGenreIds() {
        return new ArrayList<>(filterGenreIds);
    }
}