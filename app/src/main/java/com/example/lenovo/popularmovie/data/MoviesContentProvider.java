package com.example.lenovo.popularmovie.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by lenovo on 8/21/2017.
 */

public class MoviesContentProvider extends ContentProvider {

    public static final int MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;

    public static UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES + "/#", MOVIE_WITH_ID);
        return uriMatcher;

    }

    private MovieDatabaseHelper db;

    @Override
    public boolean onCreate() {
        db = new MovieDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri);
        final SQLiteDatabase readDb = db.getReadableDatabase();
        Cursor returnCursor = null;
        switch (match){
            case MOVIES : {
                returnCursor = readDb.query(
                        MoviesContract.FavouriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIE_WITH_ID: {
                String id = uri.getPathSegments().get(1);
                String mSelection = MoviesContract.FavouriteEntry.MOVIE_ID + "=?";
                String[] mSelectionArgs = new String[]{id};
                returnCursor = readDb.query(
                        MoviesContract.FavouriteEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default: throw new UnsupportedOperationException("The uri is not supported " + uri);
        }
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match){
            case MOVIES: {
                long id = db.getWritableDatabase().insert(
                        MoviesContract.FavouriteEntry.TABLE_NAME,
                        null,
                        values
                );
                if(id > 0) {
                    returnUri = ContentUris.withAppendedId(MoviesContract.FavouriteEntry.CONTENT_URI, id);
                }else {
                    throw  new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Uri didn't match.");
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int isDeleted;
        switch (match){
            case MOVIE_WITH_ID: {
                String id = uri.getPathSegments().get(1);
                String mSelection = MoviesContract.FavouriteEntry.MOVIE_ID + "=?";
                String[] mSelectionArgs = new String[] {id};
                isDeleted =  db.getWritableDatabase().delete(
                        MoviesContract.FavouriteEntry.TABLE_NAME,
                        mSelection,
                        mSelectionArgs
                );
                break;
            }

            default: throw new UnsupportedOperationException("The query to delete doesn't matched. " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return isDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
