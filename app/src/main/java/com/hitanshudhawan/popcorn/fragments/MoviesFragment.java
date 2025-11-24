package com.hitanshudhawan.popcorn.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.hitanshudhawan.popcorn.R;
import com.hitanshudhawan.popcorn.activities.ViewAllMoviesActivity;
import com.hitanshudhawan.popcorn.adapters.MovieBriefsLargeAdapter;
import com.hitanshudhawan.popcorn.adapters.MovieBriefsSmallAdapter;
import com.hitanshudhawan.popcorn.network.movies.MovieBrief;
import com.hitanshudhawan.popcorn.utils.Constants;
import com.hitanshudhawan.popcorn.utils.MovieGenres;
import com.hitanshudhawan.popcorn.utils.NetworkConnection;
import com.hitanshudhawan.popcorn.viewmodels.MoviesViewModel;

import java.util.ArrayList;
import java.util.List;

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

    private MoviesViewModel mMoviesViewModel;

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
            mMoviesViewModel.loadAllMovies(getString(R.string.MOVIE_DB_API_KEY), "US");
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
    }

    private void initAdapters() {
        mNowShowingMovies = new ArrayList<>();
        mNowShowingAdapter = new MovieBriefsLargeAdapter(getContext(), mNowShowingMovies);
        mNowShowingRecyclerView.setAdapter(mNowShowingAdapter);
        mNowShowingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mPopularMovies = new ArrayList<>();
        mPopularAdapter = new MovieBriefsSmallAdapter(getContext(), mPopularMovies);
        mPopularRecyclerView.setAdapter(mPopularAdapter);
        mPopularRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mUpcomingMovies = new ArrayList<>();
        mUpcomingAdapter = new MovieBriefsLargeAdapter(getContext(), mUpcomingMovies);
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
    }

    private void openViewAllMovies(String viewAllType) {
        if (!NetworkConnection.isConnected(getContext())) {
            Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getContext(), ViewAllMoviesActivity.class);
        intent.putExtra(Constants.VIEW_ALL_MOVIES_TYPE, viewAllType);
        startActivity(intent);
    }

    private void observeViewModel() {
        mMoviesViewModel.getGenresList().observe(getViewLifecycleOwner(), genres -> {
            if (genres != null) {
                MovieGenres.loadGenresList(genres);
            }
        });

        mMoviesViewModel.getNowShowingMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && !movies.isEmpty()) {
                mNowShowingMovies.clear();
                mNowShowingMovies.addAll(movies);
                mNowShowingAdapter.notifyDataSetChanged();
                mNowShowingLayout.setVisibility(View.VISIBLE);
                mNowShowingRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        });

        mMoviesViewModel.getPopularMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && !movies.isEmpty()) {
                mPopularMovies.clear();
                mPopularMovies.addAll(movies);
                mPopularAdapter.notifyDataSetChanged();
                mPopularLayout.setVisibility(View.VISIBLE);
                mPopularRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        });

        mMoviesViewModel.getUpcomingMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && !movies.isEmpty()) {
                mUpcomingMovies.clear();
                mUpcomingMovies.addAll(movies);
                mUpcomingAdapter.notifyDataSetChanged();
                mUpcomingLayout.setVisibility(View.VISIBLE);
                mUpcomingRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        });

        mMoviesViewModel.getTopRatedMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && !movies.isEmpty()) {
                mTopRatedMovies.clear();
                mTopRatedMovies.addAll(movies);
                mTopRatedAdapter.notifyDataSetChanged();
                mTopRatedLayout.setVisibility(View.VISIBLE);
                mTopRatedRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        });

        mMoviesViewModel.getError().observe(getViewLifecycleOwner(), isError -> {
            if (isError) {
                Toast.makeText(getContext(), R.string.error_loading_movies, Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }
}
