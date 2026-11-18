package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        NoteEntity::class,
        QuestionPaperEntity::class,
        BookmarkEntity::class,
        RatingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CloudNotesDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun paperDao(): PaperDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun ratingDao(): RatingDao

    companion object {
        @Volatile
        private var INSTANCE: CloudNotesDatabase? = null

        fun getInstance(context: Context): CloudNotesDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CloudNotesDatabase::class.java,
                    "cloudnotes_hub.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
