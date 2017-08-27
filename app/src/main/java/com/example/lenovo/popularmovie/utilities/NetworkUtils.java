package com.example.lenovo.popularmovie.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.lenovo.popularmovie.BuildConfig;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lenovo on 6/28/2017.
 */

/**
 * This is the helper class contains static methods
 * for networking calls and stuff like those.
 */
public class NetworkUtils {

    // Movie Db Api Key
    private final static String API_KEY = BuildConfig.THE_MOVIE_DB_API_TOKEN;
    // Movie Db Base Api Address
    private final static String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie";
    // Base address for the poster of movies
    public final static String MOVIEDB_POSTER_PATH_BASE_URL = "http://image.tmdb.org/t/p/w185";

    final static String PARAM_API_KEY = "api_key";
    public final static String PATH_POPULAR = "popular";
    public final static String PATH_TOP_RATED = "top_rated";

    // Endpoints.
    public final static String TRAILER_ENDPOINT = "videos";
    public final static String REVIEWS_ENDPOINT = "reviews";
    public final static String YOUTUBE_WATCH_ENDPOINT = "https://www.youtube.com/watch";

    /**
     * This helper function checks if the network is available or not
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    /**This is the helper function which returns the URL
     * for the movies to fetch from MovieDb Api.
     * @param sortedPath - It's string which directs to popular or rating endpoint.
     * @return URL
     */
    public static URL buildUrl(String sortedPath){
        // The endpoint from where movies to be fetch.
        String endpoint = MOVIEDB_BASE_URL + "/" + sortedPath;
        // Build the Uri from the endpoint string.
        Uri builtUri = Uri.parse(endpoint).buildUpon()
                        .appendQueryParameter(PARAM_API_KEY, API_KEY)
                        .build();

        URL url = null;
        // Make Url and the return it.
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This function fetch the data from the Internet using OkHttp
     * @param url - The url to fetch data from
     * @return String of data from the internet.
     * @throws IOException
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        // New OkHttp client with timeouts
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // Creating a request object.
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Get Response from the client
        Response response = client.newCall(request).execute();
        //Get the response body
        String result = null;
        //check if response is successful
        if(response.isSuccessful()) {
            result = response.body().string();
        }
        //close the response.
        response.close();
        return result;

    }

    /**
     *  This function return the Url to load the review for a movie
     * @param movieId - String of movie id to fetch the reviews.
     * @return Url  to load review for the particular movie
     */
    public static URL getReviewsUrl(String movieId){
        String endpoint = MOVIEDB_BASE_URL + "/" + movieId + "/" + REVIEWS_ENDPOINT;
        Uri builtUri = Uri.parse(endpoint).buildUpon()
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return url;
    }

    /**
     * This function return the Url to load the trailer for a movie
     * @param movieId - String of movie id to fetch the reviews.
     * @return Url to load trailer for the particular movie
     */
    public static URL getTrailersUrl(String movieId){
        String endpoint = MOVIEDB_BASE_URL + "/" + movieId + "/" + TRAILER_ENDPOINT;
        Uri builtUri = Uri.parse(endpoint).buildUpon()
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return url;
    }

    /**
     * This function takes the key and return youtube video Uri.
     * @param key Youtube video key
     * @return youtube Uri of the trailer.
     */
    public static Uri getYoutubeTrailerUri(String key){
        Uri builtUri = Uri.parse(YOUTUBE_WATCH_ENDPOINT)
                .buildUpon()
                .appendQueryParameter("v", key)
                .build();

        return builtUri;
    }

}
