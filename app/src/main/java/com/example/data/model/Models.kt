package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    ROLE_STUDENT,
    ROLE_ADMIN
}

@Serializable
enum class ExamType(val displayName: String) {
    MID_SEMESTER("Mid Semester"),
    END_SEMESTER("End Semester"),
    SUPPLEMENTARY("Supplementary"),
    INTERNAL("Internal"),
    OTHER("Other")
}

@Serializable
data class NoteFileItem(
    val fileId: String,
    val originalFileName: String,
    val storedFileName: String,
    val contentType: String,
    val size: Long,
    val localPath: String,
    val extractedText: String = ""
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val branch: String,
    val semester: Int,
    val roles: Set<Role>,
    val enabled: Boolean,
    val createdAt: Long
) {
    val isAdmin: Boolean
        get() = roles.contains(Role.ROLE_ADMIN)
}

data class NoteDto(
    val id: String,
    val title: String,
    val subject: String,
    val branch: String,
    val semester: Int,
    val description: String,
    val uploaderId: String,
    val uploaderName: String,
    val files: List<NoteFileItem>,
    val totalSize: Long,
    val downloadCount: Int,
    val bookmarkCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

data class QuestionPaperDto(
    val id: String,
    val title: String,
    val subject: String,
    val branch: String,
    val semester: Int,
    val year: Int,
    val examType: ExamType,
    val description: String,
    val uploaderId: String,
    val uploaderName: String,
    val files: List<NoteFileItem>,
    val totalSize: Long,
    val createdAt: Long,
    val updatedAt: Long
)

data class SummaryResult(
    val title: String,
    val markdownContent: String,
    val provider: String,
    val estimatedReadTimeMinutes: Int
)

data class DashboardStats(
    val totalNotesUploaded: Int,
    val totalPapersUploaded: Int,
    val aggregateFileSize: Long,
    val totalBookmarks: Int
)

data class AdminOverviewStats(
    val totalUsers: Int,
    val totalNotes: Int,
    val totalPapers: Int,
    val notesBySemester: Map<Int, Int>,
    val notesByBranch: Map<String, Int>
)

data class RatingState(
    val average: Double,
    val totalRatings: Int,
    val userRating: Int
)

object AcademicConstants {
    val BRANCHES = listOf(
        "Computer Science",
        "Information Technology",
        "Electronics",
        "Mechanical",
        "Civil",
        "Electrical"
    )

    val REGISTRATION_BRANCHES = BRANCHES + "General"

    val SEMESTERS = (1..8).toList()

    const val MAX_FILES_PER_UPLOAD = 10
    const val MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L
}
