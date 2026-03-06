package com.example.mediaexplorer.data.repository;

import android.content.Context;

import com.example.mediaexplorer.data.local.AppDatabase;
import com.example.mediaexplorer.data.local.FavoriteDao;
import com.example.mediaexplorer.data.local.FavoriteMovieEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Repository для работы с избранными фильмами (Room)
public class FavoritesRepository {

    // Callback для возврата результата из фонового потока
    public interface RepoCallback<T> {
        void onSuccess(T data);
        void onError(String msg);
    }

    // DAO для работы с таблицей избранного
    private final FavoriteDao dao;

    // Фоновый поток для операций с базой
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    // Получаем DAO из базы данных
    public FavoritesRepository(Context ctx) {
        AppDatabase db = AppDatabase.getInstance(ctx);
        dao = db.favoriteDao();
    }

    // Добавление фильма в избранное
    public void insert(FavoriteMovieEntity e, RepoCallback<Void> cb) {
        io.execute(() -> {
            try {
                dao.insert(e);
                cb.onSuccess(null);
            } catch (Exception ex) {
                cb.onError(ex.getMessage());
            }
        });
    }

    // Удаление фильма из избранного
    public void delete(int id, RepoCallback<Void> cb) {
        io.execute(() -> {
            try {
                dao.deleteById(id);
                cb.onSuccess(null);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Проверка: находится ли фильм в избранном
    public void isFavorite(int id, RepoCallback<Boolean> cb) {
        io.execute(() -> {
            try {
                cb.onSuccess(dao.isFavorite(id) > 0);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Получение всех избранных фильмов
    public void getAll(RepoCallback<List<FavoriteMovieEntity>> cb) {
        io.execute(() -> {
            try {
                cb.onSuccess(dao.getAll());
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Получение фильмов по статусу (любимое / просмотрено / позже)
    public void getByStatus(String status, RepoCallback<List<FavoriteMovieEntity>> cb) {
        io.execute(() -> {
            try {
                cb.onSuccess(dao.getByStatus(status));
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Обновление пользовательского комментария
    public void updateComment(int id, String text, RepoCallback<Void> cb) {
        io.execute(() -> {
            try {
                dao.updateComment(id, text);
                cb.onSuccess(null);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Получение комментария
    public void getComment(int id, RepoCallback<String> cb) {
        io.execute(() -> {
            try {
                cb.onSuccess(dao.getComment(id));
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Обновление пользовательского рейтинга фильма
    public void updateRating(int id, float rating, RepoCallback<Void> cb) {
        io.execute(() -> {
            try {
                dao.updateRating(id, rating);
                cb.onSuccess(null);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Получение рейтинга
    public void getRating(int id, RepoCallback<Float> cb) {
        io.execute(() -> {
            try {
                Float r = dao.getRating(id);
                cb.onSuccess(r != null ? r : 0f);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Обновление статуса фильма (любимое / просмотрено / позже)
    public void updateStatus(int id, String status, RepoCallback<Void> cb) {
        io.execute(() -> {
            try {
                dao.updateStatus(id, status);
                cb.onSuccess(null);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Получение статуса фильма
    public void getStatus(int id, RepoCallback<String> cb) {
        io.execute(() -> {
            try {
                cb.onSuccess(dao.getStatus(id));
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Обновление emoji-реакции на фильм
    public void updateEmoji(int id, String emoji, RepoCallback<Void> cb) {
        io.execute(() -> {
            try {
                dao.updateEmoji(id, emoji);
                cb.onSuccess(null);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }

    // Получение emoji
    public void getEmoji(int id, RepoCallback<String> cb) {
        io.execute(() -> {
            try {
                cb.onSuccess(dao.getEmoji(id));
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        });
    }
}