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

import android.content.Context;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;

/** Styling helper facilitating creation of custom dialog title text configurations. */
public class DialogUtils {

  /**
   * Instantiates a customized TextView matching Material Design 3 guidelines for dialog headers.
   *
   * @param context Host context.
   * @param titleText Target textual header text.
   * @return Fully configured title TextView wrapper.
   */
  public static TextView createStyledDialogTitle(Context context, CharSequence titleText) {
    TextView customTitle = new TextView(context);
    customTitle.setTextAppearance(
        com.google.android.material.R.style.TextAppearance_Material3_TitleLarge);
    customTitle.setText(titleText);

    // Full width for alignment with dialog content
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    customTitle.setLayoutParams(params);

    // Balanced padding (top > bottom)
    int padding =
        (int)
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, context.getResources().getDisplayMetrics());
    customTitle.setPadding(padding, padding, padding, padding / 3);

    customTitle.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);
    customTitle.setIncludeFontPadding(false); // important
    customTitle.setLineSpacing(0, 1f); // optional

    // Apply colorPrimary
    TypedValue typedValue = new TypedValue();
    if (context
        .getTheme()
        .resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)) {
      customTitle.setTextColor(typedValue.data);
    }

    return customTitle;
  }
}
