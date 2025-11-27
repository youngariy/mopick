package com.youngariy.mopick.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.youngariy.mopick.R;
import com.youngariy.mopick.activities.MovieDetailActivity;
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.Favourite;
import com.youngariy.mopick.utils.MovieGenres;

import java.util.List;

/**
 * Created by hitanshu on 30/7/17.
 */

public class MovieBriefsLargeAdapter extends RecyclerView.Adapter<MovieBriefsLargeAdapter.MovieViewHolder> {

    private Context mContext;
    private List<MovieBrief> mMovies;
    private boolean mFullWidthCards;

    public MovieBriefsLargeAdapter(Context context, List<MovieBrief> movies, boolean fullWidthCards) {
        mContext = context;
        mMovies = movies;
        mFullWidthCards = fullWidthCards;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MovieViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_show_large, parent, false));
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Glide.with(mContext.getApplicationContext()).load(Constants.IMAGE_LOADING_BASE_URL_780 + mMovies.get(position).getBackdropPath())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.moviePosterImageView);

        String title = mMovies.get(position).getTitle();
        if (title != null && !title.trim().isEmpty()) {
            holder.movieTitleTextView.setText(title);
            holder.movieTitleTextView.setVisibility(View.VISIBLE);
        } else {
            holder.movieTitleTextView.setVisibility(View.GONE);
        }

        if (mMovies.get(position).getVoteAverage() != null && mMovies.get(position).getVoteAverage() > 0) {
            holder.movieRatingTextView.setVisibility(View.VISIBLE);
            holder.movieRatingTextView.setText(String.format("%.1f", mMovies.get(position).getVoteAverage()) + Constants.RATING_SYMBOL);
        } else {
            holder.movieRatingTextView.setVisibility(View.GONE);
        }

        setGenres(holder, mMovies.get(position));

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

    private void setGenres(MovieViewHolder holder, MovieBrief movie) {
        String genreString = "";
        if (movie.getGenreIds() == null) {
            holder.movieGenreTextView.setText("");
            return;
        }
        for (int i = 0; i < movie.getGenreIds().size(); i++) {
            if (movie.getGenreIds().get(i) == null) continue;
            if (MovieGenres.getGenreName(movie.getGenreIds().get(i)) == null) continue;
            genreString += MovieGenres.getGenreName(movie.getGenreIds().get(i)) + ", ";
        }
        if (!genreString.isEmpty())
            holder.movieGenreTextView.setText(genreString.substring(0, genreString.length() - 2));
        else
            holder.movieGenreTextView.setText("");
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        public CardView movieCard;
        public RelativeLayout imageLayout;
        public ImageView moviePosterImageView;
        public TextView movieTitleTextView;
        public TextView movieRatingTextView;
        public TextView movieGenreTextView;
        public ImageButton movieFavImageButton;


        public MovieViewHolder(View itemView) {
            super(itemView);
            movieCard = (CardView) itemView.findViewById(R.id.card_view_show_card);
            imageLayout = (RelativeLayout) itemView.findViewById(R.id.image_layout_show_card);
            moviePosterImageView = (ImageView) itemView.findViewById(R.id.image_view_show_card);
            movieTitleTextView = (TextView) itemView.findViewById(R.id.text_view_title_show_card);
            movieRatingTextView = (TextView) itemView.findViewById(R.id.text_view_rating_show_card);
            movieGenreTextView = (TextView) itemView.findViewById(R.id.text_view_genre_show_card);
            movieFavImageButton = (ImageButton) itemView.findViewById(R.id.image_button_fav_show_card);

            int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            // Leave breathing room so overlay text (rating/star) doesn't clip at edges.
            float widthFactor = mFullWidthCards ? 0.9f : 0.82f;
            RecyclerView.LayoutParams cardParams = (RecyclerView.LayoutParams) movieCard.getLayoutParams();
            if (cardParams != null) {
                cardParams.width = (int) (screenWidth * widthFactor);
                movieCard.setLayoutParams(cardParams);
            }
            imageLayout.getLayoutParams().width = (int) (screenWidth * widthFactor);
            imageLayout.getLayoutParams().height = (int) ((screenWidth * widthFactor) / 1.77);
            movieCard.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailSurface));
            movieGenreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailTextSecondary));
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
                        Favourite.addMovieToFav(mContext, movie.getId(), movie.getPosterPath(), movie.getTitle(), movie.getVoteAverage());
                        movieFavImageButton.setImageResource(R.mipmap.ic_favorite_black_18dp);
                    }
                }
            });
        }
    }

}




