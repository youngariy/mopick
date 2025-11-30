package com.youngariy.mopick.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.youngariy.mopick.database.DatabaseHelper;
import com.youngariy.mopick.network.movies.MovieBrief;
import com.youngariy.mopick.network.tvshows.TVShowBrief;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hitanshu on 9/8/17.
 */

public class Favourite {

    //MOVIES

    public static void addMovieToFav(Context context, Integer movieId, String posterPath, String name, Double voteAverage) {
        if (movieId == null) return;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        if (!isMovieFav(context, movieId)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.MOVIE_ID, movieId);
            contentValues.put(DatabaseHelper.POSTER_PATH, posterPath);
            contentValues.put(DatabaseHelper.NAME, name);
            if (voteAverage != null) {
                contentValues.put(DatabaseHelper.VOTE_AVERAGE, voteAverage);
            }
            database.insert(DatabaseHelper.FAV_MOVIES_TABLE_NAME, null, contentValues);
        }
        database.close();
    }

    public static void removeMovieFromFav(Context context, Integer movieId) {
        if (movieId == null) return;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        if (isMovieFav(context, movieId)) {
            database.delete(DatabaseHelper.FAV_MOVIES_TABLE_NAME, DatabaseHelper.MOVIE_ID + " = " + movieId, null);
        }
        database.close();
    }

    public static boolean isMovieFav(Context context, Integer movieId) {
        if (movieId == null) return false;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        boolean isMovieFav;
        Cursor cursor = database.query(DatabaseHelper.FAV_MOVIES_TABLE_NAME, null, DatabaseHelper.MOVIE_ID + " = " + movieId, null, null, null, null);
        if (cursor.getCount() == 1)
            isMovieFav = true;
        else
            isMovieFav = false;

        cursor.close();
        database.close();
        return isMovieFav;
    }

    public static List<MovieBrief> getFavMovieBriefs(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        List<MovieBrief> favMovies = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.FAV_MOVIES_TABLE_NAME, null, null, null, null, null, DatabaseHelper.ID + " DESC");
        while (cursor.moveToNext()) {
            int movieIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.MOVIE_ID);
            int movieId = -1; // Default to an invalid ID
            if (movieIdColumnIndex != -1) {
                movieId = cursor.getInt(movieIdColumnIndex);
            }

            int posterPathColumnIndex = cursor.getColumnIndex(DatabaseHelper.POSTER_PATH);
            String posterPath = null;
            if (posterPathColumnIndex != -1) {
                posterPath = cursor.getString(posterPathColumnIndex);
            }

            int nameColumnIndex = cursor.getColumnIndex(DatabaseHelper.NAME);
            String name = null;
            if (nameColumnIndex != -1) {
                name = cursor.getString(nameColumnIndex);
            }
            int voteAverageColumnIndex = cursor.getColumnIndex(DatabaseHelper.VOTE_AVERAGE);
            Double voteAverage = null;
            if (voteAverageColumnIndex != -1 && !cursor.isNull(voteAverageColumnIndex)) {
                voteAverage = cursor.getDouble(voteAverageColumnIndex);
            }
            favMovies.add(new MovieBrief(null, movieId, null, voteAverage, name, null, posterPath, null, null, null, null, null, null, null));
        }
        cursor.close();
        database.close();
        return favMovies;
    }

    public static List<Integer> getFavMovieIds(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        List<Integer> ids = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.FAV_MOVIES_TABLE_NAME, new String[]{DatabaseHelper.MOVIE_ID}, null, null, null, null, DatabaseHelper.ID + " DESC");
        while (cursor.moveToNext()) {
            int movieIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.MOVIE_ID);
            if (movieIdColumnIndex != -1) {
                ids.add(cursor.getInt(movieIdColumnIndex));
            }
        }
        cursor.close();
        database.close();
        return ids;
    }

    //TV SHOWS

    public static void addTVShowToFav(Context context, Integer tvShowId, String posterPath, String name, Double voteAverage) {
        if (tvShowId == null) return;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        if (!isTVShowFav(context, tvShowId)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.TV_SHOW_ID, tvShowId);
            contentValues.put(DatabaseHelper.POSTER_PATH, posterPath);
            contentValues.put(DatabaseHelper.NAME, name);
            if (voteAverage != null) {
                contentValues.put(DatabaseHelper.VOTE_AVERAGE, voteAverage);
            }
            database.insert(DatabaseHelper.FAV_TV_SHOWS_TABLE_NAME, null, contentValues);
        }
        database.close();
    }

    public static void removeTVShowFromFav(Context context, Integer tvShowId) {
        if (tvShowId == null) return;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        if (isTVShowFav(context, tvShowId)) {
            database.delete(DatabaseHelper.FAV_TV_SHOWS_TABLE_NAME, DatabaseHelper.TV_SHOW_ID + " = " + tvShowId, null);
        }
        database.close();
    }

    public static boolean isTVShowFav(Context context, Integer tvShowId) {
        if (tvShowId == null) return false;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        boolean isTVShowFav;
        Cursor cursor = database.query(DatabaseHelper.FAV_TV_SHOWS_TABLE_NAME, null, DatabaseHelper.TV_SHOW_ID + " = " + tvShowId, null, null, null, null);
        if (cursor.getCount() == 1)
            isTVShowFav = true;
        else
            isTVShowFav = false;

        cursor.close();
        database.close();
        return isTVShowFav;
    }

    public static List<TVShowBrief> getFavTVShowBriefs(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        List<TVShowBrief> favTVShows = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.FAV_TV_SHOWS_TABLE_NAME, null, null, null, null, null, DatabaseHelper.ID + " DESC");
        while (cursor.moveToNext()) {
            int tvShowIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.TV_SHOW_ID);
            int tvShowId = -1;
            if (tvShowIdColumnIndex != -1) {
                tvShowId = cursor.getInt(tvShowIdColumnIndex);
            }

            int posterPathColumnIndex = cursor.getColumnIndex(DatabaseHelper.POSTER_PATH);
            String posterPath = null;
            if (posterPathColumnIndex != -1) {
                posterPath = cursor.getString(posterPathColumnIndex);
            }

            int nameColumnIndex = cursor.getColumnIndex(DatabaseHelper.NAME);
            String name = null;
            if (nameColumnIndex != -1) {
                name = cursor.getString(nameColumnIndex);
            }
            int voteAverageColumnIndex = cursor.getColumnIndex(DatabaseHelper.VOTE_AVERAGE);
            Double voteAverage = null;
            if (voteAverageColumnIndex != -1 && !cursor.isNull(voteAverageColumnIndex)) {
                voteAverage = cursor.getDouble(voteAverageColumnIndex);
            }
            favTVShows.add(new TVShowBrief(null, tvShowId, name, null, voteAverage, posterPath, null, null, null, null, null, null, null));
        }
        cursor.close();
        database.close();
        return favTVShows;
    }

    public static List<Integer> getFavTVShowIds(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        List<Integer> ids = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.FAV_TV_SHOWS_TABLE_NAME, new String[]{DatabaseHelper.TV_SHOW_ID}, null, null, null, null, DatabaseHelper.ID + " DESC");
        while (cursor.moveToNext()) {
            int tvShowIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.TV_SHOW_ID);
            if (tvShowIdColumnIndex != -1) {
                ids.add(cursor.getInt(tvShowIdColumnIndex));
            }
        }
        cursor.close();
        database.close();
        return ids;
    }

}




