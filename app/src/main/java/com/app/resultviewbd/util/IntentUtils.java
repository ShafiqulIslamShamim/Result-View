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
import android.content.Intent;
import android.net.Uri;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;

/** Accessor helper facilitating intent instantiation. */
public class IntentUtils {

  /**
   * Instantiates an external ACTION_VIEW Intent targeting standard websites.
   *
   * @param context Active reference context.
   * @param url Site destination URL.
   * @return Configured view Intent.
   */
  public static Intent openUrl(Context context, String url) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    return intent;
  }
}
