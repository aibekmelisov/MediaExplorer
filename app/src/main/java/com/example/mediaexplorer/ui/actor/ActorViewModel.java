package com.example.mediaexplorer.ui.actor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mediaexplorer.data.remote.dto.PersonDetailsDto;
import com.example.mediaexplorer.data.remote.dto.PersonMovieCreditsDto;
import com.example.mediaexplorer.data.repository.MoviesRepository;

public class ActorViewModel extends AndroidViewModel {

    private final MoviesRepository repo = new MoviesRepository();

    private final MutableLiveData<PersonDetailsDto> person = new MutableLiveData<>();
    private final MutableLiveData<PersonMovieCreditsDto> credits = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ActorViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<PersonDetailsDto> person() { return person; }
    public LiveData<PersonMovieCreditsDto> credits() { return credits; }
    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }

    public void load(int personId) {
        loading.setValue(true);
        error.setValue(null);

        repo.personDetails(personId, "ru-RU", new MoviesRepository.RepoCallback<PersonDetailsDto>() {
            @Override public void onSuccess(PersonDetailsDto data) {
                person.postValue(data);
                loading.postValue(false);
            }
            @Override public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });

        repo.personMovieCredits(personId, "ru-RU", new MoviesRepository.RepoCallback<PersonMovieCreditsDto>() {
            @Override public void onSuccess(PersonMovieCreditsDto data) {
                credits.postValue(data);
            }
            @Override public void onError(String message) { }
        });
    }
}