package com.example.cbzbulkrenamer.domain

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.cbzbulkrenamer.data.models.FilePreview
import com.example.cbzbulkrenamer.data.models.ProgressUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Handles the rename operation with collision detection and progress reporting.
 */
class RenameEngine(private val context: Context) {

    /**
     * Renames files and emits progress updates.
     */
    fun renameFiles(
        folderUri: Uri,
        previews: List<FilePreview>
    ): Flow<Result<ProgressUpdate>> = flow {
        withContext(Dispatchers.IO) {
            try {
                // Detect collisions
                val targetNames = mutableMapOf<String, MutableList<FilePreview>>()
                previews.forEach { preview ->
                    targetNames.getOrPut(preview.newName) { mutableListOf() }.add(preview)
                }

                val conflicts = targetNames.filter { it.value.size > 1 }.keys

                val documentFile = DocumentFile.fromTreeUri(context, folderUri)
                    ?: throw Exception("Folder not accessible")

                var successCount = 0
                var failureCount = 0
                var skippedCount = 0

                previews.forEachIndexed { index, preview ->
                    // Emit progress
                    emit(
                        Result.success(
                            ProgressUpdate(
                                currentIndex = index + 1,
                                totalFiles = previews.size,
                                currentFile = preview.originalName,
                                successCount = successCount,
                                failureCount = failureCount,
                                skippedCount = skippedCount
                            )
                        )
                    )

                    if (preview.newName in conflicts) {
                        // Skip files that would cause collision
                        skippedCount++
                        return@forEachIndexed
                    }

                    // Skip if name would be the same
                    if (preview.originalName == preview.newName) {
                        skippedCount++
                        return@forEachIndexed
                    }

                    // Attempt rename
                    try {
                        val fileUri = Uri.parse(preview.documentUri)
                        val fileDoc = DocumentFile.fromSingleUri(context, fileUri)
                        if (fileDoc != null && fileDoc.exists()) {
                            val renamed = fileDoc.renameTo(preview.newName)
                            if (renamed) {
                                successCount++
                            } else {
                                failureCount++
                            }
                        } else {
                            failureCount++
                        }
                    } catch (e: Exception) {
                        failureCount++
                    }
                }

                // Emit final result
                emit(
                    Result.success(
                        ProgressUpdate(
                            currentIndex = previews.size,
                            totalFiles = previews.size,
                            currentFile = "",
                            successCount = successCount,
                            failureCount = failureCount,
                            skippedCount = skippedCount
                        )
                    )
                )
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
    }
}
