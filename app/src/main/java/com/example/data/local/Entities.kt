package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ExamType
import com.example.data.model.NoteDto
import com.example.data.model.NoteFileItem
import com.example.data.model.QuestionPaperDto
import com.example.data.model.Role
import com.example.data.model.UserDto
import kotlinx.serialization.json.Json

private val jsonCodec = Json { ignoreUnknownKeys = true }

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val name: String,
    val branch: String,
    val semester: Int,
    val rolesCsv: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDto(): UserDto {
        val parsedRoles = rolesCsv.split(",")
            .mapNotNull { runCatching { Role.valueOf(it.trim()) }.getOrNull() }
            .toSet()
        return UserDto(
            id = id,
            name = name,
            email = email,
            branch = branch,
            semester = semester,
            roles = parsedRoles.ifEmpty { setOf(Role.ROLE_STUDENT) },
            enabled = enabled,
            createdAt = createdAt
        )
    }
}

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val branch: String,
    val semester: Int,
    val description: String,
    val uploaderId: String,
    val uploaderName: String,
    val filesJson: String,
    val totalSize: Long,
    val downloadCount: Int = 0,
    val bookmarkCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDto(): NoteDto {
        val files = runCatching {
            jsonCodec.decodeFromString<List<NoteFileItem>>(filesJson)
        }.getOrDefault(emptyList())
        return NoteDto(
            id = id,
            title = title,
            subject = subject,
            branch = branch,
            semester = semester,
            description = description,
            uploaderId = uploaderId,
            uploaderName = uploaderName,
            files = files,
            totalSize = totalSize,
            downloadCount = downloadCount,
            bookmarkCount = bookmarkCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@Entity(tableName = "question_papers")
data class QuestionPaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val branch: String,
    val semester: Int,
    val year: Int,
    val examType: String,
    val description: String,
    val uploaderId: String,
    val uploaderName: String,
    val filesJson: String,
    val totalSize: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDto(): QuestionPaperDto {
        val files = runCatching {
            jsonCodec.decodeFromString<List<NoteFileItem>>(filesJson)
        }.getOrDefault(emptyList())
        val parsedExamType = runCatching { ExamType.valueOf(examType) }.getOrDefault(ExamType.OTHER)
        return QuestionPaperDto(
            id = id,
            title = title,
            subject = subject,
            branch = branch,
            semester = semester,
            year = year,
            examType = parsedExamType,
            description = description,
            uploaderId = uploaderId,
            uploaderName = uploaderName,
            files = files,
            totalSize = totalSize,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@Entity(
    tableName = "bookmarks",
    primaryKeys = ["itemId", "itemType"]
)
data class BookmarkEntity(
    val itemId: String,
    val itemType: String, // "NOTE" or "PAPER"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey val noteId: String,
    val average: Double,
    val totalRatings: Int,
    val userRating: Int
)

fun encodeNoteFiles(files: List<NoteFileItem>): String = jsonCodec.encodeToString(files)
