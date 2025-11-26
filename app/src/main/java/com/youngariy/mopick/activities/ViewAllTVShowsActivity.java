package com.youngariy.mopick.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.youngariy.mopick.R;
import com.youngariy.mopick.adapters.TVShowBriefsSmallAdapter;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.tvshows.AiringTodayTVShowsResponse;
import com.youngariy.mopick.network.tvshows.OnTheAirTVShowsResponse;
import com.youngariy.mopick.network.tvshows.PopularTVShowsResponse;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.network.tvshows.TopRatedTVShowsResponse;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.NetworkConnection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAllTVShowsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<TVShowBrief> mTVShows;
    private TVShowBriefsSmallAdapter mTVShowsAdapter;

    private String mTVShowType; // Changed to String

    private boolean pagesOver = false;
    private int presentPage = 1;
    private boolean loading = true;
    private int previousTotal = 0;
    private int visibleThreshold = 5;

    private Call<AiringTodayTVShowsResponse> mAiringTodayTVShowsCall;
    private Call<OnTheAirTVShowsResponse> mOnTheAirTVShowsCall;
    private Call<PopularTVShowsResponse> mPopularTVShowsCall;
    private Call<TopRatedTVShowsResponse> mTopRatedTVShowsCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_tvshows);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent receivedIntent = getIntent();
        mTVShowType = receivedIntent.getStringExtra(Constants.VIEW_ALL_TV_SHOWS_TYPE); // Changed to getStringExtra

        if (mTVShowType == null) {
            finish();
            return;
        }

        // Replaced switch with if-else if for String comparison
        if (mTVShowType.equals(Constants.AIRING_TODAY_TV_SHOWS_TYPE)) {
            setTitle(R.string.airing_today_tv_shows);
        } else if (mTVShowType.equals(Constants.ON_THE_AIR_TV_SHOWS_TYPE)) {
            setTitle(R.string.on_the_air_tv_shows);
        } else if (mTVShowType.equals(Constants.POPULAR_TV_SHOWS_TYPE)) {
            setTitle(R.string.popular_tv_shows);
        } else if (mTVShowType.equals(Constants.TOP_RATED_TV_SHOWS_TYPE)) {
            setTitle(R.string.top_rated_tv_shows);
        }

        mRecyclerView = findViewById(R.id.recycler_view_view_all);
        mTVShows = new ArrayList<>();
        mTVShowsAdapter = new TVShowBriefsSmallAdapter(this, mTVShows);
        mRecyclerView.setAdapter(mTVShowsAdapter);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = gridLayoutManager.getChildCount();
                int totalItemCount = gridLayoutManager.getItemCount();
                int firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    if (NetworkConnection.isConnected(ViewAllTVShowsActivity.this)) {
                        loadTVShows(mTVShowType);
                    } else {
                        Toast.makeText(ViewAllTVShowsActivity.this, R.string.no_network, Toast.LENGTH_SHORT).show();
                    }
                    loading = true;
                }
            }
        });

        if (NetworkConnection.isConnected(this)) {
            loadTVShows(mTVShowType);
        } else {
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTVShowsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAiringTodayTVShowsCall != null) mAiringTodayTVShowsCall.cancel();
        if (mOnTheAirTVShowsCall != null) mOnTheAirTVShowsCall.cancel();
        if (mPopularTVShowsCall != null) mPopularTVShowsCall.cancel();
        if (mTopRatedTVShowsCall != null) mTopRatedTVShowsCall.cancel();
    }

    private void loadTVShows(String tvShowType) { // Changed parameter to String
        if (pagesOver) return;

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        // Replaced switch with if-else if for String comparison
        if (tvShowType.equals(Constants.AIRING_TODAY_TV_SHOWS_TYPE)) {
            mAiringTodayTVShowsCall = apiService.getAiringTodayTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage);
            mAiringTodayTVShowsCall.enqueue(new Callback<AiringTodayTVShowsResponse>() {
                @Override
                public void onResponse(@NonNull Call<AiringTodayTVShowsResponse> call, @NonNull Response<AiringTodayTVShowsResponse> response) {
                    if (!response.isSuccessful()) {
                        mAiringTodayTVShowsCall = call.clone();
                        mAiringTodayTVShowsCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) return;

                    for (TVShowBrief tvShowBrief : response.body().getResults()) {
                        if (tvShowBrief != null && tvShowBrief.getName() != null && tvShowBrief.getPosterPath() != null)
                            mTVShows.add(tvShowBrief);
                    }
                    mTVShowsAdapter.notifyDataSetChanged();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<AiringTodayTVShowsResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        } else if (tvShowType.equals(Constants.ON_THE_AIR_TV_SHOWS_TYPE)) {
            mOnTheAirTVShowsCall = apiService.getOnTheAirTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage);
            mOnTheAirTVShowsCall.enqueue(new Callback<OnTheAirTVShowsResponse>() {
                @Override
                public void onResponse(@NonNull Call<OnTheAirTVShowsResponse> call, @NonNull Response<OnTheAirTVShowsResponse> response) {
                    if (!response.isSuccessful()) {
                        mOnTheAirTVShowsCall = call.clone();
                        mOnTheAirTVShowsCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) return;

                    for (TVShowBrief tvShowBrief : response.body().getResults()) {
                        if (tvShowBrief != null && tvShowBrief.getName() != null && tvShowBrief.getPosterPath() != null)
                            mTVShows.add(tvShowBrief);
                    }
                    mTVShowsAdapter.notifyDataSetChanged();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<OnTheAirTVShowsResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        } else if (tvShowType.equals(Constants.POPULAR_TV_SHOWS_TYPE)) {
            mPopularTVShowsCall = apiService.getPopularTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage);
            mPopularTVShowsCall.enqueue(new Callback<PopularTVShowsResponse>() {
                @Override
                public void onResponse(@NonNull Call<PopularTVShowsResponse> call, @NonNull Response<PopularTVShowsResponse> response) {
                    if (!response.isSuccessful()) {
                        mPopularTVShowsCall = call.clone();
                        mPopularTVShowsCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) return;

                    for (TVShowBrief tvShowBrief : response.body().getResults()) {
                        if (tvShowBrief != null && tvShowBrief.getName() != null && tvShowBrief.getPosterPath() != null)
                            mTVShows.add(tvShowBrief);
                    }
                    mTVShowsAdapter.notifyDataSetChanged();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<PopularTVShowsResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        } else if (tvShowType.equals(Constants.TOP_RATED_TV_SHOWS_TYPE)) {
            mTopRatedTVShowsCall = apiService.getTopRatedTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage);
            mTopRatedTVShowsCall.enqueue(new Callback<TopRatedTVShowsResponse>() {
                @Override
                public void onResponse(@NonNull Call<TopRatedTVShowsResponse> call, @NonNull Response<TopRatedTVShowsResponse> response) {
                    if (!response.isSuccessful()) {
                        mTopRatedTVShowsCall = call.clone();
                        mTopRatedTVShowsCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) return;

                    for (TVShowBrief tvShowBrief : response.body().getResults()) {
                        if (tvShowBrief != null && tvShowBrief.getName() != null && tvShowBrief.getPosterPath() != null)
                            mTVShows.add(tvShowBrief);
                    }
                    mTVShowsAdapter.notifyDataSetChanged();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<TopRatedTVShowsResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}




