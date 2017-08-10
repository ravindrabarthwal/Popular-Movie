package com.example.lenovo.popularmovie;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        MoviesAdapter.MovieClickListener,
        AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<String>{

    private RecyclerView mRecyclerView;
    private MoviesAdapter mAdapter;
    private Spinner mSortSpinner;

    private ProgressBar mLoadingIndicator;
    private TextView mErrorTextView;
    private GridLayout gridLayout;
    private JSONArray mJsonArray;
    //Default Sorting Path to Popular
    private String defaultSort = NetworkUtils.PATH_POPULAR;

    // The tag for the moviedburl, use in bundle
    private final String MOVIE_DB_URL = "movie_url";
    // Loader UNIQUE ID
    private final int MOVIE_DB_LOADER_ID = 22;

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
        mSortSpinner.setAdapter(spinnerAdapter);
        mSortSpinner.setOnItemSelectedListener(this);

        // Bind the views to their respective variable.
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorTextView = (TextView) findViewById(R.id.tv_error_text_view);
        gridLayout = (GridLayout) findViewById(R.id.grid_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Load the movies asynchronously or in background thread
        loadMovies();

        /** Setup the MovieAdapter which is a custom Adapter.
         *  Here we are passing null in json result(2nd argument), bcoz the movie data
         *  will take some time to fetch. So to prevent app to crash during runtime because
         *  of no network connection.
         */
        mAdapter = new MoviesAdapter(this, null, this);
        // Spinning up the gridlayout manager.
        GridLayoutManager gm = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        String orientation = (String) gridLayout.getTag();
        if(orientation == "landscape")
            gm.setSpanCount(3);

        // Set layout manager and adapter on the recyclerview.
        mRecyclerView.setLayoutManager(gm);
        mRecyclerView.setAdapter(mAdapter);

    }

    /*
        LoadMovies function:
            This function load the movie data and use defaultSort parameter
            to choose between popular movie or most rated movie data.
     */
    private void loadMovies(){
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
            loaderManager.initLoader(MOVIE_DB_LOADER_ID, queryBundle, this);
        }else {
            //Otherwise restart the loader.
            loaderManager.restartLoader(MOVIE_DB_LOADER_ID, queryBundle, this);
        }
    }

    // A helper function to show the results
    private void showResult(){
        gridLayout.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
    }
    // A helper function to show the error.
    private void showError(){
        gridLayout.setVisibility(View.INVISIBLE);
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
            defaultSort = NetworkUtils.PATH_POPULAR;
        }else {
            defaultSort = NetworkUtils.PATH_TOP_RATED;
        }
        loadMovies();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // Loader function.
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        // return new AsyncTaskLoader
        return new AsyncTaskLoader<String>(this) {

            String result;
            @Override
            protected void onStartLoading() {
                if(args == null)
                    return;

                mLoadingIndicator.setVisibility(View.VISIBLE);
                gridLayout.setVisibility(View.INVISIBLE);
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

    @Override
    public void onClick(int position) {
        //URL url = NetworkUtils.buildUrl(String.valueOf(id));
        try {
            JSONObject jsonObject = mJsonArray.getJSONObject(position);
            Intent intent = new Intent(this, MovieDetail.class);
            intent.putExtra("movie_object", jsonObject.toString());
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}