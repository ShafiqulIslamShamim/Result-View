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

import android.app.Activity;
import android.content.*;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.print.PdfPrint; // Import from the custom class
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.provider.MediaStore;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.loadingindicator.LoadingIndicator;
import java.io.File;
import java.io.OutputStream;

/**
 * Accessor utility facilitating silent conversion of active WebView elements into downloadable
 * high-definition JPEG images. Employs android.print adapters and system PdfRenderer layers to
 * generate lossless multi-page image files.
 */
public class WebViewToImageUtil {

  private static AlertDialog progressDialog;

  // ===================== PUBLIC ENTRY =====================

  /**
   * Orchestrates WebView captures by generating a temporary PDF print, splitting pages using
   * PdfRenderer, and storing pages to MediaStore.
   *
   * @param activity Active host activity context.
   * @param webView Capturable WebView target layout.
   */
  public static void saveWebViewAsImages(Activity activity, WebView webView) {
    showProcessingDialog(activity);

    File pdfFile = new File(activity.getCacheDir(), "webview_temp.pdf");

    String albumName = "PdfImages";
    String fileName = "Result_" + System.currentTimeMillis();

    saveWebViewAsPdf(
        activity,
        webView,
        pdfFile,
        () ->
            new Thread(
                    () -> {
                      convertPdfToImages(activity, pdfFile, fileName, albumName);
                      new Handler(Looper.getMainLooper())
                          .post(
                              () -> {
                                dismissProcessingDialog();
                                showSuccessDialog(activity, albumName);
                              });
                    })
                .start());
  }

  // ===================== WEBVIEW → PDF (USING PDFPRINT HACK FOR SILENT SAVE) =====================

  /**
   * Captures WebView markup rendering silently directly into a temporary output file utilizing
   * default page properties.
   *
   * @param activity Host context.
   * @param webView Active webview target.
   * @param pdfFile Output file reference destination.
   * @param onDone Success completion callback hook.
   */
  private static void saveWebViewAsPdf(
      Activity activity, WebView webView, File pdfFile, Runnable onDone) {
    try {
      // IMPORTANT: Add resolution to prevent layout failure
      PrintAttributes attributes =
          new PrintAttributes.Builder()
              .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
              .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 300, 300)) // 300 DPI
              .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
              .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
              .build();

      PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter("webview");

      PdfPrint pdfPrint = new PdfPrint(attributes);
      pdfPrint.print(adapter, activity.getCacheDir(), "webview_temp.pdf");

