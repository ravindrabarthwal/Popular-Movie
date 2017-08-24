package com.example.lenovo.popularmovie.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.lenovo.popularmovie.data.MoviesContract.FavouriteEntry;

/**
 * Created by lenovo on 8/21/2017.
 */

public class MovieDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favourite_movie.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + FavouriteEntry.TABLE_NAME + " (" +
                FavouriteEntry.MOVIE_ID + " INTEGER PRIMARY KEY, " +
                FavouriteEntry.MOVIE_TITLE + " TEXT NOT NULL, " +
                FavouriteEntry.MOVIE_POSTER + " TEXT NOT NULL, " +
                FavouriteEntry.MOVIE_DATE + " TEXT NOT NULL, " +
                FavouriteEntry.MOVIE_RATING + " TEXT NOT NULL, " +
                FavouriteEntry.MOVIE_SYNOPSIS + " TEXT NOT NULL, " +
                FavouriteEntry.MOVIE_REVIEW_AUTHOR + " TEXT , " +
                FavouriteEntry.MOVIE_AUTHOR_CONTENT + " TEXT , " +
                FavouriteEntry.MOVIE_TRAILER_LINK + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteEntry.TABLE_NAME);
        onCreate(db);
    }
}
