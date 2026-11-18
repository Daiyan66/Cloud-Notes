# CloudNotes Hub (Android)

CloudNotes Hub is a native Android application built with **Kotlin** and **Jetpack Compose** (Material Design 3), rewritten from the CloudNotes Hub full-stack academic resource platform.

## Core Features

- **Home & Discovery**: Editorial hero overview with live library counts across course notes, examination archives, and saved bookmarks.
- **Course Notes Browser**: Search by title, subject, or topic; filter by academic branch, semester (1–8), and saved bookmarks; view ratings and attached document counts.
- **Note Detail & AI Study Summaries**: Interactive 5-star rating bar, bookmarking, sharing, multi-file attachment list, and an on-device **AI Study Summary** generator (with PDF/text extraction and summary caching).
- **In-App Document Viewer**: Native `PdfRenderer` multi-page PDF reader, image viewer (Coil), text/Markdown viewer, Downloads export, and Android Share Sheet / external app support via `FileProvider`.
- **Previous Exam Question Papers**: Search and filter past papers by branch, semester, exam type (`Mid Semester`, `End Semester`, `Supplementary`, `Internal`, `Other`), and saved bookmarks, with expandable multi-file drawers.
- **Student Dashboard & Uploads**: Upload multi-file study notes and question papers (up to 10 files, 10MB max per file) via Android's document picker, track storage usage, delete your uploads, and manage saved bookmarks.
- **Authentication & Admin Console**: Local account registration and sign-in (`student@cloudnotes.edu` / `admin@cloudnotes.edu` demo accounts pre-seeded), plus an Admin Console with platform metrics, branch breakdowns, and user access status controls.
- **Light & Dark Theme**: User-toggleable light/dark Material 3 theme backed by DataStore preferences and locally bundled `Plus Jakarta Sans` and `Inter` typography.

## Tech Stack

- **UI**: Jetpack Compose, Material Design 3, WindowSizeClass adaptive navigation (`NavigationBar` / `NavigationRail`)
- **Navigation**: Type-safe Navigation Compose (`@Serializable` routes)
- **Persistence**: Room Database + DataStore Preferences + local app file storage (`filesDir/uploads`)
- **Build**: Gradle Kotlin DSL, AGP 9.1.1, Kotlin 2.2.21, JDK 21, SDK 36
