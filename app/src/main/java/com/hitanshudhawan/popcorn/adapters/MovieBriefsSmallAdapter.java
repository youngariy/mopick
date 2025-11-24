package com.hitanshudhawan.popcorn.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hitanshudhawan.popcorn.R;
import com.hitanshudhawan.popcorn.activities.MovieDetailActivity;
import com.hitanshudhawan.popcorn.network.movies.MovieBrief;
import com.hitanshudhawan.popcorn.utils.Constants;
import com.hitanshudhawan.popcorn.utils.Favourite;

import java.util.List;

/**
 * Created by hitanshu on 31/7/17.
 */

public class MovieBriefsSmallAdapter extends RecyclerView.Adapter<MovieBriefsSmallAdapter.MovieViewHolder> {

    private Context mContext;
    private List<MovieBrief> mMovies;

    public MovieBriefsSmallAdapter(Context context, List<MovieBrief> movies) {
        mContext = context;
        mMovies = movies;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MovieViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_show_small, parent, false));
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Glide.with(mContext.getApplicationContext()).load(Constants.IMAGE_LOADING_BASE_URL_342 + mMovies.get(position).getPosterPath())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.moviePosterImageView);

        if (mMovies.get(position).getTitle() != null)
            holder.movieTitleTextView.setText(mMovies.get(position).getTitle());
        else
            holder.movieTitleTextView.setText("");

        if (Favourite.isMovieFav(mContext, mMovies.get(position).getId())) {
            holder.movieFavImageButton.setImageResource(R.mipmap.ic_favorite_black_18dp);
        } else {
            holder.movieFavImageButton.setImageResource(R.mipmap.ic_favorite_border_black_18dp);
        }
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        public CardView movieCard;
        public ImageView moviePosterImageView;
        public TextView movieTitleTextView;
        public ImageButton movieFavImageButton;


        public MovieViewHolder(View itemView) {
            super(itemView);
            movieCard = (CardView) itemView.findViewById(R.id.card_view_show_card);
            moviePosterImageView = (ImageView) itemView.findViewById(R.id.image_view_show_card);
            movieTitleTextView = (TextView) itemView.findViewById(R.id.text_view_title_show_card);
            movieFavImageButton = (ImageButton) itemView.findViewById(R.id.image_button_fav_show_card);

            // Target two cards per row in favorites; width accounts for margins.
            moviePosterImageView.getLayoutParams().width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.45);
            moviePosterImageView.getLayoutParams().height = (int) ((mContext.getResources().getDisplayMetrics().widthPixels * 0.45) / 0.66);
            movieCard.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailSurface));
            movieTitleTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailTextPrimary));
            moviePosterImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailDivider));
            movieFavImageButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));

            movieCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MovieDetailActivity.class);
                    intent.putExtra(Constants.MOVIE_ID, mMovies.get(getAdapterPosition()).getId());
                    mContext.startActivity(intent);
                }
            });

            movieFavImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    MovieBrief movie = mMovies.get(getAdapterPosition());
                    if (Favourite.isMovieFav(mContext, movie.getId())) {
                        Favourite.removeMovieFromFav(mContext, movie.getId());
                        movieFavImageButton.setImageResource(R.mipmap.ic_favorite_border_black_18dp);
                    } else {
                        Favourite.addMovieToFav(mContext, movie.getId(), movie.getPosterPath(), movie.getTitle());
                        movieFavImageButton.setImageResource(R.mipmap.ic_favorite_black_18dp);
                    }
                }
            });
        }
    }

}
