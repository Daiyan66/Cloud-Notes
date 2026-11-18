package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminOverviewStats
import com.example.data.model.DashboardStats
import com.example.data.model.ExamType
import com.example.data.model.NoteDto
import com.example.data.model.NoteFileItem
import com.example.data.model.QuestionPaperDto
import com.example.data.model.RatingState
import com.example.data.model.SummaryResult
import com.example.data.model.UserDto
import com.example.data.repository.CloudNotesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotesFilterState(
    val query: String = "",
    val branch: String = "All",
    val semester: Int? = null,
    val savedOnly: Boolean = false
)

data class PapersFilterState(
    val query: String = "",
    val branch: String = "All",
    val semester: Int? = null,
    val examType: ExamType? = null,
    val savedOnly: Boolean = false
)

data class SummaryUiState(
    val isLoading: Boolean = false,
    val result: SummaryResult? = null,
    val error: String? = null
)

class CloudNotesViewModel(
    private val repository: CloudNotesRepository
) : ViewModel() {

    val currentUser: StateFlow<UserDto?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val darkModePreference: StateFlow<Boolean?> = repository.darkModePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _notesFilter = MutableStateFlow(NotesFilterState())
    val notesFilter: StateFlow<NotesFilterState> = _notesFilter.asStateFlow()

    private val _papersFilter = MutableStateFlow(PapersFilterState())
    val papersFilter: StateFlow<PapersFilterState> = _papersFilter.asStateFlow()

    private val allNotesFlow = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allPapersFlow = repository.allPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allBookmarksFlow = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ratingsMap: StateFlow<Map<String, RatingState>> = repository.allRatings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val bookmarkedNoteIds: StateFlow<Set<String>> = allBookmarksFlow
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.filter { it.itemType == "NOTE" }.map { it.itemId }.toSet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val bookmarkedPaperIds: StateFlow<Set<String>> = allBookmarksFlow
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.filter { it.itemType == "PAPER" }.map { it.itemId }.toSet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val totalBookmarksCount: StateFlow<Int> = allBookmarksFlow
        .combine(MutableStateFlow(Unit)) { list, _ -> list.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val filteredNotes: StateFlow<List<NoteDto>> = combine(
        allNotesFlow,
        _notesFilter,
        bookmarkedNoteIds
    ) { notes, filter, savedIds ->
        notes.filter { note ->
            val matchesQuery = filter.query.isBlank() ||
                note.title.contains(filter.query, ignoreCase = true) ||
                note.subject.contains(filter.query, ignoreCase = true) ||
                note.description.contains(filter.query, ignoreCase = true)
            val matchesBranch = filter.branch == "All" ||
                note.branch.equals(filter.branch, ignoreCase = true)
            val matchesSem = filter.semester == null || note.semester == filter.semester
            val matchesSaved = !filter.savedOnly || savedIds.contains(note.id)
            matchesQuery && matchesBranch && matchesSem && matchesSaved
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPapers: StateFlow<List<QuestionPaperDto>> = combine(
        allPapersFlow,
        _papersFilter,
        bookmarkedPaperIds
    ) { papers, filter, savedIds ->
        papers.filter { paper ->
            val matchesQuery = filter.query.isBlank() ||
                paper.title.contains(filter.query, ignoreCase = true) ||
                paper.subject.contains(filter.query, ignoreCase = true) ||
                paper.year.toString().contains(filter.query.trim()) ||
                paper.description.contains(filter.query, ignoreCase = true)
            val matchesBranch = filter.branch == "All" ||
                paper.branch.equals(filter.branch, ignoreCase = true)
            val matchesSem = filter.semester == null || paper.semester == filter.semester
            val matchesExamType = filter.examType == null || paper.examType == filter.examType
            val matchesSaved = !filter.savedOnly || savedIds.contains(paper.id)
            matchesQuery && matchesBranch && matchesSem && matchesExamType && matchesSaved
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedNotes: StateFlow<List<NoteDto>> = combine(
        allNotesFlow,
        bookmarkedNoteIds
    ) { notes, savedIds ->
        notes.filter { savedIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedPapers: StateFlow<List<QuestionPaperDto>> = combine(
        allPapersFlow,
        bookmarkedPaperIds
    ) { papers, savedIds ->
        papers.filter { savedIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val myNotes: StateFlow<List<NoteDto>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observeNotesByUploader(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val myPapers: StateFlow<List<QuestionPaperDto>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else repository.observePapersByUploader(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = combine(
        myNotes,
        myPapers,
        totalBookmarksCount
    ) { notes, papers, bookmarks ->
        val bytes = notes.sumOf { it.totalSize } + papers.sumOf { it.totalSize }
        DashboardStats(
            totalNotesUploaded = notes.size,
            totalPapersUploaded = papers.size,
            aggregateFileSize = bytes,
            totalBookmarks = bookmarks
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardStats(0, 0, 0L, 0)
    )

    val adminUsers: StateFlow<List<UserDto>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminStats: StateFlow<AdminOverviewStats> = combine(
        adminUsers,
        allNotesFlow,
        allPapersFlow
    ) { users, notes, papers ->
        repository.computeAdminStats(users, notes, papers)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AdminOverviewStats(0, 0, 0, emptyMap(), emptyMap())
    )

    private val _summaryStates = MutableStateFlow<Map<String, SummaryUiState>>(emptyMap())
    val summaryStates: StateFlow<Map<String, SummaryUiState>> = _summaryStates.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    fun observeNote(noteId: String): Flow<NoteDto?> = repository.observeNote(noteId)

    fun getRatingForNote(noteId: String, map: Map<String, RatingState>): RatingState {
        return map[noteId] ?: repository.baselineRating(noteId)
    }

    fun toggleDarkMode(currentIsDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(!currentIsDark)
        }
    }

    fun updateNotesFilter(transform: (NotesFilterState) -> NotesFilterState) {
        _notesFilter.value = transform(_notesFilter.value)
    }

    fun resetNotesFilter() {
        _notesFilter.value = NotesFilterState()
    }

    fun updatePapersFilter(transform: (PapersFilterState) -> PapersFilterState) {
        _papersFilter.value = transform(_papersFilter.value)
    }

    fun resetPapersFilter() {
        _papersFilter.value = PapersFilterState()
    }

    fun toggleNoteBookmark(noteId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(noteId, "NOTE")
        }
    }

    fun togglePaperBookmark(paperId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(paperId, "PAPER")
        }
    }

    fun clearBookmarks(type: String? = null) {
        viewModelScope.launch {
            repository.clearBookmarks(type)
            _snackbarMessages.emit("Bookmarks cleared")
        }
    }

    fun rateNote(noteId: String, score: Int) {
        viewModelScope.launch {
            repository.rateNote(noteId, score)
            _snackbarMessages.emit("Rated $score/5 stars")
        }
    }

    fun generateSummary(note: NoteDto, fileId: String?) {
        viewModelScope.launch {
            _summaryStates.value = _summaryStates.value + (note.id to SummaryUiState(isLoading = true))
            repository.generateSummary(note, fileId)
                .onSuccess { summary ->
                    _summaryStates.value = _summaryStates.value + (
                        note.id to SummaryUiState(isLoading = false, result = summary)
                    )
                }
                .onFailure { err ->
                    _summaryStates.value = _summaryStates.value + (
                        note.id to SummaryUiState(
                            isLoading = false,
                            error = err.message ?: "Failed to generate summary."
                        )
                    )
                }
        }
    }

    fun login(email: String, password: String, onResult: (Result<UserDto>) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(email, password)
            onResult(result)
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        branch: String,
        semester: Int,
        onResult: (Result<UserDto>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.register(name, email, password, branch, semester)
            onResult(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _snackbarMessages.emit("Signed out")
        }
    }

    fun uploadNote(
        title: String,
        subject: String,
        branch: String,
        semester: Int,
        description: String,
        fileUris: List<Uri>,
        onResult: (Result<NoteDto>) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                onResult(Result.failure(IllegalStateException("Please sign in to upload notes.")))
                return@launch
            }
            val result = repository.createNote(user, title, subject, branch, semester, description, fileUris)
            onResult(result)
        }
    }

    fun uploadQuestionPaper(
        title: String,
        subject: String,
        branch: String,
        semester: Int,
        year: Int,
        examType: ExamType,
        description: String,
        fileUris: List<Uri>,
        onResult: (Result<QuestionPaperDto>) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                onResult(Result.failure(IllegalStateException("Please sign in to upload question papers.")))
                return@launch
            }
            val result = repository.createQuestionPaper(
                user, title, subject, branch, semester, year, examType, description, fileUris
            )
            onResult(result)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.deleteNote(noteId, user)
                .onSuccess { _snackbarMessages.emit("Note deleted") }
                .onFailure { _snackbarMessages.emit(it.message ?: "Failed to delete note") }
        }
    }

    fun deletePaper(paperId: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.deletePaper(paperId, user)
                .onSuccess { _snackbarMessages.emit("Question paper deleted") }
                .onFailure { _snackbarMessages.emit(it.message ?: "Failed to delete question paper") }
        }
    }

    fun toggleUserStatus(targetUserId: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.toggleUserStatus(targetUserId, user)
                .onSuccess { _snackbarMessages.emit("User access status updated") }
                .onFailure { _snackbarMessages.emit(it.message ?: "Could not update user status") }
        }
    }

    fun saveFileToDownloads(item: NoteFileItem) {
        viewModelScope.launch {
            repository.storageService.saveToDownloads(item)
                .onSuccess { msg -> _snackbarMessages.emit(msg) }
                .onFailure { err -> _snackbarMessages.emit(err.message ?: "Failed to save file") }
        }
    }

    fun buildShareIntent(item: NoteFileItem) = repository.storageService.buildShareIntent(item)

    fun buildOpenExternalIntent(item: NoteFileItem) = repository.storageService.buildOpenExternalIntent(item)

    class Factory(private val repository: CloudNotesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CloudNotesViewModel(repository) as T
        }
    }
}
