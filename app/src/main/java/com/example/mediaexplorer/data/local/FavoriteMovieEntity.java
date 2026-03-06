package com.example.mediaexplorer.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteMovieEntity {
//данные из API
    @PrimaryKey
    public int id;

    public String title;
    public String posterPath;
    public String releaseDate;
    public double voteAverage;
    public String overview;

    public String comment;     // комментарий
    public float userRating;   // рейтинг пользователя

    // NEW
    public String userStatus;      // TO_WATCH / WATCHED / FAVORITE
    public String emojiReaction;   // эмоджи пользователя
}