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
package com.app.resultviewbd.activity;

import android.content.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.*;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.app.resultviewbd.R;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import java.util.*;

/**
 * Base activity class that serves as the foundation for all activities in the application. Manages
 * configuration updates, dynamic Material 3 custom theming, localizations, and system bar
 * appearance handling.
 */
public abstract class BaseActivity extends AppCompatActivity {

  protected String lastAppliedThemePref;
  protected String lastAppliedAppThemePref;
  protected boolean lastAppliedAmoled;

  protected String currentAppliedLanguage;

  /**
   * Initializes the activity, applies localized resources, configures standard Edge-to-Edge window
   * fitting, and invokes the base creation lifecycle.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {

    applyLocalTheme();

    LocaleHelper.applyLocale(this);
    currentAppliedLanguage = LocaleHelper.getSavedLanguage(this);

    // Modern Android edge-to-edge
    EdgeToEdge.enable(this);

    super.onCreate(savedInstanceState);
  }

  /**
   * Monitors dynamic preference adjustments (such as dark mode toggle, AMOLED black mode, and local
   * languages) during activity resumption, triggering an automatic recreation layout cycle on
   * change.
   */
  @Override
  protected void onResume() {
    super.onResume();

    String themePref = SharedPrefValues.getValue("theme_preference", "0");
    String appThemePref = SharedPrefValues.getValue("app_theme_preference", "0");
    boolean amoled = SharedPrefValues.getValue("amoled_black_mode", false);
    String language = LocaleHelper.getSavedLanguage(this);

    boolean needsRecreate =
        !Objects.equals(lastAppliedThemePref, themePref)
            || !Objects.equals(lastAppliedAppThemePref, appThemePref)
            || lastAppliedAmoled != amoled
            || !Objects.equals(currentAppliedLanguage, language);

    if (needsRecreate) {
      lastAppliedThemePref = themePref;
      lastAppliedAppThemePref = appThemePref;
      lastAppliedAmoled = amoled;
      currentAppliedLanguage = language;

      recreate();
    }
  }

  /**
   * Sets the activity content view from a layout resource ID and prepares status/navigation
   * paddings.
   *
   * @param layoutResID Integer resource ID of the target XML layout.
   */
  @Override
  public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);
    afterContentSet();
  }

  /**
   * Sets the activity content view from an inflated View and prepares status/navigation paddings.
   *
   * @param view Target View hierarchy to display.
   */
  @Override
  public void setContentView(View view) {
    super.setContentView(view);
    afterContentSet();
  }

  /**
   * General post-content inflation workflow helper. Sets edge padding and applies translucent
   * system bar icons.
   */
  private void afterContentSet() {
    setupEdgeToEdgePadding();
    getWindow().getDecorView().post(this::applySystemBarAppearance);
  }

  /**
   * Dynamically resolves and sets the custom Theme resource (Emerald, Ocean, Coral, Blossom, etc.)
   * in combination with light/dark preference modes.
   */
  protected void applyLocalTheme() {
    boolean isLight = isLightThemeActive();
    String appThemePref = SharedPrefValues.getValue("app_theme_preference", "0");
    boolean amoledEnabled = SharedPrefValues.getValue("amoled_black_mode", false);

    lastAppliedThemePref = SharedPrefValues.getValue("theme_preference", "0");
    lastAppliedAppThemePref = appThemePref;
    lastAppliedAmoled = amoledEnabled;

    final int themeRes;

    if (appThemePref.equals("0")) {
      themeRes = isLight ? R.style.AppThemeLight : R.style.AppThemeDark;
    } else if (appThemePref.equals("1")) {
      themeRes = isLight ? R.style.AppThemeEmeraldLight : R.style.AppThemeEmeraldDark;
    } else if (appThemePref.equals("2")) {
      themeRes = isLight ? R.style.AppThemeBlossomLight : R.style.AppThemeBlossomDark;
    } else if (appThemePref.equals("3")) {
      themeRes = isLight ? R.style.AppThemeOceanLight : R.style.AppThemeOceanDark;
    } else if (appThemePref.equals("4")) {
      themeRes = isLight ? R.style.AppThemeAmberLight : R.style.AppThemeAmberDark;
    } else if (appThemePref.equals("5")) {
      themeRes = isLight ? R.style.AppThemeCoralLight : R.style.AppThemeCoralDark;
    } else {
      themeRes = isLight ? R.style.AppThemeLight : R.style.AppThemeDark;
    }

    setTheme(themeRes);

    if (!isLight && amoledEnabled) {
      getTheme().applyStyle(R.style.AmoledOverlay, true);
    }
  }

  /**
   * Resolves system status and navigation bar icons to display cleanly on light or dark backdrops.
   */
  protected void applySystemBarAppearance() {
    boolean isLight = isLightThemeActive();

    View decorView = getWindow().getDecorView();
    WindowInsetsControllerCompat controller =
        new WindowInsetsControllerCompat(getWindow(), decorView);

    controller.setAppearanceLightStatusBars(isLight);
    controller.setAppearanceLightNavigationBars(isLight);
  }

  /**
   * Applies top status bar spacing, left/right window margins, and handles IME virtual keyboard
   * shifts.
   */
  private void setupEdgeToEdgePadding() {
    View root = findViewById(android.R.id.content);
    if (root == null) return;

    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

          int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);

          v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);

          return insets;
        });
  }

  /**
   * Resolves whether the current active aesthetic configuration represents light mode or dark mode.
   *
   * @return true if light theme is active, false if dark mode.
   */
  protected boolean isLightThemeActive() {

    String themePref = SharedPrefValues.getValue("theme_preference", "0");

    switch (themePref) {
      case "2": // Dark forced
        return false;

      case "3": // Light forced
        return true;

      default: // Follow system
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        return mode != Configuration.UI_MODE_NIGHT_YES;
    }
  }

  /**
   * Displays a long-duration native Android Toast notification message.
   *
   * @param message Text string to show to the user.
   */
  protected void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }
}
