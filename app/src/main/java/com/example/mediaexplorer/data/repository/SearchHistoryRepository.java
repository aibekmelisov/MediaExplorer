package com.example.mediaexplorer.data.repository;

import android.content.Context;

import com.example.mediaexplorer.data.local.AppDatabase;
import com.example.mediaexplorer.data.local.SearchHistoryDao;
import com.example.mediaexplorer.data.local.SearchHistoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchHistoryRepository {

    private final SearchHistoryDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public SearchHistoryRepository(Context ctx) {
        dao = AppDatabase.getInstance(ctx).searchHistoryDao();
    }

    public void saveQuery(String q) {
        if (q == null) return;
        String query = q.trim();
        if (query.isEmpty()) return;

        io.execute(() -> {
            // чтобы одинаковый запрос поднимался наверх
            dao.deleteByQuery(query);
            dao.insert(new SearchHistoryEntity(query, System.currentTimeMillis()));
        });
    }

    public void clearAll() {
        io.execute(dao::clearAll);
    }

    public void loadLatest(int limit, Callback<List<SearchHistoryEntity>> cb) {
        io.execute(() -> {
            try {
                List<SearchHistoryEntity> list = dao.getLatest(limit);
                cb.onSuccess(list);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String msg);
    }

    public void deleteQuery(String q) {
        if (q == null) return;
        String query = q.trim();
        if (query.isEmpty()) return;
        io.execute(() -> dao.deleteByQuery(query));
    }
}