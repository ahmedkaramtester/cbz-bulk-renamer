package com.example.cbzbulkrenamer.domain

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.cbzbulkrenamer.data.models.RenamedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SerializedRenamedFile(
    val documentUri: String,
    val originalName: String,
    val renamedName: String
)

/**
 * Manages undo operations, allowing restoration of original filenames.
 */
class UndoManager(private val context: Context) {

    /**
     * Reverts a batch of renames.
     */
    suspend fun undoRenames(renamedFiles: List<RenamedFile>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var undoneCount = 0

            renamedFiles.forEach { renamedFile ->
                try {
                    val fileUri = Uri.parse(renamedFile.documentUri)
                    val fileDoc = DocumentFile.fromSingleUri(context, fileUri)
                    if (fileDoc != null && fileDoc.exists()) {
                        val renamed = fileDoc.renameTo(renamedFile.originalName)
                        if (renamed) {
                            undoneCount++
                        }
                    }
                } catch (e: Exception) {
                    // Continue with next file
                }
            }

            Result.success(undoneCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Serializes renamed files for storage.
     */
    fun serializeRenamedFiles(files: List<RenamedFile>): String {
        return try {
            val serialized = files.map {
                SerializedRenamedFile(
                    documentUri = it.documentUri,
                    originalName = it.originalName,
                    renamedName = it.renamedName
                )
            }
            Json.encodeToString(serialized)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Deserializes renamed files from storage.
     */
    fun deserializeRenamedFiles(json: String): List<RenamedFile> {
        return try {
            val decoded = Json.decodeFromString<List<SerializedRenamedFile>>(json)
            decoded.map {
                RenamedFile(
                    documentUri = it.documentUri,
                    originalName = it.originalName,
                    renamedName = it.renamedName
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
