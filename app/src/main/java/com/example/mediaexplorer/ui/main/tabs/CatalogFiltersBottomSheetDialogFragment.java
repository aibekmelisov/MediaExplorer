package com.example.mediaexplorer.ui.main.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.mediaexplorer.R;
import com.example.mediaexplorer.data.remote.dto.GenreDto;
import com.example.mediaexplorer.data.remote.dto.GenreListResponseDto;
import com.example.mediaexplorer.data.repository.MoviesRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CatalogFiltersBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private Spinner spinnerMode;
    private Spinner spinnerGenres;
    private Spinner spinnerSort;

    private View groupYear;
    private View groupGenres;

    private CheckBox checkYear;
    private NumberPicker pickerYear;

    private Button btnApply;
    private Button btnReset;

    private MainSharedViewModel sharedVm;

    private final List<GenreDto> genres = new ArrayList<>();

    public CatalogFiltersBottomSheetDialogFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bs_catalog_filters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        spinnerMode = v.findViewById(R.id.spinnerMode);
        spinnerGenres = v.findViewById(R.id.spinnerGenres);
        spinnerSort = v.findViewById(R.id.spinnerSort);

        groupYear = v.findViewById(R.id.groupYear);
        groupGenres = v.findViewById(R.id.groupGenres);

        checkYear = v.findViewById(R.id.checkYear);
        pickerYear = v.findViewById(R.id.pickerYear);

        btnApply = v.findViewById(R.id.btnApply);
        btnReset = v.findViewById(R.id.btnReset);

        sharedVm = new ViewModelProvider(requireActivity()).get(MainSharedViewModel.class);

        setupModeSpinner();
        setupSortSpinner();
        setupYearPicker();

        // Реакция на чекбокс года
        checkYear.setOnCheckedChangeListener((buttonView, isChecked) ->
                pickerYear.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        // Реакция на смену режима (чтобы скрывать год/жанры где не логично)
        spinnerMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateUiForMode(getModeValue());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        loadGenres();

        // Заполнить текущими значениями
        MainSharedViewModel.CatalogState cur = sharedVm.catalogState().getValue();
        if (cur != null) {
            selectMode(cur.mode);
            selectSort(cur.sortBy);

            if (cur.year != null) {
                checkYear.setChecked(true);
                pickerYear.setVisibility(View.VISIBLE);
                pickerYear.setValue(cur.year);
            } else {
                checkYear.setChecked(false);
                pickerYear.setVisibility(View.GONE);
            }
        } else {
            // дефолтно: без фильтра по году
            checkYear.setChecked(false);
            pickerYear.setVisibility(View.GONE);
        }

        // применяем скрытие полей согласно режиму
        updateUiForMode(getModeValue());

        btnApply.setOnClickListener(view -> apply());
        btnReset.setOnClickListener(view -> {
            sharedVm.resetCatalog();
            dismiss();
        });
    }

    private void setupModeSpinner() {
        String[] modes = new String[]{"Каталог (Discover)", "В кино (Now Playing)", "Скоро (Upcoming)"};
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, modes);
        spinnerMode.setAdapter(a);
    }

    private void setupSortSpinner() {
        String[] sorts = new String[]{"Популярность", "Рейтинг", "Новинки", "Старые"};
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, sorts);
        spinnerSort.setAdapter(a);
    }

    private void setupYearPicker() {
        pickerYear.setMinValue(1900);
        pickerYear.setMaxValue(2100);
        pickerYear.setWrapSelectorWheel(true);
        pickerYear.setValue(Calendar.getInstance().get(Calendar.YEAR));
    }

    private void updateUiForMode(String mode) {
        boolean isDiscover = MainSharedViewModel.MODE_DISCOVER.equals(mode);

        // Скрываем только год
        if (groupYear != null) groupYear.setVisibility(isDiscover ? View.VISIBLE : View.GONE);

        // Жанры оставляем ВСЕГДА (ничего не делаем)

        // если скрыли год — выключаем чекбокс
        if (!isDiscover) {
            checkYear.setChecked(false);
            pickerYear.setVisibility(View.GONE);
        }
    }

    private void selectMode(String mode) {
        if (MainSharedViewModel.MODE_NOW_PLAYING.equals(mode)) spinnerMode.setSelection(1);
        else if (MainSharedViewModel.MODE_UPCOMING.equals(mode)) spinnerMode.setSelection(2);
        else spinnerMode.setSelection(0);
    }

    private void selectSort(String sortBy) {
        if ("vote_average.desc".equals(sortBy)) spinnerSort.setSelection(1);
        else if ("primary_release_date.desc".equals(sortBy)) spinnerSort.setSelection(2);
        else if ("primary_release_date.asc".equals(sortBy)) spinnerSort.setSelection(3);
        else spinnerSort.setSelection(0);
    }

    private String getSortQuery() {
        int pos = spinnerSort.getSelectedItemPosition();
        if (pos == 1) return "vote_average.desc";
        if (pos == 2) return "primary_release_date.desc";
        if (pos == 3) return "primary_release_date.asc";
        return "popularity.desc";
    }

    private String getModeValue() {
        int pos = spinnerMode.getSelectedItemPosition();
        if (pos == 1) return MainSharedViewModel.MODE_NOW_PLAYING;
        if (pos == 2) return MainSharedViewModel.MODE_UPCOMING;
        return MainSharedViewModel.MODE_DISCOVER;
    }

    private void apply() {
        MainSharedViewModel.CatalogState s = new MainSharedViewModel.CatalogState();
        s.mode = getModeValue();
        s.sortBy = getSortQuery();

        // год (только если discover и чекбокс включен)
        if (MainSharedViewModel.MODE_DISCOVER.equals(s.mode) && checkYear.isChecked()) {
            int year = pickerYear.getValue();
            if (year < 1900 || year > 2100) {
                Toast.makeText(requireContext(), "Год 1900–2100", Toast.LENGTH_SHORT).show();
                return;
            }
            s.year = year;
        } else {
            s.year = null;
        }

        // жанр применяем всегда (для любых режимов)
        GenreDto g = (GenreDto) spinnerGenres.getSelectedItem();
        if (g != null && g.id != -1) s.genreId = g.id;
        else s.genreId = null;

        sharedVm.setCatalogState(s);
        dismiss();
    }

    private void loadGenres() {
        MoviesRepository repo = new MoviesRepository();
        repo.genres("ru-RU", new MoviesRepository.RepoCallback<GenreListResponseDto>() {
            @Override
            public void onSuccess(GenreListResponseDto data) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    genres.clear();

                    GenreDto all = new GenreDto();
                    all.id = -1;
                    all.name = "Все жанры";
                    genres.add(all);

                    if (data != null && data.genres != null) genres.addAll(data.genres);

                    ArrayAdapter<GenreDto> a = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, genres);
                    spinnerGenres.setAdapter(a);

                    // выставим выбранный жанр
                    MainSharedViewModel.CatalogState cur = sharedVm.catalogState().getValue();
                    if (cur != null && cur.genreId != null) {
                        for (int i = 0; i < genres.size(); i++) {
                            if (genres.get(i) != null && genres.get(i).id == cur.genreId) {
                                spinnerGenres.setSelection(i);
                                break;
                            }
                        }
                    } else {
                        spinnerGenres.setSelection(0);
                    }
                });
            }

            @Override
            public void onError(String message) {
                // жанры не критичны
            }
        });
    }
}