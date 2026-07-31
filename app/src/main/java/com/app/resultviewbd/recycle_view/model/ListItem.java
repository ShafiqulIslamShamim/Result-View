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

/** Interface for RecyclerView items with multiple view types. */
public interface ListItem {
  int TYPE_CATEGORY = 0;
  int TYPE_ITEM = 1;

  /**
   * Retrieves structural list representation code.
   *
   * @return Return list type code.
   */
  int getType();

  /**
   * Retrieves unique structural item name.
   *
   * @return String name.
   */
  String getName();

  /**
   * Retrieves user-facing display item text.
   *
   * @return String title.
   */
  String getTitle();
}
