package com.github.barteksc.sample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;

import java.util.ArrayList;
import java.util.List;

public class PDFViewActivity extends AppCompatActivity implements
        OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();
    private static final String SAMPLE_FILE = "sample.pdf";

    private PDFView pdfView;
    private Uri uri;
    private int pageNumber = 0;
    private String pdfFileName;
    private boolean isNightMode = false;
    private boolean isSwipeHorizontal = false;
    private boolean isBestQuality = true;

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), resultUri -> {
                if (resultUri != null) {
                    uri = resultUri;
                    pageNumber = 0;
                    displayFromUri(resultUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfView = findViewById(R.id.pdfView);
        pdfView.setBackgroundColor(Color.LTGRAY);

        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_toc) {
            showTableOfContentsDialog();
            return true;
        } else if (id == R.id.menu_jump) {
            showJumpToPageDialog();
            return true;
        } else if (id == R.id.menu_night_mode) {
            toggleNightMode();
            return true;
        } else if (id == R.id.menu_scroll_direction) {
            toggleScrollDirection();
            return true;
        } else if (id == R.id.menu_doc_info) {
            showDocumentInfoDialog();
            return true;
        } else if (id == R.id.pickFile) {
            launchPicker();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchPicker() {
        try {
            filePickerLauncher.launch("application/pdf");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;
        pdfView.fromAsset(assetFileName)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10)
                .nightMode(isNightMode)
                .useBestQuality(isBestQuality)
                .swipeHorizontal(isSwipeHorizontal)
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);
        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10)
                .nightMode(isNightMode)
                .useBestQuality(isBestQuality)
                .swipeHorizontal(isSwipeHorizontal)
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
    }

    /**
     * Display the Table of Contents (Bookmarks/Chapters) in an interactive dialog.
     */
    private void showTableOfContentsDialog() {
        if (!pdfView.hasTableOfContents()) {
            Toast.makeText(this, R.string.no_toc, Toast.LENGTH_LONG).show();
            return;
        }

        List<PdfDocument.Bookmark> bookmarks = pdfView.getTableOfContents();
        List<TocEntry> flatList = new ArrayList<>();
        flattenBookmarks(bookmarks, "", flatList);

        if (flatList.isEmpty()) {
            Toast.makeText(this, R.string.no_toc, Toast.LENGTH_LONG).show();
            return;
        }

        CharSequence[] items = new CharSequence[flatList.size()];
        for (int i = 0; i < flatList.size(); i++) {
            TocEntry entry = flatList.get(i);
            items[i] = String.format("%s (Page %d)", entry.title, entry.pageIdx + 1);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.table_of_contents)
                .setItems(items, (dialog, which) -> {
                    TocEntry selected = flatList.get(which);
                    pdfView.jumpTo(selected.pageIdx, true);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static class TocEntry {
        final String title;
        final int pageIdx;

        TocEntry(String title, int pageIdx) {
            this.title = title;
            this.pageIdx = pageIdx;
        }
    }

    private void flattenBookmarks(List<PdfDocument.Bookmark> bookmarks, String indent, List<TocEntry> result) {
        if (bookmarks == null) return;
        for (PdfDocument.Bookmark b : bookmarks) {
            String title = indent + (b.getTitle() != null ? b.getTitle() : "Untitled");
            result.add(new TocEntry(title, (int) b.getPageIdx()));
            if (b.hasChildren()) {
                flattenBookmarks(b.getChildren(), indent + "    ", result);
            }
        }
    }

    private void toggleNightMode() {
        isNightMode = !isNightMode;
        pdfView.setNightMode(isNightMode);
        Toast.makeText(this, isNightMode ? "Night Mode Enabled" : "Night Mode Disabled", Toast.LENGTH_SHORT).show();
    }

    private void toggleQuality() {
        isBestQuality = !isBestQuality;
        pageNumber = pdfView.getCurrentPage();
        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
        Toast.makeText(this, isBestQuality ? "HD Quality Mode (ARGB_8888) Enabled" : "Standard Mode (RGB_565) Enabled", Toast.LENGTH_SHORT).show();
    }

    private void toggleScrollDirection() {
        isSwipeHorizontal = !isSwipeHorizontal;
        pageNumber = pdfView.getCurrentPage();
        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
        Toast.makeText(this, isSwipeHorizontal ? "Horizontal Scrolling" : "Vertical Scrolling", Toast.LENGTH_SHORT).show();
    }

    private void showJumpToPageDialog() {
        int pageCount = pdfView.getPageCount();
        if (pageCount <= 0) return;

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("1 - " + pageCount);

        new AlertDialog.Builder(this)
                .setTitle(R.string.jump_to_page)
                .setMessage("Enter page number (1 to " + pageCount + "):")
                .setView(input)
                .setPositiveButton("Go", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    try {
                        int targetPage = Integer.parseInt(text);
                        if (targetPage >= 1 && targetPage <= pageCount) {
                            pdfView.jumpTo(targetPage - 1, true);
                        } else {
                            Toast.makeText(this, "Page number must be between 1 and " + pageCount, Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid page number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showDocumentInfoDialog() {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        StringBuilder sb = new StringBuilder();
        if (meta != null) {
            if (meta.getTitle() != null && !meta.getTitle().isEmpty()) {
                sb.append("Title: ").append(meta.getTitle()).append("\n\n");
            }
            if (meta.getAuthor() != null && !meta.getAuthor().isEmpty()) {
                sb.append("Author: ").append(meta.getAuthor()).append("\n\n");
            }
            if (meta.getSubject() != null && !meta.getSubject().isEmpty()) {
                sb.append("Subject: ").append(meta.getSubject()).append("\n\n");
            }
            if (meta.getProducer() != null && !meta.getProducer().isEmpty()) {
                sb.append("Producer: ").append(meta.getProducer()).append("\n\n");
            }
            if (meta.getCreationDate() != null && !meta.getCreationDate().isEmpty()) {
                sb.append("Creation Date: ").append(meta.getCreationDate()).append("\n\n");
            }
        }
        sb.append("Total Pages: ").append(pdfView.getPageCount());

        new AlertDialog.Builder(this)
                .setTitle(R.string.doc_info)
                .setMessage(sb.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s (%d / %d)", pdfFileName != null ? pdfFileName : "PDF", page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        Log.d(TAG, "PDF Loaded with " + nbPages + " pages");
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page, t);
    }
}
