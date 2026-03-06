package com.example.mediaexplorer.ui.main.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediaexplorer.R;
import com.example.mediaexplorer.ui.details.DetailsActivity;
import com.example.mediaexplorer.ui.main.MovieAdapter;

import java.util.ArrayList;

public class MoviesListFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";
    private static final String ARG_TRENDING_WINDOW = "trending_window"; // day/week

    public static MoviesListFragment newInstance(int category, @Nullable String trendingWindow) {
        MoviesListFragment f = new MoviesListFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_CATEGORY, category);
        if (trendingWindow != null) b.putString(ARG_TRENDING_WINDOW, trendingWindow);
        f.setArguments(b);
        return f;
    }

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView txtError;

    private MovieAdapter adapter;
    private MoviesTabViewModel vm;

    private int category;
    private String trendingWindow;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private MainSharedViewModel sharedVm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movies_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        progressBar = v.findViewById(R.id.progressBar);
        recyclerView = v.findViewById(R.id.recyclerView);
        txtError = v.findViewById(R.id.txtError);

        Bundle args = getArguments();
        category = args != null ? args.getInt(ARG_CATEGORY, MoviesTabViewModel.CAT_POPULAR) : MoviesTabViewModel.CAT_POPULAR;
        trendingWindow = args != null ? args.getString(ARG_TRENDING_WINDOW, "week") : "week";

        adapter = new MovieAdapter(movie -> {
            Intent i = new Intent(requireContext(), DetailsActivity.class);
            i.putExtra(DetailsActivity.EXTRA_MOVIE_ID, movie.id);
            startActivity(i);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (!rv.canScrollVertically(1)) {
                    if (!isLoading && !isLastPage) {
                        loadPage(currentPage + 1);
                    }
                }
            }
        });

        vm = new ViewModelProvider(this).get(MoviesTabViewModel.class);
        vm.init(category, trendingWindow);
        sharedVm = new ViewModelProvider(requireActivity()).get(MainSharedViewModel.class);

        sharedVm.topPeriod().observe(getViewLifecycleOwner(), period -> {
            // реагируем только если это вкладка "Топ"
            if (category != MoviesTabViewModel.CAT_TOP_RATED) return;

            vm.setTopPeriod(period);
            restartPagingAndLoad();
        });

        sharedVm.trendingWindow().observe(getViewLifecycleOwner(), window -> {
            if (category != MoviesTabViewModel.CAT_TRENDING) return;

            vm.setTrendingWindow(window);
            restartPagingAndLoad();
        });

        sharedVm.catalogState().observe(getViewLifecycleOwner(), state -> {
            if (category != MoviesTabViewModel.CAT_CATALOG) return;
            vm.setCatalogState(state);
            restartPagingAndLoad();
        });

        vm.loading().observe(getViewLifecycleOwner(), l -> {
            isLoading = Boolean.TRUE.equals(l);
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        vm.error().observe(getViewLifecycleOwner(), err -> {
            if (err == null || err.trim().isEmpty()) {
                txtError.setVisibility(View.GONE);
            } else {
                txtError.setVisibility(View.VISIBLE);
                txtError.setText(err);
                Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
            }
        });

        vm.pageData().observe(getViewLifecycleOwner(), data -> {
            if (data == null || data.results == null) return;

            if (vm.getRequestedPage() == 1) adapter.setItems(data.results);
            else adapter.addItems(data.results);

            currentPage = vm.getRequestedPage();

            if (data.totalPages > 0 && currentPage >= data.totalPages) isLastPage = true;

            if (currentPage == 1 && data.results.isEmpty()) {
                txtError.setVisibility(View.VISIBLE);
                txtError.setText("Ничего не найдено");
            }
        });


        // first load
        restartPagingAndLoad();
    }

    private void restartPagingAndLoad() {
        currentPage = 1;
        isLastPage = false;
        adapter.setItems(new ArrayList<>());
        loadPage(1);
    }

    private void loadPage(int page) {
        vm.loadPage(page);
    }
}