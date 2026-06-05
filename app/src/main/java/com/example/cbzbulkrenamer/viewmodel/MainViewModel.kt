package com.example.cbzbulkrenamer.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cbzbulkrenamer.data.models.FilePreview
import com.example.cbzbulkrenamer.data.models.ProgressUpdate
import com.example.cbzbulkrenamer.data.models.RenamedFile
import com.example.cbzbulkrenamer.data.repository.PreferenceRepository
import com.example.cbzbulkrenamer.domain.FileScanner
import com.example.cbzbulkrenamer.domain.RenameEngine
import com.example.cbzbulkrenamer.domain.UndoManager
import com.example.cbzbulkrenamer.util.StorageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainScreenState(
    val folderUri: Uri? = null,
    val folderName: String = "No folder selected",
    val previews: List<FilePreview> = emptyList(),
    val isScanning: Boolean = false,
    val isRenaming: Boolean = false,
    val progress: ProgressUpdate? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hasUndoData: Boolean = false,
    val canUndo: Boolean = false,
    val lastRenamedFiles: List<RenamedFile> = emptyList()
)

class MainViewModel(private val context: Context) : ViewModel() {

    private val fileScanner = FileScanner(context)
    private val renameEngine = RenameEngine(context)
    private val undoManager = UndoManager(context)
    private val preferenceRepository = PreferenceRepository(context)

    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    init {
        loadLastFolderUri()
    }

    private fun loadLastFolderUri() {
        viewModelScope.launch {
            preferenceRepository.folderUri.collect { uri ->
                if (uri != null) {
                    val folderUri = Uri.parse(uri)
                    if (StorageUtils.isUriAccessible(context, folderUri)) {
                        _state.value = _state.value.copy(
                            folderUri = folderUri,
                            folderName = getFolderName(folderUri)
                        )
                        scanFolder(folderUri)
                    }
                }
            }
        }
    }

    fun setSelectedFolder(folderUri: Uri) {
        viewModelScope.launch {
            try {
                StorageUtils.persistUriPermission(context, folderUri)
                preferenceRepository.setFolderUri(folderUri.toString())

                _state.value = _state.value.copy(
                    folderUri = folderUri,
                    folderName = getFolderName(folderUri),
                    previews = emptyList(),
                    errorMessage = null
                )

                scanFolder(folderUri)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Failed to access folder: ${e.message}"
                )
            }
        }
    }

    private fun scanFolder(folderUri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, errorMessage = null)
            try {
                val result = fileScanner.scanFolder(folderUri)
                result.onSuccess { previews ->
                    _state.value = _state.value.copy(
                        previews = previews,
                        isScanning = false
                    )
                }
                result.onFailure { error ->
                    _state.value = _state.value.copy(
                        isScanning = false,
                        errorMessage = "Failed to scan folder: ${error.message}"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    errorMessage = "Error scanning folder: ${e.message}"
                )
            }
        }
    }

    fun renameFiles() {
        val currentState = _state.value
        if (currentState.folderUri == null || currentState.previews.isEmpty()) {
            _state.value = _state.value.copy(
                errorMessage = "No files to rename"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRenaming = true,
                errorMessage = null,
                successMessage = null,
                lastRenamedFiles = emptyList()
            )

            var finalProgress: ProgressUpdate? = null
            var renamedFiles = mutableListOf<RenamedFile>()

            renameEngine.renameFiles(currentState.folderUri, currentState.previews)
                .collect { result ->
                    result.onSuccess { progress ->
                        _state.value = _state.value.copy(progress = progress)
                        finalProgress = progress
                    }
                    result.onFailure { error ->
                        _state.value = _state.value.copy(
                            errorMessage = "Rename error: ${error.message}",
                            isRenaming = false
                        )
                    }
                }

            if (finalProgress != null && finalProgress!!.successCount > 0) {
                // Collect renamed files from previews
                renamedFiles = currentState.previews
                    .filter { it.originalName != it.newName }
                    .map { RenamedFile(
                        documentUri = it.documentUri,
                        originalName = it.originalName,
                        renamedName = it.newName
                    ) }
                    .toMutableList()

                _state.value = _state.value.copy(
                    lastRenamedFiles = renamedFiles,
                    canUndo = true
                )

                // Save undo state
                try {
                    val json = undoManager.serializeRenamedFiles(renamedFiles)
                    preferenceRepository.setUndoState(json)
                } catch (e: Exception) {
                    // Ignore serialization errors
                }
            }

            _state.value = _state.value.copy(
                isRenaming = false,
                successMessage = if (finalProgress != null) {
                    "Renamed: ${finalProgress!!.successCount}, Failed: ${finalProgress!!.failureCount}, Skipped: ${finalProgress!!.skippedCount}"
                } else {
                    null
                }
            )
        }
    }

    fun undoLastOperation() {
        val renamedFiles = _state.value.lastRenamedFiles
        if (renamedFiles.isEmpty()) {
            _state.value = _state.value.copy(
                errorMessage = "No previous operation to undo"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRenaming = true,
                errorMessage = null,
                successMessage = null
            )

            val result = undoManager.undoRenames(renamedFiles)
            result.onSuccess { count ->
                _state.value = _state.value.copy(
                    isRenaming = false,
                    canUndo = false,
                    lastRenamedFiles = emptyList(),
                    successMessage = "Restored $count files to original names"
                )
                preferenceRepository.clearUndoState()
                // Rescan to show updated file list
                _state.value.folderUri?.let { scanFolder(it) }
            }
            result.onFailure { error ->
                _state.value = _state.value.copy(
                    isRenaming = false,
                    errorMessage = "Undo failed: ${error.message}"
                )
            }
        }
    }

    private fun getFolderName(uri: Uri): String {
        return try {
            val parts = uri.path?.split("/") ?: return "Selected Folder"
            parts.lastOrNull() ?: "Selected Folder"
        } catch (e: Exception) {
            "Selected Folder"
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(successMessage = null)
    }
}
