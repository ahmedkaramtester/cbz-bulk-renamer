package com.example.cbzbulkrenamer.util

import android.net.Uri
import java.io.File

/**
 * Applies the rename rule: removes everything from the first underscore to the extension.
 *
 * Examples:
 * "chapter 210_6d22c8.cbz" -> "chapter 210.cbz"
 * "chapter 1_ab12cd.cbz" -> "chapter 1.cbz"
 * "volume 5.2_xyz789.cbz" -> "volume 5.2.cbz"
 */
fun String.applyRenameRule(): String {
    val underscoreIndex = this.indexOf('_')
    if (underscoreIndex == -1) {
        // No underscore, return as is
        return this
    }

    // Get the part before the underscore
    val beforeUnderscore = this.substring(0, underscoreIndex)

    // Get the extension
    val extensionIndex = this.lastIndexOf('.')
    val extension = if (extensionIndex != -1) this.substring(extensionIndex) else ""

    // Combine
    return beforeUnderscore + extension
}

/**
 * Extracts filename from a full path.
 */
fun String.getFileName(): String {
    return this.substringAfterLast('/')
}

/**
 * Gets the name without extension.
 */
fun String.getNameWithoutExtension(): String {
    return this.substringBeforeLast('.')
}

/**
 * Gets the file extension including the dot.
 */
fun String.getExtension(): String {
    return this.substringAfterLast('.').let {
        if (it.isEmpty()) "" else "." + it
    }
}

/**
 * Checks if this is a CBZ file.
 */
fun String.isCbzFile(): Boolean {
    return this.lowercase().endsWith(".cbz")
}
