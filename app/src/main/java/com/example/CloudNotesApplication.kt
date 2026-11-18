package com.example

import android.app.Application
import com.example.data.ai.AiSummarizerService
import com.example.data.local.CloudNotesDatabase
import com.example.data.preferences.SessionPreferences
import com.example.data.repository.CloudNotesRepository
import com.example.data.seed.DatabaseSeeder
import com.example.data.storage.LocalStorageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CloudNotesApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var repository: CloudNotesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = CloudNotesDatabase.getInstance(this)
        val storageService = LocalStorageService(this)
        val aiService = AiSummarizerService()
        val prefs = SessionPreferences(this)

        repository = CloudNotesRepository(
            userDao = db.userDao(),
            noteDao = db.noteDao(),
            paperDao = db.paperDao(),
            bookmarkDao = db.bookmarkDao(),
            ratingDao = db.ratingDao(),
            sessionPreferences = prefs,
            storageService = storageService,
            aiSummarizerService = aiService
        )

        appScope.launch {
            DatabaseSeeder(
                userDao = db.userDao(),
                noteDao = db.noteDao(),
                paperDao = db.paperDao(),
                storageService = storageService
            ).seedIfEmpty()
        }
    }
}
