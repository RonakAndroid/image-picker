package com.chintansoni.imagepicker.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    private const val FOLDER_CAMERA_PICS = "cameraPics"
    private const val PROVIDER = ".imagepicker.fileprovider"

    @Throws(IOException::class)
    internal fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        return File.createTempFile("PNG_${timeStamp}_", ".png", getFolder(context))
    }

    fun getUri(context: Context, file: File): Uri = FileProvider.getUriForFile(
        context,
        context.packageName + PROVIDER,
        file
    )

    private fun getFolder(context: Context): File {
        val storageDir = File(
            context.cacheDir,
            FOLDER_CAMERA_PICS
        )
        storageDir.mkdirs()
        return storageDir
    }
}

fun File.copyInputStreamToFile(inputStream: InputStream) {
    inputStream.use { input ->
        this.outputStream().use { fileOut ->
            input.copyTo(fileOut)
        }
    }
}