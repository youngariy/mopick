package com.youngariy.mopick.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.youngariy.mopick.R;
import com.google.android.material.tabs.TabLayout;
import com.youngariy.mopick.adapters.FavouritesPagerAdapter;

/**
 * Created by hitanshu on 10/8/17.
 */

public class FavouritesFragment extends Fragment {

    private ViewPager mFavViewPager;
    private TabLayout mFavTabLayout;
    private FavouritesPagerAdapter mFavPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        mFavViewPager = view.findViewById(R.id.view_pager_fav);
        mFavTabLayout = view.findViewById(R.id.tab_layout_fav);

        mFavPagerAdapter = new FavouritesPagerAdapter(getChildFragmentManager(), getContext());
        mFavViewPager.setAdapter(mFavPagerAdapter);
        mFavViewPager.setOffscreenPageLimit(mFavPagerAdapter.getCount() - 1);
        mFavTabLayout.setupWithViewPager(mFavViewPager);

        return view;
    }
}




