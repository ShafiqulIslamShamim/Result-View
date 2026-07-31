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
package com.app.resultviewbd.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import androidx.preference.PreferenceManager;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;

/**
 * Accessor helper facilitating type-safe values reading and writing operations out of the system's
 * default shared preference databases.
 */
public class SharedPrefValues {

  /**
   * Retrieves default SharedPreferences instance.
   *
   * @return Active SharedPreferences structure.
   */
  private static SharedPreferences getSharedPreferences() {
    Context context = AppContext.get();
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  /**
   * Reads String value associated to key.
   *
   * @param key Entry identification key.
   * @param defaultValue Value returned when missing or blank.
   * @return Found String value.
   */
  public static String getValue(String key, String defaultValue) {
    SharedPreferences prefs = getSharedPreferences();
    if (prefs != null && prefs.contains(key)) {
      String value = prefs.getString(key, null);
      return !TextUtils.isEmpty(value) ? value : defaultValue;
    }
    return defaultValue;
  }

  /**
   * Reads integer value associated to key.
   *
   * @param key Entry identification key.
   * @param defaultValue Value returned when missing or unparseable.
   * @return Found integer value.
   */
  public static int getValue(String key, int defaultValue) {
    String value = getValue(key, String.valueOf(defaultValue));
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Reads float value associated to key.
   *
   * @param key Entry identification key.
   * @param defaultValue Value returned when missing or unparseable.
   * @return Found float value.
   */
  public static float getValue(String key, float defaultValue) {
    String value = getValue(key, String.valueOf(defaultValue));
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Reads double value associated to key.
   *
   * @param key Entry identification key.
   * @param defaultValue Value returned when missing or unparseable.
   * @return Found double value.
   */
  public static double getValue(String key, double defaultValue) {
    String value = getValue(key, String.valueOf(defaultValue));
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Reads boolean value associated to key.
   *
   * @param key Entry identification key.
   * @param defaultValue Value returned when missing or unparseable.
   * @return Found boolean value.
   */
  public static boolean getValue(String key, boolean defaultValue) {
    String value = getValue(key, defaultValue ? "1" : "0");

    try {
      return Integer.parseInt(value) != 0;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Maps unformatted textual values cleanly onto boolean flags.
   *
   * @param value Textual boolean or numerical state indicator.
   * @return Evaluated boolean value.
   */
  public static boolean parseFlexibleBoolean(String value) {
    if (value == null) return false;
    value = value.trim().toLowerCase();
    return value.equals("true") || value.equals("1");
  }

  /**
   * Translates active boolean states into their standard integer equivalents.
   *
   * @param value Original boolean state.
   * @return Return 1 for true, 0 for false.
   */
  public static int booleanToInt(boolean value) {
    return value ? 1 : 0;
  }

  /**
   * Commits updates associated to key synchronously to memory.
   *
   * @param key Target configuration lookup key.
   * @param value Value string payload.
   */
  public static void putValue(String key, String value) {
    SharedPreferences prefs = getSharedPreferences();
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(key, value);
    editor.apply(); // or editor.commit();
  }

  /**
   * Writes values if the designated key is not already registered.
   *
   * @param key Target configuration lookup key.
   * @param defaultValue Default fallback value.
   */
  public static void putValueIfAbsent(String key, String defaultValue) {
    SharedPreferences prefs = getSharedPreferences();
    if (!prefs.contains(key)) {
      SharedPreferences.Editor editor = prefs.edit();
      editor.putString(key, defaultValue);
      editor.apply();
    }
  }
}
