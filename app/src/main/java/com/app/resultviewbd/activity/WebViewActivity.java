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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.*;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.Settings;
import android.view.*;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.*;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import com.app.resultviewbd.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.recycle_view.adapter.*;
import com.app.resultviewbd.recycle_view.model.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import im.delight.android.webview.AdvancedWebView;
import io.github.mohammedbaqernull.seasonal.SeasonalEffects;

/**
 * Activity hosting a customized, full-featured web container utilizing AdvancedWebView. It
 * integrates features like print dispatching, blob scheme file downloads, network status
 * monitoring, and custom PDF/image rendering.
 */
public class WebViewActivity extends BaseActivity implements AdvancedWebView.Listener {

  private AdvancedWebView mWebView;
  private ProgressBar topProgressBar;
  private MaterialToolbar toolbar;
  private NetworkCallback networkCallback;
  private SpeedDialView speedDial;

  private String url;

  // SharedPreferences keys
  private static final String PREFS_NAME = "webview_prefs";
  private static final String KEY_DESKTOP_MODE = "desktop_mode";
  private boolean isDesktopMode = true;

  /**
   * Initializes layout components, setups toolbar, extracts page bundle parameters, restores
   * persistent web views preference states, and registers interface bindings.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (GlobalWinterSystem.isWinterNow()) {
      SeasonalEffects.INSTANCE.setSnowflakeCount(0);
    }

    setContentView(R.layout.activity_webview);

    speedDial = findViewById(R.id.speedDial);
    toolbar = findViewById(R.id.toolbar);

    mWebView = findViewById(R.id.webView);
    topProgressBar = findViewById(R.id.topProgressBar);

    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    url = getIntent().getStringExtra("url");
    String pageName = getIntent().getStringExtra("name");
    if (pageName != null) getSupportActionBar().setTitle(pageName);

    // LOAD Desktop Mode from SharedPreferences
    isDesktopMode =
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_DESKTOP_MODE, true);

    setupWebView();
    setDesktopMode(isDesktopMode);

    setupDraggableFab();
    loadUrl();
  }

  /**
   * Evaluates the active web URL and triggers page load inside the AdvancedWebView. Displays local
   * fallback assets if the connection is missing.
   */
  private void loadUrl() {
    if (OTAUpdateHelper.isInternetAvailable(this) && url != null && !url.isEmpty()) {
      mWebView.loadUrl(url);
    } else {
      mWebView.loadUrl("file:///android_asset/offline.html");
      Toast.makeText(this, getString(R.string.toast_no_url_or_network), Toast.LENGTH_SHORT).show();
    }
  }

  // ───────────────────────────────────────────────────────────
  //  WebView Setup
  // ───────────────────────────────────────────────────────────

  /** Configures WebView settings, registers javascript interfaces, and handles network changes. */
  @SuppressLint({"SetJavaScriptEnabled"})
  private void setupWebView() {

    mWebView.setListener(this, this);

    WebSettings ws = mWebView.getSettings();
    ws.setJavaScriptEnabled(true);
    ws.setDomStorageEnabled(true);
    ws.setSupportZoom(true);
    ws.setBuiltInZoomControls(true);
    ws.setDisplayZoomControls(false);
    ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

    mWebView.setCookiesEnabled(true);
    mWebView.setThirdPartyCookiesEnabled(true);

    mWebView.setDownloadListener(
        (url, userAgent, contentDisposition, mimetype, contentLength) ->
            downloadFile(url, userAgent, contentDisposition, mimetype, contentLength));

    // JavaScript interface for print()
    mWebView.addJavascriptInterface(
        new Object() {
          @android.webkit.JavascriptInterface
          public void onPrintRequest() {
            runOnUiThread(() -> printWebView(mWebView));
          }
        },
        "AndroidPrint");

    // JavaScript interface for Handling Blob Downloads / Web Previews
    mWebView.addJavascriptInterface(
        new Object() {
          @android.webkit.JavascriptInterface
          public void saveBlob(String base64Data, String mimeType, String filename) {
            handleBlobDownload(base64Data, mimeType, filename);
          }
        },
        "BlobHandler");

    // WebChromeClient (progress + popup window)
    mWebView.setWebChromeClient(
        new WebChromeClient() {

          @Override
          public void onProgressChanged(WebView view, int newProgress) {
            topProgressBar.setProgress(newProgress);

            if (newProgress == 100) {
              topProgressBar
                  .animate()
                  .alpha(0f)
                  .setDuration(250)
                  .withEndAction(
                      () -> {
                        topProgressBar.setVisibility(View.GONE);
                        topProgressBar.setAlpha(1f);
                      })
                  .start();
            } else if (topProgressBar.getVisibility() == View.GONE) {
              topProgressBar.setVisibility(View.VISIBLE);
            }
          }

          @Override
          public boolean onCreateWindow(
              WebView view, boolean dialog, boolean userGesture, Message resultMsg) {

            WebView newWebView = new WebView(WebViewActivity.this);

            newWebView.addJavascriptInterface(
                new Object() {
                  @android.webkit.JavascriptInterface
                  public void onPrintRequest() {
                    runOnUiThread(() -> printWebView(newWebView));
                  }
                },
                "AndroidPrint");

            newWebView.setWebViewClient(
                new WebViewClient() {

                  @Override
                  public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    if (url.startsWith("blob:") || url.startsWith("data:")) {
                      printWebView(v);
                      return true;
                    }
                    return false;
                  }

                  @Override
                  @SuppressWarnings("deprecation")
                  public boolean shouldOverrideUrlLoading(WebView v, String url) {
                    if (url.startsWith("blob:") || url.startsWith("data:")) {
                      printWebView(v);
                      return true;
                    }
                    return false;
                  }

                  @Override
                  public void onPageFinished(WebView v, String url) {
                    v.evaluateJavascript(
                        "(function(){if(typeof"
                            + " window.print==='function'){setTimeout(window.print,400);}})();",
                        null);
                  }
                });

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
          }
        });

