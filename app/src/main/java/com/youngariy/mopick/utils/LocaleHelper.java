package com.youngariy.mopick.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {
    private static final String PREFS_NAME = "MoPickPrefs";
    private static final String SELECTED_LANGUAGE = "Selected_Language";
    private static final String DEFAULT_LANGUAGE = "en";

    public static Context setLocale(Context context) {
        String language = getPersistedLanguage(context);
        return updateResources(context, language);
    }

    public static Context setLocale(Context context, String language) {
        persistLanguage(context, language);
        return updateResources(context, language);
    }

    public static String getLanguage(Context context) {
        return getPersistedLanguage(context);
    }

    public static String getLanguageCode(Context context) {
        String language = getPersistedLanguage(context);
        // TMDB API language codes
        switch (language) {
            case "ko":
                return "ko";
            case "zh":
                return "zh";
            case "ja":
                return "ja";
            default:
                return "en";
        }
    }

    public static String getRegionCode(Context context) {
        String language = getPersistedLanguage(context);
        // TMDB API region codes: "KR" for Korea, "CN" for China, "JP" for Japan, "US" for United States
        switch (language) {
            case "ko":
                return "KR";
            case "zh":
                return "CN";
            case "ja":
                return "JP";
            default:
                return "US";
        }
    }

    private static String getPersistedLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE, DEFAULT_LANGUAGE);
    }

    private static void persistLanguage(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }
}

