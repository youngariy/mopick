package com.youngariy.mopick.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.youngariy.mopick.R;
import com.youngariy.mopick.adapters.SlotMovieTitleAdapter;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.movies.Movie;
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.network.movies.TopRatedMoviesResponse;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.utils.Favourite;
import com.youngariy.mopick.utils.LocaleHelper;
import com.youngariy.mopick.utils.NetworkConnection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendActivity extends AppCompatActivity {

    private RecyclerView mSlotRecyclerView;
    private SlotMovieTitleAdapter mSlotAdapter;
    private List<String> mSlotTitles;
    private CardView mSlotCardView;
    private CardView mMovieDetailsCardView;
    private TextView mSlotTitleTextView;
    private ImageView mMoviePosterImageView;
    private TextView mMovieTitleTextView;
    private TextView mMovieOverviewTextView;
    private TextView mMovieRatingTextView;
    private TextView mMovieReleaseDateTextView;

    private List<MovieBrief> mAllTopRatedMovies;
    private List<MovieBrief> mRecommendedMovies;
    private MovieBrief mSelectedMovie;
    private boolean mIsSpinning = false;
    private Random mRandom = new Random();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        // 상단바 색상 설정
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorMovieDetailBackground));
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.recommend);

        initViews();
        loadTopRatedMovies();
    }

    private void initViews() {
        mSlotRecyclerView = findViewById(R.id.recycler_view_slot);
        mSlotCardView = findViewById(R.id.card_view_slot_machine);
        mMovieDetailsCardView = findViewById(R.id.card_view_movie_details);
        mSlotTitleTextView = findViewById(R.id.text_view_slot_title);
        mMoviePosterImageView = findViewById(R.id.image_view_movie_poster);
        mMovieTitleTextView = findViewById(R.id.text_view_movie_title);
        mMovieOverviewTextView = findViewById(R.id.text_view_movie_overview);
        mMovieRatingTextView = findViewById(R.id.text_view_movie_rating);
        mMovieReleaseDateTextView = findViewById(R.id.text_view_movie_release_date);

        mSlotTitles = new ArrayList<>();
        mSlotAdapter = new SlotMovieTitleAdapter(mSlotTitles);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSlotRecyclerView.setLayoutManager(layoutManager);
        mSlotRecyclerView.setAdapter(mSlotAdapter);
        mSlotRecyclerView.setNestedScrollingEnabled(false);
        
        // RecyclerView를 중앙에 위치시키기 위해 스크롤 리스너 추가
        mSlotRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !mIsSpinning) {
                    snapToCenter();
                }
            }
        });

        // 당기기 제스처 설정 - CardView 전체가 아닌 슬롯 머신 영역만
        findViewById(R.id.frame_slot_machine).setOnTouchListener(new SwipeToSpinListener());
    }

    private void loadTopRatedMovies() {
        if (!NetworkConnection.isConnected(this)) {
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        mAllTopRatedMovies = new ArrayList<>();
        loadTopRatedMoviesPage(1);
    }

    private void loadTopRatedMoviesPage(int page) {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(this);
        String region = LocaleHelper.getRegionCode(this);

        Call<TopRatedMoviesResponse> call = apiService.getTopRatedMovies(
            getString(R.string.MOVIE_DB_API_KEY), page, region, language);
        
        call.enqueue(new Callback<TopRatedMoviesResponse>() {
            @Override
            public void onResponse(@NonNull Call<TopRatedMoviesResponse> call, 
                                 @NonNull Response<TopRatedMoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null && 
                    response.body().getResults() != null) {
                    
                    for (MovieBrief movie : response.body().getResults()) {
                        if (movie != null && movie.getTitle() != null && 
                            movie.getPosterPath() != null) {
                            mAllTopRatedMovies.add(movie);
                        }
                    }

                    // 다음 페이지가 있으면 계속 로드
                    if (page < response.body().getTotalPages() && page < 10) {
                        loadTopRatedMoviesPage(page + 1);
                    } else {
                        // 모든 영화 로드 완료, 추천 알고리즘 적용
                        applyRecommendationAlgorithm();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TopRatedMoviesResponse> call, 
                                @NonNull Throwable t) {
                Toast.makeText(RecommendActivity.this, R.string.error_loading_movies, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyRecommendationAlgorithm() {
        // 즐겨찾기한 영화와 TV 쇼 가져오기
        List<MovieBrief> favMovies = Favourite.getFavMovieBriefs(this);
        List<TVShowBrief> favTVShows = Favourite.getFavTVShowBriefs(this);

        // 즐겨찾기의 카테고리 수집
        Set<Integer> favoriteGenres = new HashSet<>();
        for (MovieBrief movie : favMovies) {
            if (movie.getGenreIds() != null) {
                favoriteGenres.addAll(movie.getGenreIds());
            }
        }
        for (TVShowBrief tvShow : favTVShows) {
            if (tvShow.getGenreIds() != null) {
                favoriteGenres.addAll(tvShow.getGenreIds());
            }
        }

        // 추천 영화 필터링
        mRecommendedMovies = new ArrayList<>();
        if (!favoriteGenres.isEmpty()) {
            // 즐겨찾기 카테고리와 겹치는 영화 우선 추천
            for (MovieBrief movie : mAllTopRatedMovies) {
                if (movie.getGenreIds() != null) {
                    for (Integer genreId : movie.getGenreIds()) {
                        if (favoriteGenres.contains(genreId)) {
                            mRecommendedMovies.add(movie);
                            break;
                        }
                    }
                }
            }
        }

        // 추천 영화가 적으면 전체 최고 평점 영화 추가
        if (mRecommendedMovies.size() < 20) {
            for (MovieBrief movie : mAllTopRatedMovies) {
                if (!mRecommendedMovies.contains(movie)) {
                    mRecommendedMovies.add(movie);
                }
                if (mRecommendedMovies.size() >= 50) break;
            }
        }

        // 슬롯 머신에 표시할 제목 리스트 생성
        updateSlotTitles();
    }

    private void updateSlotTitles() {
        mSlotTitles.clear();
        List<String> originalTitles = new ArrayList<>();
        for (MovieBrief movie : mRecommendedMovies) {
            originalTitles.add(movie.getTitle());
        }
        
        // 순환을 위해 앞뒤로 복사 (무한 스크롤 효과)
        for (int i = 0; i < 10; i++) {
            mSlotTitles.addAll(originalTitles);
        }
        
        mSlotAdapter.notifyDataSetChanged();
        
        // 초기 위치를 중간으로 설정
        new Handler().postDelayed(() -> {
            if (mSlotTitles.size() > 0) {
                int startPosition = mSlotTitles.size() / 2;
                mSlotRecyclerView.scrollToPosition(startPosition);
            }
        }, 100);
    }

    private class SwipeToSpinListener implements View.OnTouchListener {
        private float startY;
        private boolean isDragging = false;

        @Override
        public boolean onTouch(View v, android.view.MotionEvent event) {
            if (mIsSpinning || mRecommendedMovies == null || mRecommendedMovies.isEmpty()) {
                return false;
            }

            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    isDragging = false;
                    return true;

                case android.view.MotionEvent.ACTION_MOVE:
                    float deltaY = event.getY() - startY;
                    if (Math.abs(deltaY) > 50 && !isDragging) {
                        isDragging = true;
                    }
                    return true;

                case android.view.MotionEvent.ACTION_UP:
                    if (isDragging && event.getY() - startY > 100) {
                        // 아래로 당김 - 슬롯 머신 시작
                        startSlotMachine();
                        return true;
                    }
                    isDragging = false;
                    return false;
            }
            return false;
        }
    }

    private void startSlotMachine() {
        if (mIsSpinning || mRecommendedMovies == null || mRecommendedMovies.isEmpty()) {
            return;
        }

        mIsSpinning = true;
        mSlotTitleTextView.setText(R.string.recommending);
        mMovieDetailsCardView.setVisibility(View.GONE);

        // 애니메이션 시작
        int spinCount = 20 + mRandom.nextInt(10); // 20-30번 회전
        animateSlotMachine(spinCount);
    }

    private void animateSlotMachine(int spinCount) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mSlotRecyclerView.getLayoutManager();
        if (layoutManager == null) return;

        View firstVisibleView = layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition());
        if (firstVisibleView == null) return;

        int itemHeight = firstVisibleView.getHeight();
        if (itemHeight == 0) itemHeight = 120; // 기본 높이

        // 빠른 스크롤
        int scrollAmount = itemHeight;
        if (spinCount > 0) {
            mSlotRecyclerView.smoothScrollBy(0, scrollAmount);
            
            // 점점 느려지게
            int delay = spinCount > 15 ? 20 : (spinCount > 10 ? 40 : (spinCount > 5 ? 60 : 100));
            new Handler().postDelayed(() -> animateSlotMachine(spinCount - 1), delay);
        } else {
            // 최종 선택 - 중앙에 정렬
            new Handler().postDelayed(() -> {
                snapToCenter();
                new Handler().postDelayed(() -> {
                    int finalPosition = layoutManager.findFirstVisibleItemPosition();
                    selectFinalMovie(finalPosition);
                }, 300);
            }, 300);
        }
    }

    private void snapToCenter() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mSlotRecyclerView.getLayoutManager();
        if (layoutManager == null) return;

        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        if (firstVisiblePosition < 0) return;

        View firstVisibleView = layoutManager.findViewByPosition(firstVisiblePosition);
        if (firstVisibleView == null) return;

        int itemHeight = firstVisibleView.getHeight();
        if (itemHeight == 0) itemHeight = 120; // 기본 높이
        
        int recyclerHeight = mSlotRecyclerView.getHeight();
        int centerY = recyclerHeight / 2;
        int itemTop = firstVisibleView.getTop();
        int itemCenterY = itemTop + itemHeight / 2;
        int offset = centerY - itemCenterY;

        // 중앙으로 스냅
        if (Math.abs(offset) > 5) {
            mSlotRecyclerView.smoothScrollBy(0, offset);
        }
    }

    private void selectFinalMovie(int position) {
        // 실제 영화 선택 (순환을 고려)
        int actualIndex = position % mRecommendedMovies.size();
        if (actualIndex < 0) actualIndex = 0;
        if (actualIndex >= mRecommendedMovies.size()) actualIndex = mRecommendedMovies.size() - 1;
        
        mSelectedMovie = mRecommendedMovies.get(actualIndex);

        // 선택된 영화 제목 표시
        mSlotTitleTextView.setText(mSelectedMovie.getTitle());

        // 영화 상세 정보 로드
        loadMovieDetails(mSelectedMovie.getId());

        mIsSpinning = false;
    }

    private void loadMovieDetails(Integer movieId) {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(this);

        Call<Movie> call = apiService.getMovieDetails(
            movieId, getString(R.string.MOVIE_DB_API_KEY), language);
        
        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayMovieDetails(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                // 기본 정보만 표시
                displayMovieDetailsFromBrief(mSelectedMovie);
            }
        });
    }

    private void displayMovieDetails(Movie movie) {
        mMovieTitleTextView.setText(movie.getTitle());
        mMovieOverviewTextView.setText(movie.getOverview());
        mMovieRatingTextView.setText(String.valueOf(movie.getVoteAverage()));
        mMovieReleaseDateTextView.setText(movie.getReleaseDate());

        // 포스터 이미지 로드
        if (movie.getPosterPath() != null) {
            String imageUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
            Glide.with(this)
                .load(imageUrl)
                .into(mMoviePosterImageView);
        }

        mMovieDetailsCardView.setVisibility(View.VISIBLE);
    }

    private void displayMovieDetailsFromBrief(MovieBrief movie) {
        mMovieTitleTextView.setText(movie.getTitle());
        mMovieOverviewTextView.setText("");
        if (movie.getVoteAverage() != null) {
            mMovieRatingTextView.setText(String.valueOf(movie.getVoteAverage()));
        }
        if (movie.getReleaseDate() != null) {
            mMovieReleaseDateTextView.setText(movie.getReleaseDate());
        }

        // 포스터 이미지 로드
        if (movie.getPosterPath() != null) {
            String imageUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
            Glide.with(this)
                .load(imageUrl)
                .into(mMoviePosterImageView);
        }

        mMovieDetailsCardView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}

