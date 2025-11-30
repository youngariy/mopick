package com.youngariy.mopick.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
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

import com.youngariy.mopick.R;
import com.youngariy.mopick.activities.TVShowDetailActivity;
import com.youngariy.mopick.activities.ViewAllTVShowsActivity;
import com.youngariy.mopick.adapters.SlotMovieTitleAdapter;
import com.youngariy.mopick.adapters.TVShowBriefsLargeAdapter;
import com.youngariy.mopick.adapters.TVShowBriefsSmallAdapter;
import com.youngariy.mopick.broadcastreceivers.ConnectivityBroadcastReceiver;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.network.tvshows.AiringTodayTVShowsResponse;
import com.youngariy.mopick.network.tvshows.Genre;
import com.youngariy.mopick.network.tvshows.GenresList;
import com.youngariy.mopick.network.tvshows.OnTheAirTVShowsResponse;
import com.youngariy.mopick.network.tvshows.PopularTVShowsResponse;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.network.tvshows.TopRatedTVShowsResponse;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.Favourite;
import com.youngariy.mopick.utils.LocaleHelper;
import com.youngariy.mopick.utils.NetworkConnection;
import com.youngariy.mopick.utils.TVShowGenres;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitanshu on 13/8/17.
 */

public class TVShowsFragment extends Fragment {

    private ProgressBar mProgressBar;
    private boolean mAiringTodaySectionLoaded;
    private boolean mOnTheAirSectionLoaded;
    private boolean mPopularSectionLoaded;
    private boolean mTopRatedSectionLoaded;

    private FrameLayout mAiringTodayLayout;
    private TextView mAiringTodayViewAllTextView;
    private RecyclerView mAiringTodayRecyclerView;
    private List<TVShowBrief> mAiringTodayTVShows;
    private TVShowBriefsLargeAdapter mAiringTodayAdapter;

    private FrameLayout mOnTheAirLayout;
    private TextView mOnTheAirViewAllTextView;
    private RecyclerView mOnTheAirRecyclerView;
    private List<TVShowBrief> mOnTheAirTVShows;
    private TVShowBriefsSmallAdapter mOnTheAirAdapter;

    private FrameLayout mPopularLayout;
    private TextView mPopularViewAllTextView;
    private RecyclerView mPopularRecyclerView;
    private List<TVShowBrief> mPopularTVShows;
    private TVShowBriefsLargeAdapter mPopularAdapter;

    private FrameLayout mTopRatedLayout;
    private TextView mTopRatedViewAllTextView;
    private RecyclerView mTopRatedRecyclerView;
    private List<TVShowBrief> mTopRatedTVShows;
    private TVShowBriefsSmallAdapter mTopRatedAdapter;

    private Snackbar mConnectivitySnackbar;
    private ConnectivityBroadcastReceiver mConnectivityBroadcastReceiver;
    private boolean isBroadcastReceiverRegistered;
    private boolean isFragmentLoaded;
    private Call<GenresList> mGenresListCall;
    private Call<AiringTodayTVShowsResponse> mAiringTodayTVShowsCall;
    private Call<OnTheAirTVShowsResponse> mOnTheAirTVShowsCall;
    private Call<PopularTVShowsResponse> mPopularTVShowsCall;
    private Call<TopRatedTVShowsResponse> mTopRatedTVShowsCall;

    private MaterialButton mGenreButton;
    private List<Genre> mGenres;
    private Integer mSelectedGenreId = null;
    private TextView mEmptyStateTextView;

    // 원본 데이터 저장
    private List<TVShowBrief> mOriginalAiringTodayTVShows = new ArrayList<>();
    private List<TVShowBrief> mOriginalOnTheAirTVShows = new ArrayList<>();
    private List<TVShowBrief> mOriginalPopularTVShows = new ArrayList<>();
    private List<TVShowBrief> mOriginalTopRatedTVShows = new ArrayList<>();

