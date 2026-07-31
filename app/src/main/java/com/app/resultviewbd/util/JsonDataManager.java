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

import android.content.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.resultviewbd.*;
import com.app.resultviewbd.R;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Manager handling remote server JSON downloads, parsing, and caching. Leverages Google Volley and
 * manages fallback mechanisms for internet disruptions.
 */
public class JsonDataManager {

  private static final String PREFS_NAME = "LinksPrefs";
  private static final String KEY_JSON = "cached_json";
  private static final String KEY_UPDATED = "updated_timestamp";

  private final Context context;
  private final String jsonUrl;
  private final RequestQueue requestQueue;
  private ConnectivityManager connectivityManager;
  private ConnectivityManager.NetworkCallback networkCallback;

  private AlertDialog loadingDialog;
  private AlertDialog noInternetDialog;

  private boolean isWaitingForInternet = false;
  private OnDataLoadedListener pendingListener;

  /** Callback interface used to dispatch fetched results lists or network errors. */
  public interface OnDataLoadedListener {
    /**
     * Called when JSON contents successfully load and parse into lists.
     *
     * @param jsonObject The complete parsed remote config document.
     * @param boardNames Board name text list.
     * @param boardUrls Board URL target list.
     * @param serverNames Result servers name list.
     * @param serverUrls Result servers URL list.
     * @param nuNames National University list names.
     * @param nuUrls National University list target URLs.
     */
    void onDataLoaded(
        JSONObject jsonObject,
        List<String> boardNames,
        List<String> boardUrls,
        List<String> serverNames,
        List<String> serverUrls,
        List<String> nuNames,
        List<String> nuUrls);

    /**
     * Called when JSON loading fails.
     *
     * @param message Textual failure error message.
     */
    void onError(String message);
  }

