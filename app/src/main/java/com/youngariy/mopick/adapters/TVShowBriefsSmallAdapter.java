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
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.youngariy.mopick.R;
import com.youngariy.mopick.activities.TVShowDetailActivity;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.Favourite;

import java.util.List;

/**
 * Created by hitanshu on 13/8/17.
 */

public class TVShowBriefsSmallAdapter extends RecyclerView.Adapter<TVShowBriefsSmallAdapter.TVShowViewHolder> {

    private Context mContext;
    private List<TVShowBrief> mTVShows;

    public TVShowBriefsSmallAdapter(Context context, List<TVShowBrief> tvShows) {
        mContext = context;
        mTVShows = tvShows;
    }

    @Override
    public TVShowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TVShowViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_show_small, parent, false));
    }

    @Override
    public void onBindViewHolder(TVShowViewHolder holder, int position) {

        Glide.with(mContext.getApplicationContext()).load(Constants.IMAGE_LOADING_BASE_URL_342 + mTVShows.get(position).getPosterPath())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.tvShowPosterImageView);

        if (mTVShows.get(position).getName() != null)
            holder.tvShowTitleTextView.setText(mTVShows.get(position).getName());
        else
            holder.tvShowTitleTextView.setText("");

        if (mTVShows.get(position).getVoteAverage() != null && mTVShows.get(position).getVoteAverage() > 0) {
            holder.tvShowRatingTextView.setVisibility(View.VISIBLE);
            holder.tvShowRatingTextView.setText(String.format("%.1f", mTVShows.get(position).getVoteAverage()) + Constants.RATING_SYMBOL);
        } else {
            holder.tvShowRatingTextView.setVisibility(View.GONE);
        }

        if (Favourite.isTVShowFav(mContext, mTVShows.get(position).getId())) {
            holder.tvShowFavImageButton.setImageResource(R.mipmap.ic_favorite_black_18dp);
        } else {
            holder.tvShowFavImageButton.setImageResource(R.mipmap.ic_favorite_border_black_18dp);
        }
    }

    @Override
    public int getItemCount() {
        return mTVShows.size();
    }

    public class TVShowViewHolder extends RecyclerView.ViewHolder {

        public CardView tvShowCard;
        public ImageView tvShowPosterImageView;
        public TextView tvShowTitleTextView;
        public TextView tvShowRatingTextView;
        public ImageButton tvShowFavImageButton;


        public TVShowViewHolder(View itemView) {
            super(itemView);
            tvShowCard = (CardView) itemView.findViewById(R.id.card_view_show_card);
            tvShowPosterImageView = (ImageView) itemView.findViewById(R.id.image_view_show_card);
            tvShowTitleTextView = (TextView) itemView.findViewById(R.id.text_view_title_show_card);
            tvShowRatingTextView = (TextView) itemView.findViewById(R.id.text_view_rating_show_card);
            tvShowFavImageButton = (ImageButton) itemView.findViewById(R.id.image_button_fav_show_card);

            // Target max two cards visible per viewport in horizontal lists.
            int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            int cardWidth = (int) (screenWidth * 0.46);
            RecyclerView.LayoutParams cardParams = (RecyclerView.LayoutParams) tvShowCard.getLayoutParams();
            if (cardParams != null) {
                cardParams.width = cardWidth;
                tvShowCard.setLayoutParams(cardParams);
            }
            tvShowPosterImageView.getLayoutParams().width = cardWidth;
            tvShowPosterImageView.getLayoutParams().height = (int) (cardWidth / 0.66f);
            tvShowRatingTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));

            tvShowCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, TVShowDetailActivity.class);
                    intent.putExtra(Constants.TV_SHOW_ID, mTVShows.get(getAdapterPosition()).getId());
                    mContext.startActivity(intent);
                }
            });

            tvShowFavImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    if (Favourite.isTVShowFav(mContext, mTVShows.get(getAdapterPosition()).getId())) {
                        Favourite.removeTVShowFromFav(mContext, mTVShows.get(getAdapterPosition()).getId());
                        tvShowFavImageButton.setImageResource(R.mipmap.ic_favorite_border_black_18dp);
                    } else {
                        Favourite.addTVShowToFav(mContext, mTVShows.get(getAdapterPosition()).getId(), mTVShows.get(getAdapterPosition()).getPosterPath(), mTVShows.get(getAdapterPosition()).getName(), mTVShows.get(getAdapterPosition()).getVoteAverage());
                        tvShowFavImageButton.setImageResource(R.mipmap.ic_favorite_black_18dp);
                    }
                }
            });
        }
    }

}




