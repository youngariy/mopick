package com.hitanshudhawan.popcorn.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hitanshudhawan.popcorn.R;
import com.hitanshudhawan.popcorn.adapters.MovieBriefsSmallAdapter;
import com.hitanshudhawan.popcorn.network.ApiClient;
import com.hitanshudhawan.popcorn.network.ApiInterface;
import com.hitanshudhawan.popcorn.network.movies.MovieBrief;
import com.hitanshudhawan.popcorn.network.movies.NowShowingMoviesResponse;
import com.hitanshudhawan.popcorn.network.movies.PopularMoviesResponse;
import com.hitanshudhawan.popcorn.network.movies.TopRatedMoviesResponse;
import com.hitanshudhawan.popcorn.network.movies.UpcomingMoviesResponse;
import com.hitanshudhawan.popcorn.utils.Constants;
import com.hitanshudhawan.popcorn.utils.NetworkConnection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAllMoviesActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<MovieBrief> mMovies;
    private MovieBriefsSmallAdapter mMoviesAdapter;

    private String mMovieType; // Changed to String

    private boolean pagesOver = false;
    private int presentPage = 1;
    private boolean loading = true;
    private int previousTotal = 0;
    private int visibleThreshold = 5;

    private Call<NowShowingMoviesResponse> mNowShowingMoviesCall;
    private Call<PopularMoviesResponse> mPopularMoviesCall;
    private Call<UpcomingMoviesResponse> mUpcomingMoviesCall;
    private Call<TopRatedMoviesResponse> mTopRatedMoviesCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_movies);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent receivedIntent = getIntent();
        mMovieType = receivedIntent.getStringExtra(Constants.VIEW_ALL_MOVIES_TYPE); // Changed to getStringExtra

        if (mMovieType == null) {
            finish();
            return;
        }

        // Replaced switch with if-else if for String comparison
        if (mMovieType.equals(Constants.NOW_SHOWING_MOVIES_TYPE)) {
            setTitle(R.string.now_showing_movies);
        } else if (mMovieType.equals(Constants.POPULAR_MOVIES_TYPE)) {
            setTitle(R.string.popular_movies);
        } else if (mMovieType.equals(Constants.UPCOMING_MOVIES_TYPE)) {
            setTitle(R.string.upcoming_movies);
        } else if (mMovieType.equals(Constants.TOP_RATED_MOVIES_TYPE)) {
            setTitle(R.string.top_rated_movies);
        }

        mRecyclerView = findViewById(R.id.recycler_view_view_all);
        mMovies = new ArrayList<>();
        mMoviesAdapter = new MovieBriefsSmallAdapter(this, mMovies);
        mRecyclerView.setAdapter(mMoviesAdapter);
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
                    if (NetworkConnection.isConnected(ViewAllMoviesActivity.this)) {
                        loadMovies(mMovieType);
                    } else {
                        Toast.makeText(ViewAllMoviesActivity.this, R.string.no_network, Toast.LENGTH_SHORT).show();
                    }
                    loading = true;
                }
            }
        });

        if (NetworkConnection.isConnected(this)) {
            loadMovies(mMovieType);
        } else {
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMoviesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNowShowingMoviesCall != null) mNowShowingMoviesCall.cancel();
        if (mPopularMoviesCall != null) mPopularMoviesCall.cancel();
        if (mUpcomingMoviesCall != null) mUpcomingMoviesCall.cancel();
        if (mTopRatedMoviesCall != null) mTopRatedMoviesCall.cancel();
    }

    private void loadMovies(String movieType) { // Changed parameter to String
        if (pagesOver) return;

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        // Replaced switch with if-else if for String comparison
        if (movieType.equals(Constants.NOW_SHOWING_MOVIES_TYPE)) {
            mNowShowingMoviesCall = apiService.getNowShowingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, "US");
            mNowShowingMoviesCall.enqueue(new Callback<NowShowingMoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<NowShowingMoviesResponse> call, @NonNull Response<NowShowingMoviesResponse> response) {
                    if (!response.isSuccessful()) {
                        mNowShowingMoviesCall = call.clone();
                        mNowShowingMoviesCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) return;

                    for (MovieBrief movieBrief : response.body().getResults()) {
                        if (movieBrief != null && movieBrief.getTitle() != null && movieBrief.getPosterPath() != null)
                            mMovies.add(movieBrief);
                    }
                    mMoviesAdapter.notifyDataSetChanged();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<NowShowingMoviesResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        } else if (movieType.equals(Constants.POPULAR_MOVIES_TYPE)) {
            mPopularMoviesCall = apiService.getPopularMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, "US");
            mPopularMoviesCall.enqueue(new Callback<PopularMoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<PopularMoviesResponse> call, @NonNull Response<PopularMoviesResponse> response) {
                    if (!response.isSuccessful()) {
                        mPopularMoviesCall = call.clone();
                        mPopularMoviesCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) return;

                    for (MovieBrief movieBrief : response.body().getResults()) {
                        if (movieBrief != null && movieBrief.getTitle() != null && movieBrief.getPosterPath() != null)
                            mMovies.add(movieBrief);
                    }
                    mMoviesAdapter.notifyDataSetChanged();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<PopularMoviesResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        } else if (movieType.equals(Constants.UPCOMING_MOVIES_TYPE)) {
            mUpcomingMoviesCall = apiService.getUpcomingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, "US");
            mUpcomingMoviesCall.enqueue(new Callback<UpcomingMoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<UpcomingMoviesResponse> call, @NonNull Response<UpcomingMoviesResponse> response) {
                    if (!response.isSuccessful()) {
                        mUpcomingMoviesCall = call.clone();
                        mUpcomingMoviesCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) return;

                    for (MovieBrief movieBrief : response.body().getResults()) {
                        if (movieBrief != null && movieBrief.getTitle() != null && movieBrief.getPosterPath() != null)
                            mMovies.add(movieBrief);
                    }
                    mMoviesAdapter.notifyDataSetChanged();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<UpcomingMoviesResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        } else if (movieType.equals(Constants.TOP_RATED_MOVIES_TYPE)) {
            mTopRatedMoviesCall = apiService.getTopRatedMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, "US");
            mTopRatedMoviesCall.enqueue(new Callback<TopRatedMoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<TopRatedMoviesResponse> call, @NonNull Response<TopRatedMoviesResponse> response) {
                    if (!response.isSuccessful()) {
                        mTopRatedMoviesCall = call.clone();
                        mTopRatedMoviesCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) return;

                    for (MovieBrief movieBrief : response.body().getResults()) {
                        if (movieBrief != null && movieBrief.getTitle() != null && movieBrief.getPosterPath() != null)
                            mMovies.add(movieBrief);
                    }
                    mMoviesAdapter.notifyDataSetChanged();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<TopRatedMoviesResponse> call, @NonNull Throwable t) {
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
