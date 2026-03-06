package com.example.mediaexplorer.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.remote.dto.GenreDto;
import com.example.mediaexplorer.data.remote.dto.GenreListResponseDto;
import com.example.mediaexplorer.data.repository.MoviesRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    public static FilterBottomSheet newInstance() {
        return new FilterBottomSheet();
    }

    private MaterialAutoCompleteTextView ddYear;
    private MaterialAutoCompleteTextView ddRating;
    private ChipGroup chipGroupGenres;
    private ProgressBar progressGenres;
    private SearchViewModel vm;

    private final MoviesRepository repo = new MoviesRepository();
    private List<GenreDto> genres = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bottomsheet_filters, container, false);

        ddYear = v.findViewById(R.id.ddYear);
        ddRating = v.findViewById(R.id.ddRating);
        chipGroupGenres = v.findViewById(R.id.chipGroupGenres);
        progressGenres = v.findViewById(R.id.progressGenres);

        Button btnApply = v.findViewById(R.id.btnApply);
        Button btnReset = v.findViewById(R.id.btnReset);

        vm = new androidx.lifecycle.ViewModelProvider(requireActivity())
                .get(SearchViewModel.class);

        setupDropdowns();
        loadGenres();
        restoreCurrentFilters();


        btnApply.setOnClickListener(view -> applyFilters());
        btnReset.setOnClickListener(view -> resetFilters());

        return v;
    }

    private void setupDropdowns() {
        // YEAR dropdown
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        years.add("Любой");
        for (int y = currentYear; y >= 1950; y--) years.add(String.valueOf(y));

        ddYear.setSimpleItems(years.toArray(new String[0]));
        ddYear.setText("Любой", false);

        // RATING dropdown (0.0..10.0 шаг 0.5)
        List<String> ratings = new ArrayList<>();
        ratings.add("Любой");
        for (double r = 10.0; r >= 0.0; r -= 0.5) {
            ratings.add(String.format(java.util.Locale.US, "%.1f", r));
        }

        ddRating.setSimpleItems(ratings.toArray(new String[0]));
        ddRating.setText("Любой", false);
    }

    private void loadGenres() {
        progressGenres.setVisibility(View.VISIBLE);
        chipGroupGenres.setVisibility(View.GONE);

        repo.genres("ru-RU", new MoviesRepository.RepoCallback<GenreListResponseDto>() {
            @Override
            public void onSuccess(GenreListResponseDto data) {
                if (!isAdded()) return;

                progressGenres.setVisibility(View.GONE);

                genres = (data != null && data.genres != null) ? data.genres : new ArrayList<>();
                renderGenreChips(genres);

                chipGroupGenres.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                progressGenres.setVisibility(View.GONE);
                chipGroupGenres.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Жанры не загрузились: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderGenreChips(List<GenreDto> genres) {
        chipGroupGenres.removeAllViews();

        for (GenreDto g : genres) {
            Chip chip = new Chip(requireContext());
            chip.setText(g.name);
            chip.setCheckable(true);
            chip.setTag(g.id);
            chipGroupGenres.addView(chip);
        }
    }

    private void applyFilters() {
        // year
        Integer year = null;
        String yearStr = String.valueOf(ddYear.getText()).trim();
        if (!yearStr.isEmpty() && !"Любой".equalsIgnoreCase(yearStr)) {
            try { year = Integer.parseInt(yearStr); } catch (Exception ignored) {}
        }

        // min rating
        Double minRating = null;
        String ratingStr = String.valueOf(ddRating.getText()).trim();
        if (!ratingStr.isEmpty() && !"Любой".equalsIgnoreCase(ratingStr)) {
            try { minRating = Double.parseDouble(ratingStr); } catch (Exception ignored) {}
        }

        // selected genres
        List<Integer> genreIds = new ArrayList<>();
        for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
            View child = chipGroupGenres.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    Object tag = chip.getTag();
                    try { genreIds.add(Integer.parseInt(String.valueOf(tag))); } catch (Exception ignored) {}
                }
            }
        }

        ((SearchActivity) requireActivity()).applyFilters(year, minRating, genreIds);
        dismiss();
    }

    private void resetFilters() {
        ((SearchActivity) requireActivity()).resetFilters();
        dismiss();
    }

    private void restoreCurrentFilters() {

        // --- YEAR ---
        Integer year = vm.getFilterYear();
        if (year != null) {
            ddYear.setText(String.valueOf(year), false);
        } else {
            ddYear.setText("Любой", false);
        }

        // --- RATING ---
        Double rating = vm.getFilterMinRating();
        if (rating != null) {
            ddRating.setText(String.format(java.util.Locale.US, "%.1f", rating), false);
        } else {
            ddRating.setText("Любой", false);
        }

        // --- GENRES ---
        List<Integer> selectedGenres = vm.getFilterGenreIds();

        chipGroupGenres.post(() -> {
            for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
                View child = chipGroupGenres.getChildAt(i);
                if (child instanceof com.google.android.material.chip.Chip) {
                    com.google.android.material.chip.Chip chip =
                            (com.google.android.material.chip.Chip) child;

                    Object tag = chip.getTag();
                    try {
                        int id = Integer.parseInt(String.valueOf(tag));
                        chip.setChecked(selectedGenres.contains(id));
                    } catch (Exception ignored) {}
                }
            }
        });
    }
}