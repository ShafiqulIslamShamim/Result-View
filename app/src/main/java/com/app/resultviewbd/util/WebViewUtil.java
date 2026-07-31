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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;

/** Accessor helper facilitating browser redirection and Chrome Custom Tab instantiations. */
public class WebViewUtil {

  /** Private constructor to prevent direct instantiation. */
  private WebViewUtil() {}

  /**
   * Open link using Chrome Custom Tabs if available, fallback to browser/WebView.
   *
   * @param context Active context.
   * @param name Target header string name for inner WebView layout fallbacks.
   * @param url Site destination URL.
   */
  public static void openLink(@NonNull Context context, @NonNull String name, @NonNull String url) {
    Uri uri = Uri.parse(url);

    // Try Custom Tabs (no session)
    try {
      CustomTabsIntent customTabsIntent =
          new CustomTabsIntent.Builder().setShowTitle(true).setUrlBarHidingEnabled(true).build();

      //  customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      customTabsIntent.launchUrl(context, uri);

      return; // success → stop here
    } catch (Exception ignored) {
    }

    // Try default browser
    try {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
      browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(browserIntent);
      return;
    } catch (ActivityNotFoundException ignored) {
    }

    // Final fallback: open internal WebView
    Intent i = new Intent(context, WebViewActivity.class);
    i.putExtra("name", name);
    i.putExtra("url", url);
    //  i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(i);
  }
}
