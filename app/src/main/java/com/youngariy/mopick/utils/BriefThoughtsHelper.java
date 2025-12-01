package com.youngariy.mopick.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.youngariy.mopick.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for managing Brief Thoughts in database
 */
public class BriefThoughtsHelper {

    public static void addBriefThought(Context context, BriefThought thought) {
        if (thought == null) return;
        
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.CONTENT_ID, thought.getContentId());
        contentValues.put(DatabaseHelper.CONTENT_TYPE, thought.getContentType());
        contentValues.put(DatabaseHelper.CONTENT_TITLE, thought.getContentTitle());
        contentValues.put(DatabaseHelper.STATUS, thought.getStatus());
        contentValues.put(DatabaseHelper.RATING, thought.getRating());
        
        // Store moods as comma-separated string
        String moodsString = "";
        if (thought.getMoods() != null && !thought.getMoods().isEmpty()) {
            moodsString = String.join(",", thought.getMoods());
        }
        contentValues.put(DatabaseHelper.MOODS, moodsString);
        
        contentValues.put(DatabaseHelper.RECOMMEND, thought.getRecommend());
        contentValues.put(DatabaseHelper.CREATED_AT, System.currentTimeMillis());

        database.insert(DatabaseHelper.BRIEF_THOUGHTS_TABLE_NAME, null, contentValues);
        database.close();
    }

    public static void updateBriefThought(Context context, BriefThought thought) {
        if (thought == null || thought.getId() <= 0) return;
        
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.CONTENT_ID, thought.getContentId());
        contentValues.put(DatabaseHelper.CONTENT_TYPE, thought.getContentType());
        contentValues.put(DatabaseHelper.CONTENT_TITLE, thought.getContentTitle());
        contentValues.put(DatabaseHelper.STATUS, thought.getStatus());
        contentValues.put(DatabaseHelper.RATING, thought.getRating());
        
        // Store moods as comma-separated string
        String moodsString = "";
        if (thought.getMoods() != null && !thought.getMoods().isEmpty()) {
            moodsString = String.join(",", thought.getMoods());
        }
        contentValues.put(DatabaseHelper.MOODS, moodsString);
        
        contentValues.put(DatabaseHelper.RECOMMEND, thought.getRecommend());

        database.update(DatabaseHelper.BRIEF_THOUGHTS_TABLE_NAME, contentValues,
                DatabaseHelper.ID + " = ?", new String[]{String.valueOf(thought.getId())});
        database.close();
    }

    public static void deleteBriefThought(Context context, int thoughtId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.delete(DatabaseHelper.BRIEF_THOUGHTS_TABLE_NAME,
                DatabaseHelper.ID + " = ?", new String[]{String.valueOf(thoughtId)});
        database.close();
    }

    public static List<BriefThought> getAllBriefThoughts(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        List<BriefThought> thoughts = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.BRIEF_THOUGHTS_TABLE_NAME, null, null, null,
                null, null, DatabaseHelper.CREATED_AT + " DESC");

        while (cursor.moveToNext()) {
            BriefThought thought = cursorToBriefThought(cursor);
            if (thought != null) {
                thoughts.add(thought);
            }
        }

        cursor.close();
        database.close();
        return thoughts;
    }

    public static BriefThought getBriefThoughtById(Context context, int thoughtId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        BriefThought thought = null;
        Cursor cursor = database.query(DatabaseHelper.BRIEF_THOUGHTS_TABLE_NAME, null,
                DatabaseHelper.ID + " = ?", new String[]{String.valueOf(thoughtId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            thought = cursorToBriefThought(cursor);
        }

        cursor.close();
        database.close();
        return thought;
    }

    public static BriefThought getBriefThoughtByContent(Context context, int contentId, String contentType) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        BriefThought thought = null;
        Cursor cursor = database.query(DatabaseHelper.BRIEF_THOUGHTS_TABLE_NAME, null,
                DatabaseHelper.CONTENT_ID + " = ? AND " + DatabaseHelper.CONTENT_TYPE + " = ?",
                new String[]{String.valueOf(contentId), contentType},
                null, null, DatabaseHelper.CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            thought = cursorToBriefThought(cursor);
        }

        cursor.close();
        database.close();
        return thought;
    }

    private static BriefThought cursorToBriefThought(Cursor cursor) {
        try {
            int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.ID);
            int contentIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.CONTENT_ID);
            int contentTypeColumnIndex = cursor.getColumnIndex(DatabaseHelper.CONTENT_TYPE);
            int contentTitleColumnIndex = cursor.getColumnIndex(DatabaseHelper.CONTENT_TITLE);
            int statusColumnIndex = cursor.getColumnIndex(DatabaseHelper.STATUS);
            int ratingColumnIndex = cursor.getColumnIndex(DatabaseHelper.RATING);
            int moodsColumnIndex = cursor.getColumnIndex(DatabaseHelper.MOODS);
            int recommendColumnIndex = cursor.getColumnIndex(DatabaseHelper.RECOMMEND);
            int createdAtColumnIndex = cursor.getColumnIndex(DatabaseHelper.CREATED_AT);

            int id = idColumnIndex != -1 ? cursor.getInt(idColumnIndex) : 0;
            int contentId = contentIdColumnIndex != -1 ? cursor.getInt(contentIdColumnIndex) : 0;
            String contentType = contentTypeColumnIndex != -1 ? cursor.getString(contentTypeColumnIndex) : "";
            String contentTitle = contentTitleColumnIndex != -1 ? cursor.getString(contentTitleColumnIndex) : "";
            String status = statusColumnIndex != -1 ? cursor.getString(statusColumnIndex) : "";
            int rating = ratingColumnIndex != -1 ? cursor.getInt(ratingColumnIndex) : 0;
            String moodsString = moodsColumnIndex != -1 ? cursor.getString(moodsColumnIndex) : "";
            String recommend = recommendColumnIndex != -1 ? cursor.getString(recommendColumnIndex) : "";
            long createdAt = createdAtColumnIndex != -1 ? cursor.getLong(createdAtColumnIndex) : System.currentTimeMillis();

            // Parse moods from comma-separated string
            List<String> moods = new ArrayList<>();
            if (moodsString != null && !moodsString.isEmpty()) {
                moods = new ArrayList<>(Arrays.asList(moodsString.split(",")));
            }

            return new BriefThought(id, contentId, contentType, contentTitle, status, rating, moods, recommend, createdAt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

