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
 * Represents a clickable item in RecyclerView. Supports optional summary without breaking existing
 * constructors.
 */
public class ItemModel implements ListItem {

  private final String title;
  private final String url;
  private final String category;
  private final String summary; // optional

  /* =========================
  Constructors
  ========================= */

  /**
   * Constructs an ItemModel with only a title.
   *
   * @param title Row title text.
   */
  public ItemModel(String title) {
    this(title, null, null, null);
  }

  /**
   * Constructs an ItemModel with title and url components.
   *
   * @param title Row title text.
   * @param url Site URL target.
   */
  public ItemModel(String title, String url) {
    this(title, url, null, null);
  }

  /**
   * Constructs an ItemModel with title and summary components.
   *
   * @param title Row title text.
   * @param summary Brief description summary string.
   * @param i Dummy type indexing discriminator.
   */
  public ItemModel(String title, String summary, int i) {
    this(title, null, null, summary);
  }

  /**
   * Constructs an ItemModel with title, url, and category components.
   *
   * @param title Row title text.
   * @param url Site URL target.
   * @param category Parent category name.
   */
  public ItemModel(String title, String url, String category) {
    this(title, url, category, null);
  }

  /**
   * Constructs a fully specified ItemModel with title, url, category, and summary.
   *
   * @param title Row title text.
   * @param url Site URL target.
   * @param category Parent category name.
   * @param summary Brief description summary string.
   */
  public ItemModel(String title, String url, String category, String summary) {
    this.title = title;
    this.url = url != null ? url : "";
    this.category = category != null ? category : "";
    this.summary = summary;
  }

  /* =========================
  Interface implementation
  ========================= */

  /**
   * Returns list item type code.
   *
   * @return Item list type identifier.
   */
  @Override
  public int getType() {
    return TYPE_ITEM;
  }

  /**
   * Returns row title name.
   *
   * @return Title string.
   */
  @Override
  public String getName() {
    return title;
  }

  /**
   * Returns row title name.
   *
   * @return Title string.
   */
  @Override
  public String getTitle() {
    return title;
  }

  /* =========================
  Getters
  ========================= */

  /**
   * Returns site web URL.
   *
   * @return URL destination address.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Returns parent category group name.
   *
   * @return Category string.
   */
  public String getCategory() {
    return category;
  }

  /**
   * Returns brief description summary string.
   *
   * @return Summary string.
   */
  public String getSummary() {
    return summary;
  }

  /**
   * Checks if summary contents are active.
   *
   * @return true if summary is specified.
   */
  public boolean hasSummary() {
    return summary != null && !summary.trim().isEmpty();
  }

  /**
   * Builds debugging representation of row item configuration.
   *
   * @return Configuration debugging representation text.
   */
  @Override
  public String toString() {
    return "ItemModel{"
        + "title='"
        + title
        + '\''
        + ", url='"
        + url
        + '\''
        + ", category='"
        + category
        + '\''
        + ", summary='"
        + summary
        + '\''
        + '}';
  }
}
