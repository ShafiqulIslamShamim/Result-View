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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.*;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.*;
import androidx.annotation.NonNull;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.resultviewbd.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * SettingsActivity houses the nested Preference Screens of the application. Supports multiple
 * sub-settings routes, dynamic theme updates, AMOLED black adjustments, and handles app restart
 * delays on preference resets.
 */
public class SettingsActivity extends BaseActivity {

  private static final String EXTRA_PREF_KEY = "pref_key";
  private static final String EXTRA_PREF_TITLE = "pref_title";
  private static final String EXTRA_PARENT_KEY = "parent_key";
  private static final String EXTRA_PARENT_TITLE = "parent_title";
  private static final String PREF_CHANGE_FLAG = "preference_changed";

  //  Preference Keys
  public static final String KEY_DEVELOPER = "pref_developer_name_key";
  public static final String KEY_NEWS = "pref_news_information_key";
  public static final String KEY_CHECK_UPDATES = "pref_updates_checker_key";
  public static final String KEY_PRIVACY = "pref_privacy_policy_key";

  public static final String KEY_RATE_IT = "pref_rate_it_key";
  public static final String KEY_MORE_APPS = "pref_try_more_apps_key";
  public static final String KEY_FEEDBACK = "pref_feedback_key";

  private MaterialToolbar toolbar;

  /**
   * Helper utility method to construct intent instances referencing sub-preference screens.
   *
   * @param context Host Context.
   * @param prefKey Sub-preference group XML key name.
   * @param prefTitle Screen header label to show.
   * @param parentKey XML Key identifying the parent settings layout.
   * @param parentTitle Header of parent settings layout.
   * @return A fully populated Intent ready for startActivity dispatch.
   */
  public static Intent createIntent(
      Context context, String prefKey, String prefTitle, String parentKey, String parentTitle) {
    Intent intent = new Intent(context, SettingsActivity.class);
    intent.putExtra(EXTRA_PREF_KEY, prefKey);
    intent.putExtra(EXTRA_PREF_TITLE, prefTitle);
    intent.putExtra(EXTRA_PARENT_KEY, parentKey);
    intent.putExtra(EXTRA_PARENT_TITLE, parentTitle);
    return intent;
  }

  /**
   * Constructs an Intent to open the root Settings Activity screen.
   *
   * @param context Host Context.
   * @return Intent mapping to root settings container.
   */
  public static Intent createRootIntent(Context context) {
    return createIntent(context, "main_settings", "Settings", null, null);
  }

  /**
   * Sets up the settings content frame layout, configures the collapsing action toolbar layout,
   * loads the PreferenceFragmentCompat, and installs modern on-back-pressed callbacks to handle
   * main activity restarts on configuration switches.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    setContentView(R.layout.activity_settings);

    toolbar = findViewById(R.id.toolbar);
    CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(true);
      collapsingToolbar.setTitle(getString(R.string.settings_title));
    }

    String prefKey = getIntent().getStringExtra(EXTRA_PREF_KEY);
    String prefTitle = getIntent().getStringExtra(EXTRA_PREF_TITLE);

    if (getSupportActionBar() != null) {
      getSupportActionBar()
          .setTitle(prefTitle != null ? prefTitle : getString(R.string.settings_title));
    }

    if (savedInstanceState == null) {
      SettingsFragment fragment = new SettingsFragment();
      Bundle args = new Bundle();
      args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, prefKey);
      fragment.setArguments(args);
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.settings_container, fragment)
          .commit();
    }

    // Re-set title just in case
    if (getSupportActionBar() != null) {
      getSupportActionBar()
          .setTitle(prefTitle != null ? prefTitle : getString(R.string.settings_title));
    }

    // Onbackpressed modern handling
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {

                String parentKey = getIntent().getStringExtra(EXTRA_PARENT_KEY);

                if (parentKey == null) { // Root screen

                  SharedPreferences prefs =
                      PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);

                  boolean prefChanged = prefs.getBoolean(PREF_CHANGE_FLAG, false);

                  if (prefChanged) {
                    // Reset flag
                    prefs.edit().putBoolean(PREF_CHANGE_FLAG, false).apply();

                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                  }
                }

                // Default back behavior
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
              }
            });
  }

  /**
   * Resolves back navigation event trigger to the system onBackPressed callback.
   *
   * @return true representing action has been handled.
   */
  @Override
  public boolean onSupportNavigateUp() {
    getOnBackPressedDispatcher().onBackPressed();
    return true;
  }

