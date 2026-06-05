package com.example.cbzbulkrenamer.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Utility class for Storage Access Framework (SAF) operations.
 */
object StorageUtils {

    /**
     * Gets a DocumentFile from a URI.
     */
    fun getDocumentFile(context: Context, uri: Uri): DocumentFile? {
        return try {
            DocumentFile.fromTreeUri(context, uri)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if a URI is still accessible.
     */
    fun isUriAccessible(context: Context, uri: Uri): Boolean {
        return try {
            val docFile = DocumentFile.fromTreeUri(context, uri)
            docFile?.exists() == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Persists a URI permission grant.
     */
    fun persistUriPermission(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: Exception) {
            // Permission may already be granted
        }
    }

    /**
     * Releases a URI permission.
     */
    fun releaseUriPermission(context: Context, uri: Uri) {
        try {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: Exception) {
            // Ignore
        }
    }
}
