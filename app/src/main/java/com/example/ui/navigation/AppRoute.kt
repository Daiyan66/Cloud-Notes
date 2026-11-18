package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Notes : AppRoute

    @Serializable
    data class NoteDetail(val noteId: String) : AppRoute

    @Serializable
    data object Papers : AppRoute

    @Serializable
    data class Dashboard(val initialTab: String = "uploads") : AppRoute

    @Serializable
    data object UploadNote : AppRoute

    @Serializable
    data object UploadPaper : AppRoute

    @Serializable
    data object Login : AppRoute

    @Serializable
    data object Register : AppRoute

    @Serializable
    data object Admin : AppRoute
}
