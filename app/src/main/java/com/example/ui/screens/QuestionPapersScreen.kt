package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AcademicConstants
import com.example.data.model.ExamType
import com.example.data.model.NoteFileItem
import com.example.data.model.QuestionPaperDto
import com.example.ui.PapersFilterState
import com.example.ui.components.AcademicBadge
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FilterDropdownChip
import com.example.ui.theme.Amber500
import com.example.ui.theme.AmberBadgeBg
import com.example.ui.theme.AmberBadgeText

@Composable
fun QuestionPapersScreen(
    papers: List<QuestionPaperDto>,
    filter: PapersFilterState,
    bookmarkedIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onBranchChange: (String) -> Unit,
    onSemesterChange: (Int?) -> Unit,
    onExamTypeChange: (ExamType?) -> Unit,
    onToggleSavedOnly: () -> Unit,
    onResetFilters: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onDownloadFile: (NoteFileItem) -> Unit,
    onShareFile: (NoteFileItem) -> Unit,
    onOpenExternalFile: (NoteFileItem) -> Unit,
    onUploadPaperClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var previewFile by remember { mutableStateOf<NoteFileItem?>(null) }

    val branchOptions = listOf("All" to "All Branches") +
        AcademicConstants.BRANCHES.map { it to it }
    val semesterOptions: List<Pair<Int?, String>> = listOf(null to "All Semesters") +
        AcademicConstants.SEMESTERS.map { it to "Semester $it" }
    val examTypeOptions: List<Pair<ExamType?, String>> = listOf(null to "All Exam Types") +
        ExamType.entries.map { it to it.displayName }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Examination Archives",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Access, study, and preview past midterm, semester-end, and supplementary question papers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = filter.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search papers by subject, year or title...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (filter.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("papers_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterDropdownChip(
                        label = "Branch",
                        selectedLabel = filter.branch,
                        isFiltered = filter.branch != "All",
                        options = branchOptions,
                        onSelect = onBranchChange,
                        testTag = "papers_branch_filter"
                    )

                    FilterDropdownChip(
                        label = "Semester",
                        selectedLabel = filter.semester?.let { "Sem $it" } ?: "All Semesters",
                        isFiltered = filter.semester != null,
                        options = semesterOptions,
                        onSelect = onSemesterChange,
                        testTag = "papers_semester_filter"
                    )

                    FilterDropdownChip(
                        label = "Exam Type",
                        selectedLabel = filter.examType?.displayName ?: "All Exam Types",
                        isFiltered = filter.examType != null,
                        options = examTypeOptions,
                        onSelect = onExamTypeChange,
                        testTag = "papers_exam_type_filter"
                    )

                    FilterChip(
                        selected = filter.savedOnly,
                        onClick = onToggleSavedOnly,
                        label = { Text("Saved") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (filter.savedOnly) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("papers_saved_filter")
                    )

                    if (filter.query.isNotBlank() || filter.branch != "All" || filter.semester != null || filter.examType != null || filter.savedOnly) {
                        TextButton(
                            onClick = onResetFilters,
                            modifier = Modifier.testTag("papers_reset_filter")
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }

            if (papers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    EmptyStateCard(
                        icon = Icons.Filled.Layers,
                        title = if (filter.savedOnly) "No saved question papers" else "No question papers found",
                        description = if (filter.savedOnly) {
                            "Tap the bookmark icon on any paper card to save it for quick revision."
                        } else {
                            "Try selecting a different branch, semester, exam type, or search query."
                        },
                        actionLabel = if (filter.savedOnly) "View All Papers" else "Reset Filters",
                        onAction = onResetFilters
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 310.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 88.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(papers, key = { it.id }) { paper ->
                        PaperCard(
                            paper = paper,
                            isSaved = bookmarkedIds.contains(paper.id),
                            isExpanded = expandedCardId == paper.id,
                            onToggleExpand = {
                                expandedCardId = if (expandedCardId == paper.id) null else paper.id
                            },
                            onToggleBookmark = { onToggleBookmark(paper.id) },
                            onPreviewFile = { previewFile = it },
                            onDownloadFile = onDownloadFile
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onUploadPaperClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("papers_upload_fab")
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Upload Question Paper")
        }
    }

    previewFile?.let { file ->
        DocumentViewerDialog(
            file = file,
            onDownload = onDownloadFile,
            onShare = onShareFile,
            onOpenExternal = onOpenExternalFile,
            onDismiss = { previewFile = null }
        )
    }
}

@Composable
private fun PaperCard(
    paper: QuestionPaperDto,
    isSaved: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleBookmark: () -> Unit,
    onPreviewFile: (NoteFileItem) -> Unit,
    onDownloadFile: (NoteFileItem) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("paper_card_${paper.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AcademicBadge(
                        text = paper.examType.displayName,
                        containerColor = AmberBadgeBg,
                        contentColor = AmberBadgeText
                    )
                    Text(
                        text = "Year ${paper.year}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("paper_bookmark_${paper.id}")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove bookmark" else "Bookmark paper",
                        tint = if (isSaved) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = paper.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${paper.subject} • ${paper.branch} (Sem ${paper.semester})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = paper.description.ifBlank { "Archive examination question paper." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${paper.files.size} document${if (paper.files.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (paper.files.size == 1) {
                    val onlyFile = paper.files.first()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onPreviewFile(onlyFile) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview")
                        }
                        Button(
                            onClick = { onDownloadFile(onlyFile) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onToggleExpand,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (isExpanded) "Hide Files" else "View & Preview")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = paper.files.size > 1 && isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paper.files.forEach { file ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = file.originalFileName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onPreviewFile(file) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Visibility,
                                        contentDescription = "Preview file",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDownloadFile(file) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Download,
                                        contentDescription = "Download file",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
