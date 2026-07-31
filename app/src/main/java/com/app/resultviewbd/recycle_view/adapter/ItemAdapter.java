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
package com.app.resultviewbd.recycle_view.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.recycle_view.model.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An adapter for the main RecyclerView that displays collapsible categories and expressive items
 * representing boards, result servers, rescrutiny links, and utility calculators. Supports buttery
 * smooth animations, filtering, and search functionality.
 */
public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  /** Interface definition for a callback to be invoked when an item in this adapter is clicked. */
  public interface OnItemClickListener {
    /**
     * Called when an item has been clicked.
     *
     * @param position The position of the item in the master list.
     */
    void onItemClick(int position);
  }

  private List<ListItem> masterItems = new ArrayList<>();
  private final List<ListItem> visibleItems = new ArrayList<>();
  private final Set<String> collapsedCategories = new HashSet<>();
  private String searchQuery = "";
  private final OnItemClickListener listener;
  private Context context;

  private static final int TYPE_CATEGORY = ListItem.TYPE_CATEGORY;
  private static final int TYPE_ITEM = ListItem.TYPE_ITEM;

  /**
   * Constructs an ItemAdapter with a list of items and a click listener.
   *
   * @param items The master list of items to populate the adapter with.
   * @param listener The callback to run when an item is clicked.
   */
  public ItemAdapter(List<ListItem> items, OnItemClickListener listener) {
    this(items, null, listener);
  }

  /**
   * Constructs an ItemAdapter with a list of items, an Android context, and a click listener.
   *
   * @param items The master list of items to populate the adapter with.
   * @param context The Context used to load preferences for expansion/collapse states.
   * @param listener The callback to run when an item is clicked.
   */
  public ItemAdapter(List<ListItem> items, Context context, OnItemClickListener listener) {
    this.context = context;
    this.masterItems = items != null ? items : new ArrayList<>();
    this.listener = listener;
    initializeDefaultCollapsedStates();
    updateVisibleItems();
  }

  /**
   * Sets the search query to filter items. The query is case-insensitive.
   *
   * @param query The search query string.
   */
  public void setSearchQuery(String query) {
    this.searchQuery = query != null ? query.toLowerCase().trim() : "";
    updateVisibleItemsWithDiff();
  }

  /**
   * Toggles the expansion or collapse state of a specific category. Persists the collapsed state in
   * SharedPreferences.
   *
   * @param categoryName The name of the category to toggle.
   */
  public void toggleCategory(String categoryName) {
    boolean isCollapsedNow;
    if (collapsedCategories.contains(categoryName)) {
      collapsedCategories.remove(categoryName);
      isCollapsedNow = false;
    } else {
      collapsedCategories.add(categoryName);
      isCollapsedNow = true;
    }
    if (context != null) {
      SharedPreferences prefs =
          context.getSharedPreferences("category_expansion_prefs", Context.MODE_PRIVATE);
      prefs.edit().putBoolean("category_collapsed_" + categoryName, isCollapsedNow).apply();
    }
    updateVisibleItemsWithDiff();
  }

  /**
   * Recalculates the items that should be visible on screen based on collapsed states and any
   * current search queries.
   */
  private void updateVisibleItems() {
    visibleItems.clear();
    String currentCategory = "";
    CategoryItem lastCategoryAdded = null;
    int lastCategoryIndex = -1;

    for (ListItem item : masterItems) {
      if (item.getType() == TYPE_CATEGORY) {
        currentCategory = ((CategoryItem) item).getCategoryName();
        if (searchQuery.isEmpty()) {
          visibleItems.add(item);
        } else {
          lastCategoryAdded = (CategoryItem) item;
          lastCategoryIndex = visibleItems.size();
        }
      } else {
        ItemModel itemModel = (ItemModel) item;
        String category = itemModel.getCategory();
        if (category == null || category.trim().isEmpty()) {
          category = currentCategory;
        }

        // Check if item matches search query
        boolean matchesSearch =
            searchQuery.isEmpty()
                || itemModel.getTitle().toLowerCase().contains(searchQuery)
                || (itemModel.hasSummary()
                    && itemModel.getSummary().toLowerCase().contains(searchQuery));

        if (matchesSearch) {
          if (!searchQuery.isEmpty() && lastCategoryAdded != null) {
            visibleItems.add(lastCategoryIndex, lastCategoryAdded);
            lastCategoryAdded = null; // Prevent adding the resource multiple times
          }

          if (searchQuery.isEmpty()) {
            if (!collapsedCategories.contains(category)) {
              visibleItems.add(itemModel);
            }
          } else {
            visibleItems.add(itemModel);
          }
        }
      }
    }
  }

  /**
   * Recalculates the visible items list and updates RecyclerView using DiffUtil for beautiful,
   * buttery smooth animations of expanding, collapsing, and list filtering.
   */
  private void updateVisibleItemsWithDiff() {
    final List<ListItem> oldList = new ArrayList<>(visibleItems);
    updateVisibleItems();

    DiffUtil.DiffResult result =
        DiffUtil.calculateDiff(
            new DiffUtil.Callback() {
              @Override
              public int getOldListSize() {
                return oldList.size();
              }

              @Override
              public int getNewListSize() {
                return visibleItems.size();
              }

              @Override
              public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                ListItem oldItem = oldList.get(oldItemPosition);
                ListItem newItem = visibleItems.get(newItemPosition);
                if (oldItem.getType() != newItem.getType()) return false;
                return oldItem.getName().equals(newItem.getName());
              }

              @Override
              public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                ListItem oldItem = oldList.get(oldItemPosition);
                ListItem newItem = visibleItems.get(newItemPosition);
                return oldItem.getTitle().equals(newItem.getTitle());
              }
            });
    result.dispatchUpdatesTo(this);
  }

  /** Restores the saved collapse states of each category from SharedPreferences. */
  private void initializeDefaultCollapsedStates() {
    collapsedCategories.clear();
    if (context != null) {
      SharedPreferences prefs =
          context.getSharedPreferences("category_expansion_prefs", Context.MODE_PRIVATE);
      for (ListItem item : masterItems) {
        if (item.getType() == TYPE_CATEGORY) {
          String catName = ((CategoryItem) item).getCategoryName();
          boolean isCollapsed = prefs.getBoolean("category_collapsed_" + catName, false);
          if (isCollapsed) {
            collapsedCategories.add(catName);
          }
        }
      }
    }
  }

  /**
   * Formats the item title by capitalizing the first character and converting the rest to
   * lowercase.
   *
   * @param title The raw title string to format.
   * @return The formatted title string.
   */
  private String formatItemTitle(String title) {
    if (title == null) return "";
    title = title.trim();
    if (title.isEmpty()) return "";
    String firstChar = title.substring(0, 1).toUpperCase();
    if (title.length() > 1) {
      return firstChar + title.substring(1).toLowerCase();
    } else {
      return firstChar;
    }
  }

  /**
   * Refreshes the adapter by re-initializing states and updating the visible items. Triggers a full
   * dataset notification.
   */
  public void notifyDataChanged() {
    initializeDefaultCollapsedStates();
    updateVisibleItems();
    super.notifyDataSetChanged();
  }

  /** ViewHolder for collapsible category headers. */
  public class CategoryViewHolder extends RecyclerView.ViewHolder {
    TextView textCategory;
    ImageView categoryIcon;
    ImageView imgChevron;

    /**
     * Constructs a CategoryViewHolder with the root view.
     *
     * @param itemView The category view root.
     */
    public CategoryViewHolder(@NonNull View itemView) {
      super(itemView);
      textCategory = itemView.findViewById(R.id.textCategory);
      categoryIcon = itemView.findViewById(R.id.categoryIcon);
      imgChevron = itemView.findViewById(R.id.imgChevron);
    }
  }

  /** ViewHolder for individual item cards. */
  public class ItemViewHolder extends RecyclerView.ViewHolder {
    TextView textTitle;
    TextView textSummary;
    ImageView itemIcon;

    /**
     * Constructs an ItemViewHolder with the root view and registers click listeners.
     *
     * @param itemView The item view root.
     * @param listener The click callback listener.
     */
    public ItemViewHolder(@NonNull View itemView, OnItemClickListener listener) {
      super(itemView);
      textTitle = itemView.findViewById(R.id.textTitle);
      textSummary = itemView.findViewById(R.id.textSummary);
      itemIcon = itemView.findViewById(R.id.itemIcon);

      itemView.setOnClickListener(
          v -> {
            int pos = getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
              ListItem clickedItem = visibleItems.get(pos);
              int masterPos = masterItems.indexOf(clickedItem);
              if (masterPos != -1) {
                listener.onItemClick(masterPos);
              }
            }
          });
    }
  }

  /**
   * Returns the view type integer of the item at the specified position.
   *
   * @param position Position to query.
   * @return integer representing either category header or row item.
   */
  @Override
  public int getItemViewType(int position) {
    return visibleItems.get(position).getType();
  }

  /**
   * Creates a new ViewHolder based on the specified view type.
   *
   * @param parent The ViewGroup into which the new View will be added.
   * @param viewType The view type of the new View.
   * @return A new ViewHolder holding the inflated view.
   */
  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());

    if (viewType == TYPE_CATEGORY) {
      View view = inflater.inflate(R.layout.item_category_header, parent, false);
      return new CategoryViewHolder(view);
    } else {
      View view = inflater.inflate(R.layout.item_row, parent, false);
      return new ItemViewHolder(view, listener);
    }
  }

  /**
   * Binds the data of the item at the specified position to its ViewHolder. Sets texts, leading
   * icons, click expand/collapse behavior on headers, and details on child items.
   *
   * @param holder The ViewHolder to update.
   * @param position The position of the item in the list.
   */
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (getItemViewType(position) == TYPE_CATEGORY) {
      CategoryItem category = (CategoryItem) visibleItems.get(position);
      CategoryViewHolder cvh = (CategoryViewHolder) holder;

      cvh.textCategory.setText(category.getCategoryName().toUpperCase());

      // Set beautiful category leading icons
      String name = category.getCategoryName().toUpperCase();
      int resId = R.drawable.ic_school; // Default (SSC & HSC educational cap)
      if (name.contains("NATIONAL") || name.contains("UNIVERSITY") || name.contains("জাতীয়")) {
        resId = R.drawable.ic_domain;
      } else if (name.contains("GLOBAL")
          || name.contains("FEATURES")
          || name.contains("অন্যান্য")) {
        resId = R.drawable.ic_public_globe;
      }
      cvh.categoryIcon.setImageResource(resId);

      // Trailing Chevron rotation: 0 degrees if collapsed (pointing right), 90 degrees if expanded
      // (pointing downward)
      boolean isCollapsed = collapsedCategories.contains(category.getCategoryName());
      cvh.imgChevron.setRotation(isCollapsed ? 0f : 90f);

      cvh.itemView.setOnClickListener(
          v -> {
            toggleCategory(category.getCategoryName());
            boolean isCollapsedNow = collapsedCategories.contains(category.getCategoryName());
            cvh.imgChevron.animate().rotation(isCollapsedNow ? 0f : 90f).setDuration(150).start();
          });

    } else {
      ItemModel item = (ItemModel) visibleItems.get(position);
      ItemViewHolder vh = (ItemViewHolder) holder;

      vh.textTitle.setText(item.getTitle());

      // Assign expressive, beautiful leading material icons
      String title = item.getTitle().toUpperCase();
      int resId = R.drawable.ic_assignment; // Default assignment document icon
      if (title.contains("RESULT") || title.contains("ফল")) {
        resId = R.drawable.ic_assignment;
      } else if (title.contains("SCRUTINY")
          || title.contains("পুনঃনিরীক্ষণ")
          || title.contains("REEXAMINE")) {
        resId = R.drawable.ic_search;
      } else if (title.contains("NOTICE") || title.contains("নোটিশ")) {
        resId = R.drawable.newspaper_24px;
      } else if (title.contains("CALCULATOR")
          || title.contains("ক্যালকুলেটর")
          || title.contains("GPA")) {
        resId = R.drawable.ic_calculator;
      } else if (title.contains("JPG")
          || title.contains("IMAGE")
          || title.contains("জেপিজি")
          || title.contains("পিডিএফ")
          || title.contains("SAVER")) {
        resId = R.drawable.ic_save_alt;
      }
      vh.itemIcon.setImageResource(resId);

      // Show summary only when exists
      if (item.hasSummary()) {
        vh.textSummary.setText(item.getSummary());
        vh.textSummary.setVisibility(View.VISIBLE);
      } else {
        vh.textSummary.setVisibility(View.GONE);
      }
    }
  }

  /**
   * Returns the total count of items currently visible.
   *
   * @return Count of visible items.
   */
  @Override
  public int getItemCount() {
    return visibleItems.size();
  }
}
