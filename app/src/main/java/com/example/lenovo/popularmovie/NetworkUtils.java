package com.example.lenovo.popularmovie;

import android.net.Uri;
import android.util.Log;

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

public class NetworkUtils {


    private final static String API_KEY = "208ef74101e203e08118b26201fdc3f2";
    private final static String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie";
    public final static String MOVIEDB_POSTER_PATH_BASE_URL = "http://image.tmdb.org/t/p/w185";

    final static String PARAM_API_KEY = "api_key";
    final static String PATH_POPULAR = "popular";
    final static String PATH_TOP_RATED = "top_rated";

    public static URL buildUrl(String sortedPath){
        String endpoint = MOVIEDB_BASE_URL + "/" + sortedPath;
        Uri builtUri = Uri.parse(endpoint).buildUpon()
                        .appendQueryParameter(PARAM_API_KEY, API_KEY)
                        .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

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

}
