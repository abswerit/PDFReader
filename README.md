# PDFReader

[![JitPack](https://jitpack.io/v/abswerit/pdfreader.svg)](https://jitpack.io/#abswerit/pdfreader)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Android 15 Ready](https://img.shields.io/badge/16%20KB%20Page%20Size-Supported-success)](16KB_SUPPORT.md)

**PDFReader** is an Android PDF viewing library based on `PdfiumAndroid` that provides a complete, smooth, and customizable PDF viewing experience.

Supports modern Android devices, including **16 KB memory page size** (required for Android 15+ and Google Play compliance).

---

## Features

- ⚡ **Fast & Smooth:** Powered by optimized native Pdfium rendering.
- 📱 **16 KB Page Alignment:** Fully compatible with Android 15 16 KB memory page requirements.
- 🔍 **Gestures & Zooming:** Pinch to zoom, double-tap zoom, and smooth scrolling.
- 🔄 **Scroll Orientation:** Vertical and horizontal page scrolling.
- 🌙 **Night Mode:** Inverted color rendering for comfortable reading in low light.
- 📂 **Flexible Sources:** Load PDFs from Assets, Internal/External Files, Content URIs, Streams, and Byte arrays.
- 🎛️ **Page Management:** Render specific pages, custom page spacing, and custom scroll handles.

---

## Installation

### 1. Add JitPack repository

In modern Android projects (Gradle 7+), add JitPack to your `settings.gradle`:

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

*Or in your project-level `build.gradle` (if using classic structure):*
```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Add the dependency

In your app module's `build.gradle` / `build.gradle.kts`:

```groovy
dependencies {
    implementation 'com.github.abswerit:pdfreader:1.0.0'
}
```

---

## Quick Start

### 1. Add `PDFView` to your layout (XML)

```xml
<com.github.barteksc.pdfviewer.PDFView
    android:id="@+id/pdfView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

### 2. Load a PDF in Kotlin / Java

#### In Kotlin:
```kotlin
val pdfView: PDFView = findViewById(R.id.pdfView)

// Load from Assets
pdfView.fromAsset("sample.pdf")
    .defaultPage(0)
    .enableSwipe(true)
    .swipeHorizontal(false)
    .enableDoubletap(true)
    .enableAnnotationRendering(true)
    .scrollHandle(DefaultScrollHandle(this))
    .spacing(10) // in dp
    .onLoad { nbPages -> 
        // PDF loaded successfully
    }
    .onPageChange { page, pageCount ->
        // Page changed
    }
    .onError { t ->
        // Error handling
    }
    .load()
```

#### Load from other sources:
```kotlin
// From File
pdfView.fromFile(file).load()

// From Uri (FileProvider / ContentResolver)
pdfView.fromUri(uri).load()

// From ByteArray
pdfView.fromBytes(byteArray).load()

// From InputStream
pdfView.fromStream(inputStream).load()
```

---

## Configuration Options

| Method | Description | Default |
| :--- | :--- | :--- |
| `enableSwipe(boolean)` | Enable or disable swiping to change pages | `true` |
| `swipeHorizontal(boolean)` | Change scroll direction from vertical to horizontal | `false` |
| `enableDoubletap(boolean)` | Enable or disable double-tap to zoom | `true` |
| `defaultPage(int)` | Set initial page to display (0-indexed) | `0` |
| `nightMode(boolean)` | Invert colors for dark mode reading | `false` |
| `enableAnnotationRendering(boolean)` | Render annotations (comments, forms, etc.) | `false` |
| `scrollHandle(ScrollHandle)` | Attach a custom or default scroll handle widget | `null` |
| `spacing(int)` | Spacing between pages in dp | `0` |
| `autoSpacing(boolean)` | Automatically calculate page spacing | `false` |
| `pageFitPolicy(FitPolicy)` | Page fit policy (`WIDTH`, `HEIGHT`, `BOTH`) | `FitPolicy.WIDTH` |
| `password(String)` | Open password-protected PDFs | `null` |

---

## 16 KB Page Size Compatibility

This library uses `io.github.oothp:pdfium-android` (version `1.9.5-beta01`+), which includes native binaries built with 16 KB page alignment. It meets the requirements for Google Play 64-bit and Android 15 compatibility.

---

## Author & Support

- **Website:** [Abswer.com](https://abswer.com)
- **GitHub:** [@abswerit](https://github.com/abswerit)
- **Email:** [contact@abswer.com](mailto:contact@abswer.com)

---

## License

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
