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

import android.content.*;
import android.os.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.resultviewbd.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.firebase.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.recycle_view.adapter.*;
import com.app.resultviewbd.recycle_view.model.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;
import org.json.*;

public class BoardListActivity extends BaseActivity {

  private RecyclerView recyclerView;
  private List<ListItem> boardList = new ArrayList<>();
  private MaterialToolbar toolbar;
  private ItemAdapter adapter;
  private RemoteConfigDataManager remoteConfigDataManager;

  /**
   * Initializes the activity, configures the Toolbar, registers the RecyclerView adapter for
   * handling Board result options, and loads board links.
   *
   * @param savedInstanceState Saved instance state bundle.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_server_list);

    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(R.string.board_server_title);
    }

    recyclerView = findViewById(R.id.recyclerServer);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    adapter =
        new ItemAdapter(
            boardList,
            position -> {
              ListItem item = boardList.get(position);
              if (item instanceof ItemModel) {
                ItemModel model = (ItemModel) item;
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(
                    "name", JsonNameTranslator.translateNameFromJson(this, model.getTitle()));
                intent.putExtra("url", model.getUrl());
                startActivity(intent);
              }
            });
    recyclerView.setAdapter(adapter);

    loadBoardsFromJson();
  }

  /** Fetches the dynamic list of Board result servers from the remote config JSON. */
  private void loadBoardsFromJson() {
    remoteConfigDataManager = new RemoteConfigDataManager(this);
    Context context = this;
    remoteConfigDataManager.loadData(
        new RemoteConfigDataManager.OnDataLoadedListener() {
          @Override
          public void onDataLoaded(JSONObject configJson) {
            try {
              List<String> boardNames =
                  remoteConfigDataManager.getNameList(configJson, "ssc_hsc_boards");
              List<String> boardUrls =
                  remoteConfigDataManager.getUrlList(configJson, "ssc_hsc_boards");

              boardList.clear();
              for (int i = 0; i < boardNames.size(); i++) {
                boardList.add(
                    new ItemModel(
                        JsonNameTranslator.translateNameFromJson(context, boardNames.get(i)),
                        boardUrls.get(i)));
              }
              adapter.notifyDataChanged();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onError(String message) {
            showToast(getString(R.string.toast_failed_load_menu, message));
          }
        });
  }

  /** Cleans up observers, dialog overlays, and memory references on activity destruction. */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (remoteConfigDataManager != null) {
      remoteConfigDataManager.cleanup(); // মেমরি লিক প্রিভেন্ট
    }
  }

  /**
   * Handles toolbar back navigation clicking action.
   *
   * @return true to indicate back-navigation was handled.
   */
  @Override
  public boolean onSupportNavigateUp() {
    getOnBackPressedDispatcher().onBackPressed();
    return true;
  }
}
