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

import android.util.Log;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility tool checking whether arbitrary textual strings represent html templates or plain text
 * content.
 */
public class HtmlDetector {
  private static final String TAG = "HtmlDetector";
  private static final double HTML_CHAR_RATIO_THRESHOLD =
      0.05; // Lowered threshold for verbose HTML

  /**
   * Detects if the input string contains HTML content.
   *
   * @param text The input string to check.
   * @return True if the string contains HTML, false otherwise.
   */
  public static boolean isHtml(String text) {
    // Input validation
    if (text == null || text.trim().isEmpty()) {
      Log.d(TAG, "Input is null or empty");
      return false;
    }
    text = text.trim();

    // Common HTML patterns
    String[] htmlPatterns = {
      "<\\s*[a-zA-Z][^>]*>", // Opening tags: <div>, <p>
      "</\\s*[a-zA-Z][^>]*>", // Closing tags: </div>, </p>
      "<!DOCTYPE[^>]*>", // DOCTYPE: <!DOCTYPE html>
      "<!--[\\s\\S]*?-->", // Comments: <!-- ... -->
      "<[a-zA-Z][^>]*\\s+[^>]*=[^>]*>", // Tags with attributes: <a href="...">
      "&[a-zA-Z0-9#]+;" // Entities: &amp;, &quot;
    };

    // Check for HTML patterns
    for (String pattern : htmlPatterns) {
      Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(text);
      if (m.find()) {
        Log.d(TAG, "Matched pattern: " + pattern);
        return true;
      }
    }

    // Simple parsing check for tag structure
    if (text.contains("<") && text.contains(">")) {
      try {
        // Count start/end tags to validate structure
        String tagPattern = "<[a-zA-Z][^>]*>|</[a-zA-Z][^>]*>";
        Pattern tagP = Pattern.compile(tagPattern, Pattern.CASE_INSENSITIVE);
        Matcher tagM = tagP.matcher(text);
        int tagCount = 0;
        while (tagM.find()) {
          tagCount++;
        }
        Log.d(TAG, "Tag count: " + tagCount);
        if (tagCount > 0) {
          return true;
        }
      } catch (Exception e) {
        Log.e(TAG, "Parsing error: " + e.getMessage());
      }
    }

    // Heuristic: ratio of HTML-specific characters
    int totalLength = text.length();
    int htmlChars = 0;
    for (char c : text.toCharArray()) {
      if (c == '<' || c == '>') {
        htmlChars++;
      }
    }
    double htmlCharRatio = totalLength > 0 ? (double) htmlChars / totalLength : 0;
    Log.d(
        TAG,
        "HTML chars: "
            + htmlChars
            + ", Total length: "
            + totalLength
            + ", Ratio: "
            + htmlCharRatio);
    if (htmlCharRatio > HTML_CHAR_RATIO_THRESHOLD) {
      return true;
    }

    return false;
  }
}
