package com.example.mediaexplorer.ui.main.tabs;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                return MoviesListFragment.newInstance(
                        MoviesTabViewModel.CAT_POPULAR, null);

            case 1:
                return MoviesListFragment.newInstance(
                        MoviesTabViewModel.CAT_TOP_RATED, null);

            case 2:
                return MoviesListFragment.newInstance(
                        MoviesTabViewModel.CAT_TRENDING, "week");

            case 3:
                return MoviesListFragment.newInstance(
                        MoviesTabViewModel.CAT_UPCOMING, null);

            case 4:
                return MoviesListFragment.newInstance(
                        MoviesTabViewModel.CAT_CATALOG, null);

            default:
                return MoviesListFragment.newInstance(
                        MoviesTabViewModel.CAT_POPULAR, null);
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}