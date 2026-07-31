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
package com.app.resultviewbd.fragment;

import android.content.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.app.resultviewbd.R;
import com.app.resultviewbd.util.GradeHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

/**
 * Fragment presenting CGPA calculation tools for University students. Supports calculation on
 * either a single subject-wise list or overall year-wise aggregate summaries.
 */
public class UniversityGpaFragment extends Fragment {

  private NestedScrollView nestedScrollView;
  private LinearLayout subjectsContainer, yearContainer;
  private LinearLayout subjectWiseContainer, yearWiseContainer;
  private MaterialButton addSubjectBtn, addYearBtn, calculateBtn;
  private TextView resultCgpa, resultDivision, resultFinalYearCgpa;
  private MaterialCardView resultCard;
  private MaterialButtonToggleGroup universityModeToggleGroup;

  private final ArrayList<View> subjects = new ArrayList<>();
  private final ArrayList<View> yearList = new ArrayList<>();
  private int yearCounter = 1;

  /**
   * Inflates layout, configures UI toggle group states, and instantiates default entry templates.
   *
   * @param inflater LayoutInflater used to inflate the layout files.
   * @param container ViewGroup layout parent.
   * @param savedInstanceState Saved instance state bundle.
   * @return Return inflated view representation.
   */
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_university_gpa, container, false);

    nestedScrollView = root.findViewById(R.id.nestedScrollView);
    subjectsContainer = root.findViewById(R.id.subjectsContainer);
    yearContainer = root.findViewById(R.id.yearContainer);
    addSubjectBtn = root.findViewById(R.id.addSubjectBtn);
    addYearBtn = root.findViewById(R.id.addYearBtn);
    calculateBtn = root.findViewById(R.id.calculateBtn);
    resultCgpa = root.findViewById(R.id.resultCgpa);
    resultDivision = root.findViewById(R.id.resultDivision);
    resultFinalYearCgpa = root.findViewById(R.id.resultFinalYearCgpa);
    resultCard = root.findViewById(R.id.resultCard);
    universityModeToggleGroup = root.findViewById(R.id.universityModeToggleGroup);
    subjectWiseContainer = root.findViewById(R.id.subjectWiseContainer);
    yearWiseContainer = root.findViewById(R.id.yearWiseContainer);

    // Set default state
    universityModeToggleGroup.check(R.id.btn_subject_wise);
    subjectWiseContainer.setVisibility(View.VISIBLE);
    yearWiseContainer.setVisibility(View.GONE);
    showResultForSubjectWise();

    universityModeToggleGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked) return;

          if (checkedId == R.id.btn_subject_wise) {
            subjectWiseContainer.setVisibility(View.VISIBLE);
            yearWiseContainer.setVisibility(View.GONE);
            showResultForSubjectWise();
          } else if (checkedId == R.id.btn_year_wise) {
            subjectWiseContainer.setVisibility(View.GONE);
            yearWiseContainer.setVisibility(View.VISIBLE);
            addDefaultYearsIfNeeded();
            showResultForYearWise();
          }
          clearResults();
        });

    for (int i = 0; i < 6; i++) {
      addSubjectView();
    }

    addSubjectBtn.setOnClickListener(v -> addSubjectView());
    addYearBtn.setOnClickListener(v -> addYearView());
    calculateBtn.setOnClickListener(
        v -> {
          calculateUniversity();
        });

    return root;
  }

  /**
   * Pre-populates the year container layout with 4 defaults representing a typical degree
   * timeframe.
   */
  private void addDefaultYearsIfNeeded() {
    if (yearList.isEmpty()) {
      yearCounter = 1;
      yearContainer.removeAllViews();
      yearList.clear();
      for (int i = 0; i < 4; i++) {
        addYearView();
      }
    }
  }

  /** Formats resulting overlay texts specifically to display subject-wise math values. */
  private void showResultForSubjectWise() {
    resultCgpa.setVisibility(View.VISIBLE);
    resultFinalYearCgpa.setVisibility(View.GONE);
  }

  /** Formats resulting overlay texts specifically to display year-wise math values. */
  private void showResultForYearWise() {
    resultCgpa.setVisibility(View.GONE);
    resultFinalYearCgpa.setVisibility(View.VISIBLE);
  }

  /** Inflates and attaches subject layout card elements dynamically into layout container. */
  private void addSubjectView() {
    View v = getLayoutInflater().inflate(R.layout.item_subject_input, subjectsContainer, false);

    MaterialButton removeBtn = v.findViewById(R.id.removeBtn);
    AutoCompleteTextView spinner = v.findViewById(R.id.gradeSpinner);
    TextInputEditText creditEt = v.findViewById(R.id.creditInput);

    if (TextUtils.isEmpty(creditEt.getText())) {
      creditEt.setText("4");
    }

    v.findViewById(R.id.creditLayout).setVisibility(View.VISIBLE);

    removeBtn.setOnClickListener(
        btn -> {
          subjectsContainer.removeView(v);
          subjects.remove(v);
        });

    String[] grades = getResources().getStringArray(R.array.university_grades);
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(requireContext(), R.layout.m3_spinner_dropdown_item, grades);
    spinner.setAdapter(adapter);

    subjectsContainer.addView(v);
    subjects.add(v);
  }

  /** Inflates and attaches year layout card elements dynamically into layout container. */
  private void addYearView() {
    View v = getLayoutInflater().inflate(R.layout.item_year_input, yearContainer, false);

    TextInputEditText yearNameInput = v.findViewById(R.id.yearNameInput);
    MaterialButton removeBtn = v.findViewById(R.id.removeYearBtn);

    String defaultName = getYearNameWithOrdinal(yearCounter);
    yearNameInput.setText(defaultName);

    yearNameInput.setOnFocusChangeListener(
        (view, hasFocus) -> {
          if (!hasFocus && yearNameInput.getText().toString().trim().isEmpty()) {
            yearNameInput.setText(defaultName);
          }
        });

    removeBtn.setOnClickListener(
        btn -> {
          yearContainer.removeView(v);
          yearList.remove(v);
          updateYearNamesAfterRemoval();
        });

    yearContainer.addView(v);
    yearList.add(v);
    yearCounter++;
  }

  /**
   * Resolves appropriate ordinal string configurations for year descriptors.
   *
   * @param number Year context number.
   * @return Ordinal text name.
   */
  private String getYearNameWithOrdinal(int number) {
    if (number >= 1 && number <= 50) {
      int resId =
          getResources()
              .getIdentifier(
                  "year_ordinal_" + number, "string", requireActivity().getPackageName());
      if (resId != 0) return getString(resId);
    }
    return getString(R.string.year_ordinal_default);
  }

  /** Re-evaluates ordinal values dynamically to fix order sequence names after layout deletions. */
  private void updateYearNamesAfterRemoval() {
    yearCounter = 1;
    for (View view : yearList) {
      TextInputEditText nameInput = view.findViewById(R.id.yearNameInput);
      String current = nameInput.getText().toString().trim();
      String expected = getYearNameWithOrdinal(yearCounter);
      if (current.equals(expected)) {
        nameInput.setText(expected);
      }
      yearCounter++;
    }
  }

  /** Cleans up result text views, hiding the final output card representation. */
  private void clearResults() {
    resultCgpa.setText(R.string.result_cgpa);
    resultDivision.setText(R.string.result_division);
    resultFinalYearCgpa.setText(R.string.result_final_year_cgpa);
    resultCard.setVisibility(View.GONE);
  }

  /**
   * Parses active input list values, calculates averages weighted on credits or simple numeric
   * aggregates, evaluates result classes, and scrolls to output representations.
   */
  private void calculateUniversity() {
    try {
      boolean isSubjectWise =
          universityModeToggleGroup.getCheckedButtonId() == R.id.btn_subject_wise;

      if (isSubjectWise) {
        double totalCredits = 0, weightedSum = 0;

        for (View v : subjects) {
          AutoCompleteTextView gradeSpinner = v.findViewById(R.id.gradeSpinner);
          TextInputEditText creditEt = v.findViewById(R.id.creditInput);
          String grade = gradeSpinner.getText().toString().trim();
          String creditStr = creditEt.getText() != null ? creditEt.getText().toString().trim() : "";

          if (TextUtils.isEmpty(grade) || TextUtils.isEmpty(creditStr))
            throw new IllegalArgumentException(getString(R.string.error_fill_all_fields));

          double credit = Double.parseDouble(creditStr);
          double point = GradeHelper.universityPoint(grade);

          totalCredits += credit;
          weightedSum += point * credit;
        }

        double cgpa = totalCredits > 0 ? weightedSum / totalCredits : 0;
        cgpa = Math.min(cgpa, 4.0);
        resultCgpa.setText(getString(R.string.result_gpa_format, cgpa));

        String division = getDivision(cgpa);
        resultDivision.setText(getString(R.string.result_division) + " " + division);

      } else {
        double sum = 0;
        int count = 0;

        for (View y : yearList) {
          TextInputEditText cgpaEt = y.findViewById(R.id.yearCgpa);
          String val = cgpaEt.getText().toString().trim();

          if (TextUtils.isEmpty(val)) {
            throw new IllegalArgumentException(getString(R.string.error_fill_all_fields));
          }

          if (Double.parseDouble(val) > 4.00) {
            throw new IllegalArgumentException(getString(R.string.err_invalid_cgpa));
          }

          sum += Double.parseDouble(val);
          count++;
        }

        double avgCgpa = sum / count;
        resultFinalYearCgpa.setText(getString(R.string.result_final_year_cgpa_format, avgCgpa));
        resultDivision.setText(getString(R.string.result_division) + " " + getDivision(avgCgpa));
      }

      closeKeyboard();
      resultCard.setVisibility(View.VISIBLE);
      scrollToBottom();

    } catch (Exception e) {
      Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  /** Safely hides input keyboards if currently open on screen. */
  public void closeKeyboard() {
    View view = requireActivity().getCurrentFocus();
    if (view != null) {
      InputMethodManager imm =
          (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  /**
   * Returns standard first, second, or third class labels based on CGPA scoring thresholds.
   *
   * @param cgpa Resulting cumulative grade point average score.
   * @return Division class label string.
   */
  private String getDivision(double cgpa) {
    if (cgpa >= 3.00) return getString(R.string.division_first_class);
    if (cgpa >= 2.25) return getString(R.string.division_second_class);
    if (cgpa >= 2.00) return getString(R.string.division_third_class);
    return getString(R.string.division_fail);
  }

  /** Scrolls nested scroll view down to the bottom of layout contents. */
  private void scrollToBottom() {
    nestedScrollView.post(
        () -> {
          View root = nestedScrollView.getChildAt(0);
          nestedScrollView.smoothScrollTo(0, root.getBottom());
        });
  }
}
