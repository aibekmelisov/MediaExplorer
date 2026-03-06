package com.example.mediaexplorer.ui.favorites;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mediaexplorer.data.local.FavoriteMovieEntity;
import com.example.mediaexplorer.data.repository.FavoritesRepository;

import java.util.List;

public class FavoritesViewModel extends AndroidViewModel {

    private final FavoritesRepository repo;

    private final MutableLiveData<List<FavoriteMovieEntity>> items = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public FavoritesViewModel(@NonNull Application app) {
        super(app);
        repo = new FavoritesRepository(app);
    }

    public LiveData<List<FavoriteMovieEntity>> items() { return items; }
    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }

    public void load() {
        loading.setValue(true);
        error.setValue(null);

        repo.getAll(new FavoritesRepository.RepoCallback<List<FavoriteMovieEntity>>() {
            @Override public void onSuccess(List<FavoriteMovieEntity> data) {
                loading.postValue(false);
                items.postValue(data);
            }
            @Override public void onError(String msg) {
                loading.postValue(false);
                error.postValue(msg);
            }
        });
    }

    public void loadByStatus(String status) {
        loading.setValue(true);
        error.setValue(null);

        repo.getByStatus(status, new FavoritesRepository.RepoCallback<List<FavoriteMovieEntity>>() {
            @Override public void onSuccess(List<FavoriteMovieEntity> data) {
                loading.postValue(false);
                items.postValue(data);
            }
            @Override public void onError(String msg) {
                loading.postValue(false);
                error.postValue(msg);
            }
        });
    }

    public void delete(int id, String currentStatusFilter) {
        repo.delete(id, new FavoritesRepository.RepoCallback<Void>() {
            @Override public void onSuccess(Void data) {
                if (currentStatusFilter == null) load();
                else loadByStatus(currentStatusFilter);
            }
            @Override public void onError(String msg) { error.postValue(msg); }
        });
    }
}