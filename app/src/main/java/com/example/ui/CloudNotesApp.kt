package com.example.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.R
import com.example.data.model.NoteDto
import com.example.data.model.NoteFileItem
import com.example.ui.navigation.AppRoute
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NoteDetailScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.QuestionPapersScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.UploadNoteScreen
import com.example.ui.screens.UploadPaperScreen
import com.example.ui.theme.Amber500
import com.example.ui.theme.CloudNotesTheme

private data class TopLevelNavItem(
    val label: String,
    val route: AppRoute,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudNotesApp(
    viewModel: CloudNotesViewModel,
    widthSizeClass: WindowWidthSizeClass
) {
    val darkPref by viewModel.darkModePreference.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val isDark = darkPref ?: systemDark

    CloudNotesTheme(darkTheme = isDark) {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current

        val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
        val notesFilter by viewModel.notesFilter.collectAsStateWithLifecycle()
        val papersFilter by viewModel.papersFilter.collectAsStateWithLifecycle()
        val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
        val filteredPapers by viewModel.filteredPapers.collectAsStateWithLifecycle()
        val bookmarkedNoteIds by viewModel.bookmarkedNoteIds.collectAsStateWithLifecycle()
        val bookmarkedPaperIds by viewModel.bookmarkedPaperIds.collectAsStateWithLifecycle()
        val totalBookmarks by viewModel.totalBookmarksCount.collectAsStateWithLifecycle()
        val ratingsMap by viewModel.ratingsMap.collectAsStateWithLifecycle()
        val summaryStates by viewModel.summaryStates.collectAsStateWithLifecycle()
        val myNotes by viewModel.myNotes.collectAsStateWithLifecycle()
        val myPapers by viewModel.myPapers.collectAsStateWithLifecycle()
        val bookmarkedNotes by viewModel.bookmarkedNotes.collectAsStateWithLifecycle()
        val bookmarkedPapers by viewModel.bookmarkedPapers.collectAsStateWithLifecycle()
        val dashboardStats by viewModel.dashboardStats.collectAsStateWithLifecycle()
        val adminUsers by viewModel.adminUsers.collectAsStateWithLifecycle()
        val adminStats by viewModel.adminStats.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.snackbarMessages.collect { msg ->
                snackbarHostState.showSnackbar(msg)
            }
        }

        val shareFileAction: (NoteFileItem) -> Unit = { file ->
            val intent = viewModel.buildShareIntent(file)
            if (intent != null) {
                context.startActivity(Intent.createChooser(intent, "Share ${file.originalFileName}"))
            }
        }

        val openExternalAction: (NoteFileItem) -> Unit = { file ->
            val intent = viewModel.buildOpenExternalIntent(file)
            if (intent != null) {
                runCatching {
                    context.startActivity(Intent.createChooser(intent, "Open with"))
                }.onFailure {
                    viewModel.saveFileToDownloads(file)
                }
            }
        }

        val shareNoteTextAction: (NoteDto) -> Unit = { note ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, note.title)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${note.title}\nSubject: ${note.subject} (${note.branch} - Sem ${note.semester})\nShared via CloudNotes Hub"
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
        }

        val navItems = remember {
            listOf(
                TopLevelNavItem(
                    label = "Home",
                    route = AppRoute.Home,
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    testTag = "nav_home"
                ),
                TopLevelNavItem(
                    label = "Notes",
                    route = AppRoute.Notes,
                    selectedIcon = Icons.Filled.Book,
                    unselectedIcon = Icons.Outlined.Book,
                    testTag = "nav_notes"
                ),
                TopLevelNavItem(
                    label = "Papers",
                    route = AppRoute.Papers,
                    selectedIcon = Icons.Filled.Description,
                    unselectedIcon = Icons.Outlined.Description,
                    testTag = "nav_papers"
                ),
                TopLevelNavItem(
                    label = "Dashboard",
                    route = AppRoute.Dashboard(),
                    selectedIcon = Icons.Filled.Dashboard,
                    unselectedIcon = Icons.Outlined.Dashboard,
                    testTag = "nav_dashboard"
                )
            )
        }

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        val isCompact = widthSizeClass == WindowWidthSizeClass.Compact

        val navigateTopLevel: (AppRoute) -> Unit = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = route !is AppRoute.Dashboard
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Book,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navigateTopLevel(AppRoute.Dashboard(initialTab = "saved")) },
                            modifier = Modifier.testTag("topbar_saved_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (totalBookmarks > 0) {
                                        Badge { Text(totalBookmarks.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = "Saved Bookmarks",
                                    tint = if (totalBookmarks > 0) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.toggleDarkMode(isDark) },
                            modifier = Modifier.testTag("topbar_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                                tint = if (isDark) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            onClick = {
                                if (currentUser != null) {
                                    navigateTopLevel(AppRoute.Dashboard())
                                } else {
                                    navController.navigate(AppRoute.Login)
                                }
                            },
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .testTag("topbar_auth_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = currentUser?.name?.substringBefore(" ") ?: "Log in",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (isCompact) {
                    NavigationBar {
                        navItems.forEach { item ->
                            val selected = currentDestination?.let { dest ->
                                when (item.route) {
                                    is AppRoute.Home -> dest.hasRoute<AppRoute.Home>()
                                    is AppRoute.Notes -> dest.hasRoute<AppRoute.Notes>() || dest.hasRoute<AppRoute.NoteDetail>()
                                    is AppRoute.Papers -> dest.hasRoute<AppRoute.Papers>()
                                    is AppRoute.Dashboard -> dest.hasRoute<AppRoute.Dashboard>()
                                    else -> false
                                }
                            } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!isCompact) {
                    NavigationRail {
                        Spacer(modifier = Modifier.size(8.dp))
                        navItems.forEach { item ->
                            val selected = currentDestination?.let { dest ->
                                when (item.route) {
                                    is AppRoute.Home -> dest.hasRoute<AppRoute.Home>()
                                    is AppRoute.Notes -> dest.hasRoute<AppRoute.Notes>() || dest.hasRoute<AppRoute.NoteDetail>()
                                    is AppRoute.Papers -> dest.hasRoute<AppRoute.Papers>()
                                    is AppRoute.Dashboard -> dest.hasRoute<AppRoute.Dashboard>()
                                    else -> false
                                }
                            } == true

                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Home,
                    modifier = Modifier.weight(1f)
                ) {
                    composable<AppRoute.Home> {
                        HomeScreen(
                            notesCount = filteredNotes.size,
                            papersCount = filteredPapers.size,
                            bookmarksCount = totalBookmarks,
                            onExploreNotes = { navigateTopLevel(AppRoute.Notes) },
                            onExplorePapers = { navigateTopLevel(AppRoute.Papers) }
                        )
                    }

                    composable<AppRoute.Notes> {
                        NotesScreen(
                            notes = filteredNotes,
                            filter = notesFilter,
                            bookmarkedIds = bookmarkedNoteIds,
                            ratingsProvider = { id -> viewModel.getRatingForNote(id, ratingsMap) },
                            onQueryChange = { q -> viewModel.updateNotesFilter { it.copy(query = q) } },
                            onBranchChange = { b -> viewModel.updateNotesFilter { it.copy(branch = b) } },
                            onSemesterChange = { s -> viewModel.updateNotesFilter { it.copy(semester = s) } },
                            onToggleSavedOnly = { viewModel.updateNotesFilter { it.copy(savedOnly = !it.savedOnly) } },
                            onResetFilters = { viewModel.resetNotesFilter() },
                            onToggleBookmark = { id -> viewModel.toggleNoteBookmark(id) },
                            onOpenNote = { noteId -> navController.navigate(AppRoute.NoteDetail(noteId)) },
                            onUploadNoteClick = {
                                if (currentUser != null) {
                                    navController.navigate(AppRoute.UploadNote)
                                } else {
                                    navController.navigate(AppRoute.Login)
                                }
                            }
                        )
                    }

                    composable<AppRoute.NoteDetail> { entry ->
                        val route = entry.toRoute<AppRoute.NoteDetail>()
                        val note by viewModel.observeNote(route.noteId).collectAsStateWithLifecycle(initialValue = null)
                        val summaryUi = summaryStates[route.noteId] ?: SummaryUiState()

                        NoteDetailScreen(
                            note = note,
                            currentUser = currentUser,
                            isBookmarked = bookmarkedNoteIds.contains(route.noteId),
                            rating = viewModel.getRatingForNote(route.noteId, ratingsMap),
                            summaryState = summaryUi,
                            onBack = { navController.popBackStack() },
                            onToggleBookmark = { viewModel.toggleNoteBookmark(route.noteId) },
                            onShareNoteText = shareNoteTextAction,
                            onRateNote = { score -> viewModel.rateNote(route.noteId, score) },
                            onGenerateSummary = { fileId ->
                                note?.let { viewModel.generateSummary(it, fileId) }
                            },
                            onRequireLogin = { navController.navigate(AppRoute.Login) },
                            onDownloadFile = { file -> viewModel.saveFileToDownloads(file) },
                            onShareFile = shareFileAction,
                            onOpenExternalFile = openExternalAction
                        )
                    }

                    composable<AppRoute.Papers> {
                        QuestionPapersScreen(
                            papers = filteredPapers,
                            filter = papersFilter,
                            bookmarkedIds = bookmarkedPaperIds,
                            onQueryChange = { q -> viewModel.updatePapersFilter { it.copy(query = q) } },
                            onBranchChange = { b -> viewModel.updatePapersFilter { it.copy(branch = b) } },
                            onSemesterChange = { s -> viewModel.updatePapersFilter { it.copy(semester = s) } },
                            onExamTypeChange = { e -> viewModel.updatePapersFilter { it.copy(examType = e) } },
                            onToggleSavedOnly = { viewModel.updatePapersFilter { it.copy(savedOnly = !it.savedOnly) } },
                            onResetFilters = { viewModel.resetPapersFilter() },
                            onToggleBookmark = { id -> viewModel.togglePaperBookmark(id) },
                            onDownloadFile = { file -> viewModel.saveFileToDownloads(file) },
                            onShareFile = shareFileAction,
                            onOpenExternalFile = openExternalAction,
                            onUploadPaperClick = {
                                if (currentUser != null) {
                                    navController.navigate(AppRoute.UploadPaper)
                                } else {
                                    navController.navigate(AppRoute.Login)
                                }
                            }
                        )
                    }

                    composable<AppRoute.Dashboard> { entry ->
                        val route = entry.toRoute<AppRoute.Dashboard>()
                        DashboardScreen(
                            currentUser = currentUser,
                            initialTab = route.initialTab,
                            stats = dashboardStats,
                            myNotes = myNotes,
                            myPapers = myPapers,
                            bookmarkedNotes = bookmarkedNotes,
                            bookmarkedPapers = bookmarkedPapers,
                            onNavigateLogin = { navController.navigate(AppRoute.Login) },
                            onNavigateRegister = { navController.navigate(AppRoute.Register) },
                            onNavigateUploadNote = { navController.navigate(AppRoute.UploadNote) },
                            onNavigateUploadPaper = { navController.navigate(AppRoute.UploadPaper) },
                            onNavigateAdmin = { navController.navigate(AppRoute.Admin) },
                            onNavigateBrowseNotes = { navigateTopLevel(AppRoute.Notes) },
                            onNavigateBrowsePapers = { navigateTopLevel(AppRoute.Papers) },
                            onOpenNote = { noteId -> navController.navigate(AppRoute.NoteDetail(noteId)) },
                            onDeleteNote = { noteId -> viewModel.deleteNote(noteId) },
                            onDeletePaper = { paperId -> viewModel.deletePaper(paperId) },
                            onToggleNoteBookmark = { noteId -> viewModel.toggleNoteBookmark(noteId) },
                            onTogglePaperBookmark = { paperId -> viewModel.togglePaperBookmark(paperId) },
                            onClearBookmarkedNotes = { viewModel.clearBookmarks("NOTE") },
                            onClearBookmarkedPapers = { viewModel.clearBookmarks("PAPER") },
                            onLogout = { viewModel.logout() }
                        )
                    }

                    composable<AppRoute.UploadNote> {
                        if (currentUser == null) {
                            LaunchedEffect(Unit) {
                                navController.navigate(AppRoute.Login) {
                                    popUpTo<AppRoute.UploadNote> { inclusive = true }
                                }
                            }
                        } else {
                            UploadNoteScreen(
                                onBack = { navController.popBackStack() },
                                onSubmit = { title, subject, branch, semester, description, uris, onDone ->
                                    viewModel.uploadNote(title, subject, branch, semester, description, uris) { res ->
                                        onDone(res.map { it.id })
                                    }
                                },
                                onSuccess = { noteId ->
                                    navController.navigate(AppRoute.NoteDetail(noteId)) {
                                        popUpTo<AppRoute.UploadNote> { inclusive = true }
                                    }
                                }
                            )
                        }
                    }

                    composable<AppRoute.UploadPaper> {
                        if (currentUser == null) {
                            LaunchedEffect(Unit) {
                                navController.navigate(AppRoute.Login) {
                                    popUpTo<AppRoute.UploadPaper> { inclusive = true }
                                }
                            }
                        } else {
                            UploadPaperScreen(
                                onBack = { navController.popBackStack() },
                                onSubmit = { title, subject, branch, semester, year, examType, description, uris, onDone ->
                                    viewModel.uploadQuestionPaper(
                                        title, subject, branch, semester, year, examType, description, uris
                                    ) { res ->
                                        onDone(res.map { Unit })
                                    }
                                },
                                onSuccess = {
                                    navigateTopLevel(AppRoute.Papers)
                                }
                            )
                        }
                    }

                    composable<AppRoute.Login> {
                        LoginScreen(
                            onBack = { navController.popBackStack() },
                            onLogin = { email, password, onDone ->
                                viewModel.login(email, password) { res ->
                                    onDone(res.map { Unit })
                                }
                            },
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onNavigateRegister = {
                                navController.navigate(AppRoute.Register) {
                                    popUpTo<AppRoute.Login> { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<AppRoute.Register> {
                        RegisterScreen(
                            onBack = { navController.popBackStack() },
                            onRegister = { name, email, password, branch, semester, onDone ->
                                viewModel.register(name, email, password, branch, semester) { res ->
                                    onDone(res.map { Unit })
                                }
                            },
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onNavigateLogin = {
                                navController.navigate(AppRoute.Login) {
                                    popUpTo<AppRoute.Register> { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<AppRoute.Admin> {
                        if (currentUser?.isAdmin != true) {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        } else {
                            AdminScreen(
                                currentUser = currentUser,
                                stats = adminStats,
                                users = adminUsers,
                                onBack = { navController.popBackStack() },
                                onToggleUserStatus = { userId -> viewModel.toggleUserStatus(userId) }
                            )
                        }
                    }
                }
            }
        }
    }
}
