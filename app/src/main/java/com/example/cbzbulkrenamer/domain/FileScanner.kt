package com.example.cbzbulkrenamer.domain

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.cbzbulkrenamer.data.models.FilePreview
import com.example.cbzbulkrenamer.util.applyRenameRule
import com.example.cbzbulkrenamer.util.isCbzFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Scans a directory for CBZ files and generates preview data.
 */
class FileScanner(private val context: Context) {

    /**
     * Scans a folder URI and returns a list of CBZ file previews.
     */
    suspend fun scanFolder(folderUri: Uri): Result<List<FilePreview>> = withContext(Dispatchers.IO) {
        try {
            val documentFile = DocumentFile.fromTreeUri(context, folderUri)
            if (documentFile == null || !documentFile.exists()) {
                return@withContext Result.failure(Exception("Folder not accessible"))
            }

            val previews = mutableListOf<FilePreview>()

            documentFile.listFiles().forEach { file ->
                if (file.isFile && file.name?.isCbzFile() == true) {
                    val originalName = file.name ?: return@forEach
                    val newName = originalName.applyRenameRule()

                    previews.add(
                        FilePreview(
                            originalName = originalName,
                            newName = newName,
                            documentUri = file.uri.toString()
                        )
                    )
                }
            }

            // Sort by original name for better readability
            previews.sortBy { it.originalName }
            Result.success(previews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
