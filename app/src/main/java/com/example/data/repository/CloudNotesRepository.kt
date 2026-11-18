package com.example.data.repository

import android.net.Uri
import com.example.data.ai.AiSummarizerService
import com.example.data.local.BookmarkDao
import com.example.data.local.BookmarkEntity
import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import com.example.data.local.PaperDao
import com.example.data.local.QuestionPaperEntity
import com.example.data.local.RatingDao
import com.example.data.local.RatingEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.local.encodeNoteFiles
import com.example.data.model.AcademicConstants
import com.example.data.model.AdminOverviewStats
import com.example.data.model.ExamType
import com.example.data.model.NoteDto
import com.example.data.model.NoteFileItem
import com.example.data.model.QuestionPaperDto
import com.example.data.model.RatingState
import com.example.data.model.Role
import com.example.data.model.SummaryResult
import com.example.data.model.UserDto
import com.example.data.preferences.SessionPreferences
import com.example.data.seed.DatabaseSeeder
import com.example.data.storage.LocalStorageService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.math.roundToInt

class CloudNotesRepository(
    private val userDao: UserDao,
    private val noteDao: NoteDao,
    private val paperDao: PaperDao,
    private val bookmarkDao: BookmarkDao,
    private val ratingDao: RatingDao,
    private val sessionPreferences: SessionPreferences,
    val storageService: LocalStorageService,
    private val aiSummarizerService: AiSummarizerService
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<UserDto?> = sessionPreferences.currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(null)
        else userDao.observeById(userId).map { entity ->
            if (entity != null && entity.enabled) entity.toDto() else null
        }
    }

    val darkModePreference: Flow<Boolean?> = sessionPreferences.darkMode

    val allNotes: Flow<List<NoteDto>> = noteDao.observeAllNotes().map { list ->
        list.map { it.toDto() }
    }

    val allPapers: Flow<List<QuestionPaperDto>> = paperDao.observeAllPapers().map { list ->
        list.map { it.toDto() }
    }

    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.observeAllBookmarks()

    val allRatings: Flow<Map<String, RatingState>> = ratingDao.observeAllRatings().map { list ->
        list.associate { entity ->
            entity.noteId to RatingState(
                average = entity.average,
                totalRatings = entity.totalRatings,
                userRating = entity.userRating
            )
        }
    }

    val allUsers: Flow<List<UserDto>> = userDao.observeAllUsers().map { list ->
        list.map { it.toDto() }
    }

    fun observeNote(noteId: String): Flow<NoteDto?> =
        noteDao.observeNoteById(noteId).map { it?.toDto() }

    fun observeNotesByUploader(uploaderId: String): Flow<List<NoteDto>> =
        noteDao.observeNotesByUploader(uploaderId).map { list -> list.map { it.toDto() } }

    fun observePapersByUploader(uploaderId: String): Flow<List<QuestionPaperDto>> =
        paperDao.observePapersByUploader(uploaderId).map { list -> list.map { it.toDto() } }

    suspend fun setDarkMode(isDark: Boolean) {
        sessionPreferences.setDarkMode(isDark)
    }

    suspend fun login(email: String, password: String): Result<UserDto> = runCatching {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Email and password are required.")
        }
        val user = userDao.getByEmail(normalizedEmail)
            ?: throw IllegalArgumentException("Invalid email or password.")
        if (user.passwordHash != DatabaseSeeder.hashPassword(password)) {
            throw IllegalArgumentException("Invalid email or password.")
        }
        if (!user.enabled) {
            throw IllegalStateException("This account has been disabled by an administrator.")
        }
        sessionPreferences.setCurrentUserId(user.id)
        user.toDto()
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        branch: String,
        semester: Int
    ): Result<UserDto> = runCatching {
        val normalizedEmail = email.trim().lowercase()
        if (name.trim().isBlank()) throw IllegalArgumentException("Full name is required.")
        if (!normalizedEmail.contains("@")) throw IllegalArgumentException("Please enter a valid email address.")
        if (password.length < 6) throw IllegalArgumentException("Password must be at least 6 characters.")
        if (userDao.getByEmail(normalizedEmail) != null) {
            throw IllegalArgumentException("An account with this email already exists.")
        }

        val entity = UserEntity(
            id = "user-${UUID.randomUUID()}",
            email = normalizedEmail,
            passwordHash = DatabaseSeeder.hashPassword(password),
            name = name.trim(),
            branch = branch,
            semester = semester.coerceIn(1, 8),
            rolesCsv = Role.ROLE_STUDENT.name,
            enabled = true,
            createdAt = System.currentTimeMillis()
        )
        userDao.insert(entity)
        sessionPreferences.setCurrentUserId(entity.id)
        entity.toDto()
    }

    suspend fun logout() {
        sessionPreferences.setCurrentUserId(null)
    }

    suspend fun createNote(
        uploader: UserDto,
        title: String,
        subject: String,
        branch: String,
        semester: Int,
        description: String,
        fileUris: List<Uri>
    ): Result<NoteDto> = runCatching {
        if (title.trim().isBlank()) throw IllegalArgumentException("Note title is required.")
        if (subject.trim().isBlank()) throw IllegalArgumentException("Subject is required.")
        if (fileUris.isEmpty()) throw IllegalArgumentException("Please attach at least one note document.")
        if (fileUris.size > AcademicConstants.MAX_FILES_PER_UPLOAD) {
            throw IllegalArgumentException("You can upload a maximum of ${AcademicConstants.MAX_FILES_PER_UPLOAD} files per note.")
        }

        val storedFiles = mutableListOf<NoteFileItem>()
        try {
            for (uri in fileUris) {
                storedFiles.add(storageService.storeUri(uri))
            }
        } catch (e: Exception) {
            storedFiles.forEach { storageService.deleteFile(it.localPath) }
            throw e
        }

        val now = System.currentTimeMillis()
        val entity = NoteEntity(
            id = "note-${UUID.randomUUID()}",
            title = title.trim(),
            subject = subject.trim(),
            branch = branch,
            semester = semester.coerceIn(1, 8),
            description = description.trim(),
            uploaderId = uploader.id,
            uploaderName = uploader.name,
            filesJson = encodeNoteFiles(storedFiles),
            totalSize = storedFiles.sumOf { it.size },
            createdAt = now,
            updatedAt = now
        )
        noteDao.insert(entity)
        entity.toDto()
    }

    suspend fun deleteNote(noteId: String, currentUser: UserDto): Result<Unit> = runCatching {
        val entity = noteDao.getNoteById(noteId)
            ?: throw IllegalArgumentException("Note not found.")
        if (!currentUser.isAdmin && entity.uploaderId != currentUser.id) {
            throw IllegalAccessException("You are not allowed to delete this note.")
        }
        entity.toDto().files.forEach { storageService.deleteFile(it.localPath) }
        noteDao.deleteById(noteId)
        bookmarkDao.delete(noteId, "NOTE")
    }

    suspend fun createQuestionPaper(
        uploader: UserDto,
        title: String,
        subject: String,
        branch: String,
        semester: Int,
        year: Int,
        examType: ExamType,
        description: String,
        fileUris: List<Uri>
    ): Result<QuestionPaperDto> = runCatching {
        if (title.trim().isBlank()) throw IllegalArgumentException("Paper title is required.")
        if (subject.trim().isBlank()) throw IllegalArgumentException("Subject is required.")
        if (year !in 2000..2030) throw IllegalArgumentException("Exam year must be between 2000 and 2030.")
        if (fileUris.isEmpty()) throw IllegalArgumentException("Please attach at least one question paper document.")
        if (fileUris.size > AcademicConstants.MAX_FILES_PER_UPLOAD) {
            throw IllegalArgumentException("You can upload a maximum of ${AcademicConstants.MAX_FILES_PER_UPLOAD} files per question paper.")
        }

        val storedFiles = mutableListOf<NoteFileItem>()
        try {
            for (uri in fileUris) {
                storedFiles.add(storageService.storeUri(uri))
            }
        } catch (e: Exception) {
            storedFiles.forEach { storageService.deleteFile(it.localPath) }
            throw e
        }

        val now = System.currentTimeMillis()
        val entity = QuestionPaperEntity(
            id = "paper-${UUID.randomUUID()}",
            title = title.trim(),
            subject = subject.trim(),
            branch = branch,
            semester = semester.coerceIn(1, 8),
            year = year,
            examType = examType.name,
            description = description.trim(),
            uploaderId = uploader.id,
            uploaderName = uploader.name,
            filesJson = encodeNoteFiles(storedFiles),
            totalSize = storedFiles.sumOf { it.size },
            createdAt = now,
            updatedAt = now
        )
        paperDao.insert(entity)
        entity.toDto()
    }

    suspend fun deletePaper(paperId: String, currentUser: UserDto): Result<Unit> = runCatching {
        val entity = paperDao.getPaperById(paperId)
            ?: throw IllegalArgumentException("Question paper not found.")
        if (!currentUser.isAdmin && entity.uploaderId != currentUser.id) {
            throw IllegalAccessException("You are not allowed to delete this question paper.")
        }
        entity.toDto().files.forEach { storageService.deleteFile(it.localPath) }
        paperDao.deleteById(paperId)
        bookmarkDao.delete(paperId, "PAPER")
    }

    suspend fun toggleBookmark(itemId: String, itemType: String) {
        if (bookmarkDao.isBookmarked(itemId, itemType)) {
            bookmarkDao.delete(itemId, itemType)
        } else {
            bookmarkDao.insert(BookmarkEntity(itemId = itemId, itemType = itemType))
        }
    }

    suspend fun clearBookmarks(itemType: String? = null) {
        if (itemType == null) {
            bookmarkDao.clearAll()
        } else {
            bookmarkDao.clearByType(itemType)
        }
    }

    fun baselineRating(noteId: String): RatingState {
        val hash = noteId.sumOf { it.code }
        val baseCount = (hash % 12) + 4
        val baseAvg = ((4.0 + (hash % 10) / 10.0) * 10.0).roundToInt() / 10.0
        return RatingState(average = baseAvg, totalRatings = baseCount, userRating = 0)
    }

    suspend fun rateNote(noteId: String, score: Int) {
        val clamped = score.coerceIn(1, 5)
        val existing = ratingDao.getRatingForNote(noteId)
        val current = existing?.let {
            RatingState(it.average, it.totalRatings, it.userRating)
        } ?: baselineRating(noteId)

        val newTotal = if (current.userRating > 0) current.totalRatings else current.totalRatings + 1
        val oldSum = current.average * current.totalRatings - current.userRating
        val newAvg = (((oldSum + clamped) / newTotal) * 10.0).roundToInt() / 10.0

        ratingDao.upsert(
            RatingEntity(
                noteId = noteId,
                average = newAvg,
                totalRatings = newTotal,
                userRating = clamped
            )
        )
    }

    suspend fun generateSummary(note: NoteDto, fileId: String?): Result<SummaryResult> = runCatching {
        aiSummarizerService.generateSummary(note, fileId)
    }

    suspend fun toggleUserStatus(targetUserId: String, currentUser: UserDto): Result<Unit> = runCatching {
        if (!currentUser.isAdmin) throw IllegalAccessException("Administrator privileges required.")
        if (targetUserId == currentUser.id) throw IllegalArgumentException("You cannot disable your own active admin session.")
        val target = userDao.getById(targetUserId) ?: throw IllegalArgumentException("User not found.")
        userDao.updateEnabled(targetUserId, !target.enabled)
    }

    fun computeAdminStats(
        users: List<UserDto>,
        notes: List<NoteDto>,
        papers: List<QuestionPaperDto>
    ): AdminOverviewStats {
        val bySem = (1..8).associateWith { sem -> notes.count { it.semester == sem } }
        val byBranch = AcademicConstants.BRANCHES.associateWith { branch ->
            notes.count { it.branch.equals(branch, ignoreCase = true) }
        }
        return AdminOverviewStats(
            totalUsers = users.size,
            totalNotes = notes.size,
            totalPapers = papers.size,
            notesBySemester = bySem,
            notesByBranch = byBranch
        )
    }
}
