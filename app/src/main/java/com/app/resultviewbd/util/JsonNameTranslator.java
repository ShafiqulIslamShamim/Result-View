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
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;

/**
 * Translator helper mapping english remote-configuration string tokens into localized Bangla
 * equivalent text strings.
 */
public class JsonNameTranslator {

  /**
   * Convert JSON name string to localized string value.
   *
   * @param context Active context resources.
   * @param str Raw english name key from JSON.
   * @return Localized string translation, or fallback to input.
   */
  public static String translateNameFromJson(Context context, String str) {

    if (str == null || context == null) return "";

    switch (str) {

      /* ========= SSC & HSC Boards ========= */
      case "Dhaka Board":
        return context.getString(R.string.json_dhaka_board);

      case "Rajshahi Board":
        return context.getString(R.string.json_rajshahi_board);

      case "Comilla Board":
        return context.getString(R.string.json_comilla_board);

      case "Jessore Board":
        return context.getString(R.string.json_jessore_board);

      case "Chittagong Board":
        return context.getString(R.string.json_chittagong_board);

      case "Barisal Board":
        return context.getString(R.string.json_barisal_board);

      case "Sylhet Board":
        return context.getString(R.string.json_sylhet_board);

      case "Dinajpur Board":
        return context.getString(R.string.json_dinajpur_board);

      case "Mymensingh Board":
        return context.getString(R.string.json_mymensingh_board);

      case "Madrasah Board":
        return context.getString(R.string.json_madrasah_board);

      case "Technical Board":
        return context.getString(R.string.json_technical_board);

      case "DIBS":
        return context.getString(R.string.json_dibs);

      case "Education board":
        return context.getString(R.string.json_education_board);

      /* ========= Result Servers ========= */
      case "Server 1":
        return context.getString(R.string.json_server_1);

      case "Server 2":
        return context.getString(R.string.json_server_2);

      /* ========= National University Results ========= */
      case "Recently published results":
        return context.getString(R.string.json_nu_recent_results);

      case "Archive results":
        return context.getString(R.string.json_nu_archive_results);

      case "Admission results":
        return context.getString(R.string.json_nu_admission_results);

      /* ========= SSC & HSC Rescrutiny ========= */
      case "Apply for rescrutiny":
        return context.getString(R.string.json_ssc_hsc_rescrutiny_apply);

      case "Help":
        return context.getString(R.string.json_ssc_hsc_rescrutiny_help);

      /* ========= NU Rescrutiny ========= */
      case "Rescrutiny result":
        return context.getString(R.string.json_nu_rescrutiny_result);

      /* ========= Categories ========= */
      case "EDUCATIONAL BOARD":
        return context.getString(R.string.json_cat_educational_board);

      /* ========= National University ========= */
      case "ADMISSION":
        return context.getString(R.string.json_nu_admission);

      case "FORM FILLUP":
        return context.getString(R.string.json_nu_form_fillup);

      case "OFFICIAL WEBSITE":
        return context.getString(R.string.json_nu_official_website);

      /* ========= NU Notices ========= */
      case "Recent Notice":
        return context.getString(R.string.json_nu_recent_notice);

      case "Exam Notice":
        return context.getString(R.string.json_nu_exam_notice);

      case "Admission Notice":
        return context.getString(R.string.json_nu_admission_notice);

      /* ========= Default ========= */
      default:
        return str; // fallback → original JSON value
    }
  }
}