    // Reset and apply standard WebViewClient setup
    resetWebViewClientSetup();

    networkCallback =
        new NetworkCallback() {
          @Override
          public void onAvailable(Network network) {
            runOnUiThread(
                () -> {
                  String currentUrl = mWebView.getUrl();
                  if (currentUrl != null
                      && currentUrl.startsWith("file:///android_asset/offline.html")) {
                    mWebView.loadUrl(url);

                    View rootView = findViewById(android.R.id.content);

                    Snackbar.make(
                            rootView,
                            getString(R.string.toast_network_available),
                            Snackbar.LENGTH_LONG)
                        .show();
                  }
                });
          }

          @Override
          public void onLost(Network network) {
            runOnUiThread(
                () -> {
                  View rootView = findViewById(android.R.id.content);

                  Snackbar snackbar =
                      Snackbar.make(
                          rootView, getString(R.string.toast_network_lost), Snackbar.LENGTH_LONG);

                  snackbar.setAction(
                      getString(R.string.action_settings),
                      new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                          Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                          v.getContext().startActivity(intent);
                        }
                      });

                  snackbar.show();
                });
          }
        };

    ConnectivityManager connectivityManager =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

    connectivityManager.registerDefaultNetworkCallback(networkCallback);

    // Onbackpressed modern handling
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                if (!mWebView.onBackPressed()) {
                  return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
              }
            });
  }

  // ───────────────────────────────────────────────────────────
  //  Draggable FAB
  // ───────────────────────────────────────────────────────────

  /**
   * Initializes speed dial action buttons, formats backgrounds dynamically based on theme context,
   * and listens to capture-to-PDF or capture-to-image selections.
   */
  private void setupDraggableFab() {
    int colorOnPrimaryContainer =
        MaterialColors.getColor(
            speedDial, com.google.android.material.R.attr.colorOnPrimaryContainer);

    int colorPrimaryContainer =
        MaterialColors.getColor(
            speedDial, com.google.android.material.R.attr.colorPrimaryContainer);

    /* ---- PDF item ---- */
    Drawable pdfDrawable =
        ResourcesCompat.getDrawable(getResources(), R.drawable.ic_file_pdf_box, getTheme());

    speedDial.addActionItem(
        new SpeedDialActionItem.Builder(R.id.action_save_pdf, pdfDrawable)
            .setLabel(getString(R.string.pdf_save_title))
            .setFabBackgroundColor(colorPrimaryContainer)
            .setFabImageTintColor(colorOnPrimaryContainer)
            .setLabelBackgroundColor(colorPrimaryContainer)
            .setLabelColor(colorOnPrimaryContainer)
            .setLabelClickable(true)
            .create());

    /* ---- JPG item ---- */
    Drawable jpgDrawable =
        ResourcesCompat.getDrawable(getResources(), R.drawable.imagesmode_24px, getTheme());

    speedDial.addActionItem(
        new SpeedDialActionItem.Builder(R.id.action_save_jpg, jpgDrawable)
            .setLabel(getString(R.string.save_as_jpg))
            .setFabBackgroundColor(colorPrimaryContainer)
            .setFabImageTintColor(colorOnPrimaryContainer)
            .setLabelBackgroundColor(colorPrimaryContainer)
            .setLabelColor(colorOnPrimaryContainer)
            .setLabelClickable(true)
            .create());

    /* ---- Action listener ---- */
    speedDial.setOnActionSelectedListener(
        actionItem -> {
          if (actionItem.getId() == R.id.action_save_pdf) {
            printPage();
            return false;
          }
          if (actionItem.getId() == R.id.action_save_jpg) {
            WebViewToImageUtil.saveWebViewAsImages(this, mWebView);
            return false;
          }
          return false;
        });
  }

  // ───────────────────────────────────────────────────────────
  //  Download & Blob Extraction Mechanics
  // ───────────────────────────────────────────────────────────

  /**
   * Triggers download manager helper configuration or extracts base64 representation of internal
   * blob schemas.
   *
   * @param url Download origin URL.
   * @param userAgent Header field representing requesting agent client version.
   * @param contentDisposition Metadata descriptors including filename strings.
   * @param mimetype File signature representation.
   * @param contentLength Size of download transaction in bytes.
   */
  private void downloadFile(
      String url,
      String userAgent,
      String contentDisposition,
      String mimetype,
      double contentLength) {

    // INTERCEPT BLOB SCHEME LINKS
    if (url != null && url.startsWith("blob:")) {
      String guessedFileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
      triggerBlobDownloadJavaScript(url, mimetype, guessedFileName);
      return;
    }

    try {
      DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
      request.setMimeType(mimetype);
      request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url));
      request.addRequestHeader("User-Agent", userAgent);
      request.setDescription("Downloading file...");
      request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
      request.setNotificationVisibility(
          DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
      request.setDestinationInExternalPublicDir(
          Environment.DIRECTORY_DOWNLOADS,
          URLUtil.guessFileName(url, contentDisposition, mimetype));
      DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
      dm.enqueue(request);
      Toast.makeText(
              getApplicationContext(),
              getString(R.string.toast_downloading_file),
              Toast.LENGTH_LONG)
          .show();

    } catch (Exception e) {
      Toast.makeText(
              this, getString(R.string.toast_download_failed, e.getMessage()), Toast.LENGTH_SHORT)
          .show();
    }
  }

  /**
   * Injects XMLHTTP requests dynamically via evaluation strings to resolve and download blobs out
   * of active environments.
   *
   * @param blobUrl Target browser environment address schema.
   * @param mimeType Expected file content signature representation.
   * @param filename Desired physical filename designation.
   */
  private void triggerBlobDownloadJavaScript(String blobUrl, String mimeType, String filename) {
    String js =
        "javascript:(function() {"
            + "   var xhr = new XMLHttpRequest();"
            + "   xhr.open('GET', '"
            + blobUrl
            + "', true);"
            + "   xhr.responseType = 'blob';"
            + "   xhr.onload = function(e) {"
            + "       if (this.status == 200) {"
            + "           var blob = this.response;"
            + "           var reader = new FileReader();"
            + "           reader.readAsDataURL(blob);"
            + "           reader.onloadend = function() {"
            + "               var base64data = reader.result;"
            + "               BlobHandler.saveBlob(base64data, '"
            + mimeType
            + "', '"
            + filename
            + "');"
            + "           };"
            + "       }"
            + "   };"
            + "   xhr.send();"
            + "})()";

    runOnUiThread(() -> mWebView.loadUrl(js));
  }

  /**
   * Resolves raw base64 arrays onto native PDF layouts inside locally contained resources.
   *
   * @param base64Data Raw representation array.
   * @param mimeType Expected content designation.
   * @param filename Target path name mapping.
   */
  private void handleBlobDownload(String base64Data, String mimeType, String filename) {
    try {
      if (base64Data.contains(",")) {
        base64Data = base64Data.split(",")[1];
      }

      final String cleanBase64 = base64Data;

      runOnUiThread(
          () -> {
            // Load local PDF viewer framework from assets
            mWebView.loadUrl("file:///android_asset/pdf_viewer.html");

            mWebView.setWebViewClient(
                new WebViewClient() {
                  @Override
                  public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    // Inject content data arrays straight into our PDFJS canvas generator
                    mWebView.evaluateJavascript(
                        "renderPdfFromBase64('" + cleanBase64 + "');", null);

                    // Restore default page bindings so operations function smoothly going forward
                    resetWebViewClientSetup();
                  }
                });
          });

    } catch (Exception e) {
      runOnUiThread(
          () ->
              Toast.makeText(
                      this, "Failed to load web PDF layout: " + e.getMessage(), Toast.LENGTH_SHORT)
                  .show());
    }
  }

  /** Restores original navigation hook properties on standard WebView client containers. */
  private void resetWebViewClientSetup() {
    mWebView.setWebViewClient(
        new WebViewClient() {
          @Override
          public void onPageFinished(WebView view, String url) {
            speedDial.setVisibility(View.VISIBLE);
            view.evaluateJavascript(
                "(function(){window.print=function(){AndroidPrint.onPrintRequest();};})();", null);
          }
        });
  }

  // ───────────────────────────────────────────────────────────
  //  Print WebView
  // ───────────────────────────────────────────────────────────

  /** Initiates print job on current web page representation. */
  public void printPage() {
    printWebView(mWebView);
  }

  /**
   * Creates virtual print document adapters and queues layout pages onto system print pools.
   *
   * @param webViewToPrint Source content container view.
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private void printWebView(WebView webViewToPrint) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      Toast.makeText(this, getString(R.string.toast_print_not_supported), Toast.LENGTH_SHORT)
          .show();
      return;
    }

    PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
    String jobName = "Result_" + System.currentTimeMillis();

    PrintDocumentAdapter adapter = webViewToPrint.createPrintDocumentAdapter(jobName);

    PrintAttributes attr =
        new PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build();

    pm.print(jobName, adapter, attr);

    Toast.makeText(this, getString(R.string.toast_select_printer_or_pdf), Toast.LENGTH_LONG).show();
  }

  // ───────────────────────────────────────────────────────────
  //  Desktop Mode
  // ───────────────────────────────────────────────────────────

  /**
   * Aligns user agent formats and viewports to simulate either desktop computer or standard mobile
   * browser structures.
   *
   * @param enabled Set true to target Desktop environments.
   */
  public void setDesktopMode(boolean enabled) {
    WebSettings settings = mWebView.getSettings();

    if (enabled) {
      settings.setUserAgentString(WebSettings.getDefaultUserAgent(mWebView.getContext()));
      settings.setUseWideViewPort(true);
      settings.setLoadWithOverviewMode(true);
      settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
    } else {
      settings.setUserAgentString(WebSettings.getDefaultUserAgent(mWebView.getContext()));
      settings.setUseWideViewPort(false);
      settings.setLoadWithOverviewMode(false);
    }
  }

  // ───────────────────────────────────────────────────────────
  //  Toolbar Menu
  // ───────────────────────────────────────────────────────────

  /**
   * Shows optional system icons alongside text strings during toolbar menu openings.
   *
   * @param featureId ID representing the active feature layer.
   * @param menu Active popup helper menu.
   * @return true representing action has been handled.
   */
  @Override
  public boolean onMenuOpened(int featureId, Menu menu) {
    if (menu instanceof androidx.appcompat.view.menu.MenuBuilder) {
      ((androidx.appcompat.view.menu.MenuBuilder) menu).setOptionalIconsVisible(true);
    }
    return super.onMenuOpened(featureId, menu);
  }

  /**
   * Standard toolbar initialization, loading menu file bindings.
   *
   * @param menu Menu element layout representation.
   * @return true to draw menu container overlays.
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.webview_menu, menu);

    MenuItem desktopItem = menu.findItem(R.id.action_desktop_mode);
    desktopItem.setChecked(isDesktopMode);

    return true;
  }

  /**
   * Custom routing defining actions mapping to back button hooks, manual page refreshes, clipboard
   * copies, share triggers, or desktop simulation switches.
   *
   * @param item The selected MenuItem component.
   * @return true if option click event is consumed cleanly.
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == android.R.id.home) {
      getOnBackPressedDispatcher().onBackPressed();
      return true;
    }

    if (id == R.id.action_refresh) {
      mWebView.reload();
      return true;
    }

    if (id == R.id.action_open_with) {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mWebView.getUrl()));
      startActivity(Intent.createChooser(intent, getString(R.string.open_with_chooser_title)));
      return true;
    }

    if (id == R.id.action_desktop_mode) {
      isDesktopMode = !isDesktopMode;
      setDesktopMode(isDesktopMode);

      getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
          .edit()
          .putBoolean(KEY_DESKTOP_MODE, isDesktopMode)
          .apply();

      item.setChecked(isDesktopMode);
      mWebView.reload();

      Toast.makeText(
              this,
              isDesktopMode
                  ? getString(R.string.toast_desktop_mode_enabled)
                  : getString(R.string.toast_mobile_mode_enabled),
              Toast.LENGTH_SHORT)
          .show();

      return true;
    }

    if (id == R.id.action_share) {
      Intent share = new Intent(Intent.ACTION_SEND);
      share.setType("text/plain");
      share.putExtra(Intent.EXTRA_TEXT, url);
      startActivity(Intent.createChooser(share, getString(R.string.share_chooser_title)));
      return true;
    }

    if (id == R.id.action_print) {
      printPage();
      return true;
    }

    if (id == R.id.action_save_image) {
      WebViewToImageUtil.saveWebViewAsImages(this, mWebView);
      return true;
    }

    if (id == R.id.action_copy_link) {
      ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
      cm.setPrimaryClip(ClipData.newPlainText("URL", mWebView.getUrl()));
      Toast.makeText(this, getString(R.string.toast_link_copied), Toast.LENGTH_SHORT).show();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  // ───────────────────────────────────────────────────────────
  //  Lifecycle (AdvancedWebView Required)
  // ───────────────────────────────────────────────────────────

  /** Resumes WebView actions and layout loops. */
  @Override
  protected void onResume() {
    super.onResume();
    mWebView.onResume();
  }

  /**
   * Pauses rendering cycles, layout evaluation, and script compilation while app transitions to
   * background.
   */
  @Override
  protected void onPause() {
    mWebView.onPause();
    super.onPause();
  }

  /**
   * Releases network listeners, tears down WebViews to free up memory footprint, and restores
   * seasonal effects as appropriate.
   */
  @Override
  protected void onDestroy() {
    mWebView.onDestroy();
    super.onDestroy();

    if (networkCallback != null) {
      ConnectivityManager connectivityManager =
          (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    if (GlobalWinterSystem.isWinterNow()) {
      SeasonalEffects.INSTANCE.setSnowflakeCount(20);
    }
  }

  /**
   * Passes results from child activities back to the AdvancedWebView wrapper container.
   *
   * @param req The integer request code originally supplied.
   * @param res The integer result code returned by the child activity.
   * @param data An Intent, which can return result data.
   */
  @Override
  protected void onActivityResult(int req, int res, @Nullable Intent data) {
    super.onActivityResult(req, res, data);
    mWebView.onActivityResult(req, res, data);
  }

  // AdvancedWebView Listeners

  /**
   * Handles actions triggered when page starts loading inside WebView.
   *
   * @param url Site url target.
   * @param favicon Image icon associated with URL.
   */
  @Override
  public void onPageStarted(String url, Bitmap favicon) {}

  /**
   * Handles actions triggered when page has finished loading.
   *
   * @param url Site url target.
   */
  @Override
  public void onPageFinished(String url) {}

  /**
   * Handles error callbacks during page load transactions.
   *
   * @param code Int error identification.
   * @param desc Error message string.
   * @param failingUrl Destination which failed to load.
   */
  @Override
  public void onPageError(int code, String desc, String failingUrl) {}

  /**
   * Intercepts download requests from active web page targets.
   *
   * @param url File path.
   * @param suggestedFilename Suggested file name.
   * @param mimeType Expected content signature representation.
   * @param contentLength Size of download transaction in bytes.
   * @param contentDisposition Metadata description header.
   * @param userAgent Header field representing requesting agent client version.
   */
  @Override
  public void onDownloadRequested(
      String url,
      String suggestedFilename,
      String mimeType,
      long contentLength,
      String contentDisposition,
      String userAgent) {
    downloadFile(url, userAgent, contentDisposition, mimeType, contentLength);
  }

  /**
   * Callback received when requesting links targeting non-domain addresses.
   *
   * @param url Site url target.
   */
  @Override
  public void onExternalPageRequest(String url) {}
}
