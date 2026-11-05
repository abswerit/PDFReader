/**
 * Copyright 2026 Abswer (https://abswer.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.pdfviewer.util;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * High-performance PrintDocumentAdapter for streaming PDF files or URIs directly
 * to the Android Print spooler with cancellation and progress handling.
 */
public class PdfPrintDocumentAdapter extends PrintDocumentAdapter {

    private static final String TAG = "PdfPrintAdapter";

    private final Context context;
    private final File file;
    private final Uri uri;
    private final String jobName;
    private int pageCount = PrintDocumentInfo.PAGE_COUNT_UNKNOWN;

    public PdfPrintDocumentAdapter(File file, String jobName) {
        this.context = null;
        this.file = file;
        this.uri = null;
        this.jobName = (jobName != null && !jobName.trim().isEmpty()) ? jobName : "PDF_Document";
    }

    public PdfPrintDocumentAdapter(Context context, Uri uri, String jobName) {
        this.context = context;
        this.file = null;
        this.uri = uri;
        this.jobName = (jobName != null && !jobName.trim().isEmpty()) ? jobName : "PDF_Document";
    }

    public PdfPrintDocumentAdapter setPageCount(int pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        if (cancellationSignal != null && cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        PrintDocumentInfo info = new PrintDocumentInfo.Builder(jobName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pageCount > 0 ? pageCount : PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build();

        boolean changed = !newAttributes.equals(oldAttributes);
        callback.onLayoutFinished(info, changed);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal, WriteResultCallback callback) {
        InputStream in = null;
        OutputStream out = null;

        try {
            if (file != null) {
                in = new FileInputStream(file);
            } else if (context != null && uri != null) {
                in = context.getContentResolver().openInputStream(uri);
            }

            if (in == null) {
                callback.onWriteFailed("Cannot open source PDF stream");
                return;
            }

            out = new FileOutputStream(destination.getFileDescriptor());
            byte[] buf = new byte[8192];
            int bytesRead;

            while ((bytesRead = in.read(buf)) > 0) {
                if (cancellationSignal != null && cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    return;
                }
                out.write(buf, 0, bytesRead);
            }

            out.flush();
            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

        } catch (Exception e) {
            Log.e(TAG, "Failed writing PDF to print spooler", e);
            callback.onWriteFailed(e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception ignored) {
            }
            try {
                if (out != null) out.close();
            } catch (Exception ignored) {
            }
        }
    }
}

