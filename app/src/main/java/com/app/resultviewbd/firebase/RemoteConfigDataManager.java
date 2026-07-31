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
package com.app.resultviewbd.firebase;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.app.resultviewbd.R;
import com.app.resultviewbd.activity.NoInternetActivity;
import com.app.resultviewbd.recycle_view.model.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Manager class responsible for fetching, activating, and parsing configuration data from Firebase
 * Remote Config. It also handles automatic offline handling and UI loading overlays.
 */
public class RemoteConfigDataManager {

  private static final String TAG = "RemoteConfigManager";
  private static final String PARAM_CONFIG_JSON = "app_result_config_json";

  private final Context context;
  private final FirebaseRemoteConfig remoteConfig;

  private AlertDialog loadingDialog;
  private boolean isWaitingForInternet = false;
  private OnDataLoadedListener pendingListener;

  private ConnectivityManager connectivityManager;
  private ConnectivityManager.NetworkCallback networkCallback;

  /** Callback interface used to receive Remote Config data or errors. */
  public interface OnDataLoadedListener {
    /**
     * Called when Remote Config data is parsed and loaded successfully.
     *
     * @param configJson The loaded configuration JSON object.
     */
    void onDataLoaded(JSONObject configJson);

    /**
     * Called when an error occurs during parsing or loading.
     *
     * @param message The error message detailing the failure.
     */
    void onError(String message);
  }

  /**
   * Constructs a RemoteConfigDataManager and sets the default fetch interval settings.
   *
   * @param context The application or activity Context.
   */
  public RemoteConfigDataManager(Context context) {
    this.context = context;
    this.remoteConfig = FirebaseRemoteConfig.getInstance();
    this.connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    FirebaseRemoteConfigSettings configSettings =
        new FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hour in production
            .build();
    remoteConfig.setConfigSettingsAsync(configSettings);
  }

  /**
   * Initiates loading configuration data. Displays a loading overlay and falls back to a cached
   * config if offline, registering a network callback to retry later.
   *
   * @param listener The listener to report status back to.
   */
  public void loadData(OnDataLoadedListener listener) {
    showLoadingDialog();

    if (!OTAUpdateHelper.isInternetAvailable(context)) {
      dismissLoadingDialog();
      Snackbar.make(
              ((android.app.Activity) context).findViewById(android.R.id.content),
              context.getString(R.string.internet_not_available),
              Snackbar.LENGTH_LONG)
          .show();

      showNoInternetScreen(listener);
      deliverLastActivatedConfig(listener); // Use cached/activated config
      startWaitingForInternet();
      return;
    }

    stopWaitingForInternet();
    fetchFromRemoteConfig(listener);
  }

  /**
   * Navigates to the NoInternetActivity screen when internet is unavailable.
   *
   * @param listener The loading listener to retry when internet is restored.
   */
  private void showNoInternetScreen(OnDataLoadedListener listener) {
    this.pendingListener = listener;
    isWaitingForInternet = true;

    Intent intent = new Intent(context, NoInternetActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);

    startWaitingForInternet();
  }

  /**
   * Registers a network callback to automatically retry loading data when an internet connection is
   * established.
   */
  private void startWaitingForInternet() {
    if (networkCallback != null) return;

    NetworkRequest request =
        new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build();

    networkCallback =
        new ConnectivityManager.NetworkCallback() {
          @Override
          public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Log.d(TAG, "Internet restored – reloading...");

            new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(
                    () -> {
                      Snackbar.make(
                              ((android.app.Activity) context).findViewById(android.R.id.content),
                              context.getString(R.string.internet_available_auto_reload),
                              Snackbar.LENGTH_LONG)
                          .show();

                      if (isWaitingForInternet && pendingListener != null) {
                        loadData(pendingListener);
                      }
                    },
                    800);
          }
        };