  /**
   * Constructs a JsonDataManager with designated URL.
   *
   * @param context Active reference context.
   * @param jsonUrl Source URL target where configuration file resides.
   */
  public JsonDataManager(Context context, String jsonUrl) {
    this.context = context;
    this.jsonUrl = jsonUrl;
    this.requestQueue = Volley.newRequestQueue(context);
    this.connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  /**
   * Orchestrates remote config parsing; reads cached copy if offline, else initiates download.
   *
   * @param listener Callback hooks context.
   */
  public void loadData(OnDataLoadedListener listener) {
    showLoadingDialog();

    if (!OTAUpdateHelper.isInternetAvailable(context)) {
      dismissLoadingDialog();
      View rootView = ((android.app.Activity) context).findViewById(android.R.id.content);

      Snackbar.make(
              rootView, context.getString(R.string.internet_not_available), Snackbar.LENGTH_LONG)
          .show();
      //   showNoInternetDialog(listener);
      showNoInternetScreen(listener);
      fetchOrLoadCached(listener);
      startWaitingForInternet();
      return;
    }

    // Internet is available
    isWaitingForInternet = false;
    pendingListener = null;
    unregisterCallbackIfNeeded();

    fetchOrLoadCached(listener);
  }

  /**
   * Dispatches intent starting the NoInternet visual activity screen layout.
   *
   * @param listener Callback hooks context.
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
   * Displays fallback popup alerts detailing missing internet connections.
   *
   * @param listener Callback hooks context.
   */
  private void showNoInternetDialog(OnDataLoadedListener listener) {
    this.pendingListener = listener;
    isWaitingForInternet = true;

    if (noInternetDialog != null && noInternetDialog.isShowing()) {
      noInternetDialog.dismiss();
    }

    MaterialAlertDialogBuilder builder =
        new MaterialAlertDialogBuilder(context)
            .setTitle(R.string.no_internet_title)
            .setMessage(R.string.no_internet_message)
            .setCancelable(false)
            .setNeutralButton(
                R.string.cancel,
                (dialog, which) -> {
                  dialog.dismiss();
                  listener.onError(context.getString(R.string.no_internet_error));
                  //      stopWaitingForInternet();
                })
            .setNegativeButton(
                R.string.mobile_data,
                (dialog, which) -> {
                  Intent intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  context.startActivity(intent);
                })
            .setPositiveButton(
                R.string.enable_wifi,
                (dialog, which) -> {
                  Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  context.startActivity(intent);
                });

    noInternetDialog = builder.create();
    noInternetDialog.show();

    // 🔥 Important: ALWAYS start waiting for internet
    startWaitingForInternet();
  }

  /**
   * Registers a network callback mapping to auto-reload layout once connection drops are restored.
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

            Log.d("JsonDataManager", "Internet available! Auto-reloading...");
            // Display a simple Snackbar

            // This gets the root content view of the Activity
            View rootView = ((android.app.Activity) context).findViewById(android.R.id.content);

            Snackbar.make(
                    rootView,
                    context.getString(R.string.internet_available_auto_reload),
                    Snackbar.LENGTH_LONG)
                .show();

            new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(
                    () -> {
                      if (noInternetDialog != null && noInternetDialog.isShowing()) {
                        noInternetDialog.dismiss();
                      }

                      if (isWaitingForInternet && pendingListener != null) {
                        loadData(pendingListener); // auto reload
                      }

                      stopWaitingForInternet();
                    },
                    800);
          }
        };

    connectivityManager.registerNetworkCallback(request, networkCallback);
  }

  /** Unregisters connectivity callback listeners, cleaning active state holds. */
  private void stopWaitingForInternet() {
    isWaitingForInternet = false;
    pendingListener = null;
    unregisterCallbackIfNeeded();
  }

  /** Safely unregisters system connectivity network callbacks to prevent memory leaks. */
  private void unregisterCallbackIfNeeded() {
    if (networkCallback != null) {
      try {
        connectivityManager.unregisterNetworkCallback(networkCallback);
      } catch (Exception ignored) {
      }
      networkCallback = null;
    }
  }

  /** Displays a themed progress dialog indicating remote configuration retrieval actions. */
  private void showLoadingDialog() {
    if (loadingDialog != null && loadingDialog.isShowing()) return;

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setCancelable(false);

    // Context for the LoadingIndicator
    Context themedContext =
        new ContextThemeWrapper(
            context,
            com.google.android.material.R.style.Widget_Material3_LoadingIndicator_Contained);

    // Create the LoadingIndicator
    LoadingIndicator loadingIndicator = new LoadingIndicator(themedContext);
    loadingIndicator.setId(View.generateViewId());
    loadingIndicator.setVisibility(View.VISIBLE);

    // Create the TextView
    TextView textView = new TextView(context);
    textView.setId(View.generateViewId());
    textView.setText(context.getString(R.string.loading_latest_result));
    textView.setTextSize(16);

    // Create the Layout
    ConstraintLayout layout = new ConstraintLayout(context);
    layout.setPadding(60, 60, 60, 60);

    layout.addView(loadingIndicator);

    ConstraintLayout.LayoutParams textParams =
        new ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT);

    // 3. Add TextView with the specific params
    layout.addView(textView, textParams);

    // Constraints
    ConstraintSet set = new ConstraintSet();
    set.clone(layout);

    // Indicator Constraints (Left side)
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

  /** Dismisses progress alert dialogue overlays. */
  private void dismissLoadingDialog() {
    if (loadingDialog != null && loadingDialog.isShowing()) {
      loadingDialog.dismiss();
      loadingDialog = null;
    }
  }

  /**
   * Enqueues an HTTP request to fetch configuration JSON data, caching updates locally.
   *
   * @param listener Callback hooks context.
   */
  private void fetchOrLoadCached(OnDataLoadedListener listener) {
    JsonObjectRequest request =
        new JsonObjectRequest(
            Request.Method.GET,
            jsonUrl,
            null,
            response -> {
              try {
                String updatedTimestamp = response.getString("updated");
                SharedPreferences prefs =
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

                String cachedTimestamp = prefs.getString(KEY_UPDATED, "");

                if (!updatedTimestamp.equals(cachedTimestamp)) {
                  prefs
                      .edit()
                      .putString(KEY_JSON, response.toString())
                      .putString(KEY_UPDATED, updatedTimestamp)
                      .apply();
                } else {
                  String cached = prefs.getString(KEY_JSON, response.toString());
                  response = new JSONObject(cached);
                }

                parseAndDeliver(response, listener);

              } catch (JSONException e) {
                handleJsonError(e, listener);
              }
            },
            error -> {
              Log.e("JsonDataManager", "Network error", error);
              fallbackToCache(listener);
            });

    requestQueue.add(request);
  }

  /**
   * Attempts cache retrieval when connectivity requests timeout or fail.
   *
   * @param listener Callback hooks context.
   */
  private void fallbackToCache(OnDataLoadedListener listener) {
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    String cached = prefs.getString(KEY_JSON, null);

    if (cached != null) {
      try {
        JSONObject cachedJson = new JSONObject(cached);
        parseAndDeliver(cachedJson, listener);
      } catch (JSONException e) {
        handleJsonError(e, listener);
      }
    } else {
      dismissLoadingDialog();
      //  showNoInternetDialog(listener);
      showNoInternetScreen(listener);
    }
  }

  /**
   * Dispatches completed arrays of parsed JSON fields into registered callback listeners.
   *
   * @param json Parsed root JSON document.
   * @param listener Callback hooks context.
   * @throws JSONException when nested structures are missing expected formats.
   */
  private void parseAndDeliver(JSONObject json, OnDataLoadedListener listener)
      throws JSONException {

    List<String> boardNames = new ArrayList<>(), boardUrls = new ArrayList<>();
    List<String> serverNames = new ArrayList<>(), serverUrls = new ArrayList<>();
    List<String> nuNames = new ArrayList<>(), nuUrls = new ArrayList<>();

    parseSection(json, "ssc_hsc_boards", boardNames, boardUrls);
    parseSection(json, "ssc_hsc_result_servers", serverNames, serverUrls);
    parseSection(json, "nu_result", nuNames, nuUrls);

    listener.onDataLoaded(json, boardNames, boardUrls, serverNames, serverUrls, nuNames, nuUrls);
    dismissLoadingDialog();
  }

  /**
   * Handles JSON unmarshalling exceptions.
   *
   * @param e JSON Exception context.
   * @param listener Callback hooks context.
   */
  private void handleJsonError(JSONException e, OnDataLoadedListener listener) {
    e.printStackTrace();
    dismissLoadingDialog();
    listener.onError(context.getString(R.string.json_parse_error, e.getMessage()));
  }

  /**
   * Extracts targeted section arrays out of the parsed configuration file mapping values onto
   * parallel lists.
   *
   * @param json Parsed root JSON document.
   * @param key Config section identifier.
   * @param names target mapping name array.
   * @param urls target mapping URL array.
   * @throws JSONException when target keys contain misconfigured types.
   */
  private void parseSection(JSONObject json, String key, List<String> names, List<String> urls)
      throws JSONException {
    if (!json.has(key)) return;
    JSONArray array = json.getJSONArray(key);
    for (int i = 0; i < array.length(); i++) {
      JSONObject item = array.getJSONObject(i);
      names.add(item.optString("name", "Unknown"));
      urls.add(item.optString("url", "#"));
    }
  }

  /**
   * Cleans up pending dialog structures and cancels active network listeners to prevent memory
   * leaks.
   */
  public void cleanup() {
    stopWaitingForInternet();
    if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
    if (noInternetDialog != null && noInternetDialog.isShowing()) noInternetDialog.dismiss();
  }
}
