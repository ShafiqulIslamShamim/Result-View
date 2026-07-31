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
package com.app.resultviewbd.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;

/**
 * Accessor wrapper enabling safe reflective lookup of system and layout resources using string
 * identifier tags.
 */
public class ResourceUtils {

  /** Supported Android resource identifier directory groups. */
  public enum ResourceType {
    DRAWABLE("drawable"),
    STRING("string"),
    LAYOUT("layout"),
    ID("id"),
    COLOR("color"),
    DIMEN("dimen"),
    STYLE("style"),
    STYLEABLE("styleable"),
    ARRAY("array"),
    RAW("raw"),
    MIPMAP("mipmap"),
    ANIM("anim"),
    MENU("menu"),
    XML("xml"),
    ATTR("attr"), // Added attribute type
    BOOL("bool"), // Added boolean type
    INTEGER("integer"), // Added integer type
    FONT("font"), // Added font type
    NAVIGATION("navigation"); // Added navigation type

    private final String typeName;

    /**
     * Constructs enum map values.
     *
     * @param typeName Android system lookup tag prefix.
     */
    ResourceType(String typeName) {
      this.typeName = typeName;
    }

    /**
     * Retrieves prefix lookup folder tags.
     *
     * @return Lookup text name.
     */
    public String getTypeName() {
      return typeName;
    }
  }

  /**
   * Get resource ID with enum type.
   *
   * @param context Host context.
   * @param name Target resource literal name.
   * @param type Target resource type category.
   * @return Mapped integer resource ID, 0 if not found.
   */
  public static int getResourceId(Context context, String name, ResourceType type) {
    return getResourceId(context, name, type.getTypeName());
  }

  /**
   * Get resource ID with string type.
   *
   * @param context Host context.
   * @param name Target resource literal name.
   * @param type Target folder string identifier.
   * @return Mapped integer resource ID, 0 if not found.
   */
  public static int getResourceId(Context context, String name, String type) {
    if (context == null || name == null || type == null) {
      return 0;
    }

    Resources resources = context.getResources();
    String packageName = context.getPackageName();

    int resId = resources.getIdentifier(name, type, packageName);

    if (resId == 0) {
      // Try alternative: remove underscores, try lowercase, etc.
      String normalized = name.toLowerCase().replace("_", "");
      resId = resources.getIdentifier(normalized, type, packageName);
    }

    return resId;
  }

  /**
   * Find attribute resource ID.
   *
   * @param context Host context.
   * @param attrName Attribute lookup name.
   * @return Integer attribute ID, 0 if missing.
   */
  public static int getAttrId(Context context, String attrName) {
    return getResourceId(context, attrName, ResourceType.ATTR);
  }

  /**
   * Find styleable resource ID (int array).
   *
   * @param context Host context.
   * @param styleableName Styleable lookup name.
   * @return Integer styleable ID, 0 if missing.
   */
  public static int getStyleableId(Context context, String styleableName) {
    return getResourceId(context, styleableName, ResourceType.STYLEABLE);
  }

  /**
   * Get styleable array resource ID Styleable resources are special - they return int arrays.
   *
   * @param context Host context.
   * @param styleableName Target styleable lookup name.
   * @return Mapped array values, null if missing.
   */
  public static int[] getStyleableArray(Context context, String styleableName) {
    if (context == null || styleableName == null) {
      return null;
    }

    Resources resources = context.getResources();
    String packageName = context.getPackageName();

    int resId = resources.getIdentifier(styleableName, "styleable", packageName);

    if (resId != 0) {
      return resources.getIntArray(resId);
    }

    return null;
  }

  /**
   * Get individual styleable attribute ID from styleable.
   *
   * @param context Host context.
   * @param styleableName Target parent styleable name.
   * @param attrName Target attribute name.
   * @return Mapped resource id.
   */
  public static int getStyleableAttrId(Context context, String styleableName, String attrName) {
    // Styleable attributes are typically named as StyleableName_AttributeName
    String fullName = styleableName + "_" + attrName;
    return getResourceId(context, fullName, ResourceType.STYLEABLE);
  }

  /**
   * Find drawable resource ID.
   *
   * @param context Host context.
   * @param drawableName Target drawable name.
   * @return Drawable resource ID, 0 if missing.
   */
  public static int getDrawableId(Context context, String drawableName) {
    return getResourceId(context, drawableName, ResourceType.DRAWABLE);
  }

  /**
   * Find string resource ID.
   *
   * @param context Host context.
   * @param stringName Target string name.
   * @return String resource ID, 0 if missing.
   */
  public static int getStringId(Context context, String stringName) {
    return getResourceId(context, stringName, ResourceType.STRING);
  }

  /**
   * Find layout resource ID.
   *
   * @param context Host context.
   * @param layoutName Target layout name.
   * @return Layout resource ID, 0 if missing.
   */
  public static int getLayoutId(Context context, String layoutName) {
    return getResourceId(context, layoutName, ResourceType.LAYOUT);
  }

