package com.youngariy.mopick.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.youngariy.mopick.R;
import com.youngariy.mopick.adapters.FavouritesUnifiedAdapter;
import com.youngariy.mopick.network.movies.Movie;
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.network.tvshows.TVShow;
import com.youngariy.mopick.utils.Favourite;
import com.youngariy.mopick.utils.LocaleHelper;
import com.youngariy.mopick.utils.NetworkConnection;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitanshu on 10/8/17.
 */

public class FavouritesFragment extends Fragment {

    private RecyclerView mFavRecyclerView;
    private FavouritesUnifiedAdapter mFavAdapter;
    private LinearLayout mEmptyLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        mFavRecyclerView = view.findViewById(R.id.recycler_view_fav_all);
        mEmptyLayout = view.findViewById(R.id.layout_recycler_view_fav_empty);

        mFavAdapter = new FavouritesUnifiedAdapter(getContext());
        mFavRecyclerView.setAdapter(mFavAdapter);
        mFavRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        loadFavourites();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFavourites();
    }

    private void loadFavourites() {
        if (!NetworkConnection.isConnected(getContext())) {
            // fallback to stored names when offline
            List<MovieBrief> favMovies = Favourite.getFavMovieBriefs(getContext());
            List<TVShowBrief> favTVShows = Favourite.getFavTVShowBriefs(getContext());
            updateAdapter(favMovies, favTVShows);
            return;
        }

        List<Integer> movieIds = Favourite.getFavMovieIds(getContext());
        List<Integer> tvShowIds = Favourite.getFavTVShowIds(getContext());

        if (movieIds.isEmpty() && tvShowIds.isEmpty()) {
            updateAdapter(new ArrayList<>(), new ArrayList<>());
            return;
        }

        fetchFavItemsWithLanguage(movieIds, tvShowIds);
    }

    private void fetchFavItemsWithLanguage(List<Integer> movieIds, List<Integer> tvShowIds) {
        final ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        final String language = LocaleHelper.getLanguageCode(getContext());

        List<MovieBrief> movies = new ArrayList<>();
        List<TVShowBrief> tvShows = new ArrayList<>();

        int totalCalls = movieIds.size() + tvShowIds.size();
        AtomicInteger completedCalls = new AtomicInteger(0);

        for (Integer movieId : movieIds) {
            Call<Movie> call = apiService.getMovieDetails(movieId, getString(R.string.MOVIE_DB_API_KEY), language);
            call.enqueue(new Callback<Movie>() {
                @Override
                public void onResponse(Call<Movie> call, Response<Movie> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Movie movie = response.body();
                        movies.add(new MovieBrief(
                                movie.getVoteCount(),
                                movie.getId(),
                                movie.getVideo(),
                                movie.getVoteAverage(),
                                movie.getTitle(),
                                movie.getPopularity(),
                                movie.getPosterPath(),
                                movie.getOriginalLanguage(),
                                movie.getOriginalTitle(),
                                null, // genreIds not stored
                                movie.getBackdropPath(),
                                movie.getAdult(),
                                movie.getOverview(),
                                movie.getReleaseDate()
                        ));
                    }
                    if (completedCalls.incrementAndGet() == totalCalls) {
                        updateAdapter(movies, tvShows);
                    }
                }

                @Override
                public void onFailure(Call<Movie> call, Throwable t) {
                    if (completedCalls.incrementAndGet() == totalCalls) {
                        updateAdapter(movies, tvShows);
                    }
                }
            });
        }

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
                        updateAdapter(movies, tvShows);
                    }
                }

                @Override
                public void onFailure(Call<TVShow> call, Throwable t) {
                    if (completedCalls.incrementAndGet() == totalCalls) {
                        updateAdapter(movies, tvShows);
                    }
                }
            });
        }
    }

    private void updateAdapter(List<MovieBrief> favMovies, List<TVShowBrief> favTVShows) {
        if (favMovies.isEmpty() && favTVShows.isEmpty()) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mFavRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
            mFavRecyclerView.setVisibility(View.VISIBLE);
            mFavAdapter.setItems(favMovies, favTVShows);
        }
    }
}




