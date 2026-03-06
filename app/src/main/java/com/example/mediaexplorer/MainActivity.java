package com.example.mediaexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mediaexplorer.ui.favorites.FavoritesActivity;
import com.example.mediaexplorer.ui.main.tabs.MainPagerAdapter;
import com.example.mediaexplorer.ui.main.tabs.MainSharedViewModel;
import com.example.mediaexplorer.ui.search.SearchActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private FloatingActionButton fabFavorites;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private Menu topMenu;
    private MainSharedViewModel sharedVm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // подключение layout

        toolbar = findViewById(R.id.toolbar);             // верхняя панель
        fabFavorites = findViewById(R.id.fabFavorites);   // кнопка избранного
        tabLayout = findViewById(R.id.tabLayout);         // вкладки
        viewPager = findViewById(R.id.viewPager);         // контейнер фрагментов

        setSupportActionBar(toolbar); // привязка toolbar как ActionBar

        toolbar.setOnMenuItemClickListener(this::onToolbarMenuClick);
        // обработка кликов меню

        sharedVm = new ViewModelProvider(this)
                .get(MainSharedViewModel.class); // общий ViewModel

        fabFavorites.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this,
                        FavoritesActivity.class)));
        // переход в избранное

        viewPager.setAdapter(new MainPagerAdapter(this)); // адаптер вкладок
        viewPager.setOffscreenPageLimit(1); // хранение соседнего фрагмента

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Популярное"); break;
                case 1: tab.setText("Топ"); break;
                case 2: tab.setText("Тренды"); break;
                case 3: tab.setText("Скоро"); break;
                case 4: tab.setText("Каталог"); break;
            }
        }).attach(); // связывание вкладок с ViewPager

        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);

                        applyMenuVisibility(position); // обновление пунктов меню
                        invalidateOptionsMenu(); // принудительная перерисовка меню
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Подключаем меню верхней панели (toolbar)
        getMenuInflater().inflate(R.menu.menu_main_top, menu);

        // Сохраняем ссылку на меню
        topMenu = menu;

        // Настраиваем видимость пунктов меню в зависимости от текущей вкладки
        applyMenuVisibility(viewPager.getCurrentItem());

        return true;
    }

    private boolean onToolbarMenuClick(@NonNull MenuItem item) {

        int id = item.getItemId();

        // Открытие экрана поиска
        if (id == R.id.action_search) {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
            return true;
        }

        // Выбор периода (например: день / неделя для трендов)
        if (id == R.id.action_period) {
            showPeriodMenuForCurrentTab();
            return true;
        }

        // Открытие фильтров каталога
        if (id == R.id.action_filter) {
            if (viewPager.getCurrentItem() == 4) { // вкладка "Каталог"
                new com.example.mediaexplorer.ui.main.tabs.CatalogFiltersBottomSheetDialogFragment()
                        .show(getSupportFragmentManager(), "catalog_filters");
            }
            return true;
        }

        // Открытие меню сортировки каталога
        if (id == R.id.action_sort) {
            if (viewPager.getCurrentItem() == 4) { // вкладка "Каталог"
                showCatalogSortMenu();
            }
            return true;
        }

        return false;
    }

    private void applyMenuVisibility(int tab) {
        if (topMenu == null) return;

        MenuItem period = topMenu.findItem(R.id.action_period);
        MenuItem sort = topMenu.findItem(R.id.action_sort);
        MenuItem filter = topMenu.findItem(R.id.action_filter);

        boolean isTop = (tab == 1);
        boolean isTrending = (tab == 2);
        boolean isCatalog = (tab == 4);

        if (period != null) period.setVisible(isTop || isTrending);
        if (sort != null) sort.setVisible(isCatalog);
        if (filter != null) filter.setVisible(isCatalog);
    }

    private void showPeriodMenuForCurrentTab() {
        int tab = viewPager.getCurrentItem();

        // безопасность: если вдруг кнопку не скрыли
        if (tab != 1 && tab != 2) return;

        PopupMenu pm = new PopupMenu(this, toolbar);

        if (tab == 1) {
            // TOP
            pm.getMenuInflater().inflate(R.menu.menu_period_top, pm.getMenu());

            String cur = sharedVm.topPeriod().getValue();
            if (MainSharedViewModel.PERIOD_MONTH.equals(cur)) {
                MenuItem mi = pm.getMenu().findItem(R.id.period_month);
                if (mi != null) mi.setChecked(true);
            } else {
                MenuItem mi = pm.getMenu().findItem(R.id.period_all_time);
                if (mi != null) mi.setChecked(true);
            }

        } else {
            // TRENDING
            pm.getMenuInflater().inflate(R.menu.menu_period_trending, pm.getMenu());

            String cur = sharedVm.trendingWindow().getValue();
            if (MainSharedViewModel.TRENDING_DAY.equals(cur)) {
                MenuItem mi = pm.getMenu().findItem(R.id.trending_day);
                if (mi != null) mi.setChecked(true);
            } else {
                MenuItem mi = pm.getMenu().findItem(R.id.trending_week);
                if (mi != null) mi.setChecked(true);
            }
        }

        pm.setOnMenuItemClickListener(item -> {
            int iid = item.getItemId();

            // TOP
            if (iid == R.id.period_all_time) {
                sharedVm.setTopPeriod(MainSharedViewModel.PERIOD_ALL_TIME);
                return true;
            }
            if (iid == R.id.period_month) {
                sharedVm.setTopPeriod(MainSharedViewModel.PERIOD_MONTH);
                return true;
            }

            // TRENDING
            if (iid == R.id.trending_day) {
                sharedVm.setTrendingWindow(MainSharedViewModel.TRENDING_DAY);
                return true;
            }
            if (iid == R.id.trending_week) {
                sharedVm.setTrendingWindow(MainSharedViewModel.TRENDING_WEEK);
                return true;
            }

            return false;
        });

        pm.show();
    }

    private void showCatalogSortMenu() {
        PopupMenu pm = new PopupMenu(this, toolbar);
        pm.getMenuInflater().inflate(R.menu.menu_catalog_sort, pm.getMenu());

        MainSharedViewModel.CatalogState cur = sharedVm.catalogState().getValue();
        String sortBy = (cur != null) ? cur.sortBy : "popularity.desc";

        // отмечаем текущий
        if ("vote_average.desc".equals(sortBy)) pm.getMenu().findItem(R.id.sort_rating).setChecked(true);
        else if ("primary_release_date.desc".equals(sortBy)) pm.getMenu().findItem(R.id.sort_newest).setChecked(true);
        else if ("primary_release_date.asc".equals(sortBy)) pm.getMenu().findItem(R.id.sort_oldest).setChecked(true);
        else pm.getMenu().findItem(R.id.sort_popularity).setChecked(true);

        pm.setOnMenuItemClickListener(item -> {
            String newSort = mapCatalogSort(item.getItemId());
            if (newSort == null) return false;
            updateCatalogSort(newSort);
            return true;
        });

        pm.show();
    }

    private String mapCatalogSort(int itemId) {
        if (itemId == R.id.sort_popularity) return "popularity.desc";
        if (itemId == R.id.sort_rating) return "vote_average.desc";
        if (itemId == R.id.sort_newest) return "primary_release_date.desc";
        if (itemId == R.id.sort_oldest) return "primary_release_date.asc";
        return null;
    }

    private void updateCatalogSort(String sortBy) {
        MainSharedViewModel.CatalogState cur = sharedVm.catalogState().getValue();
        MainSharedViewModel.CatalogState s = new MainSharedViewModel.CatalogState();

        // скопируем старое, чтобы не потерять год/жанр/режим
        if (cur != null) {
            s.mode = cur.mode;
            s.year = cur.year;
            s.genreId = cur.genreId;
            s.sortBy = cur.sortBy;
        }

        // применяем новое
        s.sortBy = sortBy;

        // если не discover, сортировка смысла не имеет (у тебя так задумано)
        if (!MainSharedViewModel.MODE_DISCOVER.equals(s.mode)) {
            s.sortBy = "popularity.desc";
        }

        sharedVm.setCatalogState(s);
    }
}