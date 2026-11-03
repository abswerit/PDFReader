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
package com.github.barteksc.pdfviewer;

import android.os.Handler;
import android.os.Looper;

import com.github.barteksc.pdfviewer.source.DocumentSource;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class DecodingAsyncTask {

    private static final Executor DEFAULT_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors())
    );

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile boolean cancelled = false;

    private final WeakReference<PDFView> pdfViewReference;

    private final PdfiumCore pdfiumCore;
    private final String password;
    private final DocumentSource docSource;
    private final int[] userPages;
    private PdfFile pdfFile;

    DecodingAsyncTask(DocumentSource docSource, String password, int[] userPages, PDFView pdfView, PdfiumCore pdfiumCore) {
        this.docSource = docSource;
        this.userPages = userPages;
        this.cancelled = false;
        this.pdfViewReference = new WeakReference<>(pdfView);
        this.password = password;
        this.pdfiumCore = pdfiumCore;
    }

    public void executeOnExecutor(Executor executor) {
        Executor exec = executor != null ? executor : DEFAULT_EXECUTOR;
        exec.execute(this::run);
    }

    public void execute() {
        DEFAULT_EXECUTOR.execute(this::run);
    }

    private void run() {
        if (cancelled) return;
        Throwable error = null;
        try {
            PDFView pdfView = pdfViewReference.get();
            if (pdfView != null) {
                PdfDocument pdfDocument = docSource.createDocument(pdfView.getContext(), pdfiumCore, password);
                pdfFile = new PdfFile(pdfiumCore, pdfDocument, pdfView.getPageFitPolicy(), getViewSize(pdfView),
                        userPages, pdfView.isSwipeVertical(), pdfView.getSpacingPx(), pdfView.isAutoSpacingEnabled(),
                        pdfView.isFitEachPage());
            } else {
                error = new NullPointerException("pdfView == null");
            }
        } catch (Throwable t) {
            error = t;
        }

        final Throwable finalError = error;
        mainHandler.post(() -> {
            PDFView pdfView = pdfViewReference.get();
            if (pdfView != null) {
                if (finalError != null) {
                    pdfView.loadError(finalError);
                    return;
                }
                if (!cancelled) {
                    pdfView.loadComplete(pdfFile);
                }
            }
        });
    }

    private Size getViewSize(PDFView pdfView) {
        return new Size(pdfView.getWidth(), pdfView.getHeight());
    }

    public void cancel(boolean mayInterruptIfRunning) {
        cancelled = true;
    }

    public void cancel() {
        cancel(true);
    }
}