  /**
   * Modern implementation of PreferenceFragmentCompat to parse the settings schema XML, monitor
   * changes in shared preference entries, and respond to key clicks.
   */
  public static class SettingsFragment extends PreferenceFragmentCompat
      implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Instantiates the preference tree from target settings resource XML.
     *
     * @param savedInstanceState Saved instance state.
     * @param rootKey Preference key to focus on as the root hierarchy.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences, rootKey);

      PreferenceScreen root = getPreferenceScreen();
      setupPreferenceScreenListeners(root);
    }

    /** Registers settings dynamic change listener triggers upon resume. */
    @Override
    public void onResume() {
      super.onResume();
      PreferenceManager.getDefaultSharedPreferences(requireContext())
          .registerOnSharedPreferenceChangeListener(this);
    }

    /** Unregisters settings dynamic change listener triggers during pause to prevent leakage. */
    @Override
    public void onPause() {
      super.onPause();
      PreferenceManager.getDefaultSharedPreferences(requireContext())
          .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Recursively iterates sub-preference items and registers click triggers to direct sub-screens
     * smoothly.
     *
     * @param preferenceScreen Root level PreferenceScreen class layout.
     */
    private void setupPreferenceScreenListeners(PreferenceScreen preferenceScreen) {
      String parentKey = preferenceScreen.getKey();
      String parentTitle =
          preferenceScreen.getTitle() != null ? preferenceScreen.getTitle().toString() : "Settings";

      for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
        Preference preference = preferenceScreen.getPreference(i);
        if (preference instanceof PreferenceScreen) {
          PreferenceScreen subScreen = (PreferenceScreen) preference;
          subScreen.setOnPreferenceClickListener(
              p -> {
                Intent intent =
                    createIntent(
                        requireContext(),
                        p.getKey(),
                        p.getTitle().toString(),
                        parentKey,
                        parentTitle);
                requireActivity().startActivity(intent);
                return true;
              });
          setupPreferenceScreenListeners(subScreen);
        }
      }
    }

    /**
     * Receives event notices on change to dynamic keys, marking system update dirty flags and
     * scheduling immediate app re-creations or timed cold restarts where applicable.
     *
     * @param sharedPreferences Host SharedPreferences interface.
     * @param key Value key target string changed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (key == null || key.isEmpty()) return;
      sharedPreferences.edit().putBoolean(PREF_CHANGE_FLAG, true).apply();

      if (key.equals("disable_seasonal_effect")) {
        restartAppDelayed(requireContext());

      }

      // শুধু theme_preference হলে
      else if (key.equals("language_preference")) {

        requireActivity().recreate();
      }
    }

    /**
     * Closes current app context, invalidates cached stacks, and launches a fresh cold run cleanly
     * after a short pause.
     *
     * @param context Active context to trigger restarts.
     */
    private void restartAppDelayed(Context context) {
      new android.os.Handler(android.os.Looper.getMainLooper())
          .postDelayed(
              () -> {
                Intent intent = new Intent(context, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

                System.exit(0);
              },
              400);
    }

    /**
     * Captures click actions on individual non-persistent Preference rows (Check updates, news,
     * feedback, developer, etc.), delegating them to their respective visual workflows or
     * background actions.
     *
     * @param preference The clicked Preference widget object.
     * @return true if the click callback resolved successfully.
     */
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

      String key = preference.getKey();
      Context ctx = getContext();

      switch (key) {
        case KEY_DEVELOPER:
          // Developer Telegram
          openUrl(ctx, "https://t.me/md_shamim12");
          return true;

        case KEY_NEWS:
          showNewsDialog(ctx);
          return true;

        case KEY_CHECK_UPDATES:
          // OTA update checker
          OTAUpdateHelper.hookPreference(ctx);
          return true;

        case KEY_PRIVACY:
          openUrl(
              ctx,
              "https://github.com/ShafiqulIslamShamim/Result-View/blob/main/PrivacyPolicy.txt");
          return true;

        case KEY_RATE_IT:
          openUrl(ctx, "https://play.google.com/store/apps/details?id=com.app.resultviewbd");
          return true;

        case KEY_MORE_APPS:
          openUrl(
              ctx, "https://play.google.com/store/search?q=pub:Shafiqul%20Islam%20Shamim&c=apps");
          return true;

        case KEY_FEEDBACK:
          openEmail(ctx, "shafiqulislamshamimofficial@gmail.com");
          return true;
      }

      return super.onPreferenceTreeClick(preference);
    }

    // -----------------------
    // 🔗 Utility Methods
    // -----------------------

    /**
     * Resolves a plain web string URL to Uri and dispatches standard system browser intent
     * triggers.
     *
     * @param context Active Host Context.
     * @param url Raw web target link.
     */
    private void openUrl(Context context, String url) {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      context.startActivity(intent);
    }

    /**
     * Compiles current system version code details, dynamically generates developer feedback
     * headers, and safely fires an ACTION_SENDTO mailto intent request.
     *
     * @param context Active Host Context.
     * @param toEmail Recipient feedback email.
     */
    public static void openEmail(@NonNull Context context, @NonNull String toEmail) {

      String appName = "Unknown App";
      String versionName = "unknown";
      int versionCode = -1;

      try {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);

        appName =
            pm.getApplicationLabel(pm.getApplicationInfo(context.getPackageName(), 0)).toString();
        versionName = pi.versionName != null ? pi.versionName : "unknown";
        versionCode = (int) PackageInfoCompat.getLongVersionCode(pi);

      } catch (Exception ignored) {
      }

      String subject =
          "Feedback - " + appName + " v" + versionName + " (Code: " + versionCode + ")";

      Intent intent = new Intent(Intent.ACTION_SENDTO);
      intent.setData(Uri.parse("mailto:")); // <-- MUST be plain mailto
      intent.putExtra(Intent.EXTRA_EMAIL, new String[] {toEmail});
      intent.putExtra(Intent.EXTRA_SUBJECT, subject);

      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      context.startActivity(
          Intent.createChooser(intent, context.getString(R.string.send_feedback_chooser)));
    }

    /**
     * Displays a customized news and release channel dialog highlighting Facebook & Github
     * resources.
     *
     * @param ctx Active Context.
     */
    private void showNewsDialog(Context ctx) {

      // Items
      String[] titles = {getString(R.string.news_facebook), getString(R.string.news_github)};

      int[] icons = {R.drawable.facebook, R.drawable.github};

      // RecyclerView Adapter
      NewsAdapter adapter =
          new NewsAdapter(
              titles,
              icons,
              pos -> {
                if (pos == 0) {
                  // Facebook
                  openUrl(ctx, "https://www.facebook.com/share/18wbmDDERe/");
                } else if (pos == 1) {
                  // GitHub
                  openUrl(ctx, "https://github.com/ShafiqulIslamShamim/");
                }
              });

      // RecyclerView Layout
      RecyclerView recyclerView = new RecyclerView(ctx);
      recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
      recyclerView.setAdapter(adapter);
      recyclerView.setPadding(30, 30, 30, 30);

      // Material Dialog
      new MaterialAlertDialogBuilder(ctx)
          .setTitle(R.string.news_updates_title)
          .setView(recyclerView)
          .setPositiveButton(R.string.close, null)
          .show();
    }
  }
}
