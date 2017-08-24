package com.example.lenovo.popularmovie.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by lenovo on 8/21/2017.
 */

public class MoviesContract {

    public static final String CONTENT_SCHEME = "content://";
    public static final String CONTENT_AUTHORITY = "com.example.lenovo.popularmovie";
    public static final Uri BASE_CONTENT_URI = Uri.parse(CONTENT_SCHEME + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";


    public static class FavouriteEntry {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "favourite_movies";
        public static final String MOVIE_ID = "id";
        public static final String MOVIE_TITLE = "title";
        public static final String MOVIE_POSTER = "poster";
        public static final String MOVIE_DATE = "date";
        public static final String MOVIE_RATING = "rating";
        public static final String MOVIE_SYNOPSIS = "synopsis";
        public static final String MOVIE_REVIEW_AUTHOR = "author_name";
        public static final String MOVIE_AUTHOR_CONTENT = "author_content";
        public static final String MOVIE_TRAILER_LINK = "trailer_link";
    }
}
