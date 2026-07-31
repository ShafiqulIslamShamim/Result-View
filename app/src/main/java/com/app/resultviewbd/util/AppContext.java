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
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;

/**
 * Accessor wrapper storing the global App-level Context context. Enables easy background context
 * retrieval across nested helper layers.
 */
public class AppContext {
  private static Context appContext;

  /**
   * Initializes the static reference to the application context.
   *
   * @param context Host context reference.
   */
  public static void init(Context context) {
    if (appContext == null) {
      appContext = context.getApplicationContext();
    }
  }

  /**
   * Obtains the global registered Application context.
   *
   * @return Active Context reference.
   * @throws IllegalStateException when init hasn't been called.
   */
  public static Context get() {
    if (appContext == null) {
      throw new IllegalStateException("AppContext not initialized! Call init() first.");
    }
    return appContext;
  }
}
