package com.kcorteel.travel_esteban_kylian.travelshare.util;

import android.content.Context;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public final class TravelShareLocaleManager {

    private TravelShareLocaleManager() {
    }

    public static void applyLanguage(String languageTag) {
        String resolvedTag = normalizeLanguageTag(languageTag);
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(resolvedTag));
    }

    public static Locale resolveLocale(Context context) {
        LocaleListCompat applicationLocales = AppCompatDelegate.getApplicationLocales();
        if (!applicationLocales.isEmpty()) {
            Locale appLocale = applicationLocales.get(0);
            if (appLocale != null) {
                return appLocale;
            }
        }

        if (context != null) {
            LocaleList locales = context.getResources().getConfiguration().getLocales();
            if (!locales.isEmpty()) {
                return locales.get(0);
            }
        }

        return Locale.getDefault();
    }

    public static String normalizeLanguageTag(String languageTag) {
        if (languageTag == null) {
            return "fr";
        }

        String normalized = languageTag.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "en":
                return "en";
            case "es":
                return "es";
            case "fr":
            default:
                return "fr";
        }
    }
}
