package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DashboardStats
import com.example.data.model.NoteDto
import com.example.data.model.QuestionPaperDto
import com.example.data.model.UserDto
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.formatFileSize
import com.example.ui.theme.Amber500
import com.example.ui.theme.DangerRed
import com.example.ui.theme.Emerald600
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Purple600

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUser: UserDto?,
    initialTab: String,
    stats: DashboardStats,
    myNotes: List<NoteDto>,
    myPapers: List<QuestionPaperDto>,
    bookmarkedNotes: List<NoteDto>,
    bookmarkedPapers: List<QuestionPaperDto>,
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateUploadNote: () -> Unit,
    onNavigateUploadPaper: () -> Unit,
    onNavigateAdmin: () -> Unit,
    onNavigateBrowseNotes: () -> Unit,
    onNavigateBrowsePapers: () -> Unit,
    onOpenNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onDeletePaper: (String) -> Unit,
    onToggleNoteBookmark: (String) -> Unit,
    onTogglePaperBookmark: (String) -> Unit,
    onClearBookmarkedNotes: () -> Unit,
    onClearBookmarkedPapers: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember {
        mutableIntStateOf(if (initialTab == "saved") 1 else 0)
    }
    LaunchedEffect(initialTab) {
        if (initialTab == "saved") selectedTab = 1
    }

    var pendingDeleteNote by remember { mutableStateOf<NoteDto?>(null) }
    var pendingDeletePaper by remember { mutableStateOf<QuestionPaperDto?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
        ) {
            if (currentUser == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Student Dashboard",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sign in to upload course notes, contribute exam papers, generate AI study summaries, and manage your resources.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onNavigateLogin,
                                modifier = Modifier.testTag("dashboard_login_button")
                            ) {
                                Text("Sign In")
                            }
                            OutlinedButton(
                                onClick = onNavigateRegister,
                                modifier = Modifier.testTag("dashboard_register_button")
                            ) {
                                Text("Create Account")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SavedBookmarksSection(
                    bookmarkedNotes = bookmarkedNotes,
                    bookmarkedPapers = bookmarkedPapers,
                    onOpenNote = onOpenNote,
                    onToggleNoteBookmark = onToggleNoteBookmark,
                    onTogglePaperBookmark = onTogglePaperBookmark,
                    onClearBookmarkedNotes = onClearBookmarkedNotes,
                    onClearBookmarkedPapers = onClearBookmarkedPapers,
                    onNavigateBrowseNotes = onNavigateBrowseNotes,
                    onNavigateBrowsePapers = onNavigateBrowsePapers
                )
            } else {
                // Signed-In Dashboard Header
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Student Dashboard",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Welcome back, ${currentUser.name} (${currentUser.branch} • Semester ${currentUser.semester})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (currentUser.isAdmin) {
                            OutlinedButton(
                                onClick = onNavigateAdmin,
                                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.45f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                modifier = Modifier.testTag("dashboard_admin_button")
                            ) {
                                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Admin")
                            }
                        }
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier.testTag("dashboard_logout_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log out")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateUploadNote,
                        modifier = Modifier.testTag("dashboard_upload_note_button")
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Notes")
                    }
                    OutlinedButton(
                        onClick = onNavigateUploadPaper,
                        modifier = Modifier.testTag("dashboard_upload_paper_button")
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Paper")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4 Stat Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardStatCard(
                        label = "Notes Uploaded",
                        value = stats.totalNotesUploaded.toString(),
                        accent = PrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        label = "Papers Shared",
                        value = stats.totalPapersUploaded.toString(),
                        accent = Emerald600,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardStatCard(
                        label = "Storage Used",
                        value = formatFileSize(stats.aggregateFileSize),
                        accent = Purple600,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        label = "Saved Bookmarks",
                        value = stats.totalBookmarks.toString(),
                        accent = Amber500,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("My Uploads") },
                        icon = { Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("dashboard_tab_uploads")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Saved (${stats.totalBookmarks})") },
                        icon = { Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("dashboard_tab_saved")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // My Uploaded Notes
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Book, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("My Uploaded Notes", style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (myNotes.isEmpty()) {
                                Text(
                                    text = "You haven't uploaded any notes yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    myNotes.forEach { note ->
                                        UploadItemRow(
                                            title = note.title,
                                            subtitle = "${note.subject} • ${note.branch} • ${note.files.size} file(s)",
                                            onClick = { onOpenNote(note.id) },
                                            onDelete = { pendingDeleteNote = note }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // My Uploaded Papers
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Description, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("My Question Papers", style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (myPapers.isEmpty()) {
                                Text(
                                    text = "You haven't uploaded any question papers yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    myPapers.forEach { paper ->
                                        UploadItemRow(
                                            title = paper.title,
                                            subtitle = "${paper.subject} • Year ${paper.year} • ${paper.examType.displayName}",
                                            onClick = onNavigateBrowsePapers,
                                            onDelete = { pendingDeletePaper = paper }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    SavedBookmarksSection(
                        bookmarkedNotes = bookmarkedNotes,
                        bookmarkedPapers = bookmarkedPapers,
                        onOpenNote = onOpenNote,
                        onToggleNoteBookmark = onToggleNoteBookmark,
                        onTogglePaperBookmark = onTogglePaperBookmark,
                        onClearBookmarkedNotes = onClearBookmarkedNotes,
                        onClearBookmarkedPapers = onClearBookmarkedPapers,
                        onNavigateBrowseNotes = onNavigateBrowseNotes,
                        onNavigateBrowsePapers = onNavigateBrowsePapers
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    pendingDeleteNote?.let { note ->
        AlertDialog(
            onDismissRequest = { pendingDeleteNote = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete \"${note.title}\" and its attached files?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteNote(note.id)
                        pendingDeleteNote = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteNote = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    pendingDeletePaper?.let { paper ->
        AlertDialog(
            onDismissRequest = { pendingDeletePaper = null },
            title = { Text("Delete Question Paper") },
            text = { Text("Are you sure you want to delete \"${paper.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePaper(paper.id)
                        pendingDeletePaper = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePaper = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DashboardStatCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun UploadItemRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = DangerRed
                )
            }
        }
    }
}

@Composable
private fun SavedBookmarksSection(
    bookmarkedNotes: List<NoteDto>,
    bookmarkedPapers: List<QuestionPaperDto>,
    onOpenNote: (String) -> Unit,
    onToggleNoteBookmark: (String) -> Unit,
    onTogglePaperBookmark: (String) -> Unit,
    onClearBookmarkedNotes: () -> Unit,
    onClearBookmarkedPapers: () -> Unit,
    onNavigateBrowseNotes: () -> Unit,
    onNavigateBrowsePapers: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bookmark, contentDescription = null, tint = Amber500, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saved Course Notes (${bookmarkedNotes.size})", style = MaterialTheme.typography.titleLarge)
                }
                if (bookmarkedNotes.isNotEmpty()) {
                    TextButton(onClick = onClearBookmarkedNotes) {
                        Text("Clear")
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (bookmarkedNotes.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Filled.Book,
                    title = "No notes saved yet",
                    description = "Bookmark notes while browsing to access them offline quickly.",
                    actionLabel = "Explore Notes to Save",
                    onAction = onNavigateBrowseNotes
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    bookmarkedNotes.forEach { note ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenNote(note.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${note.subject} • ${note.branch} • Sem ${note.semester}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onOpenNote(note.id) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open")
                                }
                                IconButton(onClick = { onToggleNoteBookmark(note.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove bookmark", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saved Question Papers (${bookmarkedPapers.size})", style = MaterialTheme.typography.titleLarge)
                }
                if (bookmarkedPapers.isNotEmpty()) {
                    TextButton(onClick = onClearBookmarkedPapers) {
                        Text("Clear")
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (bookmarkedPapers.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Filled.Description,
                    title = "No question papers saved yet",
                    description = "Bookmark exam archives to keep them handy during exam prep.",
                    actionLabel = "Explore Question Papers",
                    onAction = onNavigateBrowsePapers
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    bookmarkedPapers.forEach { paper ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = paper.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${paper.subject} • Year ${paper.year} • ${paper.branch}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onTogglePaperBookmark(paper.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove bookmark", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
