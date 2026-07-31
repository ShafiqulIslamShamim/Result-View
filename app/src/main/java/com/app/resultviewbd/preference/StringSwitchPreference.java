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
import androidx.preference.SwitchPreferenceCompat;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;

/**
 * A customized SwitchPreferenceCompat class which persists boolean states as textual strings ("1"
 * or "0") within backing storage frameworks.
 */
public class StringSwitchPreference extends SwitchPreferenceCompat {

  /**
   * Constructs a StringSwitchPreference with specified Context and XML attributes, injecting custom
   * Material Design 3 switch component layout resources.
   *
   * @param context Active context context.
   * @param attrs Attribute sets.
   */
  public StringSwitchPreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Use Material3 switch widget
    setWidgetLayoutResource(R.layout.preference_switch_material3);
  }

  /**
   * Persists boolean switch states mapped as textual representation strings.
   *
   * @param value Boolean switch state.
   * @return true if persistence succeeded.
   */
  @Override
  protected boolean persistBoolean(boolean value) {
    return persistString(value ? "1" : "0");
  }

  /**
   * Reads preference values, resolving mapped textual strings back into boolean states.
   *
   * @param defaultReturnValue Default fallback state representation.
   * @return Active boolean state.
   */
  @Override
  public boolean getPersistedBoolean(boolean defaultReturnValue) {
    String stringValue = getPersistedString(defaultReturnValue ? "1" : "0");
    return "1".equals(stringValue);
  }
}
