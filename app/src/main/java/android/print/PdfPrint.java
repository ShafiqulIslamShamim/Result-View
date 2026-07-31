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
package android.print;

import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.File;

public class PdfPrint {

  private static final String TAG = PdfPrint.class.getSimpleName();
  private final PrintAttributes printAttributes;

  public PdfPrint(PrintAttributes printAttributes) {
    this.printAttributes = printAttributes;
  }

  public void print(PrintDocumentAdapter printAdapter, final File path, final String fileName) {
    printAdapter.onLayout(
        null,
        printAttributes,
        null,
        new PrintDocumentAdapter.LayoutResultCallback() {
          @Override
          public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
            printAdapter.onWrite(
                new PageRange[] {PageRange.ALL_PAGES},
                getOutputFile(path, fileName),
                new CancellationSignal(),
                new PrintDocumentAdapter.WriteResultCallback() {
                  @Override
                  public void onWriteFinished(PageRange[] pages) {
                    super.onWriteFinished(pages);
                  }
                });
          }
        },
        null);
  }

  private ParcelFileDescriptor getOutputFile(File path, String fileName) {
    if (!path.exists()) {
      path.mkdirs();
    }
    File file = new File(path, fileName);
    try {
      file.createNewFile();
      return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    } catch (Exception e) {
      Log.e(TAG, "Failed to open ParcelFileDescriptor", e);
    }
    return null;
  }
}
