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

import android.app.*;
import android.content.*;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.app.resultviewbd.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.recycle_view.adapter.*;
import com.app.resultviewbd.recycle_view.model.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.loadingindicator.LoadingIndicator;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Activity designed to convert PDF documents into JPEG images using Android's Storage Access
 * Framework (SAF) and PdfRenderer. This is offline, local, and private.
 */
public class JpgSaverActivity extends BaseActivity {

  private static final int REQ_PICK_PDF = 101;
  private MaterialToolbar toolbar;
  private AlertDialog progressDialog;
  private String originalPdfName = "PDF"; // Fallback
  private ActivityResultLauncher<Intent> pickPdfLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == Activity.RESULT_OK
                && result.getData() != null
                && result.getData().getData() != null) {

              Uri pdfUri = result.getData().getData();

              // Extract original PDF filename
              originalPdfName = getFileNameFromUri(pdfUri);
              if (originalPdfName == null || originalPdfName.isEmpty()) {
                originalPdfName = "PDF";
              } else {
                int dotIndex = originalPdfName.lastIndexOf('.');
                if (dotIndex >= 0) {
                  originalPdfName = originalPdfName.substring(0, dotIndex);
                }
              }

              showProcessingDialog();
              convertPdfToJpgUsingSAF(pdfUri);
            }
          });

  /**
   * Initializes the activity, sets up the Toolbar, and configures the root click listener to launch
   * the SAF PDF file picker.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_jpg_saver);

    toolbar = findViewById(R.id.toolbar);

    // Setup MaterialToolbar
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(R.string.toolbar_jpg_saver);
    }

    LinearLayout rootLayout = findViewById(R.id.rootLayout);
    rootLayout.setOnClickListener(v -> pickPdfUsingSAF());
  }

  // ===================== SAF PDF PICK =====================

  /**
   * Triggers the Storage Access Framework (SAF) file picker with an application/pdf filter to
   * select a local PDF file.
   */
  private void pickPdfUsingSAF() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("application/pdf");
    pickPdfLauncher.launch(intent);
  }

  /**
   * Queries the content resolver to resolve the actual visible display filename from a picked file
   * URI.
   *
   * @param uri The picked file content URI.
   * @return The plain display name of the selected document, or null if query fails.
   */
  private String getFileNameFromUri(Uri uri) {
    String result = null;
    try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
        if (columnIndex != -1) {
          result = cursor.getString(columnIndex);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  // ===================== PDF → JPG =====================

  /**
   * Performs asynchronous background conversion of a specified PDF document into multi-page JPEG
   * files. Displays loading indicators and notifies UI thread of successes or failure conditions.
   *
   * @param pdfUri Selected PDF document URI.
   */
  private void convertPdfToJpgUsingSAF(Uri pdfUri) {
    new Thread(
            () -> {
              try {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(pdfUri, "r");
                if (pfd == null) return;

                PdfRenderer renderer = new PdfRenderer(pfd);

                for (int i = 0; i < renderer.getPageCount(); i++) {
                  PdfRenderer.Page page = renderer.openPage(i);

                  int width = page.getWidth() * 2;
                  int height = page.getHeight() * 2;

                  Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                  Canvas canvas = new Canvas(bitmap);
                  canvas.drawColor(Color.WHITE);
                  page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                  saveBitmapAsJpg(bitmap, i + 1);

                  page.close();
                  bitmap.recycle();
                }

                renderer.close();
                pfd.close();

                runOnUiThread(
                    () -> {
                      dismissProcessingDialog();
                      showSuccessDialog();
                    });

              } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(
                    () -> {
                      dismissProcessingDialog();
                      Toast.makeText(this, R.string.jpg_conversion_failed_toast, Toast.LENGTH_SHORT)
                          .show();
                    });
              }
            })
        .start();
  }

  // ===================== SAVE JPG =====================

  /**
   * Compresses the page bitmap to an offline JPEG stream and saves it into the MediaStore Gallery
   * under a custom directory folder.
   *
   * @param bitmap Raw rendered PDF page bitmap.
   * @param pageNo Page index number to append to output filename.
   * @throws IOException If media stream compression fails.
   */
  private void saveBitmapAsJpg(Bitmap bitmap, int pageNo) throws IOException {
    String fileName = originalPdfName + "_page_" + pageNo + ".jpg";

    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    values.put(
        MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PdfImages");

    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    if (uri == null) return;

    try (OutputStream out = getContentResolver().openOutputStream(uri)) {
      if (out != null) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
      }
    }
  }

  // ===================== SUCCESS DIALOG WITH VIEW BUTTON =====================

  /**
   * Displays a MaterialAlertDialog celebrating a successful PDF image extraction run, providing
   * access to the viewing gallery or simple dismissal options.
   */
  private void showSuccessDialog() {
    new MaterialAlertDialogBuilder(this)
        .setTitle(R.string.success_dialog_title)
        .setMessage(getString(R.string.jpg_conversion_success_toast))
        .setCancelable(false)
        .setPositiveButton(
            R.string.success_dialog_positive_btn, (dialog, which) -> dialog.dismiss())
        .setNeutralButton(
            R.string.success_dialog_neutral_btn,
            (dialog, which) -> {
              openSavedImagesFolder();
              dialog.dismiss();
            })
        .show();
  }

  // ===================== OPEN SAVED IMAGES FOLDER =====================

  /**
   * Queries MediaStore for the latest newly converted image in the gallery folder and fires an
   * ACTION_VIEW Intent.
   */
  private void openSavedImagesFolder() {
    // Query for one recent image from your album to get its URI
    String albumRelativePath =
        Environment.DIRECTORY_PICTURES
            + "/PdfImages"; // Match what you used in saveBitmapToMediaStore
    String selection = MediaStore.Images.Media.RELATIVE_PATH + " = ?";
    String[] selectionArgs = {albumRelativePath + "/"}; // Include trailing slash for exact match

    Cursor cursor =
        getContentResolver()
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

      if (intent.resolveActivity(getPackageManager()) != null) {
        startActivity(intent);
      } else {
        Toast.makeText(this, R.string.error_on_image_viewer, Toast.LENGTH_SHORT).show();
      }
    } else {
      if (cursor != null) cursor.close();
      Toast.makeText(this, R.string.error_on_no_image_found, Toast.LENGTH_SHORT).show();
    }
  }

  // ===================== PROCESSING DIALOG =====================

  /**
   * Creates or shows a non-cancelable progress indicator overlay during background rendering
   * operations.
   */
  private void showProcessingDialog() {
    if (progressDialog == null) {
      MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
      builder.setCancelable(false);

      // Context for the LoadingIndicator
      Context themedContext =
          new ContextThemeWrapper(
              this,
              com.google.android.material.R.style.Widget_Material3_LoadingIndicator_Contained);

      // Create the LoadingIndicator
      LoadingIndicator loadingIndicator = new LoadingIndicator(themedContext);
      loadingIndicator.setId(View.generateViewId());
      loadingIndicator.setVisibility(View.VISIBLE);

      // Create the TextView
      TextView textView = new TextView(this);
      textView.setId(View.generateViewId());
      textView.setText(getString(R.string.pdf_to_jpg_processing_toast));
      textView.setTextSize(16);

      // Create the Layout
      ConstraintLayout layout = new ConstraintLayout(this);
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
    }
    if (!progressDialog.isShowing()) {
      progressDialog.show();
    }
  }

  /** Safely dismisses the conversion processing dialog overlay. */
  private void dismissProcessingDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }
}