  /**
   * Find color resource ID.
   *
   * @param context Host context.
   * @param colorName Target color name.
   * @return Color resource ID, 0 if missing.
   */
  public static int getColorId(Context context, String colorName) {
    return getResourceId(context, colorName, ResourceType.COLOR);
  }

  /**
   * Find dimension resource ID.
   *
   * @param context Host context.
   * @param dimenName Target dimension name.
   * @return Dimension resource ID, 0 if missing.
   */
  public static int getDimenId(Context context, String dimenName) {
    return getResourceId(context, dimenName, ResourceType.DIMEN);
  }

  /**
   * Find boolean resource ID.
   *
   * @param context Host context.
   * @param boolName Target boolean name.
   * @return Boolean resource ID, 0 if missing.
   */
  public static int getBoolId(Context context, String boolName) {
    return getResourceId(context, boolName, ResourceType.BOOL);
  }

  /**
   * Find integer resource ID.
   *
   * @param context Host context.
   * @param integerName Target integer name.
   * @return Integer resource ID, 0 if missing.
   */
  public static int getIntegerId(Context context, String integerName) {
    return getResourceId(context, integerName, ResourceType.INTEGER);
  }

  /**
   * Find animation resource ID.
   *
   * @param context Host context.
   * @param animName Target animation name.
   * @return Animation resource ID, 0 if missing.
   */
  public static int getAnimId(Context context, String animName) {
    return getResourceId(context, animName, ResourceType.ANIM);
  }

  /**
   * Find menu resource ID.
   *
   * @param context Host context.
   * @param menuName Target menu name.
   * @return Menu resource ID, 0 if missing.
   */
  public static int getMenuId(Context context, String menuName) {
    return getResourceId(context, menuName, ResourceType.MENU);
  }

  /**
   * Bulk find multiple resources.
   *
   * @param context Host context.
   * @param names Array of target resource names.
   * @param type Target folder group type.
   * @return Array of resolved resource IDs.
   */
  public static int[] getResourceIds(Context context, String[] names, ResourceType type) {
    if (names == null) return new int[0];

    int[] ids = new int[names.length];
    for (int i = 0; i < names.length; i++) {
      ids[i] = getResourceId(context, names[i], type);
    }
    return ids;
  }

  /**
   * Get attribute value from theme.
   *
   * @param context Host context.
   * @param attrName Target attribute name.
   * @return Mapped attribute integer resource ID.
   */
  public static int getThemeAttribute(Context context, String attrName) {
    int attrId = getAttrId(context, attrName);
    if (attrId == 0) {
      return 0;
    }

    TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[] {attrId});
    int value = typedArray.getResourceId(0, 0);
    typedArray.recycle();

    return value;
  }

  /**
   * Get attribute value from style.
   *
   * @param context Host context.
   * @param styleResId Target style resource.
   * @param attrName Target attribute name.
   * @return Mapped attribute integer value.
   */
  public static int getAttributeValue(Context context, int styleResId, String attrName) {
    int attrId = getAttrId(context, attrName);
    if (attrId == 0) {
      return 0;
    }

    TypedArray typedArray =
        context.getTheme().obtainStyledAttributes(styleResId, new int[] {attrId});
    int value = typedArray.getResourceId(0, 0);
    typedArray.recycle();

    return value;
  }

  /**
   * Get attribute integer value.
   *
   * @param context Host context.
   * @param styleResId Target style resource.
   * @param attrName Target attribute name.
   * @param defaultValue Fallback value if missing.
   * @return Resolved integer attribute.
   */
  public static int getAttributeIntValue(
      Context context, int styleResId, String attrName, int defaultValue) {
    int attrId = getAttrId(context, attrName);
    if (attrId == 0) {
      return defaultValue;
    }

    TypedArray typedArray =
        context.getTheme().obtainStyledAttributes(styleResId, new int[] {attrId});
    int value = typedArray.getInt(0, defaultValue);
    typedArray.recycle();

    return value;
  }

  /**
   * Get attribute boolean value.
   *
   * @param context Host context.
   * @param styleResId Target style resource.
   * @param attrName Target attribute name.
   * @param defaultValue Fallback value if missing.
   * @return Resolved boolean attribute.
   */
  public static boolean getAttributeBooleanValue(
      Context context, int styleResId, String attrName, boolean defaultValue) {
    int attrId = getAttrId(context, attrName);
    if (attrId == 0) {
      return defaultValue;
    }

    TypedArray typedArray =
        context.getTheme().obtainStyledAttributes(styleResId, new int[] {attrId});
    boolean value = typedArray.getBoolean(0, defaultValue);
    typedArray.recycle();

    return value;
  }

  /**
   * Get attribute string value.
   *
   * @param context Host context.
   * @param styleResId Target style resource.
   * @param attrName Target attribute name.
   * @return Resolved attribute text representation.
   */
  public static String getAttributeStringValue(Context context, int styleResId, String attrName) {
    int attrId = getAttrId(context, attrName);
    if (attrId == 0) {
      return null;
    }

    TypedArray typedArray =
        context.getTheme().obtainStyledAttributes(styleResId, new int[] {attrId});
    String value = typedArray.getString(0);
    typedArray.recycle();

    return value;
  }
}
