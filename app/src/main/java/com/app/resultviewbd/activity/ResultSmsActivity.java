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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for compiling and dispatching general education board results (SSC, HSC, Dakhil, Alim)
 * via formatted SMS targeting the official Teletalk 16222 gateway.
 */
public class ResultSmsActivity extends BaseActivity {

  private AutoCompleteTextView dropdownExam, dropdownBoard, dropdownYear;
  private TextInputEditText etRoll;
  private MaterialButton btnSend;
  private MaterialToolbar toolbar;

  private static final String RESULT_NUMBER = "16222";

  private final Map<String, String> boardMap = new HashMap<>();

  /**
   * Initializes views, builds board mappings and adapters, presets current year & defaults, and
   * implements an auto-matching engine for Madrasah/Technical boards on exam click.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_result_sms);

    toolbar = findViewById(R.id.toolbar);

    // Setup MaterialToolbar
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(R.string.result_by_sms_title);
    }

    dropdownExam = findViewById(R.id.dropdownExam);
    dropdownBoard = findViewById(R.id.dropdownBoard);
    etRoll = findViewById(R.id.etRoll);
    dropdownYear = findViewById(R.id.dropdownYear);
    btnSend = findViewById(R.id.btnSend);

    setupBoards();
    setupExams();
    setupYears();

    // Default selections
    dropdownExam.setText("SSC", false);
    dropdownBoard.setText("Dhaka Board", false);
    dropdownYear.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)), false);

    // Auto-select board according to exam
    dropdownExam.setOnItemClickListener(
        (parent, view, position, id) -> {
          String exam = (String) parent.getItemAtPosition(position);
          if (exam.equals("Dakhil") || exam.equals("Alim")) {
            dropdownBoard.setText("Madrasah Board", false);
            dropdownBoard.setEnabled(false);
          } else if (exam.contains("(Vocational)")) {
            dropdownBoard.setText("Technical Board", false);
            dropdownBoard.setEnabled(false);
          } else {
            dropdownBoard.setEnabled(true);
          }
        });

    btnSend.setOnClickListener(v -> sendSms());
  }

  /** Prepares and sets the list of general education exam types (SSC, HSC, Dakhil, Alim, etc.). */
  private void setupExams() {
    String[] exams = {"SSC", "HSC", "Dakhil", "Alim", "SSC (Vocational)", "HSC (Vocational)"};

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, exams);

    dropdownExam.setAdapter(adapter);
  }

  /**
   * Populates the list of regional Bangladesh education boards and maps them internally to SMS
   * short-codes (e.g. Dhaka -> DHA).
   */
  private void setupBoards() {
    String[] boardNames = {
      "Barisal Board", "Chattogram Board", "Comilla Board", "Dhaka Board",
      "Dinajpur Board", "Jessore Board", "Mymensingh Board", "Rajshahi Board",
      "Sylhet Board", "Madrasah Board", "Technical Board"
    };

    boardMap.put("Barisal Board", "BAR");
    boardMap.put("Chattogram Board", "CHA");
    boardMap.put("Comilla Board", "COM");
    boardMap.put("Dhaka Board", "DHA");
    boardMap.put("Dinajpur Board", "DIN");
    boardMap.put("Jessore Board", "JES");
    boardMap.put("Mymensingh Board", "MYM");
    boardMap.put("Rajshahi Board", "RAJ");
    boardMap.put("Sylhet Board", "SYL");
    boardMap.put("Madrasah Board", "MAD");
    boardMap.put("Technical Board", "TEC");

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, boardNames);

    dropdownBoard.setAdapter(adapter);
  }

  /** Generates dynamic year options ranging from current calendar year back down to 2013. */
  private void setupYears() {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    int startYear = 2013;
    java.util.List<String> years = new java.util.ArrayList<>();

    for (int year = currentYear; year >= startYear; year--) {
      years.add(String.valueOf(year));
    }

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years);

    dropdownYear.setAdapter(adapter);
  }

  /**
   * Validates entry fields, compiles correct standard Teletalk SMS format block, and opens system
   * message app interface safely without needing runtime permission.
   */
  private void sendSms() {
    String exam = dropdownExam.getText().toString().trim();
    String boardName = dropdownBoard.getText().toString().trim();
    String roll = etRoll.getText().toString().trim();
    String year = dropdownYear.getText().toString().trim();

    if (exam.isEmpty() || boardName.isEmpty() || roll.isEmpty() || year.isEmpty()) {
      Toast.makeText(this, R.string.error_fill_all_info, Toast.LENGTH_SHORT).show();
      return;
    }

    // Vocational exams → take only SSC/HSC
    String examCode = exam.contains("(Vocational)") ? exam.split(" ")[0] : exam;

    String boardCode = boardMap.get(boardName);
    if (boardCode == null) {
      Toast.makeText(this, R.string.error_on_board_selections, Toast.LENGTH_SHORT).show();
      return;
    }

    // Correct SMS format
    String message = examCode + " " + boardCode + " " + roll + " " + year;

    // Use Intent (NO PERMISSION NEEDED!)
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
   * Dispatches clean onBackPressed call when toolbar home navigation is clicked.
   *
   * @return true indicating navigation was handled.
   */
  @Override
  public boolean onSupportNavigateUp() {
    getOnBackPressedDispatcher().onBackPressed();
    return true;
  }
}
