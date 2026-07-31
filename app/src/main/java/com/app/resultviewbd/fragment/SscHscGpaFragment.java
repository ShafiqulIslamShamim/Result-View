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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;

/**
 * Fragment containing GPA calculation algorithms and dynamic grade input cards matching standard
 * SSC and HSC grade evaluation grids.
 */
public class SscHscGpaFragment extends Fragment {

  private NestedScrollView nestedScrollView;
  private LinearLayout subjectsContainer;
  private MaterialButton addSubjectBtn, calculateBtn;
  private TextView resultGpa, resultGrade;
  private AutoCompleteTextView optionalGradeSpinner;
  private TextInputLayout optionalGradeLayout;
  private MaterialTextView optionalTitle;
  private MaterialCardView resultCard;

  private final ArrayList<View> subjects = new ArrayList<>();

  /**
   * Inflates layout, instantiates custom Material adapters for lists, registers click listener
   * hooks, and pre-populates initial list models.
   *
   * @param inflater LayoutInflater used to inflate the layout files.
   * @param container ViewGroup layout parent.
   * @param savedInstanceState Saved instance state bundle.
   * @return Return inflated view representation.
   */
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_ssc_hsc_gpa, container, false);

    nestedScrollView = root.findViewById(R.id.nestedScrollView);
    subjectsContainer = root.findViewById(R.id.subjectsContainer);
    addSubjectBtn = root.findViewById(R.id.addSubjectBtn);
    calculateBtn = root.findViewById(R.id.calculateBtn);
    resultGpa = root.findViewById(R.id.resultGpa);
    resultGrade = root.findViewById(R.id.resultGrade);
    optionalTitle = root.findViewById(R.id.optionalTitle);
    optionalGradeLayout = root.findViewById(R.id.optionalGradeLayout);
    optionalGradeSpinner = root.findViewById(R.id.optionalGradeSpinner);
    resultCard = root.findViewById(R.id.resultCard);

    String[] grades = getResources().getStringArray(R.array.ssc_hsc_grades);
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(requireContext(), R.layout.m3_spinner_dropdown_item, grades);
    optionalGradeSpinner.setAdapter(adapter);

    // Add default 6 subjects
    for (int i = 0; i < 6; i++) {
      addSubjectView();
    }

    addSubjectBtn.setOnClickListener(v -> addSubjectView());

    calculateBtn.setOnClickListener(
        v -> {
          calculateSscHsc();
        });

    return root;
  }

  /** Inflates and attaches subject layout card elements dynamically into layout container. */
  private void addSubjectView() {
    View v = getLayoutInflater().inflate(R.layout.item_subject_input, subjectsContainer, false);

    MaterialButton removeBtn = v.findViewById(R.id.removeBtn);
    AutoCompleteTextView spinner = v.findViewById(R.id.gradeSpinner);
    v.findViewById(R.id.creditLayout).setVisibility(View.GONE); // Hide credit for SSC/HSC

    removeBtn.setOnClickListener(
        btn -> {
          subjectsContainer.removeView(v);
          subjects.remove(v);
        });

    String[] grades = getResources().getStringArray(R.array.ssc_hsc_grades);
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(requireContext(), R.layout.m3_spinner_dropdown_item, grades);
    spinner.setAdapter(adapter);

    subjectsContainer.addView(v);
    subjects.add(v);
  }

  /**
   * Evaluates input fields, performs math mapping of grades to standard point structures, adds
   * surplus points from optional subjects, and displays the resulting GPA/Grade card.
   */
  private void calculateSscHsc() {
    try {
      double totalCredits = 0, weightedSum = 0;

      for (View v : subjects) {
        AutoCompleteTextView gradeSpinner = v.findViewById(R.id.gradeSpinner);
        String grade = gradeSpinner.getText().toString().trim();
        if (TextUtils.isEmpty(grade)) throw new IllegalArgumentException();

        double point = GradeHelper.sscHscPoint(grade);
        totalCredits += 1;
        weightedSum += point;
      }

      String optGrade = optionalGradeSpinner.getText().toString().trim();
      double surplus = 0;
      if (!TextUtils.isEmpty(optGrade)) {
        double opt = GradeHelper.sscHscPoint(optGrade);
        surplus = Math.max(0, opt - 2.0);
      }

      double gpa = Math.min((weightedSum + surplus) / totalCredits, 5.0);
      resultGpa.setText(getString(R.string.result_gpa_format, gpa));

      String grade = GradeHelper.detectGrade(gpa);
      resultGrade.setText(getString(R.string.result_grade_format, grade));

      resultCard.setVisibility(View.VISIBLE);
      scrollToBottom();

    } catch (Exception e) {
      Toast.makeText(requireContext(), R.string.error_fill_all_fields, Toast.LENGTH_LONG).show();
    }
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
