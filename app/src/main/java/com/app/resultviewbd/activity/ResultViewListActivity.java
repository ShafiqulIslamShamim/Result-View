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
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.resultviewbd.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.recycle_view.adapter.*;
import com.app.resultviewbd.recycle_view.model.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity presenting the options list for SSC and HSC results inquiry, directing users to either
 * the central server portal, board-wise lookup pages, or the offline SMS gateway compiler.
 */
public class ResultViewListActivity extends BaseActivity {

  RecyclerView recyclerView;
  List<ListItem> serverList = new ArrayList<>();
  private MaterialToolbar toolbar;

  /**
   * Inflates layout, instantiates custom Material toolbars, populates choice models, and registers
   * adapter click callbacks mapping to respective activity transitions.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_server_list);

    toolbar = findViewById(R.id.toolbar);

    // Setup MaterialToolbar
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(R.string.ssc_hsc_result_title);
    }

    recyclerView = findViewById(R.id.recyclerServer);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    serverList.add(
        new ItemModel(
            getString(R.string.main_server_title), getString(R.string.main_server_summary), 0));
    serverList.add(
        new ItemModel(
            getString(R.string.board_server_title), getString(R.string.board_server_summary), 0));
    serverList.add(new ItemModel(getString(R.string.result_by_sms_title)));

    ItemAdapter adapter =
        new ItemAdapter(
            serverList,
            position -> {
              switch (position) {
                case 0:
                  startActivity(new Intent(ResultViewListActivity.this, ServerListActivity.class));
                  break;
                case 1:
                  startActivity(new Intent(ResultViewListActivity.this, BoardListActivity.class));
                  break;
                case 2:
                  startActivity(new Intent(ResultViewListActivity.this, ResultSmsActivity.class));
                  break;
              }
            });

    recyclerView.setAdapter(adapter);
  }

  /**
   * Resolves back click toolbar navigation cleanly using system onBackPressed dispatcher.
   *
   * @return true representing action has been handled.
   */
  @Override
  public boolean onSupportNavigateUp() {
    getOnBackPressedDispatcher().onBackPressed();
    return true;
  }
}
