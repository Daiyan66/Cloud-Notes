package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AcademicConstants
import com.example.data.model.ExamType
import com.example.ui.components.formatFileSize
import java.util.Calendar

private data class PickedFileDisplay(
    val uri: Uri,
    val name: String,
    val size: Long
)

private fun resolveUriDisplay(context: Context, uri: Uri): PickedFileDisplay {
    var name = uri.lastPathSegment ?: "document"
    var size = 0L
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nIdx >= 0) cursor.getString(nIdx)?.let { name = it }
            if (sIdx >= 0) size = cursor.getLong(sIdx)
        }
    }
    return PickedFileDisplay(uri, name, size)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadNoteScreen(
    onBack: () -> Unit,
    onSubmit: (
        title: String,
        subject: String,
        branch: String,
        semester: Int,
        description: String,
        fileUris: List<Uri>,
        onDone: (Result<String>) -> Unit
    ) -> Unit,
    onSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf(AcademicConstants.BRANCHES.first()) }
    var semester by remember { mutableIntStateOf(1) }
    var description by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf<List<PickedFileDisplay>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var uploading by remember { mutableStateOf(false) }

    var branchExpanded by remember { mutableStateOf(false) }
    var semesterExpanded by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        if (selectedFiles.size + uris.size > AcademicConstants.MAX_FILES_PER_UPLOAD) {
            error = "You can upload a maximum of ${AcademicConstants.MAX_FILES_PER_UPLOAD} files per note."
            return@rememberLauncherForActivityResult
        }
        val mapped = uris.map { resolveUriDisplay(context, it) }
        val oversized = mapped.firstOrNull { it.size > AcademicConstants.MAX_FILE_SIZE_BYTES }
        if (oversized != null) {
            error = "File \"${oversized.name}\" exceeds the 10MB limit."
            return@rememberLauncherForActivityResult
        }
        error = null
        selectedFiles = selectedFiles + mapped
    }

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
                .widthIn(max = 680.dp)
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Upload Study Notes", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Share clean, high quality course notes. Supported formats: PDF, JPG, PNG, DOC, DOCX, TXT (Max 10 files, 10MB each).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    error?.let { msg ->
                        ErrorBanner(msg)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Note Title *") },
                        placeholder = { Text("e.g. Operating Systems Chapter 3: Memory Management") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_note_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        placeholder = { Text("e.g. Operating Systems") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_note_subject_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = branchExpanded,
                            onExpandedChange = { branchExpanded = !branchExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = branch,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Branch") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = branchExpanded,
                                onDismissRequest = { branchExpanded = false }
                            ) {
                                AcademicConstants.BRANCHES.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            branch = option
                                            branchExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = semesterExpanded,
                            onExpandedChange = { semesterExpanded = !semesterExpanded },
                            modifier = Modifier.weight(0.85f)
                        ) {
                            OutlinedTextField(
                                value = "Semester $semester",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Semester") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = semesterExpanded,
                                onDismissRequest = { semesterExpanded = false }
                            ) {
                                AcademicConstants.SEMESTERS.forEach { sem ->
                                    DropdownMenuItem(
                                        text = { Text("Semester $sem") },
                                        onClick = {
                                            semester = sem
                                            semesterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Key topics covered, reference textbooks, professors...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            filePicker.launch(
                                arrayOf(
                                    "application/pdf",
                                    "image/*",
                                    "text/plain",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_note_pick_files_button")
                    ) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach Files (${selectedFiles.size}/10)")
                    }

                    if (selectedFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            selectedFiles.forEachIndexed { idx, file ->
                                SelectedFileRow(
                                    file = file,
                                    onRemove = {
                                        selectedFiles = selectedFiles.filterIndexed { i, _ -> i != idx }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            uploading = true
                            error = null
                            onSubmit(
                                title,
                                subject,
                                branch,
                                semester,
                                description,
                                selectedFiles.map { it.uri }
                            ) { res ->
                                uploading = false
                                res.onSuccess { noteId -> onSuccess(noteId) }
                                    .onFailure { err -> error = err.message ?: "Failed to upload note." }
                            }
                        },
                        enabled = !uploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_note_submit_button")
                    ) {
                        if (uploading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading Files...")
                        } else {
                            Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish Note")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPaperScreen(
    onBack: () -> Unit,
    onSubmit: (
        title: String,
        subject: String,
        branch: String,
        semester: Int,
        year: Int,
        examType: ExamType,
        description: String,
        fileUris: List<Uri>,
        onDone: (Result<Unit>) -> Unit
    ) -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf(AcademicConstants.BRANCHES.first()) }
    var semester by remember { mutableIntStateOf(1) }
    var yearText by remember { mutableStateOf(currentYear.toString()) }
    var examType by remember { mutableStateOf(ExamType.END_SEMESTER) }
    var description by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf<List<PickedFileDisplay>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var uploading by remember { mutableStateOf(false) }

    var branchExpanded by remember { mutableStateOf(false) }
    var semesterExpanded by remember { mutableStateOf(false) }
    var examTypeExpanded by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        if (selectedFiles.size + uris.size > AcademicConstants.MAX_FILES_PER_UPLOAD) {
            error = "You can upload a maximum of ${AcademicConstants.MAX_FILES_PER_UPLOAD} files per question paper."
            return@rememberLauncherForActivityResult
        }
        val mapped = uris.map { resolveUriDisplay(context, it) }
        val oversized = mapped.firstOrNull { it.size > AcademicConstants.MAX_FILE_SIZE_BYTES }
        if (oversized != null) {
            error = "File \"${oversized.name}\" exceeds the 10MB limit."
            return@rememberLauncherForActivityResult
        }
        error = null
        selectedFiles = selectedFiles + mapped
    }

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
                .widthIn(max = 680.dp)
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Upload Question Paper", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Contribute previous examination question papers to assist peers in test preparation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    error?.let { msg ->
                        ErrorBanner(msg)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Paper Title *") },
                        placeholder = { Text("e.g. Data Structures & Algorithms End-Semester Dec 2024") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_paper_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        placeholder = { Text("e.g. Data Structures") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_paper_subject_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = branchExpanded,
                            onExpandedChange = { branchExpanded = !branchExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = branch,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Branch") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = branchExpanded,
                                onDismissRequest = { branchExpanded = false }
                            ) {
                                AcademicConstants.BRANCHES.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            branch = option
                                            branchExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = semesterExpanded,
                            onExpandedChange = { semesterExpanded = !semesterExpanded },
                            modifier = Modifier.weight(0.85f)
                        ) {
                            OutlinedTextField(
                                value = "Semester $semester",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Semester") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = semesterExpanded,
                                onDismissRequest = { semesterExpanded = false }
                            ) {
                                AcademicConstants.SEMESTERS.forEach { sem ->
                                    DropdownMenuItem(
                                        text = { Text("Semester $sem") },
                                        onClick = {
                                            semester = sem
                                            semesterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = yearText,
                            onValueChange = { if (it.length <= 4) yearText = it.filter(Char::isDigit) },
                            label = { Text("Exam Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(0.8f)
                        )

                        ExposedDropdownMenuBox(
                            expanded = examTypeExpanded,
                            onExpandedChange = { examTypeExpanded = !examTypeExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = examType.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Exam Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examTypeExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = examTypeExpanded,
                                onDismissRequest = { examTypeExpanded = false }
                            ) {
                                ExamType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.displayName) },
                                        onClick = {
                                            examType = type
                                            examTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Exam session, marking scheme details, syllabus pattern...") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            filePicker.launch(
                                arrayOf(
                                    "application/pdf",
                                    "image/*",
                                    "text/plain",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_paper_pick_files_button")
                    ) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach Paper Files (${selectedFiles.size}/10)")
                    }

                    if (selectedFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            selectedFiles.forEachIndexed { idx, file ->
                                SelectedFileRow(
                                    file = file,
                                    onRemove = {
                                        selectedFiles = selectedFiles.filterIndexed { i, _ -> i != idx }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val parsedYear = yearText.toIntOrNull() ?: currentYear
                            uploading = true
                            error = null
                            onSubmit(
                                title,
                                subject,
                                branch,
                                semester,
                                parsedYear,
                                examType,
                                description,
                                selectedFiles.map { it.uri }
                            ) { res ->
                                uploading = false
                                res.onSuccess { onSuccess() }
                                    .onFailure { err -> error = err.message ?: "Failed to upload question paper." }
                            }
                        },
                        enabled = !uploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_paper_submit_button")
                    ) {
                        if (uploading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading Paper...")
                        } else {
                            Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish Question Paper")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SelectedFileRow(
    file: PickedFileDisplay,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${file.name} (${formatFileSize(file.size)})",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Remove file", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}
