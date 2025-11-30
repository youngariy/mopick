package com.youngariy.mopick.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.content.res.ColorStateList;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.youngariy.mopick.R;
import com.youngariy.mopick.fragments.FavouritesFragment;
import com.youngariy.mopick.fragments.MoviesFragment;
import com.youngariy.mopick.fragments.TVShowsFragment;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.LocaleHelper;
import com.youngariy.mopick.utils.NetworkConnection;

public class MainActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce;
    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigation;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private String currentLanguage;
    private int currentTabId = -1;

    private BottomNavigationView.OnItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.nav_movies:
                setTitle(R.string.movies);
                applyMoviesChrome();
                setFragment(new MoviesFragment());
                currentTabId = R.id.nav_movies;
                return true;
            case R.id.nav_tv_shows:
                setTitle(R.string.tv_shows);
                applyMoviesChrome();
                setFragment(new TVShowsFragment());
                currentTabId = R.id.nav_tv_shows;
                return true;
            case R.id.nav_favorites:
                setTitle(R.string.favorites);
                applyMoviesChrome();
                setFragment(new FavouritesFragment());
                currentTabId = R.id.nav_favorites;
                return true;
        }
        return false;
    };

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentLanguage = LocaleHelper.getLanguage(this);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_movies) {
                mBottomNavigation.setSelectedItemId(R.id.nav_movies);
                mDrawerLayout.closeDrawers();
                return true;
            } else if (id == R.id.nav_tv_shows) {
                mBottomNavigation.setSelectedItemId(R.id.nav_tv_shows);
                mDrawerLayout.closeDrawers();
                return true;
            } else if (id == R.id.nav_favorites) {
                mBottomNavigation.setSelectedItemId(R.id.nav_favorites);
                mDrawerLayout.closeDrawers();
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                mDrawerLayout.closeDrawers();
                return true;
            } else if (id == R.id.nav_about) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                mDrawerLayout.closeDrawers();
                return true;
            }
            return false;
        });

        mBottomNavigation = findViewById(R.id.bottom_navigation);
        mBottomNavigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState != null) {
            currentTabId = savedInstanceState.getInt("current_tab_id", R.id.nav_movies);
        } else {
            currentTabId = R.id.nav_movies;
        }
        mBottomNavigation.setSelectedItemId(currentTabId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String savedLanguage = LocaleHelper.getLanguage(this);
        if (currentLanguage != null && !currentLanguage.equals(savedLanguage)) {
            mBottomNavigation.setSelectedItemId(currentTabId);
            recreate();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_tab_id", currentTabId);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_movies_tv_shows_people));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!NetworkConnection.isConnected(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, R.string.no_network, Toast.LENGTH_SHORT).show();
                    return true;
                }
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra(Constants.QUERY, query);
                startActivity(intent);
                searchMenuItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void applyMoviesChrome() {
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorMovieDetailBackground));
            mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorMovieDetailTextPrimary));
        }
        if (mBottomNavigation != null) {
            mBottomNavigation.setBackgroundColor(ContextCompat.getColor(this, R.color.colorMovieDetailBackground));
            ColorStateList darkStateList = ContextCompat.getColorStateList(this, R.color.bottom_nav_selector_dark);
            mBottomNavigation.setItemIconTintList(darkStateList);
            mBottomNavigation.setItemTextColor(darkStateList);
        }
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorMovieDetailBackground));
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false);
        }
    }
}
