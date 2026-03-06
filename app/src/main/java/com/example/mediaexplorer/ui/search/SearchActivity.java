package com.example.mediaexplorer.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.local.SearchHistoryEntity;
import com.example.mediaexplorer.data.repository.SearchHistoryRepository;
import com.example.mediaexplorer.ui.details.DetailsActivity;
import com.example.mediaexplorer.ui.main.MovieAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText edtSearch;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtError;

    private RecyclerView rvHistory;
    private SearchHistoryAdapter historyAdapter;
    private SearchHistoryRepository historyRepo;

    private MovieAdapter adapter;
    private SearchViewModel vm;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = true;

    // debounce
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        edtSearch = findViewById(R.id.edtSearch);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        txtError = findViewById(R.id.txtError);
        rvHistory = findViewById(R.id.rvHistory);

        vm = new ViewModelProvider(this).get(SearchViewModel.class);

        historyRepo = new SearchHistoryRepository(getApplicationContext());
        historyAdapter = new SearchHistoryAdapter(query -> {
            edtSearch.setText(query);
            edtSearch.setSelection(query.length());
            startNewSearch(query);
        });

        rvHistory.setLayoutManager(new GridLayoutManager(this, 1));
        rvHistory.setAdapter(historyAdapter);

        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int pos = viewHolder.getAdapterPosition();
                        if (pos < 0) return;

                        SearchHistoryEntity item = historyAdapter.getItem(pos);
                        if (item != null) historyRepo.deleteQuery(item.query);

                        historyAdapter.removeAt(pos);
                        if (historyAdapter.getItemCount() == 0) rvHistory.setVisibility(View.GONE);
                    }
                }
        );
        helper.attachToRecyclerView(rvHistory);

        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.menu_search);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_clear_history) {
                historyRepo.clearAll();
                historyAdapter.setItems(new ArrayList<>());
                rvHistory.setVisibility(View.GONE);

                if (edtSearch.getText() == null || edtSearch.getText().toString().trim().isEmpty()) {
                    showEmptyState();
                }
                return true;
            }

            if (id == R.id.action_filters) {

                String q = vm.getQuery();

                if (q == null || q.trim().isEmpty()) {
                    showHint("Сначала введите запрос");
                    return true;
                }

                FilterBottomSheet.newInstance()
                        .show(getSupportFragmentManager(), "filters");

                return true;
            }

            return false;
        });

        adapter = new MovieAdapter(movie -> {
            Intent intent = new Intent(SearchActivity.this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_MOVIE_ID, movie.id);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                if (!rv.canScrollVertically(1)) {
                    if (!isLoading && !isLastPage) {
                        requestPage(currentPage + 1);
                    }
                }
            }
        });

        vm.loading().observe(this, loading -> {
            isLoading = Boolean.TRUE.equals(loading);
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        vm.error().observe(this, err -> {
            if (err == null || err.trim().isEmpty()) {
                if (txtError.getTag() == null || !"empty".equals(txtError.getTag())) {
                    txtError.setVisibility(View.GONE);
                }
            } else {
                txtError.setTag(null);
                txtError.setVisibility(View.VISIBLE);
                txtError.setText(err);
            }
        });

        vm.pageData().observe(this, data -> {
            if (data == null || data.results == null) return;
            adapter.setItems(data.results);

            if (data.results.isEmpty()) showHint("Ничего не найдено");
        });

        showEmptyState();
        loadHistory();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cancelPendingSearch();

                String q = (s == null) ? "" : s.toString().trim();

                if (q.isEmpty()) {
                    clearResultsAndEmptyState();
                    return;
                }

                if (q.length() < 2) {
                    adapter.setItems(new ArrayList<>());
                    showHint("Введите минимум 2 символа");
                    isLastPage = true;
                    rvHistory.setVisibility(View.GONE);
                    return;
                }

                searchRunnable = () -> startNewSearch(q);
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                cancelPendingSearch();

                String q = v.getText() == null ? "" : v.getText().toString().trim();

                if (q.isEmpty()) {
                    clearResultsAndEmptyState();
                    return true;
                }

                if (q.length() < 2) {
                    adapter.setItems(new ArrayList<>());
                    showHint("Введите минимум 2 символа");
                    isLastPage = true;
                    rvHistory.setVisibility(View.GONE);
                    return true;
                }

                startNewSearch(q);
                return true;
            }
            return false;
        });
    }

    private void startNewSearch(String q) {
        vm.setQuery(q);
        vm.newSearchSession();

        currentPage = 1;
        isLastPage = false;

        adapter.setItems(new ArrayList<>());
        txtError.setTag(null);
        txtError.setVisibility(View.GONE);

        historyRepo.saveQuery(q);
        loadHistory();

        rvHistory.setVisibility(View.GONE);
        requestPage(1);
    }

    private void requestPage(int page) {
        currentPage = page;
        vm.searchPage(page);
    }

    private void cancelPendingSearch() {
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
    }

    private void clearResultsAndEmptyState() {
        vm.setQuery("");
        vm.newSearchSession();

        adapter.setItems(new ArrayList<>());
        currentPage = 1;
        isLastPage = true;

        showEmptyState();
        loadHistory();
    }

    private void showEmptyState() {
        txtError.setTag("empty");
        txtError.setVisibility(View.VISIBLE);
        txtError.setText("Начните вводить название фильма");
    }

    private void showHint(String text) {
        txtError.setTag(null);
        txtError.setVisibility(View.VISIBLE);
        txtError.setText(text);
    }

    private void loadHistory() {
        historyRepo.loadLatest(10, new SearchHistoryRepository.Callback<List<SearchHistoryEntity>>() {
            @Override
            public void onSuccess(List<SearchHistoryEntity> data) {
                runOnUiThread(() -> {
                    historyAdapter.setItems(data);

                    String q = vm.getQuery();
                    boolean show = (q == null || q.trim().isEmpty());

                    rvHistory.setVisibility(
                            show && data != null && !data.isEmpty() ? View.VISIBLE : View.GONE
                    );
                });
            }

            @Override
            public void onError(String msg) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelPendingSearch();
    }

    // ✅ BottomSheet вызывает это
    public void applyFilters(Integer year, Double minRating, List<Integer> genreIds) {
        vm.setLocalFilters(year, minRating, genreIds);
        recyclerView.scrollToPosition(0);
    }

    public void resetFilters() {
        vm.clearLocalFilters();
        recyclerView.scrollToPosition(0);

        if (edtSearch.getText() == null || edtSearch.getText().toString().trim().isEmpty()) {
            showEmptyState();
            loadHistory();
        }
    }
}