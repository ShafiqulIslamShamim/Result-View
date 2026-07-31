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
package com.app.resultviewbd.recycle_view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.update_checker.*;
import com.app.resultviewbd.util.*;

/** Adapter representing board lists or notice boards options inside standard RecyclerViews. */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

  private final String[] titles;
  private final int[] icons;
  private final OnItemClickListener listener;

  /** Click listener interface callback for rows. */
  public interface OnItemClickListener {
    /**
     * Called when a row element is selected.
     *
     * @param position Row indexing index.
     */
    void onItemClick(int position);
  }

  /**
   * Constructs a NewsAdapter instance.
   *
   * @param titles List of text headings.
   * @param icons Array resource image identifiers.
   * @param listener Target click dispatcher hook.
   */
  public NewsAdapter(String[] titles, int[] icons, OnItemClickListener listener) {
    this.titles = titles;
    this.icons = icons;
    this.listener = listener;
  }

  /**
   * Instantiates the view row holder structures.
   *
   * @param parent Layout container parent.
   * @param viewType Row style type representation.
   * @return Return instantiated ViewHolder row representations.
   */
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_option, parent, false);
    return new ViewHolder(view);
  }

  /**
   * Binds dataset values onto corresponding Row component properties.
   *
   * @param holder View rows containers.
   * @param position Row indexing integer.
   */
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.title.setText(titles[position]);
    holder.icon.setImageResource(icons[position]);

    holder.itemView.setOnClickListener(
        v -> {
          if (listener != null) listener.onItemClick(position);
        });
  }

  /**
   * Evaluates dataset row counts.
   *
   * @return Total elements representation size.
   */
  @Override
  public int getItemCount() {
    return titles.length;
  }

  /** Recycler component ViewHolder definition referencing layout elements. */
  public static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView icon;
    TextView title;

    /**
     * Constructs a row view container mapping layouts onto local parameters.
     *
     * @param itemView Target root layout view container.
     */
    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      icon = itemView.findViewById(R.id.item_icon);
      title = itemView.findViewById(R.id.item_title);
    }
  }
}
