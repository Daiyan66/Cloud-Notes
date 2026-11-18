package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("UPDATE users SET enabled = :enabled WHERE id = :userId")
    suspend fun updateEnabled(userId: String, enabled: Boolean)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE uploaderId = :uploaderId ORDER BY createdAt DESC")
    fun observeNotesByUploader(uploaderId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeNoteById(id: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun countNotes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: String)
}

@Dao
interface PaperDao {
    @Query("SELECT * FROM question_papers ORDER BY year DESC, createdAt DESC")
    fun observeAllPapers(): Flow<List<QuestionPaperEntity>>

    @Query("SELECT * FROM question_papers WHERE uploaderId = :uploaderId ORDER BY createdAt DESC")
    fun observePapersByUploader(uploaderId: String): Flow<List<QuestionPaperEntity>>

    @Query("SELECT * FROM question_papers WHERE id = :id LIMIT 1")
    suspend fun getPaperById(id: String): QuestionPaperEntity?

    @Query("SELECT COUNT(*) FROM question_papers")
    suspend fun countPapers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paper: QuestionPaperEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(papers: List<QuestionPaperEntity>)

    @Query("DELETE FROM question_papers WHERE id = :paperId")
    suspend fun deleteById(paperId: String)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE itemId = :itemId AND itemType = :itemType)")
    suspend fun isBookmarked(itemId: String, itemType: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE itemId = :itemId AND itemType = :itemType")
    suspend fun delete(itemId: String, itemType: String)

    @Query("DELETE FROM bookmarks WHERE itemType = :itemType")
    suspend fun clearByType(itemType: String)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAll()
}

@Dao
interface RatingDao {
    @Query("SELECT * FROM ratings")
    fun observeAllRatings(): Flow<List<RatingEntity>>

    @Query("SELECT * FROM ratings WHERE noteId = :noteId LIMIT 1")
    fun observeRatingForNote(noteId: String): Flow<RatingEntity?>

    @Query("SELECT * FROM ratings WHERE noteId = :noteId LIMIT 1")
    suspend fun getRatingForNote(noteId: String): RatingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rating: RatingEntity)
}
