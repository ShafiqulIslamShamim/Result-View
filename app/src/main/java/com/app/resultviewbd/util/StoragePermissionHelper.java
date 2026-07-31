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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Accessor helper facilitating storage runtime permissions validations and scoped folder URI picker
 * dispatches.
 */
public class StoragePermissionHelper {

  public static final String PREF_LOG_FOLDER_URI = "log_folder_uri";

  /**
   * Validates storage permission configurations. Dispatches system folder picker prompts on API 30+
   * if target folder references are missing.
   *
   * @param activity Host activity.
   * @param folderPickerLauncher Intent results dispatcher.
   */
  public static void checkAndRequestStoragePermission(
      final AppCompatActivity activity, ActivityResultLauncher<Intent> folderPickerLauncher) {

    if (Build.VERSION.SDK_INT < 23) return;

    if (Build.VERSION.SDK_INT >= 30) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
      String folderUriStr = prefs.getString(PREF_LOG_FOLDER_URI, null);

      if (folderUriStr == null) {
        showFolderPermissionDialog(activity, folderPickerLauncher);
      }
    } else {
      if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
              != PackageManager.PERMISSION_GRANTED
          || activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
              != PackageManager.PERMISSION_GRANTED) {

        activity.requestPermissions(
            new String[] {
              Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
            },
            1001);
      }
    }
  }

  /**
   * Displays themed Material dialogue detailing why scoped storage folders require configurations.
   *
   * @param activity Host activity.
   * @param launcher Intent results dispatcher.
   */
  private static void showFolderPermissionDialog(
      AppCompatActivity activity, ActivityResultLauncher<Intent> launcher) {

    new MaterialAlertDialogBuilder(activity)
        .setCustomTitle(DialogUtils.createStyledDialogTitle(activity, "Folder Access Needed"))
        .setMessage("This app needs permission to save log files. Please select a folder.")
        .setPositiveButton("Select Folder", (d, w) -> openFolderPicker(launcher))
        .setNegativeButton("Cancel", null)
        .show();
  }

  /**
   * Dispatches ACTION_OPEN_DOCUMENT_TREE intents prompting user directory selections.
   *
   * @param launcher Intent results dispatcher.
   */
  private static void openFolderPicker(ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addFlags(
        Intent.FLAG_GRANT_READ_URI_PERMISSION
            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    launcher.launch(intent);
  }

  /**
   * Takes persistable directory path URI permissions and stores target strings inside default
   * SharedPreferences.
   *
   * @param activity Host activity.
   * @param data Resulting document intent content payload.
   */
  public static void handleFolderPickerResult(Activity activity, Intent data) {
    if (data == null) return;

    Uri treeUri = data.getData();
    if (treeUri == null) return;

    activity
        .getContentResolver()
        .takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    prefs.edit().putString(PREF_LOG_FOLDER_URI, treeUri.toString()).apply();
  }

  /**
   * Check whether target permissions have been accepted.
   *
   * @param activity Host activity.
   * @return true if permission configurations are active.
   */
  public static boolean isPermissionGranted(AppCompatActivity activity) {
    if (Build.VERSION.SDK_INT >= 30) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
      return prefs.getString(PREF_LOG_FOLDER_URI, null) != null;
    } else {
      return activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED
          && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED;
    }
  }
}
