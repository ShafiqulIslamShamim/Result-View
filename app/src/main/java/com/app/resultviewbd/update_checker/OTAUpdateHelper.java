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
package com.app.resultviewbd.update_checker;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.app.resultviewbd.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

/**
 * Utility wrapper providing in-app update checks and internet availability queries. Integrates
 * Google Play Core libraries to fetch and display update dialogues.
 */
@SuppressWarnings("deprecation")
public class OTAUpdateHelper {

  private static final int RC_APP_UPDATE = 9001;

  /**
   * Utility method evaluating the status of active internet connectivity.
   *
   * @param context Target reference context.
   * @return true if network is currently connected and active.
   */
  public static boolean isInternetAvailable(@NonNull Context context) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) return false;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      Network network = cm.getActiveNetwork();
      if (network == null) return false;
      NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
      return capabilities != null
          && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
              || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
              || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    } else {
      return checkInternetConnectionLegacy(cm);
    }
  }

  /**
   * Performs connectivity validation checks using deprecated APIs on Android versions below Q.
   *
   * @param cm Contextual system connectivity manager.
   * @return true if connectivity status maps as connected.
   */
  @SuppressWarnings("deprecation")
  private static boolean checkInternetConnectionLegacy(ConnectivityManager cm) {
    android.net.NetworkInfo activeNetwork = cm.getNetworkInfo(cm.getActiveNetwork());
    if (activeNetwork == null) {
      activeNetwork = cm.getActiveNetworkInfo();
    }
    return activeNetwork != null && activeNetwork.isConnected();
  }

  /**
   * Iterates back through context wrappers to isolate base Activity layers cleanly.
   *
   * @param context Source context parameter.
   * @return Unwrapped Activity context, or null if unresolvable.
   */
  private static Activity getActivity(Context context) {
    if (context instanceof Activity) {
      return (Activity) context;
    } else if (context instanceof ContextWrapper) {
      return getActivity(((ContextWrapper) context).getBaseContext());
    }
    return null;
  }

  /**
   * Triggers an immediate manually-driven App update check inside standard alert configurations.
   * Redirects onto external Play Store detail pages when internal calls fail.
   *
   * @param context Active reference context.
   */
  public static void hookPreference(Context context) {
    Activity activity = getActivity(context);
    if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
      Toast.makeText(context, R.string.update_no_activity, Toast.LENGTH_SHORT).show();
      return;
    }

    if (!isInternetAvailable(activity)) {
      Toast.makeText(activity, R.string.update_no_internet, Toast.LENGTH_LONG).show();
      return;
    }

    AlertDialog progressDialog =
        new MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.update_checking_title)
            .setMessage(R.string.update_checking_message)
            .setCancelable(false)
            .show();

    AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(activity);
    appUpdateManager
        .getAppUpdateInfo()
        .addOnSuccessListener(
            appUpdateInfo -> {
              if (progressDialog.isShowing()) {
                progressDialog.dismiss();
              }

              if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                  && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                  appUpdateManager.startUpdateFlowForResult(
                      appUpdateInfo,
                      activity,
                      AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                      RC_APP_UPDATE);
                } catch (Exception e) {
                  e.printStackTrace();
                  Toast.makeText(
                          activity,
                          getString(activity, R.string.update_failed_launch)
                              + ": "
                              + e.getMessage(),
                          Toast.LENGTH_SHORT)
                      .show();
                }
              } else {
                new MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.update_up_to_date_title)
                    .setMessage(R.string.update_up_to_date_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
              }
            })
        .addOnFailureListener(
            e -> {
              if (progressDialog.isShowing()) {
                progressDialog.dismiss();
              }

              try {
                String packageName = activity.getPackageName();
                android.content.Intent intent =
                    new android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(
                            "https://play.google.com/store/apps/details?id=" + packageName));
                activity.startActivity(intent);
              } catch (Exception ex) {
                Toast.makeText(activity, R.string.update_failed_play_store, Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  /**
   * Performs an automated daily update check, throttling execution frequencies to run once every 24
   * hours.
   *
   * @param context Active reference context.
   */
  public static void checkForUpdatesIfDue(Context context) {
    Activity activity = getActivity(context);
    if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

    if (!isInternetAvailable(activity)) return;

    AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(activity);

    appUpdateManager
        .getAppUpdateInfo()
        .addOnSuccessListener(
            appUpdateInfo -> {
              if (appUpdateInfo.updateAvailability()
                  == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                  appUpdateManager.startUpdateFlowForResult(
                      appUpdateInfo,
                      activity,
                      AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                      RC_APP_UPDATE);
                } catch (Exception e) {
                  e.printStackTrace();
                }
                return;
              }

              // Throttle auto-checking...
              final String PREF_NAME = "update_pref";
              final String KEY_LAST_CHECK = "last_check_time";
              final long CHECK_INTERVAL = 24L * 60 * 60 * 1000;

              SharedPreferences prefs =
                  activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
              long lastCheck = prefs.getLong(KEY_LAST_CHECK, 0);
              long currentTime = System.currentTimeMillis();

              if (currentTime - lastCheck >= CHECK_INTERVAL) {
                prefs.edit().putLong(KEY_LAST_CHECK, currentTime).apply();

                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                  new MaterialAlertDialogBuilder(activity)
                      .setTitle(R.string.update_available_title)
                      .setMessage(R.string.update_available_message)
                      .setPositiveButton(
                          R.string.update_now,
                          (dialog, which) -> {
                            try {
                              appUpdateManager.startUpdateFlowForResult(
                                  appUpdateInfo,
                                  activity,
                                  AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                                  RC_APP_UPDATE);
                            } catch (Exception e) {
                              e.printStackTrace();
                            }
                          })
                      .setNegativeButton(R.string.update_later, null)
                      .setCancelable(true)
                      .show();
                }
              }
            });
  }

  /**
   * Safely reads text resources associated with local contexts.
   *
   * @param context App context.
   * @param resId Resource identifier.
   * @return Mapped layout String text.
   */
  private static String getString(Context context, int resId) {
    return context.getString(resId);
  }
}
