package com.youngariy.mopick.utils;

import com.youngariy.mopick.network.movies.Genre;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hitanshu on 13/8/17.
 */

public class MovieGenres {

    private static HashMap<Integer, String> genresMap;

    public static boolean isGenresListLoaded() {
        return (genresMap != null);
    }

    public static void loadGenresList(List<Genre> genres) {
        if (genres == null) return;
        genresMap = new HashMap<>();
        for (Genre genre : genres) {
            genresMap.put(genre.getId(), genre.getGenreName());
        }
    }

    public static String getGenreName(Integer genreId) {
        if (genresMap == null || genreId == null) return "";
        String genreName = genresMap.get(genreId);
        return genreName != null ? genreName : "";
    }
}




