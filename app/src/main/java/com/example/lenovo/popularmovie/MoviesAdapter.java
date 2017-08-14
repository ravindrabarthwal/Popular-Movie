package com.example.lenovo.popularmovie;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lenovo on 6/26/2017.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder>{

    private int mCount = 0;
    private Context mContext;
    private JSONArray mMoviesArray;
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
        if(jsonArray != null) mCount = jsonArray.length();
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
        try {
            JSONObject movieObj = mMoviesArray.getJSONObject(position);
            String posterPath = NetworkUtils.MOVIEDB_POSTER_PATH_BASE_URL + movieObj.getString("poster_path");
            int id = movieObj.getInt("id");
            Picasso.with(mContext)
                    .load(posterPath)
                    .placeholder(R.drawable.unnamed)
                    .into(holder.mMovieImageView);
            holder.itemView.setTag(id);
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
            mOnClickListener.onClick(id);
        }
    }
}
