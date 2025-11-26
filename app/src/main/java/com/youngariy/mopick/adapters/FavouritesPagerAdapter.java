package com.youngariy.mopick.adapters;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.youngariy.mopick.R;
import com.youngariy.mopick.fragments.FavouriteMoviesFragment;
import com.youngariy.mopick.fragments.FavouriteTVShowsFragment;

/**
 * Created by hitanshu on 10/8/17.
 */

public class FavouritesPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public FavouritesPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FavouriteMoviesFragment();
            case 1:
                return new FavouriteTVShowsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.movies);
            case 1:
                return mContext.getResources().getString(R.string.tv_shows);
        }
        return null;
    }
}




