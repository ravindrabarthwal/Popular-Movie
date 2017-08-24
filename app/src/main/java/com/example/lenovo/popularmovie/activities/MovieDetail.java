package com.example.lenovo.popularmovie.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.popularmovie.R;
import com.example.lenovo.popularmovie.data.MoviesContract.FavouriteEntry;
import com.example.lenovo.popularmovie.utilities.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetail extends AppCompatActivity {
    private static final String MOVIE_ID_KEY = "movie-id-key";
    private static final java.lang.String ABSOLUTE_POSTER_PATH_BUNDLE_KEY = "absolute_poster_path";
    private final int REVIEWS_LOADER_MANAGER_ID = 101;
    private final int TRAILER_LOADER_MANAGER_ID = 102;
    private final int MOVIE_QUERY_DB_LOADER_ID = 200;
    private final int MOVIE_INSERT_DB_LOADER_ID = 201;
    private final int MOVIE_DELETE_DB_LOADER_ID = 202;
    private final int DOWNLOAD_IMAGE_LOADER_ID = 300;
    private final String MOVIE_ID_BUNDLE_KEY = "movie-id";
    private final String REVIEW_AUTHOR_KEY = "review-author";
    private final String REVIEW_CONTENT_KEY = "review-content";
    private final String TRAILER_URL_KEY = "trailer-url";
    private final String MOVIE_SYNOPSIS_KEY = "movie-synopsis";
    private final String MOVIE_RATING_KEY = "movie-rating";
    private final String MOVIE_DATE_KEY = "movie-date";
    private final String MOVIE_TITLE_KEY = "movie-title";
    private final String MOVIE_POSTER_URL = "movie-poster-url";
    private final String MOVIE_IS_FAVOURITE = "movie-fav";
    private boolean isFavourite;
    private boolean isLoadingReview;
    private String posterAbsolutePath = null;

    @BindView(R.id.movie_year) TextView mMovieYearTextView;
    @BindView(R.id.movie_rating) TextView mMovieRatingTextView;
    @BindView(R.id.review_author) TextView mReviewAuthorTextView;
    @BindView(R.id.review_content) TextView mReviewContentTextView;
    @BindView(R.id.movie_description) TextView mMovieDescriptionTextView;
    @BindView(R.id.movie_image_poster) ImageView mMovieImagePosterImageView;
    @BindView(R.id.review_progress_bar) ProgressBar mReviewProgressBar;
    @BindView(R.id.trailer_progress_bar) ProgressBar mTrailerProgressBar;
    @BindView(R.id.trailer1) LinearLayout mTrailer1LinearLayout;
    @BindView(R.id.error_review) TextView mErrorReviewTextView;
    @BindView(R.id.error_trailer) TextView mErrorTrailerTextView;
    @BindView(R.id.fab) FloatingActionButton fab;

    private LoaderManager.LoaderCallbacks<String> loadReviewAndTrailerLoaderListener =
            new LoaderManager.LoaderCallbacks<String>() {
                @Override
                public Loader onCreateLoader(final int id, final Bundle args) {
                    // retyurn a new AsyncTaskLoader
                    return new AsyncTaskLoader<String>(MovieDetail.this) {
                        // String result.
                        String result;

                        @Override
                        protected void onStartLoading() {
                            // return if args are null.
                            if(args == null) return;
                            // if result aren't null
                            if(result != null)
                                deliverResult(result); // deliver the result.
                            else forceLoad(); // otherwise, force load.
                        }

                        @Override
                        public String loadInBackground() {
                            // Get movie id from bundle,
                            String movieId = args.getString(MOVIE_ID_BUNDLE_KEY);
                            // Make url default to reviews.
                            URL fetchUrl = NetworkUtils.getReviewsUrl(movieId);
                            // if the loader id is equal to trailer loader id
                            // then change the trailer
                            if(id == TRAILER_LOADER_MANAGER_ID) fetchUrl = NetworkUtils.getTrailersUrl(movieId);
                            // try to get the response from http.
                            try {
                                //return the response.
                                return NetworkUtils.getResponseFromHttpUrl(fetchUrl);
                            } catch (IOException e) {
                                // print the error and return null.
                                e.printStackTrace();
                                return null;
                            }
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader loader, String data) {
                    // If data isn't null.
                    if(null != data || data.length() != 0){
                        // check the loader id.
                        int id = loader.getId();
                        // if it is review loader
                        if(id == REVIEWS_LOADER_MANAGER_ID)
                            showReviews(data); // show reviews
                        else
                            showTrailers(data); // show trailer.
                    }
                }

                @Override
                public void onLoaderReset(Loader loader) {

                }
            };

    private LoaderManager.LoaderCallbacks<Uri> saveMovieDetailLoaderListener =
            new LoaderManager.LoaderCallbacks<Uri>() {
                @Override
                public Loader<Uri> onCreateLoader(int id, final Bundle args) {
                    return new AsyncTaskLoader<Uri>(MovieDetail.this) {
                        Uri returnUri;

                        @Override
                        protected void onStartLoading() {
                            if(args == null) return;;

                            if(returnUri != null){
                                deliverResult(returnUri);
                            }else {
                                forceLoad();
                            }
                        }

                        @Override
                        public Uri loadInBackground() {
                            // Get the movieId;
                            String movieId = mMovieDescriptionTextView.getTag().toString();
                            String absolutePosterPath = args.getString(ABSOLUTE_POSTER_PATH_BUNDLE_KEY);
                            // Make new content value obj.
                            ContentValues contentValues = new ContentValues();
                            // Put the data to this content value.
                            contentValues.put(FavouriteEntry.MOVIE_ID, Integer.parseInt(movieId));
                            contentValues.put(FavouriteEntry.MOVIE_TITLE, getSupportActionBar().getTitle().toString());
                            contentValues.put(FavouriteEntry.MOVIE_POSTER, absolutePosterPath);
                            contentValues.put(FavouriteEntry.MOVIE_DATE, mMovieYearTextView.getText().toString());
                            contentValues.put(FavouriteEntry.MOVIE_RATING, mMovieRatingTextView.getText().toString());
                            contentValues.put(FavouriteEntry.MOVIE_SYNOPSIS, mMovieDescriptionTextView.getText().toString());
                            contentValues.put(FavouriteEntry.MOVIE_REVIEW_AUTHOR, mReviewAuthorTextView.getText().toString());
                            contentValues.put(FavouriteEntry.MOVIE_AUTHOR_CONTENT, mReviewContentTextView.getText().toString());
                            contentValues.put(FavouriteEntry.MOVIE_TRAILER_LINK, mTrailer1LinearLayout.getTag().toString());

                            // insert the data using content resolver and then return uri.
                            return getContentResolver().insert(FavouriteEntry.CONTENT_URI, contentValues);
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader<Uri> loader, Uri uri) {
                    // Check if uri is not null
                    if (uri != null) {
                        // if so, change favourite flag.
                        isFavourite = true;
                    }else {
                        // Otherwise, do opp.
                        isFavourite = false;
                    }
                    // Change the favourite flag icon.
                    changeFavouriteIcon();
                }

                @Override
                public void onLoaderReset(Loader<Uri> loader) {

                }
            };

    private LoaderManager.LoaderCallbacks<Cursor> queryMovieDetailLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
                    return new AsyncTaskLoader<Cursor>(MovieDetail.this) {
                        Cursor retCursor;
                        @Override
                        protected void onStartLoading() {
                            if(args == null) return;

                            if(null != retCursor) deliverResult(retCursor);
                            else forceLoad();
                        }

                        @Override
                        public Cursor loadInBackground() {
                            String movieId = args.getString(MOVIE_ID_BUNDLE_KEY);
                            Uri uri = FavouriteEntry.CONTENT_URI.buildUpon().appendPath(movieId).build();
                            Log.v("Uri", uri.toString());
                            return getContentResolver().query(
                                    uri,
                                    null,
                                    null,
                                    null,
                                    null);
                        }
                    };
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    if(null != data){

                        if(data.moveToNext()){
                            String posterPath = data.getString(data.getColumnIndex(FavouriteEntry.MOVIE_POSTER));
                            // Get the movie id from the object.
                            String movieId = data.getString(data.getColumnIndex(FavouriteEntry.MOVIE_ID));
                            String title = data.getString(data.getColumnIndex(FavouriteEntry.MOVIE_TITLE));
                            String date = data.getString(data.getColumnIndex(FavouriteEntry.MOVIE_DATE));
                            String rating = data.getString(data.getColumnIndex(FavouriteEntry.MOVIE_RATING));
                            String synopsis = data.getString(data.getColumnIndex(FavouriteEntry.MOVIE_SYNOPSIS));
                            String reviewAuthor = data.getString(data.getColumnIndex(FavouriteEntry.MOVIE_REVIEW_AUTHOR));
                            String reviewContent = data.getString(data.getColumnIndex(FavouriteEntry.MOVIE_AUTHOR_CONTENT));
                            String movieTrailerLink = data.getString(data.getColumnIndex(FavouriteEntry.MOVIE_TRAILER_LINK));
                            // Set movie to favourite, b'coz we loaded from favourite ;)
                            isFavourite = true;
                            isLoadingReview = false;
                            setMovieDetails(posterPath, movieId, title, date, rating,
                                    synopsis, reviewAuthor, reviewContent, movieTrailerLink);
                            getSupportActionBar().setTitle(title);
                            data.close();
                        }
                    }
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };
    /*
        This function is the way to enter to this activity.
        It hooks up the ui and perform all the necessary tasks.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Hook up the basic UI functions
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.v("onCreate", "I am called!");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Binding butterknife
        ButterKnife.bind(this);

        // Check if saveInstance isn't null
        if(savedInstanceState != null){
            // if so update the UI with the instance data.
            updateAfterSaveInstance(savedInstanceState);
        }
        // Otherwise it's newly created activity.
        else {
            // Get the intent which created this activity.
            Intent intent = getIntent();
            // If intent come from the MainActivity therefore it must have the movie detail.
            if (intent.hasExtra("movie_object")) {
                // If so then.
                try {
                    // Get JSON Object from the intent.
                    JSONObject jsonObject = new JSONObject(intent.getStringExtra("movie_object"));
                    // Get the poster path from the json object.
                    String posterPath = NetworkUtils.MOVIEDB_POSTER_PATH_BASE_URL + jsonObject.getString("poster_path");
                    // Get the movie id from the object.
                    String movieId = jsonObject.getString("id");
                    String title = jsonObject.getString("original_title");
                    String date = jsonObject.getString("release_date");
                    String rating = jsonObject.getString("vote_average");
                    String synopsis = jsonObject.getString("overview");
                    // Change the imageview image with this poster using picasso.
                    Picasso.with(this).load(posterPath).into(mMovieImagePosterImageView);

                    // Check Is the movie added to the favourite list.
                    checkIsFavourite(movieId);
                    // Set the title of the movie to the actionBar title.
                    //actionBar.setTitle(title);
                    String reviewAuthor = null, reviewContent = null, movieTrailerLink = null;

                    if(NetworkUtils.isNetworkConnected(this)) {
                        isLoadingReview = true;
                        // Load Reviews from Internet.
                        loadReviews(movieId);
                        // Load Trailers from Internet.
                        loadTrailers(movieId);
                    }
                    // Otherwise
                    else {
                        // Set the loading progress bar to invisible.
                        mTrailerProgressBar.setVisibility(View.INVISIBLE);
                        mReviewProgressBar.setVisibility(View.INVISIBLE);
                        // Put the message in the error fields of no connection.
                        mErrorTrailerTextView.setText(R.string.error_no_connection);
                        mErrorReviewTextView.setText(R.string.error_no_connection);
                        // Make those text views visible.
                        mErrorReviewTextView.setVisibility(View.VISIBLE);
                        mErrorTrailerTextView.setVisibility(View.VISIBLE);
                    }

                    setMovieDetails(posterPath, movieId, title, date, rating,
                            synopsis, reviewAuthor, reviewContent, movieTrailerLink);

                    // Check if the Network is available.
                    // If yes
                } catch (JSONException e) {
                    // If error parsing json object print the stack trace.
                    e.printStackTrace();
                }
            }
            // The activity isn't created by the intent with the data it means
            // We need to fetch the data from database.
            else {
                String movieId = intent.getStringExtra("movie_id");
                Bundle bundle = new Bundle();
                bundle.putString(MOVIE_ID_BUNDLE_KEY, movieId);

                LoaderManager loaderManager = getSupportLoaderManager();
                Loader<Cursor> loader = loaderManager.getLoader(MOVIE_QUERY_DB_LOADER_ID);

                if(loader == null)
                    loaderManager.initLoader(MOVIE_QUERY_DB_LOADER_ID, bundle, queryMovieDetailLoaderListener);
                else
                    loaderManager.restartLoader(MOVIE_QUERY_DB_LOADER_ID, bundle, queryMovieDetailLoaderListener);
            }
        }

        // Add the click listener to the Floating Action Button.
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the movieId from the textView
                String movieId = mMovieDescriptionTextView.getTag().toString();
                String posterPath = mMovieImagePosterImageView.getTag().toString();
                // Check if this movie isFavourite.
                if(isFavourite) {
                    // If yes, remove it from favourite.
                    removeFromFavourite(movieId);
                }else {
                    // Otherwise, add to the favourite.
                    downloadImageAndAddToFavourite(movieId, posterPath);
                }
            }
        });

        // Add a click listener to the trailer linear layout.
        mTrailer1LinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the String uri to data and parse it to uri.
                Uri data = Uri.parse(mTrailer1LinearLayout.getTag().toString());
                // Create new intent with the action to view.
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // Set the data to the intent.
                intent.setData(data);
                // If there are app to handle the intent the n
                if(intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent); // start the intent.
            }
        });
    }

    private void setMovieDetails(String posterPath, String movieId, String title,
                                 String date, String rating, String synopsis,
                                 String reviewAuthor, String reviewContent, String movieTrailerLink){
        // Change the image view image with this poster using picasso.
        Picasso.with(MovieDetail.this).load(posterPath).into(mMovieImagePosterImageView);
        File file = new File(posterPath);
        if(file.exists()){
            Picasso.with(MovieDetail.this).load(file).into(mMovieImagePosterImageView);
        }else {
            Picasso.with(MovieDetail.this).load(posterPath).into(mMovieImagePosterImageView);
        }
        // Also add the poster path tag to it's image view so that we can later
        // access the path easily if required.
        mMovieImagePosterImageView.setTag(posterPath);

        // Put the movie id into the description text view for later access.
        mMovieDescriptionTextView.setTag(movieId);
        // Check Is the movie added to the favourite list.
        changeFavouriteIcon();
        // Set the title of the movie to the actionBar title.
        //getSupportActionBar().setTitle(title);
        // Set the movie release date.
        mMovieYearTextView.setText(date);
        // Set the movie rating.
        mMovieRatingTextView.setText(rating);
        // Set the movie synopsis.
        mMovieDescriptionTextView.setText(synopsis);
        // If loaded from Database
        if(!isLoadingReview){
            // Check if review is not null
            if(null != reviewAuthor) {
                // If yes, set the reviews.
                showReviewsLayout(reviewAuthor, reviewContent);
            }else {
                // Otherwise show no reviews error.
                hideReviewsLayoutAndShowError();
            }
            // Check if trailer is not null
            if(null != movieTrailerLink){
                // If yes, set the trailer link on tag
                showTrailersLayout(movieTrailerLink);
            } else {
                // Otherwise show no trailer error.
                hideTrailersLayoutAndShowError();
            }
        }else {
            // Title isn't setting when getting data from db. Line 418 not working for
            // data from database.
            getSupportActionBar().setTitle(title);
        }
    }

    private void showTrailersLayout(String movieTrailerLink) {
        mTrailer1LinearLayout.setTag(movieTrailerLink);
        mTrailerProgressBar.setVisibility(View.INVISIBLE);
        mTrailer1LinearLayout.setVisibility(View.VISIBLE);
        mErrorTrailerTextView.setVisibility(View.INVISIBLE);
    }

    private void hideTrailersLayoutAndShowError() {
        mErrorTrailerTextView.setVisibility(View.INVISIBLE);
        mErrorTrailerTextView.setVisibility(View.VISIBLE);
        mTrailerProgressBar.setVisibility(View.INVISIBLE);
        mTrailer1LinearLayout.setVisibility(View.INVISIBLE);
    }

    private void showReviewsLayout(String reviewAuthor, String reviewContent) {
        mReviewAuthorTextView.setText(reviewAuthor);
        mReviewContentTextView.setText(reviewContent);
        mReviewProgressBar.setVisibility(View.INVISIBLE);
        mReviewAuthorTextView.setVisibility(View.VISIBLE);
        mReviewContentTextView.setVisibility(View.VISIBLE);
        mErrorReviewTextView.setVisibility(View.INVISIBLE);
    }

    private void hideReviewsLayoutAndShowError() {
        mReviewProgressBar.setVisibility(View.INVISIBLE);
        mReviewAuthorTextView.setVisibility(View.INVISIBLE);
        mReviewContentTextView.setVisibility(View.INVISIBLE);
        mErrorReviewTextView.setVisibility(View.VISIBLE);
    }

    /*
        This is the helper function that deletes the movie from the database.
        It takes movieId as parameter and then delete if the movie is stored in
        database.
     */
    private void removeFromFavourite(String movieId) {
        // Make the Uri
        Uri uri = FavouriteEntry.CONTENT_URI.buildUpon().appendPath(movieId).build();
        // Get content resolver and delete the row.
        int deletedRows = getContentResolver().delete(uri, null,null);
        // Check if row deleted
        if(deletedRows > 0){
            // If yes, change isFavourite flag
            isFavourite = false;
        }else {
            // If not deleted, do opp.
            isFavourite = true;
        }
        // Change the icon based on current status.
        changeFavouriteIcon();
    }

    /*
        This is the helper function and used to add the movie
        to the database favourite list for offline access. It spins
        up the loader and then save the movie detail on database.
     */
    private void addToFavourite() {
        Bundle bundle = new Bundle();
        bundle.putString(ABSOLUTE_POSTER_PATH_BUNDLE_KEY, posterAbsolutePath);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Uri> loader = loaderManager.getLoader(MOVIE_INSERT_DB_LOADER_ID);
        if(loader == null){
            loaderManager.initLoader(MOVIE_INSERT_DB_LOADER_ID, bundle, saveMovieDetailLoaderListener);
        }else {
            loaderManager.restartLoader(MOVIE_INSERT_DB_LOADER_ID, bundle, saveMovieDetailLoaderListener);
        }

    }

    /*
        This is the helper function which spins up the loader
        and download the image on background thread and then
        spins up the new loader again which then save the movie detail.
     */
    private void downloadImageAndAddToFavourite(final String movieId, final String posterPath){
        posterAbsolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/popular_movie_" + movieId + ".jpg";
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;
                        File file = new File(posterAbsolutePath);
                        try
                        {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
                            ostream.flush();
                            ostream.close();
                            posterAbsolutePath = file.getAbsolutePath();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {
                }
            }
        };
        Picasso.with(MovieDetail.this)
                .load(posterPath)
                .into(target);
        addToFavourite();
    }
    /*
        This method check if the current movie in the movie detail
        is marked favourite by the user or not. It takes movieId as
        parameter.
     */
    private void checkIsFavourite(String movieId) {
        // Build the uri.
        Uri uri = FavouriteEntry.CONTENT_URI.buildUpon().appendPath(movieId).build();
        // Get the cursor object return from the resolver query method.
        Cursor cursor = getContentResolver().query(uri, new String[]{FavouriteEntry.MOVIE_ID}, null, null, null);
        // if the cursor has item > 0 then mark the flag favourite to true.
        isFavourite = cursor.getCount() > 0;
        // close the cursor to prevent memory leaks.
        cursor.close();
        // change the favourite icon as per the condition.
        changeFavouriteIcon();
    }

    /*
     This helper method help in swapping the icon image for the fab
     */
    private void changeFavouriteIcon(){
        if(isFavourite){
            fab.setImageResource(R.drawable.ic_fav_on);
        }else {
            fab.setImageResource(R.drawable.ic_fav_off);
        }
    }

    /*
     This is the helper method extracted from the onCreate function
     where the save instance checked and update the content. It take
     bundle as the parameter which include the data of the movie after
     change in state.
     */
    private void updateAfterSaveInstance(Bundle savedInstanceState) {
        // Make the progress bar invisible.
        mTrailerProgressBar.setVisibility(View.INVISIBLE);
        mReviewProgressBar.setVisibility(View.INVISIBLE);
        // Get the value for the movie's properties.
        String reviewAuthor = savedInstanceState.getString(REVIEW_AUTHOR_KEY);
        String reviewContent = savedInstanceState.getString(REVIEW_CONTENT_KEY);
        String url = savedInstanceState.getString(TRAILER_URL_KEY);
        String synopsis = savedInstanceState.getString(MOVIE_SYNOPSIS_KEY);
        String rating = savedInstanceState.getString(MOVIE_RATING_KEY);
        String date = savedInstanceState.getString(MOVIE_DATE_KEY);
        String title = savedInstanceState.getString(MOVIE_TITLE_KEY);
        String poster = savedInstanceState.getString(MOVIE_POSTER_URL);
        String movieId = savedInstanceState.getString(MOVIE_ID_KEY);
        isFavourite = savedInstanceState.getBoolean(MOVIE_IS_FAVOURITE);

        setMovieDetails(poster, movieId, title, date, rating, synopsis, reviewAuthor, reviewContent, url);
    }

    /*
    This is the helper function that load the reviews.
    It takes movieId as the parameter and then spin up
    the loader or restart the loader and then get data from the
    internet asynchronously.
     */
    private void loadReviews(String movieId) {
        // make a bundle with the movie id in it.
        Bundle bundle = new Bundle();
        bundle.putString(MOVIE_ID_BUNDLE_KEY, movieId);

        // Get support loader manager and respected loader.
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> loader = loaderManager.getLoader(REVIEWS_LOADER_MANAGER_ID);
        // Spin or restart the loader.
        if(loader == null){
            loaderManager.initLoader(REVIEWS_LOADER_MANAGER_ID, bundle, loadReviewAndTrailerLoaderListener);
        }else {
            loaderManager.restartLoader(REVIEWS_LOADER_MANAGER_ID, bundle, loadReviewAndTrailerLoaderListener);
        }
    }
    /*
    This is the helper function that load the trailers.
    It takes movieId as the parameter and then spin up
    the loader or restart the loader and then get data from the
    internet asynchronously.
     */
    private void loadTrailers(String movieId) {
        // Make a bundle to be pass to loader.
        Bundle bundle = new Bundle();
        bundle.putString(MOVIE_ID_BUNDLE_KEY, movieId);
        // Get the supported Loader Manager and the loader.
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> loader = loaderManager.getLoader(TRAILER_LOADER_MANAGER_ID);
        // Spin up new loader or restart the previous one.
        if(loader == null){
            loaderManager.initLoader(TRAILER_LOADER_MANAGER_ID, bundle, loadReviewAndTrailerLoaderListener);
        }else {
            loaderManager.restartLoader(TRAILER_LOADER_MANAGER_ID, bundle, loadReviewAndTrailerLoaderListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
        This is a helper function that show reviews.
        Currently, It only show the 1st review. That's enough.
     */
    private void showReviews(String data) {
        // Make progress bar invisible.
        mReviewProgressBar.setVisibility(View.INVISIBLE);
        try {
            // Get the JSONObject result Array.
            JSONObject reviews = new JSONObject(data);
            JSONArray results = reviews.getJSONArray("results");
            // Check if there is no review
            if(results.length() == 0) {
                // If no review, show error and then make error visible
                // and then just return
                hideReviewsLayoutAndShowError();
                return;
            }
            // Get the review detail and put them in the textview.
            JSONObject firstReview = results.getJSONObject(0);
            String author = firstReview.getString("author");
            String content = firstReview.getString("content");

            showReviewsLayout(author, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     This is the helper function that shows the first trailer
     of the movie loaded from the internet.
     */
    private void showTrailers(String data) {
        mTrailerProgressBar.setVisibility(View.INVISIBLE);
        try {
            JSONObject trailer = new JSONObject(data);
            JSONArray results = trailer.getJSONArray("results");
            if(results.length() == 0) {
                hideTrailersLayoutAndShowError();
                return;
            }
            String id = results.getJSONObject(0).getString("key");
            Uri uri1 = NetworkUtils.getYoutubeTrailerUri(id);
            showTrailersLayout(uri1.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(REVIEW_AUTHOR_KEY, mReviewAuthorTextView.getText().toString());
        outState.putString(REVIEW_CONTENT_KEY, mReviewContentTextView.getText().toString());
        outState.putString(TRAILER_URL_KEY, mTrailer1LinearLayout.getTag().toString());
        outState.putString(MOVIE_SYNOPSIS_KEY, mMovieDescriptionTextView.getText().toString());
        outState.putString(MOVIE_DATE_KEY, mMovieYearTextView.getText().toString());
        outState.putString(MOVIE_RATING_KEY, mMovieRatingTextView.getText().toString());
        outState.putString(MOVIE_TITLE_KEY, getSupportActionBar().getTitle().toString());
        outState.putString(MOVIE_POSTER_URL, mMovieImagePosterImageView.getTag().toString());
        outState.putString(MOVIE_ID_KEY, mMovieDescriptionTextView.getTag().toString());
        outState.putBoolean(MOVIE_IS_FAVOURITE, isFavourite);
        super.onSaveInstanceState(outState);
    }

}
