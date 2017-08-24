package com.example.lenovo.popularmovie.data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.lenovo.popularmovie.utilities.NetworkUtils;
import com.example.lenovo.popularmovie.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by lenovo on 6/26/2017.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder>{

    private int mCount = 0;
    private Context mContext;
    private JSONArray mMoviesArray;
    private Cursor mCursor;
    final private MovieClickListener mOnClickListener;

    public interface MovieClickListener{
        void onClick(int position);
    }


    public MoviesAdapter(Context context, JSONArray jsonArray, MovieClickListener listener) {
        mContext = context;
        mMoviesArray = jsonArray;
        if(mMoviesArray != null)
            mCount = mMoviesArray.length();

        mOnClickListener = listener;
    }

    public void swapMovieArray(JSONArray jsonArray){
        mMoviesArray = jsonArray;
        mCursor = null;
        if(jsonArray != null) mCount = jsonArray.length();
        this.notifyDataSetChanged();
    }

    public void swapMovieArray(Cursor cursor){
        mCursor = cursor;
        mMoviesArray = null;
        mCount = cursor.getCount();
        this.notifyDataSetChanged();
    }

    @Override
    public MoviesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResourceId = R.layout.movie_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutResourceId, parent, shouldAttachToParentImmediately);
        return new MoviesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviesViewHolder holder, int position) {
        if(null != mMoviesArray)
            setDataFromJson(holder, position);
        else
            setDataFromCursor(holder, position);
    }

    private void setDataFromCursor(MoviesViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String posterPath = mCursor.getString(mCursor.getColumnIndex(MoviesContract.FavouriteEntry.MOVIE_POSTER));
        int movie_id = mCursor.getInt(mCursor.getColumnIndex(MoviesContract.FavouriteEntry.MOVIE_ID));


        Picasso.with(mContext)
                .load(new File(posterPath))
                .into(holder.mMovieImageView);
        holder.itemView.setTag(movie_id);
    }

    private void setDataFromJson(MoviesViewHolder holder, int position){
        try {
            JSONObject movieObj = mMoviesArray.getJSONObject(position);
            String posterPath = NetworkUtils.MOVIEDB_POSTER_PATH_BASE_URL + movieObj.getString("poster_path");
            int movie_id = movieObj.getInt("id");
            Picasso.with(mContext)
                    .load(posterPath)
                    .into(holder.mMovieImageView);
            holder.itemView.setTag(movie_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    public class MoviesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mMovieImageView;

        public MoviesViewHolder(View itemView) {
            super(itemView);
            mMovieImageView = (ImageView) itemView.findViewById(R.id.movie_image_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = getAdapterPosition();
            if(mCursor != null)
                id = Integer.parseInt(v.getTag().toString());
            mOnClickListener.onClick(id);
        }
    }
}
