package com.youngariy.mopick.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hitanshu on 9/8/17.
 */

// hitanshu : use Room
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.db";
    public static final String FAV_MOVIES_TABLE_NAME = "FavouriteMoviesTable";
    public static final String FAV_TV_SHOWS_TABLE_NAME = "FavouriteTVShowsTable";
    public static final String BRIEF_THOUGHTS_TABLE_NAME = "BriefThoughtsTable";
    public static final String ID = "id";
    public static final String MOVIE_ID = "movie_id";
    public static final String TV_SHOW_ID = "tv_show_id";
    public static final String POSTER_PATH = "poster_path";
    public static final String NAME = "name";
    public static final String VOTE_AVERAGE = "vote_average";
    
    // Brief Thoughts fields
    public static final String CONTENT_ID = "content_id";
    public static final String CONTENT_TYPE = "content_type"; // "movie" or "tv_show"
    public static final String CONTENT_TITLE = "content_title";
    public static final String STATUS = "status"; // "watching" or "completed"
    public static final String RATING = "rating"; // 1-5 stars
    public static final String MOODS = "moods"; // comma-separated emoji codes
    public static final String RECOMMEND = "recommend"; // "yes" or "no"
    public static final String CREATED_AT = "created_at"; // timestamp

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String queryCreateMovieTable = "CREATE TABLE " + FAV_MOVIES_TABLE_NAME + " ( "
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MOVIE_ID + " INTEGER, "
                + POSTER_PATH + " TEXT, "
                + NAME + " TEXT, "
                + VOTE_AVERAGE + " REAL )";
        String queryCreateTVShowTable = "CREATE TABLE " + FAV_TV_SHOWS_TABLE_NAME + " ( "
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TV_SHOW_ID + " INTEGER, "
                + POSTER_PATH + " TEXT, "
                + NAME + " TEXT, "
                + VOTE_AVERAGE + " REAL )";
        String queryCreateBriefThoughtsTable = "CREATE TABLE " + BRIEF_THOUGHTS_TABLE_NAME + " ( "
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CONTENT_ID + " INTEGER, "
                + CONTENT_TYPE + " TEXT, "
                + CONTENT_TITLE + " TEXT, "
                + STATUS + " TEXT, "
                + RATING + " INTEGER, "
                + MOODS + " TEXT, "
                + RECOMMEND + " TEXT, "
                + CREATED_AT + " INTEGER )";
        
        sqLiteDatabase.execSQL(queryCreateMovieTable);
        sqLiteDatabase.execSQL(queryCreateTVShowTable);
        sqLiteDatabase.execSQL(queryCreateBriefThoughtsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL("ALTER TABLE " + FAV_MOVIES_TABLE_NAME + " ADD COLUMN " + VOTE_AVERAGE + " REAL");
            sqLiteDatabase.execSQL("ALTER TABLE " + FAV_TV_SHOWS_TABLE_NAME + " ADD COLUMN " + VOTE_AVERAGE + " REAL");
        }
        if (oldVersion < 3) {
            String queryCreateBriefThoughtsTable = "CREATE TABLE " + BRIEF_THOUGHTS_TABLE_NAME + " ( "
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CONTENT_ID + " INTEGER, "
                    + CONTENT_TYPE + " TEXT, "
                    + CONTENT_TITLE + " TEXT, "
                    + STATUS + " TEXT, "
                    + RATING + " INTEGER, "
                    + MOODS + " TEXT, "
                    + RECOMMEND + " TEXT, "
                    + CREATED_AT + " INTEGER )";
            sqLiteDatabase.execSQL(queryCreateBriefThoughtsTable);
        }
    }
}




