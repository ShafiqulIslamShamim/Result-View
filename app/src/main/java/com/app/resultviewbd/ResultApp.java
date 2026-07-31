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
package com.app.resultviewbd;

import android.app.Application;
import android.content.Context;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import io.github.mohammedbaqernull.seasonal.SeasonalEffects;

/**
 * Main application class which acts as the entry point for initialization. Configures global
 * context providers and sets up seasonal winter effects if active.
 */
public class ResultApp extends Application {
  private static Context appContext;

  /**
   * Called when the application is starting, before any activity, service, or receiver objects have
   * been created. Initializes global components and weather states.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    AppContext.init(this);
    boolean seasonalEffect = SharedPrefValues.getValue("disable_seasonal_effect", false);

    if (seasonalEffect != true && GlobalWinterSystem.isWinterNow()) {
      SeasonalEffects.INSTANCE.init(this);
      SeasonalEffects.INSTANCE.enableChristmas();
      SeasonalEffects.INSTANCE.setSnowflakeCount(20);
    }
  }
}
