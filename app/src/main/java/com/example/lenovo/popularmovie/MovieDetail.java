package com.example.lenovo.popularmovie;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieDetail extends AppCompatActivity {
    private ImageView imageView;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mRating;
    private TextView mOverview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        imageView = (ImageView) findViewById(R.id.iv_thumbnail);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        mRating = (TextView) findViewById(R.id.tv_rating);
        mOverview = (TextView) findViewById(R.id.tv_overview);

        Intent intent = getIntent();
        if(intent.hasExtra("movie_object"))
        {
            try {
                JSONObject jsonObject = new JSONObject(intent.getStringExtra("movie_object"));
                String posterPath = NetworkUtils.MOVIEDB_POSTER_PATH_BASE_URL + jsonObject.getString("poster_path");
                Picasso.with(this).load(posterPath).into(imageView);
                mTitle.setText(jsonObject.getString("original_title"));
                mReleaseDate.setText(jsonObject.getString("release_date"));
                mRating.setText(jsonObject.getString("vote_average"));
                mOverview.setText(jsonObject.getString("overview"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
