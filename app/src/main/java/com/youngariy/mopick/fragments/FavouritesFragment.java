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
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.network.tvshows.TVShowBrief;
import com.youngariy.mopick.utils.Favourite;

import java.util.List;

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
        List<MovieBrief> favMovies = Favourite.getFavMovieBriefs(getContext());
        List<TVShowBrief> favTVShows = Favourite.getFavTVShowBriefs(getContext());

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




