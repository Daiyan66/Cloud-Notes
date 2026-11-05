# CloudNotes Hub

CloudNotes Hub is a secure, production-minded academic resource platform where students can share, discover, manage, and study course notes and previous examination question papers with built-in AI study summarization.

---

## Architecture & Database Selection

### Database Choice: MongoDB
- **Rationale**: Note sets, exam papers, and multi-file structures contain variable metadata and multiple embedded file descriptors (`fileId`, `originalFileName`, `storedFileName`, `size`, `contentType`, `downloadUrl`). MongoDB's document model represents course notes, question papers, user documents, and compound indexes naturally without complex relational join overhead.
- **Indexes**:
  - `users`: unique index on `email`.
  - `notes`: compound indexes on `{branch: 1, semester: 1}` and `{subject: 1}`, indexed on `uploaderId`.
  - `question_papers`: compound indexes on `{branch: 1, semester: 1}` and `{year: 1, examType: 1}`, indexed on `uploaderId`.

### Storage Abstraction:
- Decoupled `StorageService` interface. Current implementation (`LocalStorageService`) stores files on disk with strict magic byte validation via **Apache Tika**. It can be swapped for S3/Cloudinary by providing an alternate Spring Bean without modifying controllers.

### AI Provider Abstraction:
- `AiSummarizerProvider` abstracts the AI study summary generation.
- Default implementation extracts text server-side using **Apache PDFBox 3.x** and generates structured Markdown notes with key takeaways, formulas, and concepts. It caches summaries to minimize latency and computational cost.

---

## Implemented Features (Phase 1 / Half-Features)

1. **Authentication & Authorization**:
   - Secure stateless JWT authentication with BCrypt password hashing.
   - User roles (`ROLE_STUDENT`, `ROLE_ADMIN`).
   - Profile verification endpoint (`/api/auth/me`).
   - Automatic database seeding of an initial administrator on startup (`admin@cloudnotes.edu`).

2. **Course Notes**:
   - Multi-file uploads (PDF, DOC, DOCX, JPG, PNG) up to 10 files and 10MB per file.
   - Dual-layer validation (client-side and server-side with Apache Tika magic-bytes check).
   - Public browse, regex-based text search, and filtering by branch, semester, and subject.
   - Individual note detail page with attached documents list and direct download links.
   - Ownership-based deletion rules (students can delete their own notes; admins can delete any note).

3. **Previous Exam Question Papers**:
   - Upload papers categorized by Exam Type (`MID_SEMESTER`, `END_SEMESTER`, `SUPPLEMENTARY`, `INTERNAL`, `OTHER`).
   - Multi-file attachment support with download links.
   - Browse and filter by branch, semester, year, and exam category.

4. **AI Study Summarizer**:
   - Server-side PDF extraction via Apache PDFBox.
   - Generation of high-yield Markdown summaries with concepts and takeaways.
   - In-memory concurrent caching of generated summaries.
   - Academic disclaimer stating AI output must be verified against source material.

5. **Student Dashboard & Admin Console**:
   - User dashboard showing user's uploaded notes, question papers, total file count, and aggregate storage size.
   - Admin console with high-level statistics (total users, notes, papers, notes grouped by branch and semester) and user roster management with status toggling.

---

## Configuration & Environment Variables

Place these in an `.env` file or export them in your deployment environment:

| Variable | Description | Default |
|---|---|---|
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/cloudnotes_db` |
| `JWT_SECRET` | 256-bit Base64-encoded secret key | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `STORAGE_TYPE` | Storage engine (`local` or `s3`) | `local` |
| `STORAGE_LOCAL_DIR` | Local storage directory path | `./uploads` |
| `AI_PROVIDER` | AI provider engine (`mock`, `gemini`, or `openai`) | `mock` |
| `AI_API_KEY` | API Key for external AI providers | *(empty)* |
| `CORS_ALLOWED_ORIGINS` | Permitted frontend origins | `http://localhost:5173,http://localhost:3000` |
| `SEED_ADMIN_EMAIL` | Default administrator email | `admin@cloudnotes.edu` |
| `SEED_ADMIN_PASSWORD` | Default administrator password | `Admin@CloudNotes2026` |

---

## Local Setup & Running Instructions

### 1. Prerequisites
- Java 21 JDK
- Apache Maven 3.9+
- Node.js 18+ and npm
- Local MongoDB running on port 27017

### 2. Backend Setup
```bash
cd Cloud-Notes-Backend
mvn clean package -DskipTests
java -jar target/cloudnotes-backend-0.0.1-SNAPSHOT.jar
```
*The backend starts at `http://localhost:8080/api` and automatically seeds the administrator user.*

### 3. Frontend Setup
```bash
cd Cloud-Notes-Frontend
npm install
npm run dev
```
*The frontend starts at `http://localhost:5173`.*

---

## Seed Accounts

- **Administrator**:
  - Email: `admin@cloudnotes.edu`
  - Password: `Admin@CloudNotes2026`
- **Student**:
  - You can register any student account directly via the UI at `/register`.
