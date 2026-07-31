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

import com.app.resultviewbd.*;
import com.app.resultviewbd.activity.*;
import com.app.resultviewbd.exception_catcher.*;
import com.app.resultviewbd.preference.*;
import com.app.resultviewbd.recycle_view.*;
import com.app.resultviewbd.update_checker.*;

/**
 * Grade evaluation helper converting between literal grade strings and numeric grade point
 * averages.
 */
public class GradeHelper {

  /**
   * Translates standard SSC / HSC literal grades to points on a 5-point scale.
   *
   * @param grade Literal grade text.
   * @return Mapped point average value.
   */
  public static double sscHscPoint(String grade) {
    switch (grade) {
      case "A+":
        return 5.00;
      case "A":
        return 4.00;
      case "A-":
        return 3.50;
      case "B":
        return 3.00;
      case "C":
        return 2.00;
      case "D":
        return 1.00;
      case "F":
        return 0.00;
      default:
        return 0.00;
    }
  }

  /**
   * Translates standard University literal grades to points on a 4.0 scale.
   *
   * @param grade Literal grade text.
   * @return Mapped point average value.
   */
  public static double universityPoint(String grade) {
    switch (grade) {
      case "A+":
        return 4.00;
      case "A":
        return 3.75;
      case "A-":
        return 3.50;
      case "B+":
        return 3.25;
      case "B":
        return 3.00;
      case "B-":
        return 2.75;
      case "C+":
        return 2.50;
      case "C":
        return 2.25;
      case "D":
        return 2.00;
      case "F":
        return 0.00;
      default:
        return 0.00;
    }
  }

  /**
   * GPA থেকে Grade রিটার্ন করবে
   *
   * @param gpa GPA value (0.00 – 5.00)
   * @return Grade String (A+, A, A-, B, C, D, F)
   */
  public static String detectGrade(double gpa) {

    if (gpa >= 5.00) {
      return "A+";
    } else if (gpa >= 4.00) {
      return "A";
    } else if (gpa >= 3.50) {
      return "A-";
    } else if (gpa >= 3.00) {
      return "B";
    } else if (gpa >= 2.00) {
      return "C";
    } else if (gpa >= 1.00) {
      return "D";
    } else {
      return "F";
    }
  }
}
