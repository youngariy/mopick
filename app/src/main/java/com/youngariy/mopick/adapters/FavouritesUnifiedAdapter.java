package com.youngariy.mopick.adapters;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.youngariy.mopick.R;
import com.youngariy.mopick.activities.MovieDetailActivity;
import com.youngariy.mopick.activities.TVShowDetailActivity;
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.Favourite;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified adapter for displaying both movies and TV shows in favorites
 */
public class FavouritesUnifiedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MOVIE = 0;
    private static final int TYPE_TV_SHOW = 1;

    private Context mContext;
    private List<Object> mItems;

    public FavouritesUnifiedAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    public void setItems(List<MovieBrief> movies, List<TVShowBrief> tvShows) {
        mItems.clear();
        mItems.addAll(movies);
        mItems.addAll(tvShows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = mItems.get(position);
        if (item instanceof MovieBrief) {
            return TYPE_MOVIE;
        } else if (item instanceof TVShowBrief) {
            return TYPE_TV_SHOW;
        }
        return TYPE_MOVIE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_show_small, parent, false);
        if (viewType == TYPE_MOVIE) {
            return new MovieViewHolder(view, parent);
        } else {
            return new TVShowViewHolder(view, parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MovieViewHolder) {
            MovieBrief movie = (MovieBrief) mItems.get(position);
            bindMovieViewHolder((MovieViewHolder) holder, movie);
        } else if (holder instanceof TVShowViewHolder) {
            TVShowBrief tvShow = (TVShowBrief) mItems.get(position);
            bindTVShowViewHolder((TVShowViewHolder) holder, tvShow);
        }
    }

    private void bindMovieViewHolder(MovieViewHolder holder, MovieBrief movie) {
        Glide.with(mContext.getApplicationContext()).load(Constants.IMAGE_LOADING_BASE_URL_342 + movie.getPosterPath())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.moviePosterImageView);

        if (movie.getTitle() != null)
            holder.movieTitleTextView.setText(movie.getTitle());
        else
            holder.movieTitleTextView.setText("");

        if (movie.getVoteAverage() != null && movie.getVoteAverage() > 0) {
            holder.movieRatingTextView.setVisibility(View.VISIBLE);
            holder.movieRatingTextView.setText(String.format("%.1f", movie.getVoteAverage()) + Constants.RATING_SYMBOL);
        } else {
            holder.movieRatingTextView.setVisibility(View.GONE);
        }

        if (Favourite.isMovieFav(mContext, movie.getId())) {
            holder.movieFavImageButton.setImageResource(R.mipmap.ic_favorite_black_18dp);
        } else {
            holder.movieFavImageButton.setImageResource(R.mipmap.ic_favorite_border_black_18dp);
        }
    }

    private void bindTVShowViewHolder(TVShowViewHolder holder, TVShowBrief tvShow) {
        Glide.with(mContext.getApplicationContext()).load(Constants.IMAGE_LOADING_BASE_URL_342 + tvShow.getPosterPath())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.tvShowPosterImageView);

        if (tvShow.getName() != null)
            holder.tvShowTitleTextView.setText(tvShow.getName());
        else
            holder.tvShowTitleTextView.setText("");

        if (tvShow.getVoteAverage() != null && tvShow.getVoteAverage() > 0) {
            holder.tvShowRatingTextView.setVisibility(View.VISIBLE);
            holder.tvShowRatingTextView.setText(String.format("%.1f", tvShow.getVoteAverage()) + Constants.RATING_SYMBOL);
        } else {
            holder.tvShowRatingTextView.setVisibility(View.GONE);
        }

        if (Favourite.isTVShowFav(mContext, tvShow.getId())) {
            holder.tvShowFavImageButton.setImageResource(R.mipmap.ic_favorite_black_18dp);
        } else {
            holder.tvShowFavImageButton.setImageResource(R.mipmap.ic_favorite_border_black_18dp);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        public CardView movieCard;
        public ImageView moviePosterImageView;
        public TextView movieTitleTextView;
        public TextView movieRatingTextView;
        public ImageButton movieFavImageButton;

        public MovieViewHolder(View itemView, ViewGroup parent) {
            super(itemView);
            movieCard = (CardView) itemView.findViewById(R.id.card_view_show_card);
            moviePosterImageView = (ImageView) itemView.findViewById(R.id.image_view_show_card);
            movieTitleTextView = (TextView) itemView.findViewById(R.id.text_view_title_show_card);
            movieRatingTextView = (TextView) itemView.findViewById(R.id.text_view_rating_show_card);
            movieFavImageButton = (ImageButton) itemView.findViewById(R.id.image_button_fav_show_card);

            setupCardLayout(parent);
            setupColors();
            setupClickListeners();
        }

        private void setupCardLayout(ViewGroup parent) {
            boolean isGridLayout = false;
            int spanCount = 2;
            if (parent instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) parent;
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    isGridLayout = true;
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    spanCount = gridLayoutManager.getSpanCount();
                }
            }

            if (isGridLayout) {
                int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                int recyclerViewPadding = (int) (8 * mContext.getResources().getDisplayMetrics().density);
                int totalMargin = (int) (12 * mContext.getResources().getDisplayMetrics().density * spanCount);
                int cardWidth = (screenWidth - recyclerViewPadding - totalMargin) / spanCount;
                
                RecyclerView.LayoutParams cardParams = (RecyclerView.LayoutParams) movieCard.getLayoutParams();
                if (cardParams != null) {
                    cardParams.width = cardWidth;
                    movieCard.setLayoutParams(cardParams);
                }
                moviePosterImageView.getLayoutParams().width = cardWidth;
                moviePosterImageView.getLayoutParams().height = (int) (cardWidth / 0.66f);
            } else {
                int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                int cardWidth = (int) (screenWidth * 0.46);
                RecyclerView.LayoutParams cardParams = (RecyclerView.LayoutParams) movieCard.getLayoutParams();
                if (cardParams != null) {
                    cardParams.width = cardWidth;
                    movieCard.setLayoutParams(cardParams);
                }
                moviePosterImageView.getLayoutParams().width = cardWidth;
                moviePosterImageView.getLayoutParams().height = (int) (cardWidth / 0.66f);
            }
        }

        private void setupColors() {
            movieCard.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailSurface));
            movieTitleTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailTextPrimary));
            moviePosterImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailDivider));
            movieFavImageButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
            movieRatingTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        private void setupClickListeners() {
            movieCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MovieBrief movie = (MovieBrief) mItems.get(getAdapterPosition());
                    Intent intent = new Intent(mContext, MovieDetailActivity.class);
                    intent.putExtra(Constants.MOVIE_ID, movie.getId());
                    mContext.startActivity(intent);
                }
            });

            movieFavImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    MovieBrief movie = (MovieBrief) mItems.get(getAdapterPosition());
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

    public class TVShowViewHolder extends RecyclerView.ViewHolder {
        public CardView tvShowCard;
        public ImageView tvShowPosterImageView;
        public TextView tvShowTitleTextView;
        public TextView tvShowRatingTextView;
        public ImageButton tvShowFavImageButton;

        public TVShowViewHolder(View itemView, ViewGroup parent) {
            super(itemView);
            tvShowCard = (CardView) itemView.findViewById(R.id.card_view_show_card);
            tvShowPosterImageView = (ImageView) itemView.findViewById(R.id.image_view_show_card);
            tvShowTitleTextView = (TextView) itemView.findViewById(R.id.text_view_title_show_card);
            tvShowRatingTextView = (TextView) itemView.findViewById(R.id.text_view_rating_show_card);
            tvShowFavImageButton = (ImageButton) itemView.findViewById(R.id.image_button_fav_show_card);

            setupCardLayout(parent);
            setupColors();
            setupClickListeners();
        }

        private void setupCardLayout(ViewGroup parent) {
            boolean isGridLayout = false;
            int spanCount = 2;
            if (parent instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) parent;
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    isGridLayout = true;
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    spanCount = gridLayoutManager.getSpanCount();
                }
            }

            if (isGridLayout) {
                int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                int recyclerViewPadding = (int) (8 * mContext.getResources().getDisplayMetrics().density);
                int totalMargin = (int) (12 * mContext.getResources().getDisplayMetrics().density * spanCount);
                int cardWidth = (screenWidth - recyclerViewPadding - totalMargin) / spanCount;
                
                RecyclerView.LayoutParams cardParams = (RecyclerView.LayoutParams) tvShowCard.getLayoutParams();
                if (cardParams != null) {
                    cardParams.width = cardWidth;
                    tvShowCard.setLayoutParams(cardParams);
                }
                tvShowPosterImageView.getLayoutParams().width = cardWidth;
                tvShowPosterImageView.getLayoutParams().height = (int) (cardWidth / 0.66f);
            } else {
                int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                int cardWidth = (int) (screenWidth * 0.46);
                RecyclerView.LayoutParams cardParams = (RecyclerView.LayoutParams) tvShowCard.getLayoutParams();
                if (cardParams != null) {
                    cardParams.width = cardWidth;
                    tvShowCard.setLayoutParams(cardParams);
                }
                tvShowPosterImageView.getLayoutParams().width = cardWidth;
                tvShowPosterImageView.getLayoutParams().height = (int) (cardWidth / 0.66f);
            }
        }

        private void setupColors() {
            tvShowCard.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailSurface));
            tvShowTitleTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailTextPrimary));
            tvShowPosterImageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorMovieDetailDivider));
            tvShowFavImageButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
            tvShowRatingTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        private void setupClickListeners() {
            tvShowCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TVShowBrief tvShow = (TVShowBrief) mItems.get(getAdapterPosition());
                    Intent intent = new Intent(mContext, TVShowDetailActivity.class);
                    intent.putExtra(Constants.TV_SHOW_ID, tvShow.getId());
                    mContext.startActivity(intent);
                }
            });

            tvShowFavImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    TVShowBrief tvShow = (TVShowBrief) mItems.get(getAdapterPosition());
                    if (Favourite.isTVShowFav(mContext, tvShow.getId())) {
                        Favourite.removeTVShowFromFav(mContext, tvShow.getId());
                        tvShowFavImageButton.setImageResource(R.mipmap.ic_favorite_border_black_18dp);
                    } else {
                        Favourite.addTVShowToFav(mContext, tvShow.getId(), tvShow.getPosterPath(), tvShow.getName(), tvShow.getVoteAverage());
                        tvShowFavImageButton.setImageResource(R.mipmap.ic_favorite_black_18dp);
                    }
                }
            });
        }
    }
}

