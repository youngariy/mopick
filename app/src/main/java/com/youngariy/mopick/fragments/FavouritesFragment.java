package com.youngariy.mopick.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager.widget.ViewPager;

import com.youngariy.mopick.R;
import com.youngariy.mopick.adapters.FavouritesPagerAdapter;

/**
 * Created by hitanshu on 10/8/17.
 */

public class FavouritesFragment extends Fragment {

    private ViewPager mViewPager;
    private AppCompatButton mMoviesButton;
    private AppCompatButton mTvButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        mViewPager = view.findViewById(R.id.view_pager_fav);
        mMoviesButton = view.findViewById(R.id.btn_fav_movies);
        mTvButton = view.findViewById(R.id.btn_fav_tv);

        mViewPager.setAdapter(new FavouritesPagerAdapter(getChildFragmentManager(), getContext()));
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateToggle(position == 0);
            }
        });

        mMoviesButton.setOnClickListener(v -> {
            mViewPager.setCurrentItem(0, true);
            updateToggle(true);
        });
        mTvButton.setOnClickListener(v -> {
            mViewPager.setCurrentItem(1, true);
            updateToggle(false);
        });

        updateToggle(true);

        return view;
    }

    private void updateToggle(boolean moviesSelected) {
        if (moviesSelected) {
            mMoviesButton.setBackgroundResource(R.drawable.fav_toggle_active);
            mMoviesButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            mTvButton.setBackgroundResource(R.drawable.fav_toggle_inactive);
            mTvButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        } else {
            mMoviesButton.setBackgroundResource(R.drawable.fav_toggle_inactive);
            mMoviesButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            mTvButton.setBackgroundResource(R.drawable.fav_toggle_active);
            mTvButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        }
    }
}




