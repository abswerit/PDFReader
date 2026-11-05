# PDFReader

[![JitPack](https://jitpack.io/v/abswerit/PDFReader.svg)](https://jitpack.io/#abswerit/PDFReader)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Android Ready](https://img.shields.io/badge/Android%2017%20(API%2037)-Ready-blue)](build.gradle)
[![16 KB Page Size](https://img.shields.io/badge/16%20KB%20Page%20Size-100%25%20Verified%20%E2%9C%85-success)](16KB_SUPPORT.md)

**PDFReader** is an ultra-fast, robust, and modern Android PDF viewing library based on `PdfiumAndroid`. Developed and maintained by **[Abswer.com](https://abswer.com)**.

Engineered from the ground up for modern Android versions: fully compatible with **16 KB memory page size requirements** (Android 15+ and Google Play compliance) and forward-compatible up to **Android 17 (API 37)** with **zero deprecated APIs** (fully migrated away from `AsyncTask`).

---

## ✨ Features

- 📱 **16 KB Memory Page Size Verified:** All 64-bit native libraries (`arm64-v8a`, `x86_64`) are built and packaged with 16 KB page alignment (`0x4000`), passing Google Play's 16 KB policy.
- 🚀 **Android 17 (API 37) Ready:** Compiled and tested against modern Android toolchains, AGP 8.13.0, Gradle 8.14, and Java 17.
- 🧵 **Modern Concurrency:** Multi-threaded decoding powered by Java `ExecutorService` and `Handler(Looper.getMainLooper())` (no deprecated `android.os.AsyncTask`).
- 📑 **Digital Table of Contents (Bookmarks / Chapters):** Full support for reading document outlines, chapter trees, and jumping directly to chapters.
- ℹ️ **Document Metadata:** Direct access to PDF metadata (Title, Author, Subject, Creator, Producer, Creation and Modification Dates).
- 🌙 **Night Mode:** Runtime inverted color rendering for comfortable dark mode reading.
- 🔄 **Orientation & Scroll Controls:** Seamless vertical and horizontal scrolling, page snapping, and page fling animations.
- 🔍 **Smooth Gestures & Zooming:** Configurable pinch-to-zoom, double-tap zoom, and custom min/mid/max zoom factors.
- 🔗 **Interactive Links:** Built-in URI and internal page link handling.
- 🔐 **Password-Protected PDFs:** Effortlessly open encrypted PDF files with password support.
- 📂 **Multi-Source Loading:** Load PDFs from Assets, Internal/External Storage Files, Content URIs (FileProvider), Byte Arrays, and InputStreams.

---

## 📦 Installation

### 1. Add JitPack Repository

In your project-level `settings.gradle` (or `settings.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}
```

*Or in your project-level `build.gradle` (classic layout):*
```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Add Dependency

In your app module's `build.gradle` (or `build.gradle.kts`):

```groovy
dependencies {
    implementation 'com.github.abswerit:PDFReader:1.0.0'
}
```

---

## 🚀 Usage Guide

### 1. Add `PDFView` to Layout (XML)

```xml
<com.github.barteksc.pdfviewer.PDFView
    android:id="@+id/pdfView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

---

### 2. Basic PDF Loading (Kotlin)

```kotlin
val pdfView: PDFView = findViewById(R.id.pdfView)

// Load from Assets
pdfView.fromAsset("my_book.pdf")
    .defaultPage(0)
    .enableSwipe(true)
    .swipeHorizontal(false)
    .enableDoubletap(true)
    .enableAnnotationRendering(true)
    .scrollHandle(DefaultScrollHandle(this))
    .spacing(10) // dp
    .onLoad { nbPages ->
        Log.d("PDFReader", "Total pages loaded: $nbPages")
    }
    .onPageChange { page, pageCount ->
        Log.d("PDFReader", "Current page: ${page + 1} of $pageCount")
    }
    .onError { error ->
        Log.e("PDFReader", "Error loading PDF", error)
    }
    .load()
```

#### Load from other sources:
```kotlin
// From File
pdfView.fromFile(file).load()

// From Content Uri
pdfView.fromUri(contentUri).load()

// From Byte Array
pdfView.fromBytes(byteArray).load()

// From InputStream
pdfView.fromStream(inputStream).load()
```

---

### 3. 🎨 Jetpack Compose Integration (Kotlin)

PDFReader works smoothly inside **Jetpack Compose** using `AndroidView`:

```kotlin
@Composable
fun JetpackComposePdfViewer(
    assetFileName: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    isNightMode: Boolean = false,
    onPageChanged: ((page: Int, totalPages: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val pdfView = remember { PDFView(context, null) }

    DisposableEffect(Unit) {
        onDispose {
            pdfView.recycle() // Release native memory on exit
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            pdfView.apply {
                fromAsset(assetFileName)
                    .defaultPage(0)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .scrollHandle(DefaultScrollHandle(context))
                    .nightMode(isNightMode)
                    .onPageChange { p, count -> onPageChanged?.invoke(p, count) }
                    .load()
            }
        },
        update = { view ->
            view.setNightMode(isNightMode)
        }
    )
}
```

---

### 4. 📑 Table of Contents (Digital Chapters)

Extract and display document bookmarks/chapters, and jump to the target page:

```kotlin
// Check if PDF has chapters/bookmarks
if (pdfView.hasTableOfContents()) {
    val tableOfContents: List<PdfDocument.Bookmark> = pdfView.tableOfContents

    for (bookmark in tableOfContents) {
        val chapterTitle = bookmark.title
        val targetPageIndex = bookmark.pageIdx // 0-indexed

        Log.d("PDFReader", "Chapter: $chapterTitle (Page: ${targetPageIndex + 1})")

        // Handle nested sub-chapters
        if (bookmark.hasChildren()) {
            for (subChapter in bookmark.children) {
                Log.d("PDFReader", "  Sub-Chapter: ${subChapter.title}")
            }
        }
    }
}

// Jump to a specific chapter smoothly
pdfView.jumpTo(targetPageIndex, true)
```

---

### 5. ℹ️ Extracting Document Metadata

Access document properties in `onLoad` callback:

```kotlin
val meta: PdfDocument.Meta? = pdfView.documentMeta
if (meta != null) {
    val title = meta.title
    val author = meta.author
    val subject = meta.subject
    val keywords = meta.keywords
    val creator = meta.creator
    val producer = meta.producer
    val creationDate = meta.creationDate
    val modDate = meta.modDate
}
```

---

### 6. 🌙 Night Mode & Runtime Controls

```kotlin
// Toggle Night Mode (Inverts colors)
pdfView.setNightMode(true)

// Jump to any page (0-indexed) with animation
pdfView.jumpTo(pageNumber, true)

// Zoom Controls
pdfView.resetZoomWithAnimation()
pdfView.zoomWithAnimation(2.0f) // zoom to 2x
```

---

### 7. 🔍 Anti-Blur & HD Quality Mode (Fix for Scanned & Image PDFs)

If you are displaying scanned book pages, photocopied documents, or raster image PDFs, text and images can sometimes appear blurry or low-resolution under default rendering.

To solve this, enable **`useBestQuality(true)`**:

```kotlin
pdfView.fromAsset("scanned_book.pdf")
    .useBestQuality(true)       // Forces 32-bit ARGB_8888 bitmap rendering for crisp text
    .enableAntialiasing(true)    // Bilinear filtering on bitmap parts
    .load()
```

#### What makes this sharp and anti-blur:
1. **`useBestQuality(true)`:** Switches bitmap rendering from 16-bit `RGB_565` to 32-bit `ARGB_8888`. This preserves fine grayscale antialiasing around scanned text and edges.
2. **Tile & Thumbnail Boost:** Internal tile part size is upgraded to 512px and thumbnail ratio to 0.5f in `Constants`, preventing blurry downscaled previews.
3. **Canvas Filtering:** Paint operates with `setFilterBitmap(true)` and `setDither(true)` for smooth scaling without pixelation.

---

### 8. 📸 Standalone PDF Utilities, Image Exporter, Print & Share

You can render any page of a PDF directly to an Android `Bitmap` (e.g. for generating book covers, gallery previews, or exporting images), export pages directly to PNG/JPEG files on disk, print via Android's native `PrintManager`, share, or inspect document details **without inflating a PDFView**:

```kotlin
// 1. Generate Cover Photo / Thumbnail (Page 0) as Bitmap
val coverBitmap: Bitmap = PdfUtils.renderPageToBitmap(context, pdfFile, pageIndex = 0, width = 400, height = 600)
imageView.setImageBitmap(coverBitmap)

// 2. Export Any Page Directly to Image File on Disk (PNG / JPEG / WEBP)
val outFile = File(context.getExternalFilesDir(null), "page_cover.png")
val isSaved: Boolean = PdfUtils.savePageToFile(
    context = context,
    file = pdfFile,
    pageIndex = 0,
    outputFile = outFile,
    format = Bitmap.CompressFormat.PNG,
    quality = 100
)

// 3. Print PDF via Native Android System Print Spooler (PrintManager)
PdfUtils.print(activityContext, pdfFile, "My Print Job")
// Or print from Uri:
PdfUtils.print(activityContext, contentUri, "Invoice_101")

// 4. Share PDF via Native Android Share Sheet
PdfUtils.share(activityContext, contentUri, "Share PDF Document")

// 5. Get total page count directly
val pageCount: Int = PdfUtils.getPageCount(context, pdfFile)

// 6. Check if PDF is encrypted / password protected
val isLocked: Boolean = PdfUtils.isPasswordProtected(context, pdfFile)

// 7. Extract metadata directly (Title, Author, Subject, Producer, Dates, etc.)
val meta: PdfDocument.Meta = PdfUtils.getDocumentMeta(context, pdfFile)
val title = meta.title
val author = meta.author

// 8. Extract Table of Contents without opening view
val bookmarks: List<PdfDocument.Bookmark> = PdfUtils.getTableOfContents(context, pdfFile)

// 9. Cache an Asset file to a standard File
val cachedFile: File = PdfUtils.fileFromAsset(context, "sample.pdf")
```

---

## ⚙️ Configuration Reference

| Config Method | Description | Default |
| :--- | :--- | :--- |
| `enableSwipe(boolean)` | Enable or disable touch swiping | `true` |
| `swipeHorizontal(boolean)` | Scroll horizontally instead of vertically | `false` |
| `enableDoubletap(boolean)` | Enable double-tap to zoom | `true` |
| `useBestQuality(boolean)` | Render with ARGB_8888 for HD crisp scanned image PDFs | `false` |
| `defaultPage(int)` | Initial page to open (0-indexed) | `0` |
| `nightMode(boolean)` | Invert page colors for dark theme | `false` |
| `enableAnnotationRendering(boolean)` | Render embedded annotations & form fields | `false` |
| `scrollHandle(ScrollHandle)` | Attach custom or default scroll handle widget | `null` |
| `spacing(int)` | Spacing between pages in dp | `0` |
| `autoSpacing(boolean)` | Automatically adjust spacing to fit screen | `false` |
| `pageFitPolicy(FitPolicy)` | Page sizing mode (`WIDTH`, `HEIGHT`, `BOTH`) | `FitPolicy.BOTH` |
| `pageSnap(boolean)` | Snap page by page like a ViewPager | `false` |
| `pageFling(boolean)` | Enable fling velocity gesture | `false` |
| `password(String)` | Decrypt password-protected PDF | `null` |

---

## 🛡️ 16 KB Page Size Compatibility

Starting November 2025, Google Play requires all apps targeting modern Android to support **16 KB memory page size**.

This library integrates `io.github.oothp:pdfium-android` where:
- All 64-bit ELF shared objects (`arm64-v8a`, `x86_64`) have loadable segments aligned to `0x4000` (16 KB).
- Uncompressed packaging in APK ensures byte offset modulo `16384 == 0`.
- Meets all Google Play Console requirements without native crash risks.

---

## 🔒 ProGuard / R8 Rules

If your app uses R8 / ProGuard, add the following rules:

```proguard
-keep class com.github.barteksc.pdfviewer.** { *; }
-keep class com.shockwave.pdfium.** { *; }
```

---

## 👨‍💻 Author & Support

- **Organization:** [Abswer.com](https://abswer.com)
- **Developer:** Abswer ([@abswerit](https://github.com/abswerit))
- **Email:** [contact@abswer.com](mailto:contact@abswer.com) / [admin@abswer.com](mailto:admin@abswer.com)
- **Repository:** [https://github.com/abswerit/PDFReader](https://github.com/abswerit/PDFReader)

---

## 📄 License

```text
Copyright 2026 Abswer (https://abswer.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
