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
package com.pixplicity.sharp;

import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

public class Sharp {
  private final SVG svg;

  private Sharp(SVG svg) {
    this.svg = svg;
  }

  public static Sharp loadString(String svgString) {
    try {
      return new Sharp(SVG.getFromString(svgString));
    } catch (SVGParseException e) {
      throw new RuntimeException("Failed to parse SVG string", e);
    }
  }

  public Drawable getDrawable() {
    Picture picture = svg.renderToPicture();
    return new PictureDrawable(picture);
  }
}