      // Wait for the PDF to be written (adjust time if needed)
      new Handler(Looper.getMainLooper())
          .postDelayed(
              () -> {
                if (pdfFile.exists() && pdfFile.length() > 1000) {
                  onDone.run();
                } else {
                  Toast.makeText(activity, R.string.error_on_generating_pdf, Toast.LENGTH_LONG)
                      .show();
                  dismissProcessingDialog();
                }
              },
              3000); // 3 seconds - increase to 5000 if content is large

    } catch (Exception e) {
      e.printStackTrace();
      Toast.makeText(
              activity,
              activity.getString(R.string.error_on_creating_pdf) + e.getMessage(),
              Toast.LENGTH_LONG)
          .show();
      dismissProcessingDialog();
    }
  }

  // ===================== PDF → JPG =====================

  /**
   * Renders saved PDF file pages individually into high-resolution ARGB bitmap representations.
   *
   * @param activity Active context.
   * @param pdfFile Input pdf file path.
   * @param baseName Display name formatting base tag.
   * @param albumName Destination album folder.
   */
  private static void convertPdfToImages(
      Activity activity, File pdfFile, String baseName, String albumName) {
    try {
      ParcelFileDescriptor pfd =
          ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
      PdfRenderer renderer = new PdfRenderer(pfd);

      for (int i = 0; i < renderer.getPageCount(); i++) {
        PdfRenderer.Page page = renderer.openPage(i);

        Bitmap bitmap =
            Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        saveBitmapToMediaStore(activity, bitmap, albumName, baseName + "_page_" + (i + 1) + ".jpg");

        bitmap.recycle();
        page.close();
      }

      renderer.close();
      pfd.close();

      pdfFile.delete(); // Clean up

    } catch (Exception e) {
      e.printStackTrace();
      Toast.makeText(activity, R.string.error_on_conversion_jpg, Toast.LENGTH_LONG).show();
    }
  }

  // ===================== SAVE IMAGE =====================

  /**
   * Inserts rendered Bitmap items directly into the device's default MediaStore pictures directory.
   *
   * @param activity Host context.
   * @param bitmap Rendered screenshot bitmap element.
   * @param album Destination gallery subfolder name.
   * @param fileName Target layout display name.
   */
  private static void saveBitmapToMediaStore(
      Activity activity, Bitmap bitmap, String album, String fileName) {
    try {
      ContentValues values = new ContentValues();
      values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
      values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
      values.put(
          MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + album);

      Uri uri =
          activity
              .getContentResolver()
              .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

      if (uri != null) {
        try (OutputStream out = activity.getContentResolver().openOutputStream(uri)) {
          bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // ===================== PROGRESS DIALOG =====================

  /**
   * Instantiates and displays loading progress dialogues detailing JPEG splitting and capture
   * flows.
   *
   * @param activity Host context activity.
   */
  private static void showProcessingDialog(Activity activity) {
    if (progressDialog == null || !progressDialog.isShowing()) {

      /*
      progressDialog = new MaterialAlertDialogBuilder(activity)
              .setTitle(R.string.process_bar_title)
              .setMessage(R.string.pdf_to_jpg_processing_toast)
              .setCancelable(false)
              .setView(R.layout.dialog_progress_material3)
              .create();

              */

      MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
      builder.setCancelable(false);

      // Context for the LoadingIndicator
      Context themedContext =
          new ContextThemeWrapper(
              activity,
              com.google.android.material.R.style.Widget_Material3_LoadingIndicator_Contained);

      // Create the LoadingIndicator
      LoadingIndicator loadingIndicator = new LoadingIndicator(themedContext);
      loadingIndicator.setId(View.generateViewId());
      loadingIndicator.setVisibility(View.VISIBLE);

      // Create the TextView
      TextView textView = new TextView(activity);
      textView.setId(View.generateViewId());
      textView.setText(activity.getString(R.string.pdf_to_jpg_processing_toast));
      textView.setTextSize(16);

      // Create the Layout
      ConstraintLayout layout = new ConstraintLayout(activity);
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
      progressDialog = builder.create();
      progressDialog.show();
    }
  }

  /** Safely dismisses the currently active processing dialogue. */
  private static void dismissProcessingDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
      progressDialog = null;
    }
  }

  // ===================== SUCCESS DIALOG =====================

  /**
   * Displays themed dialogue advising the capture has been finished, allowing quick visual
   * redirection.
   *
   * @param activity Host context.
   * @param albumName Destination target subfolder.
   */
  private static void showSuccessDialog(Activity activity, String albumName) {
    new MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.success_dialog_title)
        .setMessage(R.string.jpg_conversion_success_toast)
        .setCancelable(false)
        .setPositiveButton(R.string.success_dialog_positive_btn, (d, w) -> d.dismiss())
        .setNeutralButton(
            R.string.success_dialog_neutral_btn,
            (d, w) -> {
              openSavedImages(activity);
              d.dismiss();
            })
        .show();
  }

  // ===================== OPEN SAVED IMAGES =====================

  /**
   * Employs reflective content resolver queries to open the most recently added captured page
   * within an external viewer layout.
   *
   * @param activity Active host activity context.
   */
  private static void openSavedImages(Activity activity) {
    // Query for one recent image from your album to get its URI
    String albumRelativePath =
        Environment.DIRECTORY_PICTURES
            + "/PdfImages"; // Match what you used in saveBitmapToMediaStore
    String selection = MediaStore.Images.Media.RELATIVE_PATH + " = ?";
    String[] selectionArgs = {albumRelativePath + "/"}; // Include trailing slash for exact match

    Cursor cursor =
        activity
            .getContentResolver()
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Images.Media._ID},
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_ADDED + " DESC" // Get the most recent one
                );

    if (cursor != null && cursor.moveToFirst()) {
      long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
      cursor.close();

      Uri imageUri =
          Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(imageUri, "image/jpeg");
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      if (intent.resolveActivity(activity.getPackageManager()) != null) {
        activity.startActivity(intent);
      } else {
        Toast.makeText(activity, R.string.error_on_image_viewer, Toast.LENGTH_SHORT).show();
      }
    } else {
      if (cursor != null) cursor.close();
      Toast.makeText(activity, R.string.error_on_no_image_found, Toast.LENGTH_SHORT).show();
    }
  }

  // ===================== GET FILE NAME FROM URI =====================

  /**
   * Employs content resolvers to extract standard DISPLAY_NAME identifiers for targeted files.
   *
   * @param activity Active activity context.
   * @param uri Target file document URI.
   * @return Display name String, or null if unresolvable.
   */
  public static String getFileNameFromUri(Activity activity, Uri uri) {
    String name = null;
    try (Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        int index = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
        if (index != -1) {
          name = cursor.getString(index);
        }
      }
    }
    return name;
  }
}
