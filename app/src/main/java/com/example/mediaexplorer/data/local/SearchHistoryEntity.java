package com.example.mediaexplorer.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "search_history")
public class SearchHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String query;  // поиск

    public long createdAt; // millis

    public SearchHistoryEntity(String query, long createdAt) {
        this.query = query;
        this.createdAt = createdAt;
    }
}