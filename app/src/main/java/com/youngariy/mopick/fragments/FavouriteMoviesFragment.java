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
import com.youngariy.mopick.adapters.MovieBriefsSmallAdapter;
import com.youngariy.mopick.network.ApiClient;
import com.youngariy.mopick.network.ApiInterface;
import com.youngariy.mopick.network.movies.Movie;
import com.youngariy.mopick.network.movies.MovieBrief;
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
 * Created by hitanshu on 10/8/17.
 */

// hitanshu : FavouriteMoviesFragment and FavouriteTVShowsFragment are mostly similar
public class FavouriteMoviesFragment extends Fragment {

    private RecyclerView mFavMoviesRecyclerView;
    private List<MovieBrief> mFavMovies;
    private MovieBriefsSmallAdapter mFavMoviesAdapter;

    private LinearLayout mEmptyLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_movies, container, false);

        mFavMoviesRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_fav_movies);
        mFavMovies = new ArrayList<>();
        mFavMoviesAdapter = new MovieBriefsSmallAdapter(getContext(), mFavMovies);
        mFavMoviesRecyclerView.setAdapter(mFavMoviesAdapter);
        mFavMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        mEmptyLayout = (LinearLayout) view.findViewById(R.id.layout_recycler_view_fav_movies_empty);

        loadFavMovies();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFavMovies();
    }

    private void loadFavMovies() {
        if (!NetworkConnection.isConnected(getContext())) {
            // fallback to stored names when offline
            List<MovieBrief> favMovieBriefs = Favourite.getFavMovieBriefs(getContext());
            updateAdapter(favMovieBriefs);
            return;
        }

        List<Integer> movieIds = Favourite.getFavMovieIds(getContext());
        if (movieIds.isEmpty()) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mFavMovies.clear();
            mFavMoviesAdapter.notifyDataSetChanged();
            return;
        }

        fetchFavMoviesWithLanguage(movieIds);
    }

    private void fetchFavMoviesWithLanguage(List<Integer> movieIds) {
        final ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        final String language = LocaleHelper.getLanguageCode(getContext());

        List<MovieBrief> movies = new ArrayList<>();
        AtomicInteger completedCalls = new AtomicInteger(0);
        int totalCalls = movieIds.size();

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
                        updateAdapter(movies);
                    }
                }

                @Override
                public void onFailure(Call<Movie> call, Throwable t) {
                    if (completedCalls.incrementAndGet() == totalCalls) {
                        updateAdapter(movies);
                    }
                }
            });
        }
    }

    private void updateAdapter(List<MovieBrief> items) {
        mFavMovies.clear();
        mFavMovies.addAll(items);
        if (items.isEmpty()) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mFavMoviesRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
            mFavMoviesRecyclerView.setVisibility(View.VISIBLE);
        }
        mFavMoviesAdapter.notifyDataSetChanged();
    }
}



