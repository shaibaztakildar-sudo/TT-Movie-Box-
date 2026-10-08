package com.example.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

sealed class UploadState {
    object Idle : UploadState()
    data class Progress(val percent: Int, val bytesCopied: Long, val totalBytes: Long, val fileName: String) : UploadState()
    data class Success(val fileUri: String, val fileName: String, val sizeDisplay: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

data class StorageStats(
    val totalVideosSizeMb: Double,
    val totalImagesSizeMb: Double,
    val totalSubtitlesSizeMb: Double,
    val totalStorageUsedMb: Double,
    val availableDiskSpaceMb: Double,
    val totalFilesCount: Int
)

class MediaStorageManager(private val context: Context) {

    private val baseDir: File = File(context.filesDir, "media_vault").apply {
        if (!exists()) mkdirs()
    }

    private val videosDir = File(baseDir, "videos").apply { if (!exists()) mkdirs() }
    private val imagesDir = File(baseDir, "images").apply { if (!exists()) mkdirs() }
    private val subtitlesDir = File(baseDir, "subtitles").apply { if (!exists()) mkdirs() }

    enum class MediaType {
        VIDEO,
        IMAGE,
        SUBTITLE
    }

    suspend fun saveMediaFromUri(
        uri: Uri,
        mediaType: MediaType,
        onProgress: (UploadState) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val originalName = queryFileName(uri) ?: "media_${System.currentTimeMillis()}"
            val totalSize = queryFileSize(uri)

            // Validate format based on mediaType
            val extension = originalName.substringAfterLast('.', "").lowercase()
            when (mediaType) {
                MediaType.VIDEO -> {
                    val validVideoExts = listOf("mp4", "mkv", "webm", "mov", "avi", "3gp", "ts", "m4v")
                    val mime = contentResolver.getType(uri) ?: ""
                    if (!mime.startsWith("video/") && extension.isNotEmpty() && extension !in validVideoExts) {
                        val err = "Invalid video format ($extension). Please select MP4, MKV, WEBM or MOV."
                        onProgress(UploadState.Error(err))
                        return@withContext Result.failure(IllegalArgumentException(err))
                    }
                }
                MediaType.IMAGE -> {
                    val validImageExts = listOf("jpg", "jpeg", "png", "webp", "gif")
                    val mime = contentResolver.getType(uri) ?: ""
                    if (!mime.startsWith("image/") && extension.isNotEmpty() && extension !in validImageExts) {
                        val err = "Invalid image format ($extension). Please select JPG, PNG, or WEBP."
                        onProgress(UploadState.Error(err))
                        return@withContext Result.failure(IllegalArgumentException(err))
                    }
                }
                MediaType.SUBTITLE -> {
                    val validSubExts = listOf("srt", "vtt", "txt", "sub")
                    if (extension.isNotEmpty() && extension !in validSubExts) {
                        val err = "Invalid subtitle format ($extension). Please select SRT or VTT."
                        onProgress(UploadState.Error(err))
                        return@withContext Result.failure(IllegalArgumentException(err))
                    }
                }
            }

            val targetDir = when (mediaType) {
                MediaType.VIDEO -> videosDir
                MediaType.IMAGE -> imagesDir
                MediaType.SUBTITLE -> subtitlesDir
            }

            val cleanName = originalName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(targetDir, "${System.currentTimeMillis()}_$cleanName")

            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                val err = "Unable to open input stream for selected file"
                onProgress(UploadState.Error(err))
                return@withContext Result.failure(IllegalStateException(err))
            }

            val outputStream = FileOutputStream(targetFile)
            val buffer = ByteArray(64 * 1024) // 64 KB buffer
            var bytesCopied: Long = 0
            var bytesRead: Int

            onProgress(UploadState.Progress(0, 0, totalSize, originalName))

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesCopied += bytesRead
                        if (totalSize > 0) {
                            val percent = ((bytesCopied * 100) / totalSize).toInt().coerceIn(0, 100)
                            onProgress(UploadState.Progress(percent, bytesCopied, totalSize, originalName))
                        }
                    }
                }
            }

            val fileUri = Uri.fromFile(targetFile).toString()
            val sizeMb = bytesCopied.toDouble() / (1024 * 1024)
            val df = DecimalFormat("#.##")
            val sizeDisplay = "${df.format(sizeMb)} MB"

            onProgress(UploadState.Success(fileUri, originalName, sizeDisplay))
            Result.success(fileUri)
        } catch (e: Exception) {
            val err = "Upload failed: ${e.localizedMessage ?: "Unknown storage error"}"
            onProgress(UploadState.Error(err))
            Result.failure(e)
        }
    }

    private fun queryFileName(uri: Uri): String? {
        if (uri.scheme == "file") return uri.lastPathSegment
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return it.getString(nameIndex)
                }
            }
        }
        return uri.lastPathSegment
    }

    private fun queryFileSize(uri: Uri): Long {
        if (uri.scheme == "file") {
            val file = uri.path?.let { File(it) }
            return file?.length() ?: 0L
        }
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    return it.getLong(sizeIndex)
                }
            }
        }
        return 0L
    }

    fun getStorageStats(): StorageStats {
        val videosBytes = videosDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        val imagesBytes = imagesDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        val subtitlesBytes = subtitlesDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

        val totalUsedBytes = videosBytes + imagesBytes + subtitlesBytes
        val fileCount = baseDir.walkTopDown().count { it.isFile }
        val usableBytes = baseDir.usableSpace

        fun toMb(bytes: Long): Double = bytes.toDouble() / (1024 * 1024)

        return StorageStats(
            totalVideosSizeMb = toMb(videosBytes),
            totalImagesSizeMb = toMb(imagesBytes),
            totalSubtitlesSizeMb = toMb(subtitlesBytes),
            totalStorageUsedMb = toMb(totalUsedBytes),
            availableDiskSpaceMb = toMb(usableBytes),
            totalFilesCount = fileCount
        )
    }

    fun deleteMediaFile(fileUri: String): Boolean {
        return try {
            val uri = Uri.parse(fileUri)
            if (uri.scheme == "file") {
                val file = File(uri.path ?: return false)
                if (file.exists() && file.canonicalPath.startsWith(baseDir.canonicalPath)) {
                    file.delete()
                } else false
            } else false
        } catch (_: Exception) {
            false
        }
    }
}
