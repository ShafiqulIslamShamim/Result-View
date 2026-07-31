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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.app.resultviewbd.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.recycle_view.adapter.*;
import com.app.resultviewbd.recycle_view.model.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;

/**
 * Activity displayed when no active internet connection is detected. Provides rapid navigation
 * shortcuts to system WiFi and Mobile Data settings panels, and automatically dismisses itself once
 * connection is re-established.
 */
public class NoInternetActivity extends BaseActivity {

  private ConnectivityManager connectivityManager;
  private ConnectivityManager.NetworkCallback networkCallback;

  /**
   * Initializes the layout, sets up actions for Wifi/Data quick links, and starts monitoring
   * background connectivity recovery status.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_no_internet);

    connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    findViewById(R.id.btnMobileData)
        .setOnClickListener(
            v -> startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)));

    findViewById(R.id.btnWifi)
        .setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));

    listenForInternet();
  }

  /**
   * Registers a network status observer to detect when an active network with
   * NET_CAPABILITY_INTERNET becomes available.
   */
  private void listenForInternet() {
    NetworkRequest request =
        new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build();

    networkCallback =
        new ConnectivityManager.NetworkCallback() {
          @Override
          public void onAvailable(@NonNull Network network) {
            runOnUiThread(() -> finish());
          }
        };

    connectivityManager.registerNetworkCallback(request, networkCallback);
  }

  /** Cleanly unregisters the active network state listener on activity shutdown. */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (networkCallback != null) {
      try {
        connectivityManager.unregisterNetworkCallback(networkCallback);
      } catch (Exception ignored) {
      }
    }
  }
}
