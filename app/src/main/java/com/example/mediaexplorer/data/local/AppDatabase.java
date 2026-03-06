package com.example.mediaexplorer.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Основная база данных приложения (Room)
@Database(entities = {FavoriteMovieEntity.class, SearchHistoryEntity.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {

    // DAO для работы с избранными фильмами
    public abstract FavoriteDao favoriteDao();

    // DAO для работы с историей поиска
    public abstract SearchHistoryDao searchHistoryDao();

    // Singleton экземпляр базы
    private static volatile AppDatabase INSTANCE;

    // Получение экземпляра базы данных
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "media_db"
                            )
                            // пересоздание базы при изменении версии
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}