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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * High-performance standalone PDF utility methods adopted from modern Pdfium patterns.
 * Allows inspecting metadata, page counts, table of contents, rendering pages directly
 * to Bitmaps (e.g. for book covers, grid thumbnails), exporting pages to image files,
 * printing, and sharing without inflating a View.
 */
public final class PdfUtils {

    private static final int BUFFER_SIZE = 4096;

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
     * Contains Title, Author, Subject, Keywords, Creator, Producer, CreationDate, ModDate.
     */
    public static PdfDocument.Meta getDocumentMeta(Context context, File file, String password) throws IOException {
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)) {
            return getDocumentMeta(context, pfd, password);
        }
    }

    public static PdfDocument.Meta getDocumentMeta(Context context, File file) throws IOException {
        return getDocumentMeta(context, file, null);
    }

    public static PdfDocument.Meta getDocumentMeta(Context context, Uri uri, String password) throws IOException {
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd == null) throw new IOException("Cannot open Uri: " + uri);
            return getDocumentMeta(context, pfd, password);
        }
    }

    public static PdfDocument.Meta getDocumentMeta(Context context, Uri uri) throws IOException {
        return getDocumentMeta(context, uri, null);
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

    public static List<PdfDocument.Bookmark> getTableOfContents(Context context, File file) throws IOException {
        return getTableOfContents(context, file, null);
    }

    public static List<PdfDocument.Bookmark> getTableOfContents(Context context, Uri uri, String password) throws IOException {
        try (ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd == null) return Collections.emptyList();
            return getTableOfContents(context, pfd, password);
        }
    }

    public static List<PdfDocument.Bookmark> getTableOfContents(Context context, Uri uri) throws IOException {
        return getTableOfContents(context, uri, null);
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
     * @param width     target bitmap width in pixels (0 for native width)
     * @param height    target bitmap height in pixels (0 for native height)
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

    public static Bitmap renderPageToBitmap(Context context, File file, int pageIndex) throws IOException {
        return renderPageToBitmap(context, file, pageIndex, 0, 0, null);
    }

    public static Bitmap renderPageToBitmap(Context context, Uri uri, int pageIndex) throws IOException {
        return renderPageToBitmap(context, uri, pageIndex, 0, 0, null);
    }

    /**
     * Render and export a specific PDF page directly to an image file on disk.
     * Supported formats: PNG, JPEG, WEBP.
     *
     * @param context    Android Context
     * @param file       PDF source file
     * @param pageIndex  0-indexed page number
     * @param outputFile Destination image file (e.g. cover.png or page_1.jpg)
     * @param format     Bitmap compression format (e.g. Bitmap.CompressFormat.PNG)
     * @param quality    Compression quality (0 - 100)
     * @return true if exported successfully
     */
    public static boolean savePageToFile(Context context, File file, int pageIndex, File outputFile,
                                         Bitmap.CompressFormat format, int quality) throws IOException {
        Bitmap bitmap = renderPageToBitmap(context, file, pageIndex);
        return writeBitmapToFile(bitmap, outputFile, format, quality);
    }

    public static boolean savePageToFile(Context context, Uri uri, int pageIndex, File outputFile,
                                         Bitmap.CompressFormat format, int quality) throws IOException {
        Bitmap bitmap = renderPageToBitmap(context, uri, pageIndex);
        return writeBitmapToFile(bitmap, outputFile, format, quality);
    }

    private static boolean writeBitmapToFile(Bitmap bitmap, File outputFile,
                                            Bitmap.CompressFormat format, int quality) throws IOException {
        if (bitmap == null) return false;
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            boolean success = bitmap.compress(format, quality, fos);
            fos.flush();
            return success;
        } finally {
            bitmap.recycle();
        }
    }

    /**
     * Copy an asset file to a cached file in context.getCacheDir()
     * so it can be handled as a standard File.
     */
    public static File fileFromAsset(Context context, String assetName) throws IOException {
        if (assetName == null || assetName.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset name cannot be blank");
        }
        File outFile = new File(context.getCacheDir(), assetName);
        if (assetName.contains("/")) {
            File parent = outFile.getParentFile();
            if (parent != null) parent.mkdirs();
        }
        try (InputStream in = context.getAssets().open(assetName);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
        return outFile;
    }

    /**
     * Read an entire InputStream into a byte array.
     */
    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        try (InputStream in = inputStream;
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    /**
     * Convert dp units to device pixels.
     */
    public static int getDP(Context context, int dp) {
        if (dp < 0) throw new IllegalArgumentException("DP value cannot be negative");
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static boolean indexExists(List<?> list, int index) {
        return list != null && index >= 0 && index < list.size();
    }

    public static boolean indexExists(int count, int index) {
        return index >= 0 && index < count;
    }

    /**
     * Print a PDF document using the Android PrintManager.
     * Works on Android 4.4+ (API 19+) all the way to Android 17 (API 37).
     *
     * @param context Android Activity or Context
     * @param file    The PDF file to print
     * @param jobName Print job name shown to the user
     */
    public static void print(Context context, File file, String jobName) {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        if (printManager != null) {
            String name = (jobName != null && !jobName.trim().isEmpty()) ? jobName : file.getName();
            printManager.print(name, new PdfPrintDocumentAdapter(file, name),
                    new PrintAttributes.Builder().build());
        }
    }

    public static void print(Context context, Uri uri, String jobName) {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        if (printManager != null) {
            String name = (jobName != null && !jobName.trim().isEmpty()) ? jobName : "PDF_Document";
            printManager.print(name, new PdfPrintDocumentAdapter(context, uri, name),
                    new PrintAttributes.Builder().build());
        }
    }

    /**
     * Share a PDF file via the native Android system share sheet.
     *
     * @param context      Android Context
     * @param uri          Content Uri of the PDF (e.g. from FileProvider)
     * @param chooserTitle Dialog title (e.g. "Share PDF via")
     */
    public static void share(Context context, Uri uri, String chooserTitle) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, chooserTitle != null ? chooserTitle : "Share PDF"));
    }
}
