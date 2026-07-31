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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import com.app.resultviewbd.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.recycle_view.adapter.*;
import com.app.resultviewbd.recycle_view.model.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for compiling and sending National University result inquiry messages via SMS using
 * preformatted structures sent directly to the official 16222 gateway.
 */
public class NuResultSmsActivity extends BaseActivity {

  private AutoCompleteTextView dropdownExam, dropdownYear;
  private TextInputEditText etRoll;
  private MaterialButton btnSend;
  private MaterialToolbar toolbar;

  private static final String RESULT_NUMBER = "16222"; // NU Result Number

  // NU Exam Codes
  private final Map<String, String> examMap = new HashMap<>();

  /**
   * Initializes the activity views, configures dynamic exam category maps and years dropdowns, sets
   * up standard click handlers and preselects defaults.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_nu_result_sms);

    toolbar = findViewById(R.id.toolbar);

    // Setup Toolbar
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(R.string.result_by_sms_title);
    }

    dropdownExam = findViewById(R.id.dropdownExam);
    etRoll = findViewById(R.id.etRoll);
    dropdownYear = findViewById(R.id.dropdownYear);
    btnSend = findViewById(R.id.btnSend);

    setupExams();
    setupYears();

    // Default selections
    dropdownExam.setText("Degree", false);
    dropdownYear.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)), false);

    btnSend.setOnClickListener(v -> sendSms());
  }

  /**
   * Prepares and loads the array of selectable National University exam levels (Honours, Degree,
   * Masters prelim, etc.) and maps them internally to official SMS inquiry codes.
   */
  private void setupExams() {
    String[] exams = {
      "Admission",
      "Degree",
      "Honours 1st Year",
      "Honours 2nd Year",
      "Honours 3rd Year",
      "Honours 4th Year",
      "Masters Preliminary",
      "Masters Final",
      "Professional"
    };

    examMap.put("Admission", "ATHN");
    examMap.put("Degree", "DEG");
    examMap.put("Honours 1st Year", "H1");
    examMap.put("Honours 2nd Year", "H2");
    examMap.put("Honours 3rd Year", "H3");
    examMap.put("Honours 4th Year", "H4");
    examMap.put("Masters Preliminary", "MP");
    examMap.put("Masters Final", "MF");
    examMap.put("Professional", "PR");

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, exams);
    dropdownExam.setAdapter(adapter);
  }

  /** Populates the exam year selection dropdown adapter list from current year down to 2010. */
  private void setupYears() {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    int startYear = 2010;
    List<String> years = new ArrayList<>();
    for (int year = currentYear; year >= startYear; year--) {
      years.add(String.valueOf(year));
    }
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years);
    dropdownYear.setAdapter(adapter);
  }

  /**
   * Compiles the official National University SMS string based on form fields, launching the system
   * messaging app via action intent (no raw send permission required).
   */
  private void sendSms() {
    String examName = dropdownExam.getText().toString().trim();
    String roll = etRoll.getText().toString().trim();
    String year = dropdownYear.getText().toString().trim();

    if (examName.isEmpty() || roll.isEmpty() || year.isEmpty()) {
      Toast.makeText(this, R.string.error_fill_all_info, Toast.LENGTH_SHORT).show();
      return;
    }

    String examCode = examMap.get(examName);
    if (examCode == null) {
      Toast.makeText(this, R.string.error_on_exam_type, Toast.LENGTH_SHORT).show();
      return;
    }

    // NU SMS Format: NU <ExamCode> <Roll>
    String message = "NU " + examCode + " " + roll;

    // Use Intent — No Permission Needed
    Intent intent = new Intent(Intent.ACTION_SENDTO);
    intent.setData(Uri.parse("smsto:" + RESULT_NUMBER));
    intent.putExtra("sms_body", message);

    try {
      startActivity(intent);
    } catch (Exception e) {
      Toast.makeText(this, R.string.error_on_opening_message_app, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Handles toolbar navigation clicks to perform clean back activity dispatching.
   *
   * @return true to indicate back navigation was handled.
   */
  @Override
  public boolean onSupportNavigateUp() {
    getOnBackPressedDispatcher().onBackPressed();
    return true;
  }
}
