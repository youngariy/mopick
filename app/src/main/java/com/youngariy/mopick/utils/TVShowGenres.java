package com.youngariy.mopick.utils;

import com.youngariy.mopick.network.tvshows.Genre;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hitanshu on 13/8/17.
 */

public class TVShowGenres {

    private static HashMap<Integer, String> genresMap;
    private static List<Genre> cachedGenres;

    public static boolean isGenresListLoaded() {
        return (genresMap != null);
    }

    public static void loadGenresList(List<Genre> genres) {
        if (genres == null) return;
        cachedGenres = genres;
        genresMap = new HashMap<>();
        for (Genre genre : genres) {
            genresMap.put(genre.getId(), genre.getGenreName());
        }
    }

    public static List<Genre> getGenresList() {
        return cachedGenres;
    }

    public static String getGenreName(Integer genreId) {
        if (genreId == null) return null;
        return genresMap.get(genreId);
    }
}




