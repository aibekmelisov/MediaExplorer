package com.example.mediaexplorer.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteMovieEntity movie);

    @Query("DELETE FROM favorites WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT COUNT(*) FROM favorites WHERE id = :id")
    int isFavorite(int id);

    @Query("SELECT * FROM favorites ORDER BY id DESC")
    List<FavoriteMovieEntity> getAll();

    // -------- COMMENT --------
    @Query("UPDATE favorites SET comment = :comment WHERE id = :id")
    void updateComment(int id, String comment);

    @Query("SELECT comment FROM favorites WHERE id = :id LIMIT 1")
    String getComment(int id);

    // -------- RATING --------
    @Query("UPDATE favorites SET userRating = :rating WHERE id = :id")
    void updateRating(int id, float rating);

    @Query("SELECT userRating FROM favorites WHERE id = :id LIMIT 1")
    Float getRating(int id);

    // -------- STATUS (NEW) --------
    @Query("UPDATE favorites SET userStatus = :status WHERE id = :id")
    void updateStatus(int id, String status);

    @Query("SELECT userStatus FROM favorites WHERE id = :id LIMIT 1")
    String getStatus(int id);

    @Query("SELECT * FROM favorites WHERE userStatus = :status ORDER BY id DESC")
    List<FavoriteMovieEntity> getByStatus(String status);

    // -------- EMOJI (NEW) --------
    @Query("UPDATE favorites SET emojiReaction = :emoji WHERE id = :id")
    void updateEmoji(int id, String emoji);

    @Query("SELECT emojiReaction FROM favorites WHERE id = :id LIMIT 1")
    String getEmoji(int id);
}