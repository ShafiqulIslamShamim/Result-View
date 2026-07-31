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

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.MenuItem;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class MainActivity extends BaseActivity {

  private static final String TAG = "MainActivity";
  public static MainActivity ActivityContext;

  private ActivityResultLauncher<Intent> folderPickerLauncher;

  private RecyclerView recyclerView;
  private List<ListItem> items = new ArrayList<>();
  private ItemAdapter adapter;
  private RemoteConfigDataManager remoteConfigDataManager;

  /**
   * Initializes the activity, sets up the window decor, checks for app updates, configures the
   * RecyclerView, and starts loading config menu items.
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   *     down then this Bundle contains the data it most recently supplied in {@link
   *     #onSaveInstanceState}.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    SharedPreferences prefs = getSharedPreferences("intro_pref", MODE_PRIVATE);
    if (!prefs.getBoolean("intro_shown", false)) {
      startActivity(new Intent(this, IntroActivity.class));
      finish();
      return;
    }

    setContentView(R.layout.activity_main);

    ActivityContext = this;
    OTAUpdateHelper.checkForUpdatesIfDue(this);

    boolean logcat = SharedPrefValues.getValue("enable_logcat", false);

    if (logcat) {

      if (StoragePermissionHelper.isPermissionGranted(this)) {
        LogcatSaver.RunLog(this);
      }

      folderPickerLauncher =
          registerForActivityResult(
              new ActivityResultContracts.StartActivityForResult(),
              result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                  Intent data = result.getData();
                  StoragePermissionHelper.handleFolderPickerResult(this, data);
                  if (StoragePermissionHelper.isPermissionGranted(this)) {
                    LogcatSaver.RunLog(this);
                  }
                }
              });

      StoragePermissionHelper.checkAndRequestStoragePermission(this, folderPickerLauncher);
    }

    MaterialToolbar toolbar = findViewById(R.id.topAppBar);
    setSupportActionBar(toolbar);

    recyclerView = findViewById(R.id.recyclerMain);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    adapter = new ItemAdapter(items, this, this::handleItemClick);
    recyclerView.setAdapter(adapter);

    // Setup round corner search bar live filtering
    EditText searchEditText = findViewById(R.id.searchEditText);
    ImageView searchClearButton = findViewById(R.id.searchClearButton);

    if (searchEditText != null) {
      searchEditText.addTextChangedListener(
          new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
              String query = s.toString();
              if (adapter != null) {
                adapter.setSearchQuery(query);
              }
              if (searchClearButton != null) {
                searchClearButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
              }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
          });
    }

    if (searchClearButton != null && searchEditText != null) {
      searchClearButton.setOnClickListener(v -> searchEditText.setText(""));
    }

    loadMenuItemsFromJson();
  }

  /**
   * Loads menu categories and dynamic items from the remote configuration JSON. Falls back to
   * showing local options or reporting errors on failure.
   */
  private void loadMenuItemsFromJson() {
    remoteConfigDataManager = new RemoteConfigDataManager(this);
    Context context = this;
    remoteConfigDataManager.loadData(
        new RemoteConfigDataManager.OnDataLoadedListener() {
          @Override
          public void onDataLoaded(JSONObject configJson) {
            try {

              items.clear();

              // SSC & HSC
              items.add(new CategoryItem(getString(R.string.category_ssc_hsc)));
              items.add(
                  new ItemModel(
                      getString(R.string.item_results),
                      null,
                      getString(R.string.category_ssc_hsc)));
              items.add(
                  new ItemModel(
                      getString(R.string.item_rescrutiny),
                      null,
                      getString(R.string.category_ssc_hsc)));

              // Add dynamic "Apply for reexamine" from JSON
              remoteConfigDataManager.addArrayItems(configJson, "cat_ssc_hsc", items);

              // NATIONAL UNIVERSITY
              items.add(new CategoryItem(getString(R.string.category_national_university)));
              items.add(
                  new ItemModel(
                      getString(R.string.item_results),
                      null,
                      getString(R.string.category_national_university)));
              items.add(
                  new ItemModel(
                      getString(R.string.item_rescrutiny),
                      null,
                      getString(R.string.category_national_university)));
              items.add(
                  new ItemModel(
                      getString(R.string.item_notice_board),
                      null,
                      getString(R.string.category_national_university)));

              remoteConfigDataManager.addArrayItems(configJson, "cat_national_university", items);

              // GLOBAL FEATURES
              items.add(new CategoryItem(getString(R.string.category_global_features)));
              items.add(new ItemModel(getString(R.string.item_gpa_calculator)));
              items.add(new ItemModel(getString(R.string.toolbar_jpg_saver)));

              adapter.notifyDataChanged();

            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onError(String message) {
            showToast(getString(R.string.toast_failed_load_menu, message));
            //  loadFallbackMenu(); // Show static menu if JSON fails
          }
        });
  }

  /** Cleans up resources, observers, and dialog overlays when the activity is being destroyed. */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (remoteConfigDataManager != null) {
      remoteConfigDataManager.cleanup(); // মেমরি লিক প্রিভেন্ট
    }
  }

  /**
   * Generates a hardcoded fallback set of local menu choices in case the remote database config
   * fails to load.
   */
  private void loadFallbackMenu() {
    items.clear();

    items.add(new CategoryItem(getString(R.string.category_ssc_hsc)));
    items.add(
        new ItemModel(getString(R.string.item_results), getString(R.string.category_ssc_hsc)));
    items.add(
        new ItemModel(getString(R.string.item_rescrutiny), getString(R.string.category_ssc_hsc)));

    items.add(new CategoryItem(getString(R.string.category_national_university)));
    items.add(
        new ItemModel(
            getString(R.string.item_results), getString(R.string.category_national_university)));
    items.add(
        new ItemModel(
            getString(R.string.item_rescrutiny), getString(R.string.category_national_university)));
    items.add(
        new ItemModel(
            getString(R.string.item_notice_board),
            getString(R.string.category_national_university)));

    items.add(new CategoryItem(getString(R.string.category_global_features)));
    items.add(new ItemModel(getString(R.string.item_gpa_calculator)));
    items.add(new ItemModel(getString(R.string.toolbar_jpg_saver)));

    adapter.notifyDataChanged();
  }

  /**
   * Dispatches intents or opens web URLs depending on which menu list item was clicked.
   *
   * @param position The list position of the clicked row element.
   */
  private void handleItemClick(int position) {
    ListItem item = items.get(position);
    String title = item.getName();

    if (title.equals(getString(R.string.item_results))
        && item instanceof ItemModel
        && ((ItemModel) item).getCategory() != null
        && ((ItemModel) item).getCategory().equals(getString(R.string.category_ssc_hsc))) {
      startActivity(new Intent(this, ResultViewListActivity.class));

    } else if (title.equals(getString(R.string.item_results))
        && item instanceof ItemModel
        && ((ItemModel) item).getCategory() != null
        && ((ItemModel) item)
            .getCategory()
            .equals(getString(R.string.category_national_university))) {
      startActivity(new Intent(this, NuServerListActivity.class));

    } else if (title.equals(getString(R.string.item_rescrutiny))
        && item instanceof ItemModel
        && ((ItemModel) item).getCategory() != null
        && ((ItemModel) item).getCategory().equals(getString(R.string.category_ssc_hsc))) {
      startActivity(new Intent(this, RescrutinyServerListActivity.class));

    } else if (title.equals(getString(R.string.item_rescrutiny))
        && item instanceof ItemModel
        && ((ItemModel) item).getCategory() != null
        && ((ItemModel) item)
            .getCategory()
            .equals(getString(R.string.category_national_university))) {
      startActivity(new Intent(this, NuRescrutinyServerListActivity.class));

    } else if (title.equals(getString(R.string.item_notice_board))
        && item instanceof ItemModel
        && ((ItemModel) item).getCategory() != null
        && ((ItemModel) item)
            .getCategory()
            .equals(getString(R.string.category_national_university))) {
      startActivity(new Intent(this, NuNoticeListActivity.class));

    } else if (title.equals(getString(R.string.item_gpa_calculator))) {
      startActivity(new Intent(this, GpaCalculatorActivity.class));

    } else if (title.equals(getString(R.string.toolbar_jpg_saver))) {
      startActivity(new Intent(this, JpgSaverActivity.class));

    } else if (item instanceof ItemModel
        && ((ItemModel) item).getUrl() != null
        && ((ItemModel) item).getUrl().startsWith("http")) {
      WebViewUtil.openLink(
          this, JsonNameTranslator.translateNameFromJson(this, title), ((ItemModel) item).getUrl());
    }
  }

  /**
   * Standard callback to inflate the toolbar menu resource and wire up action item listeners.
   *
   * @param menu The options menu in which you place your items.
   * @return true to display the menu.
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    int menuId = getResources().getIdentifier("main_menu", "menu", getPackageName());
    if (menuId == 0) {
      Log.e(TAG, "Menu resource 'main_menu' not found");
      return false;
    }
    getMenuInflater().inflate(menuId, menu);

    // Reset
    MenuItem resetItem = menu.findItem(R.id.action_reset);
    View resetView = resetItem.getActionView();
    View resetIcon = resetView.findViewById(R.id.icon_image);
    resetIcon.setOnClickListener(
        v -> {
          Intent intent = new Intent(this, MainActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
          finish();
          startActivity(intent);
        });

    // Settings
    MenuItem settingsItem = menu.findItem(R.id.settings);
    View settingsView = settingsItem.getActionView();
    View settingsIcon = settingsView.findViewById(R.id.icon_image);
    settingsIcon.setOnClickListener(
        v -> {
          startActivity(new Intent(this, SettingsActivity.class));
        });

    return true;
  }
}
