package com.youngariy.mopick.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.youngariy.mopick.R;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.youngariy.mopick.fragments.FavouriteMoviesFragment;
import com.youngariy.mopick.fragments.FavouriteTVShowsFragment;

/**
 * Created by hitanshu on 10/8/17.
 */

public class FavouritesFragment extends Fragment {

    private MaterialButtonToggleGroup mToggleGroup;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        mToggleGroup = view.findViewById(R.id.toggle_fav);

        Fragment current = getChildFragmentManager().findFragmentById(R.id.container_fav);
        if (current == null) {
            mToggleGroup.check(R.id.btn_fav_movies);
            showFragment(new FavouriteMoviesFragment());
        } else if (current instanceof FavouriteTVShowsFragment) {
            mToggleGroup.check(R.id.btn_fav_tv);
        } else {
            mToggleGroup.check(R.id.btn_fav_movies);
        }

        mToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.btn_fav_movies) {
                showFragment(new FavouriteMoviesFragment());
            } else if (checkedId == R.id.btn_fav_tv) {
                showFragment(new FavouriteTVShowsFragment());
            }
        });

        return view;
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.container_fav, fragment);
        transaction.commit();
    }
}




