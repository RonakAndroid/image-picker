package com.chintansoni.imagepicker.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.AsyncTask
import android.text.TextUtils
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException


class BitmapDecodeAsyncTask(private val func: (Bitmap?) -> Unit) : AsyncTask<File, Void, Bitmap>() {
    override fun doInBackground(vararg files: File): Bitmap? {
        return try {
            val bitmap = BitmapFactory.decodeFile(files[0].absolutePath)
            val degree = getOrientationDegree(files[0].absolutePath)
            return rotateBitmap(bitmap, degree)
        } catch (exception: Exception) {
            null
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        func.invoke(result)
    }

    private fun getOrientationDegree(imagePath: String): Int {
        if (TextUtils.isEmpty(imagePath)) {
            return 0
        }

        try {
            val exifInterface = ExifInterface(imagePath)
            val attributeInt = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            when (attributeInt) {
                ExifInterface.ORIENTATION_ROTATE_90 -> return 90
                ExifInterface.ORIENTATION_ROTATE_180 -> return 180
                ExifInterface.ORIENTATION_ROTATE_270 -> return 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return 0
    }

    private fun rotateBitmap(bitmap: Bitmap?, degree: Int): Bitmap? {
        if (degree == 0 || bitmap == null) {
            return bitmap
        }

        val matrix = Matrix()
        matrix.setRotate(
            degree.toFloat(),
            (bitmap.width / 2).toFloat(),
            (bitmap.height / 2).toFloat()
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}