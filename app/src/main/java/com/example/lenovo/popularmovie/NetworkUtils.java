package com.example.lenovo.popularmovie;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by lenovo on 6/28/2017.
 */

public class NetworkUtils {


    private final static String API_KEY = "";
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
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try{
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if(hasInput)
                return scanner.next();
            else
                return null;
        } finally {
            urlConnection.disconnect();
        }

    }

}
