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
package com.app.resultviewbd.recycle_view.model;

/**
 * Represents a non-clickable category header in the main menu. Used in MainActivity for section
 * titles like "SSC & HSC".
 */
public class CategoryItem implements ListItem {

  private final String categoryName;

  /**
   * Constructs a CategoryItem with the specified name.
   *
   * @param categoryName Textual header name.
   */
  public CategoryItem(String categoryName) {
    this.categoryName = categoryName != null ? categoryName : "Unknown Category";
  }

  /**
   * Retrieves the category name.
   *
   * @return String category name.
   */
  public String getCategoryName() {
    return categoryName;
  }

  /**
   * Retrieves structural category identifier.
   *
   * @return Category list type code.
   */
  @Override
  public int getType() {
    return TYPE_CATEGORY;
  }

  /**
   * Retrieves the name associated with category headers.
   *
   * @return String category name.
   */
  @Override
  public String getName() {
    return categoryName;
  }

  /**
   * Retrieves the category title name.
   *
   * @return String category name.
   */
  @Override
  public String getTitle() {
    return categoryName;
  }

  /**
   * Builds category debug representations.
   *
   * @return String containing mapped fields values.
   */
  @Override
  public String toString() {
    return "CategoryItem{" + "categoryName='" + categoryName + '\'' + '}';
  }
}