    connectivityManager.registerNetworkCallback(request, networkCallback);
  }

  /** Unregisters any active connectivity listeners and stops waiting for network status changes. */
  private void stopWaitingForInternet() {
    isWaitingForInternet = false;
    pendingListener = null;
    if (networkCallback != null) {
      try {
        connectivityManager.unregisterNetworkCallback(networkCallback);
      } catch (Exception ignored) {
      }
      networkCallback = null;
    }
  }

  /**
   * Fetches latest configuration from Firebase Remote Config and triggers activation.
   *
   * @param listener The listener to receive the resulting configuration JSON.
   */
  private void fetchFromRemoteConfig(OnDataLoadedListener listener) {
    remoteConfig
        .fetchAndActivate()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "Remote Config fetched & activated");
              } else {
                Log.w(TAG, "Fetch failed, using cached values");
              }
              deliverLastActivatedConfig(listener);
            });
  }

  /**
   * Decodes and delivers the last activated configuration JSON string from remote config string
   * values.
   *
   * @param listener The listener to receive the resulting configuration JSON.
   */
  private void deliverLastActivatedConfig(OnDataLoadedListener listener) {
    try {
      String configJsonStr = remoteConfig.getString(PARAM_CONFIG_JSON);
      if (configJsonStr.isEmpty()) {
        throw new JSONException("Empty config JSON");
      }
      JSONObject json = new JSONObject(configJsonStr);
      listener.onDataLoaded(json);
    } catch (JSONException e) {
      Log.e(TAG, "Invalid JSON from Remote Config", e);
      listener.onError(context.getString(R.string.json_parse_error));
    } finally {
      dismissLoadingDialog();
    }
  }

  // ==================== PUBLIC UTILITY METHODS ====================

  /**
   * Extracts a list of item names from a specified JSON array key.
   *
   * @param json The root JSONObject containing the array.
   * @param key The string key corresponding to the array.
   * @return A list of extracted names, or an empty list if not present.
   * @throws JSONException If JSON parsing fails.
   */
  public List<String> getNameList(JSONObject json, String key) throws JSONException {
    List<String> names = new ArrayList<>();
    if (!json.has(key)) return names;

    JSONArray array = json.getJSONArray(key);
    for (int i = 0; i < array.length(); i++) {
      JSONObject item = array.getJSONObject(i);
      names.add(item.optString("name", "Unknown"));
    }
    return names;
  }

  /**
   * Extracts a list of URLs from a specified JSON array key.
   *
   * @param json The root JSONObject containing the array.
   * @param key The string key corresponding to the array.
   * @return A list of extracted URLs, or an empty list if not present.
   * @throws JSONException If JSON parsing fails.
   */
  public List<String> getUrlList(JSONObject json, String key) throws JSONException {
    List<String> urls = new ArrayList<>();
    if (!json.has(key)) return urls;

    JSONArray array = json.getJSONArray(key);
    for (int i = 0; i < array.length(); i++) {
      JSONObject item = array.getJSONObject(i);
      urls.add(item.optString("url", "#"));
    }
    return urls;
  }

  /**
   * Extracts list items from a specified JSON array and populates the given target list. Translates
   * the name from JSON into the local representation.
   *
   * @param json The root JSONObject containing the array.
   * @param key The key corresponding to the array of items.
   * @param targetList The list of ListItems to add the parsed ItemModel items into.
   */
  public void addArrayItems(JSONObject json, String key, List<ListItem> targetList) {
    if (json == null || !json.has(key)) return;

    try {
      JSONArray array = json.getJSONArray(key);
      for (int i = 0; i < array.length(); i++) {
        JSONObject obj = array.getJSONObject(i);
        String name = obj.optString("name", "Unknown");
        name = JsonNameTranslator.translateNameFromJson(context, name);
        String url = obj.optString("url", "#");
        targetList.add(new ItemModel(name, url));
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to parse array: " + key, e);
    }
  }

  // ==============================================================

  /**
   * Builds and displays a MaterialAlertDialog loading popup overlay to block interactions while
   * Remote Config fetches the latest results.
   */
  private void showLoadingDialog() {
    if (loadingDialog != null && loadingDialog.isShowing()) return;

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setCancelable(false);

    LoadingIndicator loadingIndicator = new LoadingIndicator(context);
    loadingIndicator.setId(View.generateViewId());

    android.widget.TextView textView = new android.widget.TextView(context);
    textView.setId(View.generateViewId());
    textView.setText(context.getString(R.string.loading_latest_result));
    textView.setTextSize(16);

    ConstraintLayout layout = new ConstraintLayout(context);
    layout.setPadding(60, 60, 60, 60);
    layout.addView(loadingIndicator);
    layout.addView(textView);

    ConstraintSet set = new ConstraintSet();
    set.clone(layout);
    set.connect(
        loadingIndicator.getId(),
        ConstraintSet.START,
        ConstraintSet.PARENT_ID,
        ConstraintSet.START);
    set.connect(
        loadingIndicator.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
    set.connect(
        loadingIndicator.getId(),
        ConstraintSet.BOTTOM,
        ConstraintSet.PARENT_ID,
        ConstraintSet.BOTTOM);

    set.connect(
        textView.getId(), ConstraintSet.START, loadingIndicator.getId(), ConstraintSet.END, 24);
    set.connect(textView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
    set.connect(
        textView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
    set.connect(textView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
    set.applyTo(layout);

    builder.setView(layout);
    loadingDialog = builder.create();
    loadingDialog.show();
  }

  /** Safely dismisses the loading dialog overlay. */
  private void dismissLoadingDialog() {
    if (loadingDialog != null && loadingDialog.isShowing()) {
      loadingDialog.dismiss();
      loadingDialog = null;
    }
  }

  /** Performs resource cleanup, canceling connectivity observers and loading screens. */
  public void cleanup() {
    stopWaitingForInternet();
    dismissLoadingDialog();
  }
}
