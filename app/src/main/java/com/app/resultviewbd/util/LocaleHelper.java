/*
 * Copyright (c) 2026 Shafiqul Islam Shamim
 * GitHub: https://github.com/ShafiqulIslamShamim/Result-View
 *
 * All Rights Reserved.
 *
 * This source code is made publicly available solely for viewing, collaboration,
 * educational reference, and submitting pull requests to the official repository.
 *
 * No permission is granted to copy, modify, redistribute, sublicense, or use
 * this source code, in whole or in part, for personal, commercial, or any other
 * purpose without the prior written permission of the copyright holder.
 */
package com.app.resultviewbd.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;

/**
 * Utility wrapper helping apply dynamic translations or locale settings inside the active app
 * process.
 */
public final class LocaleHelper {

  private static final String KEY_LANGUAGE = "language_preference";

  /** Private constructor to prevent instantiation. */
  private LocaleHelper() {}

  /* ================== PUBLIC API ================== */

  /**
   * Reads saved preference values and applies the selected language locale onto delegate views.
   *
   * @param context Active reference context.
   */
  public static void applyLocale(Context context) {
    String lang = getSavedLanguage(context);

    if ("default".equals(lang)) {
      AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
    } else {
      LocaleListCompat locales = LocaleListCompat.forLanguageTags(lang);

      AppCompatDelegate.setApplicationLocales(locales);
    }
  }

  /**
   * Commits and applies selected translation locale languages to storage.
   *
   * @param context Host context.
   * @param lang Language code string.
   */
  public static void saveLanguage(Context context, String lang) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    prefs.edit().putString(KEY_LANGUAGE, lang).apply();
    applyLocale(context);
  }

  /**
   * Deletes custom locale choices, resetting app translation behaviors to match system locales.
   *
   * @param context Host context.
   */
  public static void clearLanguage(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    prefs.edit().remove(KEY_LANGUAGE).apply();

    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
  }

  /**
   * Extracts currently committed language configuration settings.
   *
   * @param context Host context.
   * @return Saved language identifier code, defaulting to "default".
   */
  public static String getSavedLanguage(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    return prefs.getString(KEY_LANGUAGE, "default");
  }
}
