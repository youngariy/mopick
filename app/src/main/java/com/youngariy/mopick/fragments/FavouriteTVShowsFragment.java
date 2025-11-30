package com.youngariy.mopick.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.youngariy.mopick.R;
import com.youngariy.mopick.adapters.TVShowBriefsSmallAdapter;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.tvshows.TVShow;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.utils.Favourite;
import com.youngariy.mopick.utils.LocaleHelper;
import com.youngariy.mopick.utils.NetworkConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitanshu on 13/8/17.
 */

public class FavouriteTVShowsFragment extends Fragment {

    private RecyclerView mFavTVShowsRecyclerView;
    private List<TVShowBrief> mFavTVShows;
    private TVShowBriefsSmallAdapter mFavTVShowsAdapter;

    private LinearLayout mEmptyLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_tv_shows, container, false);

        mFavTVShowsRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_fav_tv_shows);
        mFavTVShows = new ArrayList<>();
        mFavTVShowsAdapter = new TVShowBriefsSmallAdapter(getContext(), mFavTVShows);
        mFavTVShowsRecyclerView.setAdapter(mFavTVShowsAdapter);
        mFavTVShowsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        mEmptyLayout = (LinearLayout) view.findViewById(R.id.layout_recycler_view_fav_tv_shows_empty);

        loadFavTVShows();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFavTVShows();
    }

    private void loadFavTVShows() {
        if (!NetworkConnection.isConnected(getContext())) {
            List<TVShowBrief> favTVShowBriefs = Favourite.getFavTVShowBriefs(getContext());
            updateAdapter(favTVShowBriefs);
            return;
        }

        List<Integer> tvShowIds = Favourite.getFavTVShowIds(getContext());
        if (tvShowIds.isEmpty()) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mFavTVShows.clear();
            mFavTVShowsAdapter.notifyDataSetChanged();
            return;
        }

        fetchFavTVShowsWithLanguage(tvShowIds);
    }

    private void fetchFavTVShowsWithLanguage(List<Integer> tvShowIds) {
        final ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        final String language = LocaleHelper.getLanguageCode(getContext());

        List<TVShowBrief> tvShows = new ArrayList<>();
        AtomicInteger completedCalls = new AtomicInteger(0);
        int totalCalls = tvShowIds.size();

        for (Integer tvId : tvShowIds) {
            Call<TVShow> call = apiService.getTVShowDetails(tvId, getString(R.string.MOVIE_DB_API_KEY), language);
            call.enqueue(new Callback<TVShow>() {
                @Override
                public void onResponse(Call<TVShow> call, Response<TVShow> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        TVShow tv = response.body();
                        tvShows.add(new TVShowBrief(tv.getOriginalName(), tv.getId(), tv.getName(), tv.getVoteCount(), tv.getVoteAverage(),
                                tv.getPosterPath(), tv.getFirstAirDate(), tv.getPopularity(), null, tv.getOriginalLanguage(),
                                tv.getBackdropPath(), tv.getOverview(), tv.getOriginCountries()));
                    }
                    if (completedCalls.incrementAndGet() == totalCalls) {
                        updateAdapter(tvShows);
                    }
                }

                @Override
                public void onFailure(Call<TVShow> call, Throwable t) {
                    if (completedCalls.incrementAndGet() == totalCalls) {
                        updateAdapter(tvShows);
                    }
                }
            });
        }
    }

    private void updateAdapter(List<TVShowBrief> items) {
        mFavTVShows.clear();
        mFavTVShows.addAll(items);
        if (items.isEmpty()) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mFavTVShowsRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
            mFavTVShowsRecyclerView.setVisibility(View.VISIBLE);
        }
        mFavTVShowsAdapter.notifyDataSetChanged();
    }
}



