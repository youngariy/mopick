package com.youngariy.mopick.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.youngariy.mopick.R;
import com.youngariy.mopick.adapters.TVShowBriefsSmallAdapter;
import com.youngariy.mopick.adapters.TVShowCastAdapter;
import com.youngariy.mopick.adapters.VideoAdapter;
import com.youngariy.mopick.broadcastreceivers.ConnectivityBroadcastReceiver;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.tvshows.Genre;
import com.youngariy.mopick.network.tvshows.Network;
import com.youngariy.mopick.network.tvshows.SimilarTVShowsResponse;
import com.youngariy.mopick.network.tvshows.TVShow;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.network.tvshows.TVShowCastBrief;
import com.youngariy.mopick.network.tvshows.TVShowCreditsResponse;
import com.youngariy.mopick.network.videos.Video;
import com.youngariy.mopick.network.videos.VideosResponse;
import com.youngariy.mopick.network.watchproviders.WatchProvider;
import com.youngariy.mopick.network.watchproviders.WatchProviderRegion;
import com.youngariy.mopick.network.watchproviders.WatchProvidersResponse;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.Favourite;
import com.youngariy.mopick.utils.LocaleHelper;
import com.youngariy.mopick.utils.NetworkConnection;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.annotation.Nullable;
import android.content.Context;

