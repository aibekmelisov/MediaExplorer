package com.example.mediaexplorer.ui.main.tabs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mediaexplorer.data.remote.dto.MovieListResponseDto;
import com.example.mediaexplorer.data.repository.MoviesRepository;
import com.example.mediaexplorer.utils.DateUtils;

public class MoviesTabViewModel extends ViewModel {

    public static final int CAT_POPULAR = 1;
    public static final int CAT_TOP_RATED = 2;
    public static final int CAT_TRENDING = 3;
    public static final int CAT_CATALOG = 6;
    public static final int CAT_UPCOMING = 5;

    private final MoviesRepository repo = new MoviesRepository();

    private final MutableLiveData<MovieListResponseDto> pageData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private int category = CAT_POPULAR;
    private String trendingWindow = "week";

    private int requestedPage = 1;

    // TOP period
    private String topPeriod = MainSharedViewModel.PERIOD_ALL_TIME;

    // CATALOG state
    private MainSharedViewModel.CatalogState catalogState = new MainSharedViewModel.CatalogState();

    public LiveData<MovieListResponseDto> pageData() { return pageData; }
    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }

    public int getRequestedPage() { return requestedPage; }

    public void init(int category, String trendingWindow) {
        this.category = category;
        this.trendingWindow = (trendingWindow == null || trendingWindow.isEmpty()) ? "week" : trendingWindow;
    }

    public void setTopPeriod(String period) {
        if (period == null) return;
        this.topPeriod = period;
    }

    public void setTrendingWindow(String window) {
        if (window == null || window.trim().isEmpty()) return;
        this.trendingWindow = window;
    }

    public void setCatalogState(MainSharedViewModel.CatalogState state) {
        if (state == null) return;
        this.catalogState = state;
    }

    public void loadPage(int page) {
        requestedPage = page;
        loading.setValue(true);
        error.setValue(null);

        MoviesRepository.RepoCallback<MovieListResponseDto> cb = new MoviesRepository.RepoCallback<MovieListResponseDto>() {
            @Override
            public void onSuccess(MovieListResponseDto data) {
                loading.postValue(false);
                pageData.postValue(data);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        };

        String lang = "ru-RU";

        if (category == CAT_POPULAR) {
            repo.getPopular(page, lang, cb);
            return;
        }

        if (category == CAT_TOP_RATED) {
            if (MainSharedViewModel.PERIOD_MONTH.equals(topPeriod)) {
                String[] range = DateUtils.last30DaysRange();
                repo.bestOfMonth(range[0], range[1], page, lang, cb);
            } else {
                repo.getTopRated(page, lang, cb);
            }
            return;
        }

        if (category == CAT_TRENDING) {
            repo.getTrending(trendingWindow, page, lang, cb);
            return;
        }

        if (category == CAT_CATALOG) {

            Integer year = catalogState.year;
            String genresCsv = catalogState.genreId != null
                    ? String.valueOf(catalogState.genreId)
                    : null;
            String sortBy = catalogState.sortBy;

            // В КИНО
            if (MainSharedViewModel.MODE_NOW_PLAYING.equals(catalogState.mode)) {

                String[] range = DateUtils.nowPlayingRange();

                repo.discoverAdvanced(
                        year,
                        genresCsv,
                        sortBy,
                        range[0],
                        range[1],
                        50,              // минимум голосов
                        page,
                        lang,
                        cb
                );
                return;
            }

            // СКОРО
            if (MainSharedViewModel.MODE_UPCOMING.equals(catalogState.mode)) {

                String[] range = DateUtils.upcoming90DaysRange();

                repo.discoverAdvanced(
                        year,
                        genresCsv,
                        sortBy,
                        range[0],
                        range[1],
                        50,
                        page,
                        lang,
                        cb
                );
                return;
            }

            // ОБЫЧНЫЙ DISCOVER
            repo.discover(year, genresCsv, sortBy, page, lang, cb);
            return;
        }

        if (category == CAT_UPCOMING) {
            repo.getUpcoming(page, lang, cb);
            return;
        }

        repo.getPopular(page, lang, cb);
    }
}