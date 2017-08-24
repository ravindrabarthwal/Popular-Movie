package com.example.lenovo.popularmovie.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.popularmovie.R;
import com.example.lenovo.popularmovie.data.MoviesAdapter;
import com.example.lenovo.popularmovie.data.MoviesContract;
import com.example.lenovo.popularmovie.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        MoviesAdapter.MovieClickListener,
        AdapterView.OnItemSelectedListener{

    private static final String MOVIE_DEFAULT_SORT = "default-sort";
    // The tag for the moviedburl, use in bundle
    private final String MOVIE_DB_URL = "movie_url";
    private final String MOVIE_JSON_RESULT = "movie-json-result";

    private RecyclerView mRecyclerView;
    private MoviesAdapter mAdapter;
    private Spinner mSortSpinner;

    private ProgressBar mLoadingIndicator;
    private TextView mErrorTextView;
    private GridLayout gridLayout;
    private JSONArray mJsonArray = null;
    private boolean isDataLoadedFromDb = false;
    //Default Sorting Path to Popular
    private String defaultSort;

    // Loader UNIQUE ID
    private final int MOVIE_DB_LOADER_ID = 22;
    private final int DATABASE_LOADER_ID = 23;

    /*
        This is the loader for the movieDb Api. It
        fetch and download the data from the Api.
     */
    private LoaderManager.LoaderCallbacks<String> movieDbLoaderListener =
            new LoaderManager.LoaderCallbacks<String>() {
                // Loader function.
                @Override
                public Loader<String> onCreateLoader(int id, final Bundle args) {
                    // return new AsyncTaskLoader
                    return new AsyncTaskLoader<String>(MainActivity.this) {

                        String result;
                        @Override
                        protected void onStartLoading() {
                            if(args == null)
                                return;

                            mLoadingIndicator.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.INVISIBLE);
                            mErrorTextView.setVisibility(View.INVISIBLE);

                            if(result != null){
                                deliverResult(result);
                            }else{
                                forceLoad();
                            }
                        }

                        @Override
                        public String loadInBackground() {
                            try {
                                URL movieDBUrl = new URL(args.getString(MOVIE_DB_URL));
                                return NetworkUtils.getResponseFromHttpUrl(movieDBUrl);
                            }catch (IOException e){
                                e.printStackTrace();
                                return null;
                            }
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader<String> loader, String data) {
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    if(null != data & !"".equals(data)){
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            mJsonArray = jsonObject.getJSONArray("results");
                            mAdapter.swapMovieArray(mJsonArray);
                            mAdapter.notifyDataSetChanged();
                            showResult();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showError();
                        }
                    }else {
                        showError();
                    }
                }

                @Override
                public void onLoaderReset(Loader<String> loader) {

                }
            };

    /*
        This is the loader that load the movie from the
        database and set the adapter.
     */
    private LoaderManager.LoaderCallbacks<Cursor> favouriteMovieLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
                    return new AsyncTaskLoader<Cursor>(MainActivity.this) {
                        Cursor cursor;

                        @Override
                        protected void onStartLoading() {

                            if(cursor != null)
                                deliverResult(cursor);
                            else
                                forceLoad();
                        }

                        @Override
                        public Cursor loadInBackground() {
                            return getContentResolver().query(
                                    MoviesContract.FavouriteEntry.CONTENT_URI,
                                    new String[]{MoviesContract.FavouriteEntry.MOVIE_ID, MoviesContract.FavouriteEntry.MOVIE_POSTER},
                                    null,
                                    null,
                                    null);
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    if(data.getCount() == 0) {
                        mErrorTextView.setText(getString(R.string.error_no_fav));
                        mErrorTextView.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.INVISIBLE);
                    }else {
                        mAdapter.swapMovieArray(data);
                        mAdapter.notifyDataSetChanged();
                        mErrorTextView.setVisibility(View.INVISIBLE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
                    data.close();
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Populate the Spinner with the predefined array of strings
        mSortSpinner = (Spinner) findViewById(R.id.spinner_sort);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_array,
                android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the spinner. adapter on spinner.
        mSortSpinner.setAdapter(spinnerAdapter);
        // Attach click listener on the spinner.
        mSortSpinner.setOnItemSelectedListener(this);
        // setup Ui from preference.
        setupPreference();

        // Bind the views to their respective variable.
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorTextView = (TextView) findViewById(R.id.tv_error_text_view);
        gridLayout = (GridLayout) findViewById(R.id.grid_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        /** Setup the MovieAdapter which is a custom Adapter.
         *  Here we are passing null in json result(2nd argument), bcoz the movie data
         *  will take some time to fetch. So to prevent app to crash during runtime because
         *  of no network connection.
         */
        mAdapter = new MoviesAdapter(this, null, this);
        // Spinning up the gridlayout manager.
        GridLayoutManager gm = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);

        // Set layout manager and adapter on the recyclerview.
        mRecyclerView.setLayoutManager(gm);
        mRecyclerView.setAdapter(mAdapter);

        // Check if saveInstance is not null
        if(savedInstanceState != null){

            // Restore the data from the last save instance.
            String movie_result = (String) savedInstanceState.get(MOVIE_JSON_RESULT);
            defaultSort = savedInstanceState.getString(MOVIE_DEFAULT_SORT);
            if(defaultSort != null && (null != movie_result || movie_result.length() != 0)){
                try {
                    mJsonArray = new JSONArray(movie_result);
                    mAdapter.swapMovieArray(mJsonArray);
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                loadMoviesFromDb();
            }
        }else {
            // Load the movies asynchronously or in background thread if there is network.
            if(NetworkUtils.isNetworkConnected(this) && null != defaultSort) {
                // Load the movies from internet.
                loadMovies();
            }else {
                // Load the movies from background.
                loadMoviesFromDb();
            }
        }
    }

    /**
     * This is a helper function which setup the preferences.
     */
    private void setupPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int selectedPosition = sharedPreferences.getInt(getString(R.string.preference_key_sort_order), 0);
        mSortSpinner.setSelection(selectedPosition);
        switch (selectedPosition){
            case 0: defaultSort = NetworkUtils.PATH_POPULAR; break;
            case 1: defaultSort = NetworkUtils.PATH_TOP_RATED; break;
            case 2: defaultSort = null;
        }
    }

    /*
        LoadMovies function:
            This function load the movie data and use defaultSort parameter
            to choose between popular movie or most rated movie data.
     */
    private void loadMovies(){
        // Set the flag of data loaded from Db to false.
        isDataLoadedFromDb = false;
        // Get the movies url based on the current movie sort option
        URL moviesUrls = NetworkUtils.buildUrl(defaultSort);
        // A bundle that will pass on LoaderManager
        Bundle queryBundle = new Bundle();
        //Put the url on the bundle.
        queryBundle.putString(MOVIE_DB_URL, moviesUrls.toString());
        // Getting supported loaderManager
        LoaderManager loaderManager = getSupportLoaderManager();
        // Get Loader manager with previously defined unique loader id.
        Loader<String> movieLoader = loaderManager.getLoader(MOVIE_DB_LOADER_ID);
        // If no loader found
        if(movieLoader == null){
            //Initialize the loader
            loaderManager.initLoader(MOVIE_DB_LOADER_ID, queryBundle, movieDbLoaderListener);
        }else {
            //Otherwise restart the loader.
            loaderManager.restartLoader(MOVIE_DB_LOADER_ID, queryBundle, movieDbLoaderListener);
        }
    }

    /*
        This function loads movie from the database
        asynchronously by spinning up a loader.
     */
    private void loadMoviesFromDb(){
        Log.v("DbCall", "Im called");
        // Since we are loading from Db then mark it true.
        isDataLoadedFromDb = true;
        mSortSpinner.setSelection(2);
        Bundle bundle = new Bundle();
        defaultSort = null;
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Cursor> loader = loaderManager.getLoader(DATABASE_LOADER_ID);
        Loader<String> loaderNetwork = loaderManager.getLoader(MOVIE_DB_LOADER_ID);
        if(loaderNetwork != null)
            loaderNetwork.cancelLoad();
        if(loader == null){
            loaderManager.initLoader(DATABASE_LOADER_ID, bundle, favouriteMovieLoaderListener);
        }else {
            loaderManager.restartLoader(DATABASE_LOADER_ID, bundle, favouriteMovieLoaderListener);
        }
    }

    // A helper function to show the results
    private void showResult(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
    }
    // A helper function to show the error.
    private void showError(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    /* Spinner Selection Function:
            set the new sort order if changed and then
            load the movies data again.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = (String) parent.getItemAtPosition(position);
        if(item.equals(getString(R.string.spinner_popularity))){
            if(!NetworkUtils.PATH_POPULAR.equals(defaultSort)){
                if(NetworkUtils.isNetworkConnected(this)){
                    defaultSort = NetworkUtils.PATH_POPULAR;
                    loadMovies();
                } else {
                    Toast.makeText(this, getString(R.string.error_no_connection), Toast.LENGTH_LONG).show();
                }
            }
        }else if(item.equals(getString(R.string.spinner_rating))){
            if(!NetworkUtils.PATH_TOP_RATED.equals(defaultSort)){
                if(NetworkUtils.isNetworkConnected(this)) {
                    defaultSort = NetworkUtils.PATH_TOP_RATED;
                    loadMovies();
                }else {
                    Toast.makeText(this, getString(R.string.error_no_connection), Toast.LENGTH_LONG).show();
                }
            }
        }else {
            // Show the movies from db.
            loadMoviesFromDb();
            showResult();
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(getString(R.string.preference_key_sort_order), position)
                .apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(int position) {
        //URL url = NetworkUtils.buildUrl(String.valueOf(id));
        try {
            Intent intent = new Intent(this, MovieDetail.class);
            if(!isDataLoadedFromDb) {
                JSONObject jsonObject = mJsonArray.getJSONObject(position);
                intent.putExtra("movie_object", jsonObject.toString());
            }else {
                intent.putExtra("movie_id", String.valueOf(position));
            }
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String jsonResult = "";
        if(null != mJsonArray){
            jsonResult = mJsonArray.toString();
        }
        outState.putString(MOVIE_JSON_RESULT, jsonResult);
        outState.putString(MOVIE_DEFAULT_SORT, defaultSort);

        super.onSaveInstanceState(outState);
    }
}