    // 슬롯머신 관련
    private ScrollView mScrollView;
    private FrameLayout mLayoutSlotMachine;
    private RecyclerView mSlotRecyclerView;
    private TextView mRecommendStatusTextView;
    private SlotMovieTitleAdapter mSlotAdapter;
    private List<String> mSlotTitles;
    private List<TVShowBrief> mAllTopRatedTVShows;
    private List<TVShowBrief> mRecommendedTVShows;
    private TVShowBrief mSelectedTVShow;
    private boolean mIsSpinning = false;
    private boolean mIsSlotMachineVisible = false;
    private Random mRandom = new Random();
    private Handler mHandler = new Handler();
    private int mSnapAttempts = 0;
    private static final int MAX_SNAP_ATTEMPTS = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tv_shows, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
        mAiringTodaySectionLoaded = false;
        mOnTheAirSectionLoaded = false;
        mPopularSectionLoaded = false;
        mTopRatedSectionLoaded = false;

        mAiringTodayLayout = (FrameLayout) view.findViewById(R.id.layout_airing_today);
        mOnTheAirLayout = (FrameLayout) view.findViewById(R.id.layout_on_the_air);
        mPopularLayout = (FrameLayout) view.findViewById(R.id.layout_popular);
        mTopRatedLayout = (FrameLayout) view.findViewById(R.id.layout_top_rated);

        mAiringTodayViewAllTextView = (TextView) view.findViewById(R.id.text_view_view_all_airing_today);
        mOnTheAirViewAllTextView = (TextView) view.findViewById(R.id.text_view_view_all_on_the_air);
        mPopularViewAllTextView = (TextView) view.findViewById(R.id.text_view_view_all_popular);
        mTopRatedViewAllTextView = (TextView) view.findViewById(R.id.text_view_view_all_top_rated);

        mAiringTodayRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_airing_today);
        (new LinearSnapHelper()).attachToRecyclerView(mAiringTodayRecyclerView);
        mOnTheAirRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_on_the_air);
        mPopularRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_popular);
        (new LinearSnapHelper()).attachToRecyclerView(mPopularRecyclerView);
        mTopRatedRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_top_rated);

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

        mAiringTodayTVShows = new ArrayList<>();
        mOnTheAirTVShows = new ArrayList<>();
        mPopularTVShows = new ArrayList<>();
        mTopRatedTVShows = new ArrayList<>();

        mAiringTodayAdapter = new TVShowBriefsLargeAdapter(getContext(), mAiringTodayTVShows, true);
        mOnTheAirAdapter = new TVShowBriefsSmallAdapter(getContext(), mOnTheAirTVShows);
        mPopularAdapter = new TVShowBriefsLargeAdapter(getContext(), mPopularTVShows, false);
        mTopRatedAdapter = new TVShowBriefsSmallAdapter(getContext(), mTopRatedTVShows);

        mAiringTodayRecyclerView.setAdapter(mAiringTodayAdapter);
        mAiringTodayRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mOnTheAirRecyclerView.setAdapter(mOnTheAirAdapter);
        mOnTheAirRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mPopularRecyclerView.setAdapter(mPopularAdapter);
        mPopularRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mTopRatedRecyclerView.setAdapter(mTopRatedAdapter);
        mTopRatedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mAiringTodayViewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkConnection.isConnected(getContext())) {
                    Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getContext(), ViewAllTVShowsActivity.class);
                intent.putExtra(Constants.VIEW_ALL_TV_SHOWS_TYPE, Constants.AIRING_TODAY_TV_SHOWS_TYPE);
                if (mSelectedGenreId != null) {
                    intent.putExtra(Constants.SELECTED_GENRE_ID, mSelectedGenreId);
                }
                startActivity(intent);
            }
        });
        mOnTheAirViewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkConnection.isConnected(getContext())) {
                    Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getContext(), ViewAllTVShowsActivity.class);
                intent.putExtra(Constants.VIEW_ALL_TV_SHOWS_TYPE, Constants.ON_THE_AIR_TV_SHOWS_TYPE);
                if (mSelectedGenreId != null) {
                    intent.putExtra(Constants.SELECTED_GENRE_ID, mSelectedGenreId);
                }
                startActivity(intent);
            }
        });
        mPopularViewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkConnection.isConnected(getContext())) {
                    Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getContext(), ViewAllTVShowsActivity.class);
                intent.putExtra(Constants.VIEW_ALL_TV_SHOWS_TYPE, Constants.POPULAR_TV_SHOWS_TYPE);
                if (mSelectedGenreId != null) {
                    intent.putExtra(Constants.SELECTED_GENRE_ID, mSelectedGenreId);
                }
                startActivity(intent);
            }
        });
        mTopRatedViewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkConnection.isConnected(getContext())) {
                    Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getContext(), ViewAllTVShowsActivity.class);
                intent.putExtra(Constants.VIEW_ALL_TV_SHOWS_TYPE, Constants.TOP_RATED_TV_SHOWS_TYPE);
                if (mSelectedGenreId != null) {
                    intent.putExtra(Constants.SELECTED_GENRE_ID, mSelectedGenreId);
                }
                startActivity(intent);
            }
        });

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

        if (NetworkConnection.isConnected(getContext())) {
            isFragmentLoaded = true;
            loadFragment();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAiringTodayAdapter.notifyDataSetChanged();
        mOnTheAirAdapter.notifyDataSetChanged();
        mPopularAdapter.notifyDataSetChanged();
        mTopRatedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isFragmentLoaded && !NetworkConnection.isConnected(getContext())) {
            mConnectivitySnackbar = Snackbar.make(getActivity().findViewById(R.id.main_activity_fragment_container), R.string.no_network, Snackbar.LENGTH_INDEFINITE);
            mConnectivitySnackbar.show();
            mConnectivityBroadcastReceiver = new ConnectivityBroadcastReceiver(new ConnectivityBroadcastReceiver.ConnectivityReceiverListener() {
                @Override
                public void onNetworkConnectionConnected() {
                    mConnectivitySnackbar.dismiss();
                    isFragmentLoaded = true;
                    loadFragment();
                    isBroadcastReceiverRegistered = false;
                    getActivity().unregisterReceiver(mConnectivityBroadcastReceiver);
                }
            });
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            isBroadcastReceiverRegistered = true;
            getActivity().registerReceiver(mConnectivityBroadcastReceiver, intentFilter);
        } else if (!isFragmentLoaded && NetworkConnection.isConnected(getContext())) {
            isFragmentLoaded = true;
            loadFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isBroadcastReceiverRegistered) {
            mConnectivitySnackbar.dismiss();
            isBroadcastReceiverRegistered = false;
            getActivity().unregisterReceiver(mConnectivityBroadcastReceiver);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGenresListCall != null) mGenresListCall.cancel();
        if (mAiringTodayTVShowsCall != null) mAiringTodayTVShowsCall.cancel();
        if (mOnTheAirTVShowsCall != null) mOnTheAirTVShowsCall.cancel();
        if (mPopularTVShowsCall != null) mPopularTVShowsCall.cancel();
        if (mTopRatedTVShowsCall != null) mTopRatedTVShowsCall.cancel();
    }

    private void loadFragment() {

        if (TVShowGenres.isGenresListLoaded()) {
            mGenres = TVShowGenres.getGenresList();
            setupGenreButton();
            loadAiringTodayTVShows();
            loadOnTheAirTVShows();
            loadPopularTVShows();
            loadTopRatedTVShows();
        } else {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            mProgressBar.setVisibility(View.VISIBLE);
            String language = LocaleHelper.getLanguageCode(getContext());
            mGenresListCall = apiService.getTVShowGenresList(getResources().getString(R.string.MOVIE_DB_API_KEY), language);
            mGenresListCall.enqueue(new Callback<GenresList>() {
                @Override
                public void onResponse(Call<GenresList> call, Response<GenresList> response) {
                    if (!response.isSuccessful()) {
                        mGenresListCall = call.clone();
                        mGenresListCall.enqueue(this);
                        return;
                    }

                if (response.body() == null || response.body().getGenres() == null) {
                    mGenres = new ArrayList<>();
                    setupGenreButton();
                    loadAiringTodayTVShows();
                    loadOnTheAirTVShows();
                    loadPopularTVShows();
                    loadTopRatedTVShows();
                    return;
                }

                    TVShowGenres.loadGenresList(response.body().getGenres());
                    mGenres = response.body().getGenres();
                    setupGenreButton();
                    loadAiringTodayTVShows();
                    loadOnTheAirTVShows();
                    loadPopularTVShows();
                    loadTopRatedTVShows();
                }

                @Override
                public void onFailure(Call<GenresList> call, Throwable t) {

                }
            });
        }

    }

    private void loadAiringTodayTVShows() {
        final ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        mProgressBar.setVisibility(View.VISIBLE);
        String language = LocaleHelper.getLanguageCode(getContext());
        mAiringTodayTVShowsCall = apiService.getAiringTodayTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, language);
        mAiringTodayTVShowsCall.enqueue(new Callback<AiringTodayTVShowsResponse>() {
            @Override
            public void onResponse(Call<AiringTodayTVShowsResponse> call, Response<AiringTodayTVShowsResponse> response) {
                if (!response.isSuccessful()) {
                    mAiringTodayTVShowsCall = call.clone();
                    mAiringTodayTVShowsCall.enqueue(this);
                    return;
                }

                if (response.body() == null || response.body().getResults() == null) {
                    mAiringTodaySectionLoaded = true;
                    applyGenreFilter();
                    checkAllDataLoaded();
                    return;
                }

                mAiringTodaySectionLoaded = true;
                mOriginalAiringTodayTVShows.clear();
                for (TVShowBrief TVShowBrief : response.body().getResults()) {
                    if (TVShowBrief != null && TVShowBrief.getBackdropPath() != null)
                        mOriginalAiringTodayTVShows.add(TVShowBrief);
                }
                applyGenreFilter();
                checkAllDataLoaded();
            }

            @Override
            public void onFailure(Call<AiringTodayTVShowsResponse> call, Throwable t) {
                mAiringTodaySectionLoaded = true;
                applyGenreFilter();
                checkAllDataLoaded();
            }
        });
    }

    private void loadOnTheAirTVShows() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        mProgressBar.setVisibility(View.VISIBLE);
        String language = LocaleHelper.getLanguageCode(getContext());
        mOnTheAirTVShowsCall = apiService.getOnTheAirTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, language);
        mOnTheAirTVShowsCall.enqueue(new Callback<OnTheAirTVShowsResponse>() {
            @Override
            public void onResponse(Call<OnTheAirTVShowsResponse> call, Response<OnTheAirTVShowsResponse> response) {
                if (!response.isSuccessful()) {
                    mOnTheAirTVShowsCall = call.clone();
                    mOnTheAirTVShowsCall.enqueue(this);
                    return;
                }

                if (response.body() == null || response.body().getResults() == null) {
                    mOnTheAirSectionLoaded = true;
                    applyGenreFilter();
                    checkAllDataLoaded();
                    return;
                }

                mOnTheAirSectionLoaded = true;
                mOriginalOnTheAirTVShows.clear();
                for (TVShowBrief TVShowBrief : response.body().getResults()) {
                    if (TVShowBrief != null && TVShowBrief.getPosterPath() != null)
                        mOriginalOnTheAirTVShows.add(TVShowBrief);
                }
                applyGenreFilter();
                checkAllDataLoaded();
            }

            @Override
            public void onFailure(Call<OnTheAirTVShowsResponse> call, Throwable t) {
                mOnTheAirSectionLoaded = true;
                applyGenreFilter();
                checkAllDataLoaded();
            }
        });
    }

    private void loadPopularTVShows() {
        final ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        mProgressBar.setVisibility(View.VISIBLE);
        String language = LocaleHelper.getLanguageCode(getContext());
        mPopularTVShowsCall = apiService.getPopularTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, language);
        mPopularTVShowsCall.enqueue(new Callback<PopularTVShowsResponse>() {
            @Override
            public void onResponse(Call<PopularTVShowsResponse> call, Response<PopularTVShowsResponse> response) {
                if (!response.isSuccessful()) {
                    mPopularTVShowsCall = call.clone();
                    mPopularTVShowsCall.enqueue(this);
                    return;
                }

                if (response.body() == null || response.body().getResults() == null) {
                    mPopularSectionLoaded = true;
                    applyGenreFilter();
                    checkAllDataLoaded();
                    return;
                }

                mPopularSectionLoaded = true;
                mOriginalPopularTVShows.clear();
                for (TVShowBrief TVShowBrief : response.body().getResults()) {
                    if (TVShowBrief != null && TVShowBrief.getBackdropPath() != null)
                        mOriginalPopularTVShows.add(TVShowBrief);
                }
                applyGenreFilter();
                checkAllDataLoaded();
            }

            @Override
            public void onFailure(Call<PopularTVShowsResponse> call, Throwable t) {
                mPopularSectionLoaded = true;
                applyGenreFilter();
                checkAllDataLoaded();
            }
        });
    }

    private void loadTopRatedTVShows() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        mProgressBar.setVisibility(View.VISIBLE);
        String language = LocaleHelper.getLanguageCode(getContext());
        mTopRatedTVShowsCall = apiService.getTopRatedTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, language);
        mTopRatedTVShowsCall.enqueue(new Callback<TopRatedTVShowsResponse>() {
            @Override
            public void onResponse(Call<TopRatedTVShowsResponse> call, Response<TopRatedTVShowsResponse> response) {
                if (!response.isSuccessful()) {
                    mTopRatedTVShowsCall = call.clone();
                    mTopRatedTVShowsCall.enqueue(this);
                    return;
                }

                if (response.body() == null || response.body().getResults() == null) {
                    mTopRatedSectionLoaded = true;
                    applyGenreFilter();
                    checkAllDataLoaded();
                    return;
                }

                mTopRatedSectionLoaded = true;
                mOriginalTopRatedTVShows.clear();
                for (TVShowBrief TVShowBrief : response.body().getResults()) {
                    if (TVShowBrief != null && TVShowBrief.getPosterPath() != null)
                        mOriginalTopRatedTVShows.add(TVShowBrief);
                }
                applyGenreFilter();
                checkAllDataLoaded();
            }

            @Override
            public void onFailure(Call<TopRatedTVShowsResponse> call, Throwable t) {
                mTopRatedSectionLoaded = true;
                applyGenreFilter();
                checkAllDataLoaded();
            }
        });
    }

    private void checkAllDataLoaded() {
        if (mAiringTodaySectionLoaded && mOnTheAirSectionLoaded && mPopularSectionLoaded && mTopRatedSectionLoaded) {
            mProgressBar.setVisibility(View.GONE);
        }
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
        filterTVShows(mOriginalAiringTodayTVShows, mAiringTodayTVShows, mAiringTodayAdapter, mAiringTodayLayout, mAiringTodayRecyclerView);
        filterTVShows(mOriginalOnTheAirTVShows, mOnTheAirTVShows, mOnTheAirAdapter, mOnTheAirLayout, mOnTheAirRecyclerView);
        filterTVShows(mOriginalPopularTVShows, mPopularTVShows, mPopularAdapter, mPopularLayout, mPopularRecyclerView);
        filterTVShows(mOriginalTopRatedTVShows, mTopRatedTVShows, mTopRatedAdapter, mTopRatedLayout, mTopRatedRecyclerView);
        mProgressBar.setVisibility(View.GONE);
        updateEmptyState();
    }

    private void filterTVShows(List<TVShowBrief> originalList, List<TVShowBrief> filteredList,
                               RecyclerView.Adapter adapter, FrameLayout layout, RecyclerView recyclerView) {
        filteredList.clear();

        if (mSelectedGenreId == null) {
            // 전체 선택 - 원본 데이터 모두 표시
            filteredList.addAll(originalList);
        } else {
            // 선택한 장르로 필터링
            for (TVShowBrief tvShow : originalList) {
                if (tvShow.getGenreIds() != null && tvShow.getGenreIds().contains(mSelectedGenreId)) {
                    filteredList.add(tvShow);
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
        boolean anyVisible = !mAiringTodayTVShows.isEmpty() || !mOnTheAirTVShows.isEmpty()
                || !mPopularTVShows.isEmpty() || !mTopRatedTVShows.isEmpty();
        if (anyVisible) {
            mEmptyStateTextView.setVisibility(View.GONE);
            return;
        }

        boolean allSectionsLoaded = mAiringTodaySectionLoaded && mOnTheAirSectionLoaded
                && mPopularSectionLoaded && mTopRatedSectionLoaded;
        mEmptyStateTextView.setVisibility(allSectionsLoaded ? View.VISIBLE : View.GONE);
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
        
        loadTopRatedTVShowsForRecommendation();
    }

    private void loadTopRatedTVShowsForRecommendation() {
        mAllTopRatedTVShows = new ArrayList<>();
        loadTopRatedTVShowsPageForRecommendation(1);
    }

    private void loadTopRatedTVShowsPageForRecommendation(int page) {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String language = LocaleHelper.getLanguageCode(getContext());

        Call<TopRatedTVShowsResponse> call = apiService.getTopRatedTVShows(
            getString(R.string.MOVIE_DB_API_KEY), page, language);
        
        call.enqueue(new Callback<TopRatedTVShowsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TopRatedTVShowsResponse> call, 
                                 @NonNull Response<TopRatedTVShowsResponse> response) {
                if (response.isSuccessful() && response.body() != null && 
                    response.body().getResults() != null) {
                    
                    for (TVShowBrief tvShow : response.body().getResults()) {
                        if (tvShow != null && tvShow.getName() != null && 
                            tvShow.getPosterPath() != null) {
                            mAllTopRatedTVShows.add(tvShow);
                        }
                    }

                    // 다음 페이지가 있으면 계속 로드
                    if (page < response.body().getTotalPages() && page < 10) {
                        loadTopRatedTVShowsPageForRecommendation(page + 1);
                    } else {
                        // 모든 TV 프로그램 로드 완료, 추천 알고리즘 적용
                        applyRecommendationAlgorithm();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TopRatedTVShowsResponse> call, 
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

        // 추천 TV 프로그램 필터링
        mRecommendedTVShows = new ArrayList<>();
        if (!favoriteGenres.isEmpty()) {
            // 즐겨찾기 카테고리와 겹치는 TV 프로그램 우선 추천
            for (TVShowBrief tvShow : mAllTopRatedTVShows) {
                if (tvShow.getGenreIds() != null) {
                    for (Integer genreId : tvShow.getGenreIds()) {
                        if (favoriteGenres.contains(genreId)) {
                            mRecommendedTVShows.add(tvShow);
                            break;
                        }
                    }
                }
            }
        }

        // 추천 TV 프로그램이 적으면 전체 최고 평점 TV 프로그램 추가
        if (mRecommendedTVShows.size() < 20) {
            for (TVShowBrief tvShow : mAllTopRatedTVShows) {
                if (!mRecommendedTVShows.contains(tvShow)) {
                    mRecommendedTVShows.add(tvShow);
                }
                if (mRecommendedTVShows.size() >= 50) break;
            }
        }

        // 추천 TV 프로그램 리스트를 랜덤하게 섞기
        Collections.shuffle(mRecommendedTVShows, mRandom);

        // 슬롯 머신에 표시할 제목 리스트 생성
        updateSlotTitles();
        
        // 슬롯머신 시작
        mHandler.postDelayed(() -> startSlotMachine(), 300);
    }

    private void updateSlotTitles() {
        mSlotTitles.clear();
        List<String> originalTitles = new ArrayList<>();
        for (TVShowBrief tvShow : mRecommendedTVShows) {
            originalTitles.add(tvShow.getName());
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
        if (mIsSpinning || mRecommendedTVShows == null || mRecommendedTVShows.isEmpty()) {
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
                            selectFinalTVShow(finalPosition);
                        }
                    }, 800);
                }
            }, 300);
        }
    }

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

    private void selectFinalTVShow(int position) {
        if (mRecommendedTVShows == null || mRecommendedTVShows.isEmpty()) {
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
        
        // 실제 TV 프로그램 선택 (순환을 고려)
        int actualIndex = closestPosition % mRecommendedTVShows.size();
        if (actualIndex < 0) actualIndex = 0;
        if (actualIndex >= mRecommendedTVShows.size()) actualIndex = mRecommendedTVShows.size() - 1;
        
        mSelectedTVShow = mRecommendedTVShows.get(actualIndex);
        
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

        // TV 프로그램 상세 페이지로 이동
        mHandler.postDelayed(() -> {
            Intent intent = new Intent(getContext(), TVShowDetailActivity.class);
            intent.putExtra(Constants.TV_SHOW_ID, mSelectedTVShow.getId());
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
        mAllTopRatedTVShows.clear();
        mRecommendedTVShows.clear();
        mSelectedTVShow = null;
    }
}




