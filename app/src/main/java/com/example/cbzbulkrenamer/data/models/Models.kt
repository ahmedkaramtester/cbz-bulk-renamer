package com.example.cbzbulkrenamer.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a preview row showing original and proposed new filename.
 */
data class FilePreview(
    val originalName: String,
    val newName: String,
    val isConflict: Boolean = false,
    val documentUri: String = ""
)

/**
 * Represents a single rename operation result.
 */
data class RenameResult(
    val originalName: String,
    val newName: String,
    val success: Boolean,
    val error: String? = null,
    val documentUri: String = ""
)

/**
 * Summary of a rename operation.
 */
data class OperationSummary(
    val totalFiles: Int,
    val successCount: Int,
    val failureCount: Int,
    val skippedCount: Int,
    val results: List<RenameResult>
)

/**
 * Stores undo state for the last operation.
 */
@Entity(tableName = "undo_state")
data class UndoState(
    @PrimaryKey
    val id: Int = 1,
    val folderUri: String,
    val renamedFiles: String, // JSON serialized
    val timestamp: Long,
    val operationSummary: String // JSON serialized
)

/**
 * Represents a renamed file that can be reverted.
 */
data class RenamedFile(
    val documentUri: String,
    val originalName: String,
    val renamedName: String
)

/**
 * Represents progress of a rename operation.
 */
data class ProgressUpdate(
    val currentIndex: Int,
    val totalFiles: Int,
    val currentFile: String,
    val successCount: Int,
    val failureCount: Int,
    val skippedCount: Int
)
