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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * High-performance standalone PDF utility methods adopted from modern Pdfium patterns.
 * Allows inspecting metadata, page counts, table of contents, and rendering
 * pages directly to Bitmaps (e.g. for book covers, grid thumbnails) without inflating a View.
 */
public final class PdfUtils {

    private PdfUtils() {
        // Utility class
    }

    /**
     * Get the total page count of a PDF file without opening a PDFView.
     */
    public static int getPageCount(Context context, File file, String password) throws IOException {
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)) {
            return getPageCount(context, pfd, password);
        }
    }

    public static int getPageCount(Context context, File file) throws IOException {
        return getPageCount(context, file, null);
    }

    public static int getPageCount(Context context, Uri uri, String password) throws IOException {
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd == null) throw new IOException("Cannot open Uri: " + uri);
            return getPageCount(context, pfd, password);
        }
    }

    public static int getPageCount(Context context, Uri uri) throws IOException {
        return getPageCount(context, uri, null);
    }

    public static int getPageCount(Context context, ParcelFileDescriptor pfd, String password) throws IOException {
        PdfiumCore core = new PdfiumCore(context);
        PdfDocument doc = null;
        try {
            doc = core.newDocument(pfd, password);
            return core.getPageCount(doc);
        } finally {
            if (doc != null) {
                core.closeDocument(doc);
            }
        }
    }

    /**
     * Check if a PDF file is password protected.
     */
    public static boolean isPasswordProtected(Context context, File file) {
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)) {
            return isPasswordProtected(context, pfd);
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isPasswordProtected(Context context, Uri uri) {
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd == null) return true;
            return isPasswordProtected(context, pfd);
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isPasswordProtected(Context context, ParcelFileDescriptor pfd) {
        PdfiumCore core = new PdfiumCore(context);
        PdfDocument doc = null;
        try {
            doc = core.newDocument(pfd, null);
            return false;
        } catch (Exception e) {
            return true;
        } finally {
            if (doc != null) {
                core.closeDocument(doc);
            }
        }
    }

    /**
     * Retrieve document metadata without inflating a PDFView.
     */
    public static PdfDocument.Meta getDocumentMeta(Context context, File file, String password) throws IOException {
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)) {
            return getDocumentMeta(context, pfd, password);
        }
    }

    public static PdfDocument.Meta getDocumentMeta(Context context, Uri uri, String password) throws IOException {
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd == null) throw new IOException("Cannot open Uri: " + uri);
            return getDocumentMeta(context, pfd, password);
        }
    }

    public static PdfDocument.Meta getDocumentMeta(Context context, ParcelFileDescriptor pfd, String password) throws IOException {
        PdfiumCore core = new PdfiumCore(context);
        PdfDocument doc = null;
        try {
            doc = core.newDocument(pfd, password);
            return core.getDocumentMeta(doc);
        } finally {
            if (doc != null) {
                core.closeDocument(doc);
            }
        }
    }

    /**
     * Extract Table of Contents / Bookmarks hierarchy without inflating a PDFView.
     */
    public static List<PdfDocument.Bookmark> getTableOfContents(Context context, File file, String password) throws IOException {
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)) {
            return getTableOfContents(context, pfd, password);
        }
    }

    public static List<PdfDocument.Bookmark> getTableOfContents(Context context, Uri uri, String password) throws IOException {
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd == null) return Collections.emptyList();
            return getTableOfContents(context, pfd, password);
        }
    }

    public static List<PdfDocument.Bookmark> getTableOfContents(Context context, ParcelFileDescriptor pfd, String password) throws IOException {
        PdfiumCore core = new PdfiumCore(context);
        PdfDocument doc = null;
        try {
            doc = core.newDocument(pfd, password);
            return core.getTableOfContents(doc);
        } finally {
            if (doc != null) {
                core.closeDocument(doc);
            }
        }
    }

    /**
     * Render a specific page directly to an Android Bitmap.
     * Ideal for book covers, gallery thumbnails, and page exports.
     *
     * @param pageIndex 0-indexed page number
     * @param width     target bitmap width in pixels
     * @param height    target bitmap height in pixels
     * @return Rendered Bitmap with ARGB_8888 quality
     */
    public static Bitmap renderPageToBitmap(Context context, File file, int pageIndex, int width, int height, String password) throws IOException {
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)) {
            return renderPageToBitmap(context, pfd, pageIndex, width, height, password);
        }
    }

    public static Bitmap renderPageToBitmap(Context context, File file, int pageIndex, int width, int height) throws IOException {
        return renderPageToBitmap(context, file, pageIndex, width, height, null);
    }

    public static Bitmap renderPageToBitmap(Context context, Uri uri, int pageIndex, int width, int height, String password) throws IOException {
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd == null) throw new IOException("Cannot open Uri: " + uri);
            return renderPageToBitmap(context, pfd, pageIndex, width, height, password);
        }
    }

    public static Bitmap renderPageToBitmap(Context context, Uri uri, int pageIndex, int width, int height) throws IOException {
        return renderPageToBitmap(context, uri, pageIndex, width, height, null);
    }

    public static Bitmap renderPageToBitmap(Context context, ParcelFileDescriptor pfd, int pageIndex, int width, int height, String password) throws IOException {
        PdfiumCore core = new PdfiumCore(context);
        PdfDocument doc = null;
        try {
            doc = core.newDocument(pfd, password);
            core.openPage(doc, pageIndex);

            int targetWidth = width > 0 ? width : core.getPageWidthPoint(doc, pageIndex);
            int targetHeight = height > 0 ? height : core.getPageHeightPoint(doc, pageIndex);

            Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
            core.renderPageBitmap(doc, bitmap, pageIndex, 0, 0, targetWidth, targetHeight, true);
            return bitmap;
        } finally {
            if (doc != null) {
                core.closeDocument(doc);
            }
        }
    }

    /**
     * Render a page at its native point dimensions directly to a Bitmap.
     */
    public static Bitmap renderPageToBitmap(Context context, File file, int pageIndex) throws IOException {
        return renderPageToBitmap(context, file, pageIndex, 0, 0, null);
    }

    public static Bitmap renderPageToBitmap(Context context, Uri uri, int pageIndex) throws IOException {
        return renderPageToBitmap(context, uri, pageIndex, 0, 0, null);
    }
}
