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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.app.resultviewbd.R;
import com.app.resultviewbd.fragment.SscHscGpaFragment;
import com.app.resultviewbd.fragment.UniversityGpaFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * GPA and CGPA Calculator Screen featuring a dual-tab sliding layout. Supports school-level grading
 * schemas (SSC, HSC) and higher education CGPA calculation (University).
 */
public class GpaCalculatorActivity extends BaseActivity {

  private TabLayout modeTabs;
  private ViewPager2 viewPager;

  /**
   * Initializes the activity layout, hooks up the MaterialToolbar with home action button, sets up
   * the ViewPager2 sliding adapter, and binds the Sliding Tab layout mediator.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gpa_calculator);

    // Toolbar
    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(R.string.gpa_calculator_title);
    }

    modeTabs = findViewById(R.id.modeTabs);
    viewPager = findViewById(R.id.viewPager);

    // Set adapter
    viewPager.setAdapter(new GpaPagerAdapter(this));

    // Connect TabLayout with ViewPager2
    new TabLayoutMediator(
            modeTabs,
            viewPager,
            (tab, position) -> {
              tab.setText(position == 0 ? R.string.tab_ssc_hsc : R.string.tab_university);
            })
        .attach();
  }

  /**
   * Performs standard back button dispatch action when home/back navigation in actionbar is
   * selected.
   *
   * @return true to indicate the back action was consumed and handled.
   */
  @Override
  public boolean onSupportNavigateUp() {
    getOnBackPressedDispatcher().onBackPressed();
    return true;
  }

  /** ViewPager2 Fragment State Adapter for managing sliding tab fragments. */
  private static class GpaPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructs the pager adapter.
     *
     * @param activity Parent GpaCalculatorActivity containing this adapter.
     */
    public GpaPagerAdapter(@NonNull GpaCalculatorActivity activity) {
      super(activity);
    }

    /**
     * Creates a new GPA-calculating Fragment based on selected index page.
     *
     * @param position Zero-based tab position.
     * @return Fragment instance (SscHscGpaFragment or UniversityGpaFragment).
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
      return position == 0 ? new SscHscGpaFragment() : new UniversityGpaFragment();
    }

    /**
     * Returns total tab pages managed by this pager layout.
     *
     * @return count representing total tab screens.
     */
    @Override
    public int getItemCount() {
      return 2;
    }
  }
}
