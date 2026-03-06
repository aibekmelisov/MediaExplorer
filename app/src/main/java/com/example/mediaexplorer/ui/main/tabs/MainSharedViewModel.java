package com.example.mediaexplorer.ui.main.tabs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainSharedViewModel extends ViewModel {

    // TOP period
    public static final String PERIOD_ALL_TIME = "all_time";
    public static final String PERIOD_MONTH = "month";
    private final MutableLiveData<String> topPeriod = new MutableLiveData<>(PERIOD_ALL_TIME);

    // TRENDING window
    public static final String TRENDING_DAY = "day";
    public static final String TRENDING_WEEK = "week";
    private final MutableLiveData<String> trendingWindow = new MutableLiveData<>(TRENDING_WEEK);

    // CATALOG modes
    public static final String MODE_DISCOVER = "discover";
    public static final String MODE_NOW_PLAYING = "now_playing";
    public static final String MODE_UPCOMING = "upcoming";

    public static class CatalogState {
        public String mode = MODE_DISCOVER;   // discover / now_playing / upcoming
        public Integer year = null;           // фильтр
        public Integer genreId = null;        // один жанр
        public String sortBy = "popularity.desc";
    }

    private final MutableLiveData<CatalogState> catalogState = new MutableLiveData<>(new CatalogState());

    public LiveData<String> topPeriod() { return topPeriod; }
    public void setTopPeriod(String period) {
        if (period == null) return;
        topPeriod.setValue(period);
    }

    public LiveData<String> trendingWindow() { return trendingWindow; }
    public void setTrendingWindow(String window) {
        if (window == null) return;
        trendingWindow.setValue(window);
    }

    public LiveData<CatalogState> catalogState() { return catalogState; }
    public void setCatalogState(CatalogState state) {
        if (state == null) return;
        catalogState.setValue(state);
    }

    public void resetCatalog() {
        catalogState.setValue(new CatalogState());
    }
}