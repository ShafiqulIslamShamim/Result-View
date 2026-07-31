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
package com.app.resultviewbd.preference;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * A customized ListPreference implementation styled using Material Design 3 guidelines. Replaces
 * standard preference dialogs with styled single-choice alert builders.
 */
public class MaterialListPreference extends ListPreference {
  private int mClickedDialogEntryIndex;

  /**
   * Constructs a MaterialListPreference with fully specified styling context.
   *
   * @param context Active context resources.
   * @param attrs Attribute set.
   * @param defStyleAttr Default style attribute key.
   * @param defStyleRes Default style resource key.
   */
  public MaterialListPreference(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  /**
   * Constructs a MaterialListPreference with an attribute set.
   *
   * @param context Active context resources.
   * @param attrs Attribute set.
   */
  public MaterialListPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Constructs a MaterialListPreference with only a Context.
   *
   * @param context Active context resources.
   */
  public MaterialListPreference(@NonNull Context context) {
    super(context);
  }

  /**
   * Overrides click behaviors to intercept preference clicks, and builds/displays a styled choice
   * dialog.
   */
  @Override
  protected void onClick() {
    // If no entries or not enabled/persisted, don't show dialog
    if (getEntries() == null || getEntryValues() == null || !isEnabled() || !isPersistent()) {
      return;
    }

    // Find the index of current value
    mClickedDialogEntryIndex = findIndexOfValue(getValue());

    // Create Material dialog
    MaterialAlertDialogBuilder builder =
        new MaterialAlertDialogBuilder(getContext())

            // .setTitle(getDialogTitle())
            .setCustomTitle(DialogUtils.createStyledDialogTitle(getContext(), getDialogTitle()))
            .setSingleChoiceItems(
                getEntries(),
                mClickedDialogEntryIndex,
                (dialog, which) -> {
                  mClickedDialogEntryIndex = which;
                  // Update value when item is clicked
                  if (callChangeListener(getEntryValues()[which].toString())) {
                    setValueIndex(which);
                  }
                  dialog.dismiss();
                })
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());

    // Optional: Add positive button if needed
    // builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
    //     if (mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
    //         String value = getEntryValues()[mClickedDialogEntryIndex].toString();
    //         if (callChangeListener(value)) {
    //             setValue(value);
    //         }
    //     }
    // });

    builder.show();
  }
}
