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
import androidx.annotation.NonNull;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base SharedPreferences helper supporting change listener subscriptions. Must be initialized once
 * during Application initialization.
 */
public final class SharedPrefValuesBase {

  private static final String PREF_NAME = "app_prefs_v1";
  private static SharedPreferences prefs;
  private static final Set<OnPrefChangeListener> listeners =
      Collections.synchronizedSet(new HashSet<>());

  /** Callback interface used to observe preference value transitions. */
  public interface OnPrefChangeListener {
    /**
     * Dispatched when an observed preference value updates.
     *
     * @param key Target configuration key.
     * @param newValue The newly applied value string.
     */
    void onPrefChanged(@NonNull String key, @NonNull String newValue);
  }

  /**
   * Initializes the preferences reference context.
   *
   * @param context App context reference.
   */
  public static void init(@NonNull Context context) {
    if (prefs == null) {
      prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
  }

  /**
   * Reads a string preference value.
   *
   * @param key Entry identification key.
   * @param defaultValue Default value fallback if absent.
   * @return Found configuration String.
   */
  public static String getValue(@NonNull String key, @NonNull String defaultValue) {
    ensureInit();
    return prefs.getString(key, defaultValue);
  }

  /**
   * Writes a string preference value, notifying active change observers.
   *
   * @param key Target configuration key.
   * @param value Textual preference value to write.
   */
  public static void setValue(@NonNull String key, @NonNull String value) {
    ensureInit();
    String old = prefs.getString(key, null);
    if (value.equals(old)) return; // nothing changed

    prefs.edit().putString(key, value).apply();

    // notify listeners
    synchronized (listeners) {
      for (OnPrefChangeListener l : listeners) {
        try {
          l.onPrefChanged(key, value);
        } catch (Exception ignore) {
          // defensive: a misbehaving listener won't crash the loop
        }
      }
    }
  }

  /**
   * Registers a callback observer listener.
   *
   * @param l Observer callback instance.
   */
  public static void addListener(@NonNull OnPrefChangeListener l) {
    listeners.add(l);
  }

  /**
   * Unregisters a callback observer listener.
   *
   * @param l Observer callback instance.
   */
  public static void removeListener(@NonNull OnPrefChangeListener l) {
    listeners.remove(l);
  }

  /** Verifies initialization state, throwing an exception if called out of order. */
  private static void ensureInit() {
    if (prefs == null) {
      throw new IllegalStateException(
          "SharedPrefValues not initialized. Call SharedPrefValues.init(context) in"
              + " Application.onCreate()");
    }
  }
}
