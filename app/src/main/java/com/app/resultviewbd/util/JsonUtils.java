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

import android.content.*;
import android.util.Log;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.recycle_view.adapter.*;
import com.app.resultviewbd.recycle_view.model.*;
import com.app.resultviewbd.update_checker.*;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Accessor helper facilitating extraction of array lists out of remote configuration documents. */
public class JsonUtils {
  private static final String TAG = "JsonUtils";

  /**
   * Reads a given array from raw JSON, translating names to localize representations, and appends
   * into list targets.
   *
   * @param context Host Context.
   * @param json Parsed JSON object.
   * @param key Look-up array key.
   * @param targetList Recycler row target dataset list.
   */
  public static void addArrayItems(
      Context context, JSONObject json, String key, List<ListItem> targetList) {
    if (json.has(key)) {
      try {
        JSONArray array = json.getJSONArray(key);
        for (int i = 0; i < array.length(); i++) {
          JSONObject obj = array.getJSONObject(i);
          String name =
              JsonNameTranslator.translateNameFromJson(context, obj.optString("name", "Unknown"));
          String url = obj.optString("url", "#");
          targetList.add(new ItemModel(name, url));
        }
      } catch (Exception e) {
        Log.e(TAG, "Failed to parse '" + key + "'", e);
      }
    }
  }
}