public class TVShowDetailActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }

    private int mTVShowId;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;

    private ConstraintLayout mTVShowTabLayout;
    private ImageView mPosterImageView;
    private int mPosterHeight;
    private int mPosterWidth;
    private ProgressBar mPosterProgressBar;
    private ImageView mBackdropImageView;
    private int mBackdropHeight;
    private int mBackdropWidth;
    private ProgressBar mBackdropProgressBar;
    private TextView mTitleTextView;
    private TextView mGenreTextView;
    private TextView mYearTextView;
    private ImageButton mBackImageButton;
    private ImageButton mFavImageButton;
    private ImageButton mShareImageButton;

    private LinearLayout mRatingLayout;
    private TextView mRatingTextView;

    private TextView mOverviewTextView;
    private TextView mOverviewReadMoreTextView;
    private LinearLayout mDetailsLayout;
    private TextView mFirstAirDateTextView;
    private TextView mRuntimeTextView;
    private TextView mStatusTextView;
    private TextView mOriginCountryTextView;
    private TextView mNetworksTextView;
    private TextView mWatchProvidersTextView;

    private TextView mVideosTextView;
    private RecyclerView mVideosRecyclerView;
    private List<Video> mVideos;
    private VideoAdapter mVideosAdapter;

    private View mHorizontalLine;

    private TextView mCastTextView;
    private RecyclerView mCastRecyclerView;
    private List<TVShowCastBrief> mCasts;
    private TVShowCastAdapter mCastAdapter;

    private TextView mSimilarTVShowsTextView;
    private RecyclerView mSimilarTVShowsRecyclerView;
    private List<TVShowBrief> mSimilarTVShows;
    private TVShowBriefsSmallAdapter mSimilarTVShowsAdapter;

    private Snackbar mConnectivitySnackbar;
    private ConnectivityBroadcastReceiver mConnectivityBroadcastReceiver;
    private boolean isBroadcastReceiverRegistered;
    private boolean isActivityLoaded;
    private Call<TVShow> mTVShowDetailsCall;
    private Call<VideosResponse> mVideosCall;
    private Call<TVShowCreditsResponse> mTVShowCreditsCall;
    private Call<SimilarTVShowsResponse> mSimilarTVShowsCall;
    private Call<WatchProvidersResponse> mWatchProvidersCall;

    private static final Map<String, String> WATCH_PROVIDER_DISPLAY_NAMES;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("netflix", "Netflix");
        map.put("netflix kids", "Netflix");
        map.put("disney+", "Disney+");
        map.put("disney plus", "Disney+");
        map.put("watcha", "Watcha");
        map.put("wavve", "Wavve");
        map.put("wave", "Wavve");
        map.put("amazon prime video", "Prime Video");
        map.put("apple tv+", "Apple TV+");
        map.put("apple tv plus", "Apple TV+");
        WATCH_PROVIDER_DISPLAY_NAMES = Collections.unmodifiableMap(map);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvshow_detail);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        setTitle("");

        Intent receivedIntent = getIntent();
        mTVShowId = receivedIntent.getIntExtra(Constants.TV_SHOW_ID, -1);

        if (mTVShowId == -1) finish();

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        mPosterWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.25);
        mPosterHeight = (int) (mPosterWidth / 0.66);
        mBackdropWidth = getResources().getDisplayMetrics().widthPixels;
        mBackdropHeight = (int) (mBackdropWidth / 1.77);

        mTVShowTabLayout = (ConstraintLayout) findViewById(R.id.layout_toolbar_tv_show);
        mTVShowTabLayout.getLayoutParams().height = mBackdropHeight + (int) (mPosterHeight * 0.9);

        mPosterImageView = (ImageView) findViewById(R.id.image_view_poster);
        mPosterImageView.getLayoutParams().width = mPosterWidth;
        mPosterImageView.getLayoutParams().height = mPosterHeight;
        mPosterProgressBar = findViewById(R.id.progress_bar_poster);
        mPosterProgressBar.setVisibility(View.GONE);

        mBackdropImageView = (ImageView) findViewById(R.id.image_view_backdrop);
        mBackdropImageView.getLayoutParams().height = mBackdropHeight;
        mBackdropProgressBar = findViewById(R.id.progress_bar_backdrop);
        mBackdropProgressBar.setVisibility(View.GONE);

        mTitleTextView = (TextView) findViewById(R.id.text_view_title_tv_show_detail);
        mGenreTextView = (TextView) findViewById(R.id.text_view_genre_tv_show_detail);
        mYearTextView = (TextView) findViewById(R.id.text_view_year_tv_show_detail);

        mBackImageButton = (ImageButton) findViewById(R.id.image_button_back_tv_show_detail);
        mBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mFavImageButton = (ImageButton) findViewById(R.id.image_button_fav_tv_show_detail);
        mShareImageButton = (ImageButton) findViewById(R.id.image_button_share_tv_show_detail);

        mRatingLayout = (LinearLayout) findViewById(R.id.layout_rating_tv_show_detail);
        mRatingTextView = (TextView) findViewById(R.id.text_view_rating_tv_show_detail);

        mOverviewTextView = (TextView) findViewById(R.id.text_view_overview_tv_show_detail);
        mOverviewReadMoreTextView = (TextView) findViewById(R.id.text_view_read_more_tv_show_detail);
        mDetailsLayout = (LinearLayout) findViewById(R.id.layout_details_tv_show_detail);
        mFirstAirDateTextView = findViewById(R.id.text_view_first_air_date_tv_show_detail);
        mRuntimeTextView = findViewById(R.id.text_view_runtime_tv_show_detail);
        mStatusTextView = findViewById(R.id.text_view_status_tv_show_detail);
        mOriginCountryTextView = findViewById(R.id.text_view_origin_country_tv_show_detail);
        mNetworksTextView = findViewById(R.id.text_view_networks_tv_show_detail);
        mWatchProvidersTextView = findViewById(R.id.text_view_watch_providers_tv_show_detail);

        mVideosTextView = (TextView) findViewById(R.id.text_view_trailer_tv_show_detail);
        mVideosRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_trailers_tv_show_detail);
        (new LinearSnapHelper()).attachToRecyclerView(mVideosRecyclerView);
        mVideos = new ArrayList<>();
        mVideosAdapter = new VideoAdapter(TVShowDetailActivity.this, mVideos);
        mVideosRecyclerView.setAdapter(mVideosAdapter);
        mVideosRecyclerView.setLayoutManager(new LinearLayoutManager(TVShowDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));

        mHorizontalLine = (View) findViewById(R.id.view_horizontal_line);

        mCastTextView = (TextView) findViewById(R.id.text_view_cast_tv_show_detail);
        mCastRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_cast_tv_show_detail);
        mCasts = new ArrayList<>();
        mCastAdapter = new TVShowCastAdapter(TVShowDetailActivity.this, mCasts);
        mCastRecyclerView.setAdapter(mCastAdapter);
        mCastRecyclerView.setLayoutManager(new LinearLayoutManager(TVShowDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));

        mSimilarTVShowsTextView = (TextView) findViewById(R.id.text_view_similar_tv_show_detail);
        mSimilarTVShowsRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_similar_tv_show_detail);
        mSimilarTVShows = new ArrayList<>();
        mSimilarTVShowsAdapter = new TVShowBriefsSmallAdapter(TVShowDetailActivity.this, mSimilarTVShows);
        mSimilarTVShowsRecyclerView.setAdapter(mSimilarTVShowsAdapter);
        mSimilarTVShowsRecyclerView.setLayoutManager(new LinearLayoutManager(TVShowDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));

        if (NetworkConnection.isConnected(TVShowDetailActivity.this)) {
            isActivityLoaded = true;
            loadActivity();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        mSimilarTVShowsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isActivityLoaded && !NetworkConnection.isConnected(TVShowDetailActivity.this)) {
            mConnectivitySnackbar = Snackbar.make(mTitleTextView, R.string.no_network, Snackbar.LENGTH_INDEFINITE);
            mConnectivitySnackbar.show();
            mConnectivityBroadcastReceiver = new ConnectivityBroadcastReceiver(new ConnectivityBroadcastReceiver.ConnectivityReceiverListener() {
                @Override
                public void onNetworkConnectionConnected() {
                    mConnectivitySnackbar.dismiss();
                    isActivityLoaded = true;
                    loadActivity();
                    isBroadcastReceiverRegistered = false;
                    unregisterReceiver(mConnectivityBroadcastReceiver);
                }
            });
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            isBroadcastReceiverRegistered = true;
            registerReceiver(mConnectivityBroadcastReceiver, intentFilter);
        } else if (!isActivityLoaded && NetworkConnection.isConnected(TVShowDetailActivity.this)) {
            isActivityLoaded = true;
            loadActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isBroadcastReceiverRegistered) {
            isBroadcastReceiverRegistered = false;
            unregisterReceiver(mConnectivityBroadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTVShowDetailsCall != null) mTVShowDetailsCall.cancel();
        if (mVideosCall != null) mVideosCall.cancel();
        if (mTVShowCreditsCall != null) mTVShowCreditsCall.cancel();
        if (mSimilarTVShowsCall != null) mSimilarTVShowsCall.cancel();
        if (mWatchProvidersCall != null) mWatchProvidersCall.cancel();
    }

    private void loadActivity() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(this);

        mPosterProgressBar.setVisibility(View.VISIBLE);
        mBackdropProgressBar.setVisibility(View.VISIBLE);

        mTVShowDetailsCall = apiService.getTVShowDetails(mTVShowId, getResources().getString(R.string.MOVIE_DB_API_KEY), language);
        mTVShowDetailsCall.enqueue(new Callback<TVShow>() {
            @Override
            public void onResponse(Call<TVShow> call, final Response<TVShow> response) {
                if (!response.isSuccessful()) {
                    mTVShowDetailsCall = call.clone();
                    mTVShowDetailsCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;

                mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if (appBarLayout.getTotalScrollRange() + verticalOffset == 0) {
                            if (response.body().getName() != null)
                                mCollapsingToolbarLayout.setTitle(response.body().getName());
                            else
                                mCollapsingToolbarLayout.setTitle("");
                            mToolbar.setVisibility(View.VISIBLE);
                        } else {
                            mCollapsingToolbarLayout.setTitle("");
                            mToolbar.setVisibility(View.INVISIBLE);
                        }
                    }
                });

                Glide.with(getApplicationContext()).asBitmap().load(Constants.IMAGE_LOADING_BASE_URL_1280 + response.body().getPosterPath())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                mPosterProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                mPosterProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(mPosterImageView);

                Glide.with(getApplicationContext()).asBitmap().load(Constants.IMAGE_LOADING_BASE_URL_1280 + response.body().getBackdropPath())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                mBackdropProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                mBackdropProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(mBackdropImageView);

                if (response.body().getName() != null)
                    mTitleTextView.setText(response.body().getName());
                else
                    mTitleTextView.setText("");

                setGenres(response.body().getGenres());

                setYear(response.body().getFirstAirDate());

                mFavImageButton.setVisibility(View.VISIBLE);
                mShareImageButton.setVisibility(View.VISIBLE);
                setImageButtons(response.body().getId(), response.body().getPosterPath(), response.body().getName(), response.body().getHomepage(), response.body().getVoteAverage());

                if (response.body().getVoteAverage() != null && response.body().getVoteAverage() != 0) {
                    mRatingLayout.setVisibility(View.VISIBLE);
                    mRatingTextView.setText(String.format("%.1f", response.body().getVoteAverage()));
                }

                if (response.body().getOverview() != null && !response.body().getOverview().trim().isEmpty()) {
                    mOverviewReadMoreTextView.setVisibility(View.VISIBLE);
                    mOverviewTextView.setText(response.body().getOverview());
                    mOverviewReadMoreTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOverviewTextView.setMaxLines(Integer.MAX_VALUE);
                            mDetailsLayout.setVisibility(View.VISIBLE);
                            mOverviewReadMoreTextView.setVisibility(View.GONE);
                        }
                    });
                } else {
                    mOverviewTextView.setText("");
                }

                setDetails(response.body().getFirstAirDate(), response.body().getEpisodeRunTime(), response.body().getStatus(), response.body().getOriginCountries(), response.body().getNetworks());
                setWatchProviders(apiService);

                setVideos();

                mHorizontalLine.setVisibility(View.VISIBLE);

                setCasts();

                setSimilarTVShows();
            }

            @Override
            public void onFailure(Call<TVShow> call, Throwable t) {

            }
        });
    }

    private void setGenres(List<Genre> genresList) {
        String genres = "";
        if (genresList != null) {
            for (int i = 0; i < genresList.size(); i++) {
                if (genresList.get(i) == null) continue;
                if (i == genresList.size() - 1) {
                    genres = genres.concat(genresList.get(i).getGenreName());
                } else {
                    genres = genres.concat(genresList.get(i).getGenreName() + ", ");
                }
            }
        }
        mGenreTextView.setText(genres);
    }

    private void setYear(String firstAirDateString) {
        if (firstAirDateString != null && !firstAirDateString.trim().isEmpty()) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
            try {
                Date firstAirDate = sdf1.parse(firstAirDateString);
                mYearTextView.setText(sdf2.format(firstAirDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            mYearTextView.setText("");
        }
    }

    private void setImageButtons(final Integer tvShowId, final String posterPath, final String tvShowName, final String homepage, final Double voteAverage) {
        if (tvShowId == null) return;
        if (Favourite.isTVShowFav(TVShowDetailActivity.this, tvShowId)) {
            mFavImageButton.setTag(Constants.TAG_FAV);
            mFavImageButton.setImageResource(R.mipmap.ic_favorite_white_24dp);
        } else {
            mFavImageButton.setTag(Constants.TAG_NOT_FAV);
            mFavImageButton.setImageResource(R.mipmap.ic_favorite_border_white_24dp);
        }
        mFavImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if ((int) mFavImageButton.getTag() == Constants.TAG_FAV) {
                    Favourite.removeTVShowFromFav(TVShowDetailActivity.this, tvShowId);
                    mFavImageButton.setTag(Constants.TAG_NOT_FAV);
                    mFavImageButton.setImageResource(R.mipmap.ic_favorite_border_white_24dp);
                } else {
                    Favourite.addTVShowToFav(TVShowDetailActivity.this, tvShowId, posterPath, tvShowName, voteAverage);
                    mFavImageButton.setTag(Constants.TAG_FAV);
                    mFavImageButton.setImageResource(R.mipmap.ic_favorite_white_24dp);
                }
            }
        });
        mShareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent movieShareIntent = new Intent(Intent.ACTION_SEND);
                movieShareIntent.setType("text/plain");
                String extraText = "";
                if (tvShowName != null) extraText += tvShowName + "\n";
                if (homepage != null) extraText += homepage;
                movieShareIntent.putExtra(Intent.EXTRA_TEXT, extraText);
                startActivity(movieShareIntent);

            }
        });
    }

    private void setDetails(String firstAirDateString, List<Integer> runtime, String status, List<String> originCountries, List<Network> networks) {
        String firstAirDateDisplay = "-";
        if (firstAirDateString != null && !firstAirDateString.trim().isEmpty()) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("MMM d, yyyy");
            try {
                Date releaseDate = sdf1.parse(firstAirDateString);
                firstAirDateDisplay = sdf2.format(releaseDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        mFirstAirDateTextView.setText(firstAirDateDisplay);

        String runtimeDisplay = "-";
        if (runtime != null && !runtime.isEmpty() && runtime.get(0) != null && runtime.get(0) > 0) {
            int runtimeValue = runtime.get(0);
            if (runtimeValue < 60) {
                runtimeDisplay = runtimeValue + " min(s)";
            } else {
                runtimeDisplay = (runtimeValue / 60) + " hr " + (runtimeValue % 60) + " mins";
            }
        }
        mRuntimeTextView.setText(runtimeDisplay);

        if (status != null && !status.trim().isEmpty()) {
            mStatusTextView.setText(status);
        } else {
            mStatusTextView.setText("-");
        }

        String originDisplay = "-";
        if (originCountries != null && !originCountries.isEmpty()) {
            List<String> cleaned = new ArrayList<>();
            for (String country : originCountries) {
                if (country == null || country.trim().isEmpty()) continue;
                cleaned.add(country.trim());
            }
            if (!cleaned.isEmpty()) {
                originDisplay = TextUtils.join(", ", cleaned);
            }
        }
        mOriginCountryTextView.setText(originDisplay);

        String networksDisplay = "-";
        if (networks != null && !networks.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (Network network : networks) {
                if (network == null || network.getName() == null || network.getName().trim().isEmpty()) continue;
                names.add(network.getName().trim());
            }
            if (!names.isEmpty()) {
                networksDisplay = TextUtils.join(", ", names);
            }
        }
        mNetworksTextView.setText(networksDisplay);
    }

    private void setWatchProviders(ApiInterface apiService) {
        mWatchProvidersTextView.setVisibility(View.GONE);
        mWatchProvidersTextView.setText("");
        mWatchProvidersCall = apiService.getTVShowWatchProviders(mTVShowId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mWatchProvidersCall.enqueue(new Callback<WatchProvidersResponse>() {
            @Override
            public void onResponse(Call<WatchProvidersResponse> call, Response<WatchProvidersResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showWatchProvidersUnavailable();
                    return;
                }
                String region = LocaleHelper.getRegionCode(TVShowDetailActivity.this);
                WatchProviderRegion regionData = response.body().getRegion(region);
                if (regionData == null && !"US".equals(region)) {
                    regionData = response.body().getRegion("US");
                }
                if (regionData == null || regionData.getFlatrate() == null || regionData.getFlatrate().isEmpty()) {
                    showWatchProvidersUnavailable();
                    return;
                }
                List<String> providers = extractProviderNames(regionData.getFlatrate());
                if (providers.isEmpty()) {
                    showWatchProvidersUnavailable();
                    return;
                }
                String providersText = TextUtils.join(", ", providers);
                String formatted = getString(R.string.watch_providers_available, providersText);
                mWatchProvidersTextView.setText(formatted);
                mWatchProvidersTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<WatchProvidersResponse> call, Throwable t) {
                showWatchProvidersUnavailable();
            }
        });
    }

    private List<String> extractProviderNames(List<WatchProvider> providerList) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (WatchProvider provider : providerList) {
            if (provider == null || provider.getProviderName() == null) continue;
            String displayName = formatProviderName(provider.getProviderName().trim());
            if (!displayName.isEmpty()) {
                names.add(displayName);
            }
        }
        return new ArrayList<>(names);
    }

    private String formatProviderName(String rawName) {
        if (rawName == null) return "";
        String key = rawName.trim().toLowerCase();
        if (WATCH_PROVIDER_DISPLAY_NAMES.containsKey(key)) {
            return WATCH_PROVIDER_DISPLAY_NAMES.get(key);
        }
        return rawName.trim();
    }

    private void showWatchProvidersUnavailable() {
        mWatchProvidersTextView.setText(R.string.watch_providers_not_available);
        mWatchProvidersTextView.setVisibility(View.VISIBLE);
    }

    private void setVideos() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(this);
        mVideosCall = apiService.getTVShowVideos(mTVShowId, getResources().getString(R.string.MOVIE_DB_API_KEY), language);
        mVideosCall.enqueue(new Callback<VideosResponse>() {
            @Override
            public void onResponse(Call<VideosResponse> call, Response<VideosResponse> response) {
                if (!response.isSuccessful()) {
                    mVideosCall = call.clone();
                    mVideosCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getVideos() == null) return;

                for (Video video : response.body().getVideos()) {
                    if (video != null && video.getSite() != null && video.getSite().equals("YouTube") && video.getType() != null && video.getType().equals("Trailer"))
                        mVideos.add(video);
                }
                if (!mVideos.isEmpty())
                    mVideosTextView.setVisibility(View.VISIBLE);
                mVideosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<VideosResponse> call, Throwable t) {

            }
        });
    }

    private void setCasts() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        mTVShowCreditsCall = apiService.getTVShowCredits(mTVShowId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mTVShowCreditsCall.enqueue(new Callback<TVShowCreditsResponse>() {
            @Override
            public void onResponse(Call<TVShowCreditsResponse> call, Response<TVShowCreditsResponse> response) {
                if (!response.isSuccessful()) {
                    mTVShowCreditsCall = call.clone();
                    mTVShowCreditsCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getCasts() == null) return;

                for (TVShowCastBrief castBrief : response.body().getCasts()) {
                    if (castBrief != null && castBrief.getName() != null)
                        mCasts.add(castBrief);
                }

                if (!mCasts.isEmpty())
                    mCastTextView.setVisibility(View.VISIBLE);
                mCastAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<TVShowCreditsResponse> call, Throwable t) {

            }
        });
    }

    private void setSimilarTVShows() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(this);
        mSimilarTVShowsCall = apiService.getSimilarTVShows(mTVShowId, getResources().getString(R.string.MOVIE_DB_API_KEY), 1, language);
        mSimilarTVShowsCall.enqueue(new Callback<SimilarTVShowsResponse>() {
            @Override
            public void onResponse(Call<SimilarTVShowsResponse> call, Response<SimilarTVShowsResponse> response) {
                if (!response.isSuccessful()) {
                    mSimilarTVShowsCall = call.clone();
                    mSimilarTVShowsCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                for (TVShowBrief tvShowBrief : response.body().getResults()) {
                    if (tvShowBrief != null && tvShowBrief.getName() != null && tvShowBrief.getPosterPath() != null)
                        mSimilarTVShows.add(tvShowBrief);
                }

                if (!mSimilarTVShows.isEmpty())
                    mSimilarTVShowsTextView.setVisibility(View.VISIBLE);
                mSimilarTVShowsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<SimilarTVShowsResponse> call, Throwable t) {

            }
        });
    }

}




