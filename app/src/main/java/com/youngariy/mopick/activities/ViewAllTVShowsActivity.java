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
import com.youngariy.mopick.adapters.TVShowBriefsSmallAdapter;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.tvshows.AiringTodayTVShowsResponse;
import com.youngariy.mopick.network.tvshows.Genre;
import com.youngariy.mopick.network.tvshows.GenresList;
import com.youngariy.mopick.network.tvshows.OnTheAirTVShowsResponse;
import com.youngariy.mopick.network.tvshows.PopularTVShowsResponse;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.network.tvshows.TopRatedTVShowsResponse;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.LocaleHelper;
import com.youngariy.mopick.utils.NetworkConnection;
import com.youngariy.mopick.utils.TVShowGenres;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;

public class ViewAllTVShowsActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }

    private RecyclerView mRecyclerView;
    private List<TVShowBrief> mTVShows;
    private TVShowBriefsSmallAdapter mTVShowsAdapter;

    private String mTVShowType; // Changed to String
    private Integer mSelectedGenreId = null;
    private MaterialButton mGenreButton;
    private List<Genre> mGenres;
    private TextView mEmptyStateTextView;

    // 원본 데이터 저장
    private List<TVShowBrief> mOriginalTVShows = new ArrayList<>();

    private boolean pagesOver = false;
    private int presentPage = 1;
    private boolean loading = true;
    private int previousTotal = 0;
    private int visibleThreshold = 5;
    private boolean mHasLoadedAtLeastOnce = false;

    private Call<GenresList> mGenresListCall;
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
        mSelectedGenreId = receivedIntent.hasExtra(Constants.SELECTED_GENRE_ID) ? 
            receivedIntent.getIntExtra(Constants.SELECTED_GENRE_ID, -1) : null;
        if (mSelectedGenreId != null && mSelectedGenreId == -1) {
            mSelectedGenreId = null;
        }

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
        mGenreButton = findViewById(R.id.button_genre_filter);
        mEmptyStateTextView = findViewById(R.id.text_view_empty_state);
        mTVShows = new ArrayList<>();
        mTVShowsAdapter = new TVShowBriefsSmallAdapter(this, mTVShows);
        mRecyclerView.setAdapter(mTVShowsAdapter);
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
        if (mGenresListCall != null) mGenresListCall.cancel();
        if (mAiringTodayTVShowsCall != null) mAiringTodayTVShowsCall.cancel();
        if (mOnTheAirTVShowsCall != null) mOnTheAirTVShowsCall.cancel();
        if (mPopularTVShowsCall != null) mPopularTVShowsCall.cancel();
        if (mTopRatedTVShowsCall != null) mTopRatedTVShowsCall.cancel();
    }

    private void loadGenresList() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(this);
        mGenresListCall = apiService.getTVShowGenresList(getResources().getString(R.string.MOVIE_DB_API_KEY), language);
        mGenresListCall.enqueue(new Callback<GenresList>() {
            @Override
            public void onResponse(@NonNull Call<GenresList> call, @NonNull Response<GenresList> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getGenres() != null) {
                    TVShowGenres.loadGenresList(response.body().getGenres());
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
        mTVShows.clear();
        if (mSelectedGenreId == null) {
            mTVShows.addAll(mOriginalTVShows);
        } else {
            for (TVShowBrief tvShow : mOriginalTVShows) {
                if (tvShow.getGenreIds() != null && tvShow.getGenreIds().contains(mSelectedGenreId)) {
                    mTVShows.add(tvShow);
                }
            }
        }
        mTVShowsAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (mEmptyStateTextView == null) return;
        boolean showEmpty = mHasLoadedAtLeastOnce && mTVShows.isEmpty();
        mEmptyStateTextView.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
    }

    private void loadTVShows(String tvShowType) { // Changed parameter to String
        if (pagesOver) return;

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(this);

        // Replaced switch with if-else if for String comparison
        if (tvShowType.equals(Constants.AIRING_TODAY_TV_SHOWS_TYPE)) {
            mAiringTodayTVShowsCall = apiService.getAiringTodayTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, language);
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
                            mOriginalTVShows.add(tvShowBrief);
                    }
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<AiringTodayTVShowsResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        } else if (tvShowType.equals(Constants.ON_THE_AIR_TV_SHOWS_TYPE)) {
            mOnTheAirTVShowsCall = apiService.getOnTheAirTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, language);
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
                            mOriginalTVShows.add(tvShowBrief);
                    }
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<OnTheAirTVShowsResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        } else if (tvShowType.equals(Constants.POPULAR_TV_SHOWS_TYPE)) {
            mPopularTVShowsCall = apiService.getPopularTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, language);
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
                            mOriginalTVShows.add(tvShowBrief);
                    }
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
                    if (response.body().getPage() == response.body().getTotalPages()) pagesOver = true;
                    else presentPage++;
                }

                @Override
                public void onFailure(@NonNull Call<PopularTVShowsResponse> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        } else if (tvShowType.equals(Constants.TOP_RATED_TV_SHOWS_TYPE)) {
            mTopRatedTVShowsCall = apiService.getTopRatedTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage, language);
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
                            mOriginalTVShows.add(tvShowBrief);
                    }
                    mHasLoadedAtLeastOnce = true;
                    applyGenreFilter();
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




