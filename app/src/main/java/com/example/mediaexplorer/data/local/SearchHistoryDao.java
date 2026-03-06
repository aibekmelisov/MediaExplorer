package com.example.mediaexplorer.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY createdAt DESC LIMIT :limit")
    List<SearchHistoryEntity> getLatest(int limit);

    @Query("DELETE FROM search_history")
    void clearAll();

    @Query("DELETE FROM search_history WHERE query = :q")
    void deleteByQuery(String q);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(SearchHistoryEntity e);
}