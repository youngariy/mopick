package com.youngariy.mopick.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import androidx.appcompat.widget.PopupMenu;

import androidx.annotation.NonNull;

import com.youngariy.mopick.R;
import com.youngariy.mopick.adapters.MovieBriefsSmallAdapter;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.movies.Genre;
import com.youngariy.mopick.network.movies.GenresList;
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.network.movies.NowShowingMoviesResponse;
import com.youngariy.mopick.network.movies.PopularMoviesResponse;
import com.youngariy.mopick.network.movies.TopRatedMoviesResponse;
import com.youngariy.mopick.network.movies.UpcomingMoviesResponse;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.LocaleHelper;
import com.youngariy.mopick.utils.MovieGenres;
import com.youngariy.mopick.utils.NetworkConnection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;

public class ViewAllMoviesActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }

    private RecyclerView mRecyclerView;
    private List<MovieBrief> mMovies;
    private MovieBriefsSmallAdapter mMoviesAdapter;

    private String mMovieType; // Changed to String
    private Integer mSelectedGenreId = null;
    private MaterialButton mGenreButton;
    private List<Genre> mGenres;
    private TextView mEmptyStateTextView;

    // 원본 데이터 저장
    private List<MovieBrief> mOriginalMovies = new ArrayList<>();

    private boolean pagesOver = false;
    private int presentPage = 1;
    private boolean loading = true;
    private int previousTotal = 0;
    private int visibleThreshold = 5;
    private boolean mHasLoadedAtLeastOnce = false;

    private Call<GenresList> mGenresListCall;
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
        mSelectedGenreId = receivedIntent.hasExtra(Constants.SELECTED_GENRE_ID) ? 
            receivedIntent.getIntExtra(Constants.SELECTED_GENRE_ID, -1) : null;
        if (mSelectedGenreId != null && mSelectedGenreId == -1) {
            mSelectedGenreId = null;
        }

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
        mGenreButton = findViewById(R.id.button_genre_filter);
        mEmptyStateTextView = findViewById(R.id.text_view_empty_state);
        mMovies = new ArrayList<>();
        mMoviesAdapter = new MovieBriefsSmallAdapter(this, mMovies);
        mRecyclerView.setAdapter(mMoviesAdapter);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        // 상단바 색상 설정
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorMovieDetailBackground));
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false);
        }

        // 장르 목록 로드
        loadGenresList();

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
        if (mGenresListCall != null) mGenresListCall.cancel();
        if (mNowShowingMoviesCall != null) mNowShowingMoviesCall.cancel();
        if (mPopularMoviesCall != null) mPopularMoviesCall.cancel();
        if (mUpcomingMoviesCall != null) mUpcomingMoviesCall.cancel();
        if (mTopRatedMoviesCall != null) mTopRatedMoviesCall.cancel();
    }

    private void loadGenresList() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(this);
        mGenresListCall = apiService.getMovieGenresList(getResources().getString(R.string.MOVIE_DB_API_KEY), language);
        mGenresListCall.enqueue(new Callback<GenresList>() {
            @Override
            public void onResponse(@NonNull Call<GenresList> call, @NonNull Response<GenresList> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getGenres() != null) {
                    MovieGenres.loadGenresList(response.body().getGenres());
                    mGenres = response.body().getGenres();
                    loadGenresForButton();
                    // 장르 필터가 이미 설정되어 있으면 즉시 필터링 적용
                    if (mSelectedGenreId != null) {
                        applyGenreFilter();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenresList> call, @NonNull Throwable t) {
                // Handle failure
            }
        });
    }

    private void loadGenresForButton() {
        if (mGenres == null || mGenres.isEmpty()) {
            return;
        }

        // 전달받은 장르 ID가 있으면 해당 장르 이름 표시, 없으면 "카테고리" 표시
        if (mSelectedGenreId != null) {
            for (Genre genre : mGenres) {
                if (genre.getId().equals(mSelectedGenreId)) {
                    mGenreButton.setText(genre.getGenreName());
                    break;
                }
            }
        } else {
            mGenreButton.setText(getString(R.string.select_genre));
        }

        mGenreButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mGenreButton);
            
            // 모든 장르 메뉴 아이템 추가
            for (int i = 0; i < mGenres.size(); i++) {
                Genre genre = mGenres.get(i);
                popupMenu.getMenu().add(0, i, 0, genre.getGenreName());
            }
            
            popupMenu.setOnMenuItemClickListener(item -> {
                int position = item.getItemId();
                mSelectedGenreId = mGenres.get(position).getId();
                mGenreButton.setText(mGenres.get(position).getGenreName());
                applyGenreFilter();
                return true;
            });
            
            popupMenu.show();
        });
    }

    private void applyGenreFilter() {
        mMovies.clear();
        if (mSelectedGenreId == null) {
            mMovies.addAll(mOriginalMovies);
        } else {
            for (MovieBrief movie : mOriginalMovies) {
                if (movie.getGenreIds() != null && movie.getGenreIds().contains(mSelectedGenreId)) {
                    mMovies.add(movie);
                }
            }
        }
        mMoviesAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (mEmptyStateTextView == null) return;
        boolean showEmpty = mHasLoadedAtLeastOnce && mMovies.isEmpty();
        mEmptyStateTextView.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
    }

    private void loadMovies(String movieType) { // Changed parameter to String
        if (pagesOver) return;

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(this);
        String region = LocaleHelper.getRegionCode(this);

        // Replaced switch with if-else if for String comparison
        if (movieType.equals(Constants.NOW_SHOWING_MOVIES_TYPE)) {
            mNowShowingMoviesCall = apiService.getNowShowingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, region, language);
            mNowShowingMoviesCall.enqueue(new Callback<NowShowingMoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<NowShowingMoviesResponse> call, @NonNull Response<NowShowingMoviesResponse> response) {
                    if (!response.isSuccessful()) {
                        mNowShowingMoviesCall = call.clone();
                        mNowShowingMoviesCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) {
                        mHasLoadedAtLeastOnce = true;
                        applyGenreFilter();
                        return;
                    }

                    for (MovieBrief movieBrief : response.body().getResults()) {
                        if (movieBrief != null && movieBrief.getTitle() != null && movieBrief.getPosterPath() != null)
                            mOriginalMovies.add(movieBrief);
                    }
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<NowShowingMoviesResponse> call, @NonNull Throwable t) {
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                }
            });
        } else if (movieType.equals(Constants.POPULAR_MOVIES_TYPE)) {
            mPopularMoviesCall = apiService.getPopularMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, region, language);
            mPopularMoviesCall.enqueue(new Callback<PopularMoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<PopularMoviesResponse> call, @NonNull Response<PopularMoviesResponse> response) {
                    if (!response.isSuccessful()) {
                        mPopularMoviesCall = call.clone();
                        mPopularMoviesCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) {
                        mHasLoadedAtLeastOnce = true;
                        applyGenreFilter();
                        return;
                    }

                    for (MovieBrief movieBrief : response.body().getResults()) {
                        if (movieBrief != null && movieBrief.getTitle() != null && movieBrief.getPosterPath() != null)
                            mOriginalMovies.add(movieBrief);
                    }
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<PopularMoviesResponse> call, @NonNull Throwable t) {
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                }
            });
        } else if (movieType.equals(Constants.UPCOMING_MOVIES_TYPE)) {
            mUpcomingMoviesCall = apiService.getUpcomingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, region, language);
            mUpcomingMoviesCall.enqueue(new Callback<UpcomingMoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<UpcomingMoviesResponse> call, @NonNull Response<UpcomingMoviesResponse> response) {
                    if (!response.isSuccessful()) {
                        mUpcomingMoviesCall = call.clone();
                        mUpcomingMoviesCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) {
                        mHasLoadedAtLeastOnce = true;
                        applyGenreFilter();
                        return;
                    }

                    for (MovieBrief movieBrief : response.body().getResults()) {
                        if (movieBrief != null && movieBrief.getTitle() != null && movieBrief.getPosterPath() != null)
                            mOriginalMovies.add(movieBrief);
                    }
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<UpcomingMoviesResponse> call, @NonNull Throwable t) {
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                }
            });
        } else if (movieType.equals(Constants.TOP_RATED_MOVIES_TYPE)) {
            mTopRatedMoviesCall = apiService.getTopRatedMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, region, language);
            mTopRatedMoviesCall.enqueue(new Callback<TopRatedMoviesResponse>() {
                @Override
                public void onResponse(@NonNull Call<TopRatedMoviesResponse> call, @NonNull Response<TopRatedMoviesResponse> response) {
                    if (!response.isSuccessful()) {
                        mTopRatedMoviesCall = call.clone();
                        mTopRatedMoviesCall.enqueue(this);
                        return;
                    }
                    if (response.body() == null || response.body().getResults() == null) {
                        mHasLoadedAtLeastOnce = true;
                        applyGenreFilter();
                        return;
                    }

                    for (MovieBrief movieBrief : response.body().getResults()) {
                        if (movieBrief != null && movieBrief.getTitle() != null && movieBrief.getPosterPath() != null)
                            mOriginalMovies.add(movieBrief);
                    }
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<TopRatedMoviesResponse> call, @NonNull Throwable t) {
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
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




