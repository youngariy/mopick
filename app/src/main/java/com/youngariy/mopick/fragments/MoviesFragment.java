package com.youngariy.mopick.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import androidx.appcompat.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.youngariy.mopick.R;
import com.youngariy.mopick.activities.MovieDetailActivity;
import com.youngariy.mopick.activities.ViewAllMoviesActivity;
import com.youngariy.mopick.adapters.MovieBriefsLargeAdapter;
import com.youngariy.mopick.adapters.MovieBriefsSmallAdapter;
import com.youngariy.mopick.adapters.SlotMovieTitleAdapter;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.movies.Genre;
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.network.movies.TopRatedMoviesResponse;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.Favourite;
import com.youngariy.mopick.utils.LocaleHelper;
import com.youngariy.mopick.utils.MovieGenres;
import com.youngariy.mopick.utils.NetworkConnection;
import com.youngariy.mopick.viewmodels.MoviesViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoviesFragment extends Fragment {

    private ProgressBar mProgressBar;

    private FrameLayout mNowShowingLayout;
    private RecyclerView mNowShowingRecyclerView;
    private MovieBriefsLargeAdapter mNowShowingAdapter;
    private List<MovieBrief> mNowShowingMovies;

    private FrameLayout mPopularLayout;
    private RecyclerView mPopularRecyclerView;
    private MovieBriefsSmallAdapter mPopularAdapter;
    private List<MovieBrief> mPopularMovies;

    private FrameLayout mUpcomingLayout;
    private RecyclerView mUpcomingRecyclerView;
    private MovieBriefsLargeAdapter mUpcomingAdapter;
    private List<MovieBrief> mUpcomingMovies;

    private FrameLayout mTopRatedLayout;
    private RecyclerView mTopRatedRecyclerView;
    private MovieBriefsSmallAdapter mTopRatedAdapter;
    private List<MovieBrief> mTopRatedMovies;

    private MaterialButton mGenreButton;
    private List<Genre> mGenres;
    private Integer mSelectedGenreId = null;
    private TextView mEmptyStateTextView;

    // 원본 데이터 저장
    private List<MovieBrief> mOriginalNowShowingMovies = new ArrayList<>();
    private List<MovieBrief> mOriginalPopularMovies = new ArrayList<>();
    private List<MovieBrief> mOriginalUpcomingMovies = new ArrayList<>();
    private List<MovieBrief> mOriginalTopRatedMovies = new ArrayList<>();

    private MoviesViewModel mMoviesViewModel;
    private boolean mHasLoadedAtLeastOnce = false;

    // 슬롯머신 관련
    private ScrollView mScrollView;
    private FrameLayout mLayoutSlotMachine;
    private RecyclerView mSlotRecyclerView;
    private TextView mRecommendStatusTextView;
    private SlotMovieTitleAdapter mSlotAdapter;
    private List<String> mSlotTitles;
    private List<MovieBrief> mAllTopRatedMovies;
    private List<MovieBrief> mRecommendedMovies;
    private MovieBrief mSelectedMovie;
    private boolean mIsSpinning = false;
    private boolean mIsSlotMachineVisible = false;
    private Random mRandom = new Random();
    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);

        initViews(view);
        initAdapters();
        initClickListeners(view);

        mMoviesViewModel = new ViewModelProvider(this).get(MoviesViewModel.class);

        if (NetworkConnection.isConnected(getContext())) {
            observeViewModel();
            String region = LocaleHelper.getRegionCode(getContext());
            mMoviesViewModel.loadAllMovies(getContext(), getString(R.string.MOVIE_DB_API_KEY), region);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void initViews(View view) {
        mProgressBar = view.findViewById(R.id.progress_bar);
        mNowShowingLayout = view.findViewById(R.id.layout_now_showing);
        mPopularLayout = view.findViewById(R.id.layout_popular);
        mUpcomingLayout = view.findViewById(R.id.layout_upcoming);
        mTopRatedLayout = view.findViewById(R.id.layout_top_rated);

        mNowShowingRecyclerView = view.findViewById(R.id.recycler_view_now_showing);
        new LinearSnapHelper().attachToRecyclerView(mNowShowingRecyclerView);
        mPopularRecyclerView = view.findViewById(R.id.recycler_view_popular);
        mUpcomingRecyclerView = view.findViewById(R.id.recycler_view_upcoming);
        new LinearSnapHelper().attachToRecyclerView(mUpcomingRecyclerView);
        mTopRatedRecyclerView = view.findViewById(R.id.recycler_view_top_rated);

        mGenreButton = view.findViewById(R.id.button_genre_filter);
        mEmptyStateTextView = view.findViewById(R.id.text_view_empty_state);

        // 슬롯머신 초기화
        mScrollView = view.findViewById(R.id.scroll_view);
        mLayoutSlotMachine = view.findViewById(R.id.layout_slot_machine);
        mSlotRecyclerView = mLayoutSlotMachine.findViewById(R.id.recycler_view_slot);
        mRecommendStatusTextView = mLayoutSlotMachine.findViewById(R.id.text_view_recommend_status);
        mSlotTitles = new ArrayList<>();
        mSlotAdapter = new SlotMovieTitleAdapter(mSlotTitles);
        LinearLayoutManager slotLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mSlotRecyclerView.setLayoutManager(slotLayoutManager);
        mSlotRecyclerView.setAdapter(mSlotAdapter);
        mSlotRecyclerView.setNestedScrollingEnabled(false);
    }

    private void initAdapters() {
        mNowShowingMovies = new ArrayList<>();
        mNowShowingAdapter = new MovieBriefsLargeAdapter(getContext(), mNowShowingMovies, true);
        mNowShowingRecyclerView.setAdapter(mNowShowingAdapter);
        mNowShowingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mPopularMovies = new ArrayList<>();
        mPopularAdapter = new MovieBriefsSmallAdapter(getContext(), mPopularMovies);
        mPopularRecyclerView.setAdapter(mPopularAdapter);
        mPopularRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mUpcomingMovies = new ArrayList<>();
        mUpcomingAdapter = new MovieBriefsLargeAdapter(getContext(), mUpcomingMovies, false);
        mUpcomingRecyclerView.setAdapter(mUpcomingAdapter);
        mUpcomingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mTopRatedMovies = new ArrayList<>();
        mTopRatedAdapter = new MovieBriefsSmallAdapter(getContext(), mTopRatedMovies);
        mTopRatedRecyclerView.setAdapter(mTopRatedAdapter);
        mTopRatedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void initClickListeners(View view) {
        view.findViewById(R.id.text_view_view_all_now_showing).setOnClickListener(v -> openViewAllMovies(Constants.NOW_SHOWING_MOVIES_TYPE));
        view.findViewById(R.id.text_view_view_all_popular).setOnClickListener(v -> openViewAllMovies(Constants.POPULAR_MOVIES_TYPE));
        view.findViewById(R.id.text_view_view_all_upcoming).setOnClickListener(v -> openViewAllMovies(Constants.UPCOMING_MOVIES_TYPE));
        view.findViewById(R.id.text_view_view_all_top_rated).setOnClickListener(v -> openViewAllMovies(Constants.TOP_RATED_MOVIES_TYPE));

        // ScrollView 스크롤 감지 - 당기기 제스처 (맨 위에서 아래로 당김)
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            private float startY = 0;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (mIsSlotMachineVisible || mIsSpinning) {
                    return false;
                }

                int scrollY = mScrollView.getScrollY();
                boolean isAtTop = (scrollY <= 0);

                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        break;
                    case android.view.MotionEvent.ACTION_MOVE:
                        if (isAtTop && event.getY() - startY > 100) {
                            // 맨 위에서 아래로 당김
                            startRecommendation();
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void openViewAllMovies(String viewAllType) {
        if (!NetworkConnection.isConnected(getContext())) {
            Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getContext(), ViewAllMoviesActivity.class);
        intent.putExtra(Constants.VIEW_ALL_MOVIES_TYPE, viewAllType);
        if (mSelectedGenreId != null) {
            intent.putExtra(Constants.SELECTED_GENRE_ID, mSelectedGenreId);
        }
        startActivity(intent);
    }

    private void observeViewModel() {
        mMoviesViewModel.getGenresList().observe(getViewLifecycleOwner(), genres -> {
            if (genres != null) {
                MovieGenres.loadGenresList(genres);
                mGenres = genres;
                setupGenreButton();
            }
        });

        mMoviesViewModel.getNowShowingMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null) {
                mOriginalNowShowingMovies.clear();
                mOriginalNowShowingMovies.addAll(movies);
                mHasLoadedAtLeastOnce = true;
                applyGenreFilter();
            }
        });

        mMoviesViewModel.getPopularMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null) {
                mOriginalPopularMovies.clear();
                mOriginalPopularMovies.addAll(movies);
                mHasLoadedAtLeastOnce = true;
                applyGenreFilter();
            }
        });

        mMoviesViewModel.getUpcomingMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null) {
                mOriginalUpcomingMovies.clear();
                mOriginalUpcomingMovies.addAll(movies);
                mHasLoadedAtLeastOnce = true;
                applyGenreFilter();
            }
        });

        mMoviesViewModel.getTopRatedMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null) {
                mOriginalTopRatedMovies.clear();
                mOriginalTopRatedMovies.addAll(movies);
                mHasLoadedAtLeastOnce = true;
                applyGenreFilter();
            }
        });

        mMoviesViewModel.getError().observe(getViewLifecycleOwner(), isError -> {
            if (isError) {
                Toast.makeText(getContext(), R.string.error_loading_movies, Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.GONE);
                mHasLoadedAtLeastOnce = true;
                updateEmptyState();
            }
        });
    }

    private void setupGenreButton() {
        if (mGenres == null || mGenres.isEmpty()) return;

        // 기본 선택 없음 (전체 표시)
        mSelectedGenreId = null;
        mGenreButton.setText(getString(R.string.select_genre));

        mGenreButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), mGenreButton);
            
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
        filterMovies(mOriginalNowShowingMovies, mNowShowingMovies, mNowShowingAdapter, mNowShowingLayout, mNowShowingRecyclerView);
        filterMovies(mOriginalPopularMovies, mPopularMovies, mPopularAdapter, mPopularLayout, mPopularRecyclerView);
        filterMovies(mOriginalUpcomingMovies, mUpcomingMovies, mUpcomingAdapter, mUpcomingLayout, mUpcomingRecyclerView);
        filterMovies(mOriginalTopRatedMovies, mTopRatedMovies, mTopRatedAdapter, mTopRatedLayout, mTopRatedRecyclerView);
        mProgressBar.setVisibility(View.GONE);
        updateEmptyState();
    }

    private void filterMovies(List<MovieBrief> originalList, List<MovieBrief> filteredList, 
                             RecyclerView.Adapter adapter, FrameLayout layout, RecyclerView recyclerView) {
        filteredList.clear();
        
        if (mSelectedGenreId == null) {
            // 전체 선택 - 원본 데이터 모두 표시
            filteredList.addAll(originalList);
        } else {
            // 선택한 장르로 필터링
            for (MovieBrief movie : originalList) {
                if (movie.getGenreIds() != null && movie.getGenreIds().contains(mSelectedGenreId)) {
                    filteredList.add(movie);
                }
            }
        }

        adapter.notifyDataSetChanged();
        
        if (!filteredList.isEmpty()) {
            layout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void updateEmptyState() {
        if (mEmptyStateTextView == null) return;
        boolean anyVisible = !mNowShowingMovies.isEmpty() || !mPopularMovies.isEmpty()
                || !mUpcomingMovies.isEmpty() || !mTopRatedMovies.isEmpty();
        if (anyVisible) {
            mEmptyStateTextView.setVisibility(View.GONE);
            return;
        }
        mEmptyStateTextView.setVisibility(mHasLoadedAtLeastOnce ? View.VISIBLE : View.GONE);
    }

    // 슬롯머신 관련 메서드들
    private void startRecommendation() {
        if (mIsSlotMachineVisible || mIsSpinning || !NetworkConnection.isConnected(getContext())) {
            return;
        }
        
        mIsSlotMachineVisible = true;
        mLayoutSlotMachine.setVisibility(View.VISIBLE);
        mRecommendStatusTextView.setText(getString(R.string.recommending));
        
        // 슬롯머신 섹션으로 스크롤
        mHandler.postDelayed(() -> {
            if (mLayoutSlotMachine != null) {
                mLayoutSlotMachine.requestFocus();
                mLayoutSlotMachine.getParent().requestChildFocus(mLayoutSlotMachine, mLayoutSlotMachine);
            }
        }, 100);
        
        loadTopRatedMovies();
    }

    private void loadTopRatedMovies() {
        mAllTopRatedMovies = new ArrayList<>();
        loadTopRatedMoviesPage(1);
    }

    private void loadTopRatedMoviesPage(int page) {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(getContext());
        String region = LocaleHelper.getRegionCode(getContext());

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
                Toast.makeText(getContext(), R.string.error_loading_movies, 
                    Toast.LENGTH_SHORT).show();
                hideSlotMachine();
            }
        });
    }

    private void applyRecommendationAlgorithm() {
        // 즐겨찾기한 영화와 TV 쇼 가져오기
        List<MovieBrief> favMovies = Favourite.getFavMovieBriefs(getContext());
        List<TVShowBrief> favTVShows = Favourite.getFavTVShowBriefs(getContext());

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

        // 추천 영화 리스트를 랜덤하게 섞기
        Collections.shuffle(mRecommendedMovies, mRandom);

        // 슬롯 머신에 표시할 제목 리스트 생성
        updateSlotTitles();
        
        // 슬롯머신 시작
        mHandler.postDelayed(() -> startSlotMachine(), 300);
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
        mHandler.postDelayed(() -> {
            if (mSlotTitles.size() > 0) {
                int startPosition = mSlotTitles.size() / 2;
                mSlotRecyclerView.scrollToPosition(startPosition);
            }
        }, 100);
    }

    private void startSlotMachine() {
        if (mIsSpinning || mRecommendedMovies == null || mRecommendedMovies.isEmpty()) {
            return;
        }

        mIsSpinning = true;

        // 애니메이션 시작
        int spinCount = 20 + mRandom.nextInt(10); // 20-30번 회전
        animateSlotMachine(spinCount);
    }

    private void animateSlotMachine(int spinCount) {
        if (!mIsSpinning) return;
        
        LinearLayoutManager layoutManager = (LinearLayoutManager) mSlotRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            mIsSpinning = false;
            return;
        }

        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        if (firstVisiblePosition < 0) {
            mIsSpinning = false;
            return;
        }

        View firstVisibleView = layoutManager.findViewByPosition(firstVisiblePosition);
        if (firstVisibleView == null) {
            mIsSpinning = false;
            return;
        }

        int itemHeight = firstVisibleView.getHeight();
        if (itemHeight == 0) {
            // 아이템 높이를 측정
            firstVisibleView.measure(
                View.MeasureSpec.makeMeasureSpec(mSlotRecyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            itemHeight = firstVisibleView.getMeasuredHeight();
            if (itemHeight == 0) itemHeight = 60; // 기본 높이
        }

        if (spinCount > 0) {
            // 스크롤 속도 점점 느려지게
            int scrollAmount = itemHeight;
            
            // smoothScrollBy 대신 scrollBy 사용하여 더 정확한 제어
            mSlotRecyclerView.scrollBy(0, scrollAmount);
            
            // 점점 느려지게
            int delay = spinCount > 20 ? 20 : (spinCount > 15 ? 30 : (spinCount > 10 ? 50 : (spinCount > 5 ? 80 : 120)));
            mHandler.postDelayed(() -> {
                if (mIsSpinning) {
                    animateSlotMachine(spinCount - 1);
                }
            }, delay);
        } else {
            // 최종 선택 - 중앙에 정렬
            mSnapAttempts = 0;
            mHandler.postDelayed(() -> {
                if (mIsSpinning) {
                    snapToCenter();
                    mHandler.postDelayed(() -> {
                        if (!mIsSpinning && layoutManager != null) {
                            int finalPosition = layoutManager.findFirstVisibleItemPosition();
                            selectFinalMovie(finalPosition);
                        }
                    }, 800);
                }
            }, 300);
        }
    }

    private int mSnapAttempts = 0;
    private static final int MAX_SNAP_ATTEMPTS = 5;

    private void snapToCenter() {
        if (!mIsSpinning) return;
        
        LinearLayoutManager layoutManager = (LinearLayoutManager) mSlotRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            mIsSpinning = false;
            return;
        }

        int recyclerHeight = mSlotRecyclerView.getHeight();
        int centerY = recyclerHeight / 2;
        
        // 중앙에 가장 가까운 아이템 찾기
        int closestPosition = -1;
        int minDistance = Integer.MAX_VALUE;
        
        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            View child = layoutManager.getChildAt(i);
            if (child == null) continue;
            
            int childTop = child.getTop();
            int childBottom = child.getBottom();
            int childCenterY = (childTop + childBottom) / 2;
            int distance = Math.abs(centerY - childCenterY);
            
            if (distance < minDistance) {
                minDistance = distance;
                closestPosition = layoutManager.getPosition(child);
            }
        }
        
        if (closestPosition >= 0) {
            View closestView = layoutManager.findViewByPosition(closestPosition);
            if (closestView != null) {
                int itemTop = closestView.getTop();
                int itemBottom = closestView.getBottom();
                int itemCenterY = (itemTop + itemBottom) / 2;
                int offset = centerY - itemCenterY;
                
                if (Math.abs(offset) > 2 && mSnapAttempts < MAX_SNAP_ATTEMPTS) {
                    mSnapAttempts++;
                    // scrollBy로 정확하게 이동
                    mSlotRecyclerView.scrollBy(0, offset);
                    mHandler.postDelayed(() -> {
                        if (mIsSpinning && mSnapAttempts < MAX_SNAP_ATTEMPTS) {
                            snapToCenter();
                        } else {
                            mIsSpinning = false;
                            mSnapAttempts = 0;
                        }
                    }, 100);
                } else {
                    // 완전히 멈춤
                    mIsSpinning = false;
                    mSnapAttempts = 0;
                }
            } else {
                mIsSpinning = false;
                mSnapAttempts = 0;
            }
        } else {
            mIsSpinning = false;
            mSnapAttempts = 0;
        }
    }

    private void selectFinalMovie(int position) {
        if (mRecommendedMovies == null || mRecommendedMovies.isEmpty()) {
            hideSlotMachine();
            return;
        }
        
        LinearLayoutManager layoutManager = (LinearLayoutManager) mSlotRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            hideSlotMachine();
            return;
        }
        
        // 중앙에 있는 아이템 찾기
        int recyclerHeight = mSlotRecyclerView.getHeight();
        int centerY = recyclerHeight / 2;
        int closestPosition = -1;
        int minDistance = Integer.MAX_VALUE;
        
        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            View child = layoutManager.getChildAt(i);
            if (child == null) continue;
            
            int childTop = child.getTop();
            int childBottom = child.getBottom();
            int childCenterY = (childTop + childBottom) / 2;
            int distance = Math.abs(centerY - childCenterY);
            
            if (distance < minDistance) {
                minDistance = distance;
                closestPosition = layoutManager.getPosition(child);
            }
        }
        
        if (closestPosition < 0) {
            closestPosition = position;
        }
        
        // 실제 영화 선택 (순환을 고려)
        int actualIndex = closestPosition % mRecommendedMovies.size();
        if (actualIndex < 0) actualIndex = 0;
        if (actualIndex >= mRecommendedMovies.size()) actualIndex = mRecommendedMovies.size() - 1;
        
        mSelectedMovie = mRecommendedMovies.get(actualIndex);
        
        // 랜덤한 추천 문구 표시
        if (mRecommendStatusTextView != null) {
            String[] messages = {
                getString(R.string.recommend_message_1),
                getString(R.string.recommend_message_2),
                getString(R.string.recommend_message_3),
                getString(R.string.recommend_message_4),
                getString(R.string.recommend_message_5)
            };
            int randomIndex = mRandom.nextInt(messages.length);
            mRecommendStatusTextView.setText(messages[randomIndex]);
        }

        // 영화 상세 페이지로 이동
        mHandler.postDelayed(() -> {
            Intent intent = new Intent(getContext(), MovieDetailActivity.class);
            intent.putExtra(Constants.MOVIE_ID, mSelectedMovie.getId());
            startActivity(intent);
            hideSlotMachine();
        }, 1000);
    }

    private void hideSlotMachine() {
        mIsSlotMachineVisible = false;
        mIsSpinning = false;
        if (mLayoutSlotMachine != null) {
            mLayoutSlotMachine.setVisibility(View.GONE);
        }
        mSlotTitles.clear();
        if (mSlotAdapter != null) {
            mSlotAdapter.notifyDataSetChanged();
        }
        mAllTopRatedMovies.clear();
        mRecommendedMovies.clear();
        mSelectedMovie = null;
    }
}




