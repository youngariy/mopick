package com.youngariy.mopick.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import androidx.appcompat.widget.PopupMenu;

import com.youngariy.mopick.R;
import com.youngariy.mopick.activities.ViewAllTVShowsActivity;
import com.youngariy.mopick.adapters.TVShowBriefsLargeAdapter;
import com.youngariy.mopick.adapters.TVShowBriefsSmallAdapter;
import com.youngariy.mopick.broadcastreceivers.ConnectivityBroadcastReceiver;
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
}




