package com.ix.imagex.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.Glide

object ImageUtils {

    var imageByte: ByteArray? = null

    fun uriToByte(context: Context, uri: Uri) {
        try {
            val iis = context.contentResolver.openInputStream(uri)
            imageByte = iis?.readBytes()
            iis?.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun write(rate: Double, quality: Int, format: String, out: Uri, context: Context) {
        val sb = Glide.with(context)
            .asBitmap()
            .load(imageByte)
            .submit()
            .get()
        val result = Glide.with(context)
            .asBitmap()
            .load(imageByte)
            .override((sb.width * rate).toInt(), (sb.height * rate).toInt())
            .submit()
            .get()
        val ios = context.contentResolver.openOutputStream(out)
        when(format){
            "JPEG" -> {
                result.compress(Bitmap.CompressFormat.JPEG, quality * 10, ios)
            }
            "PNG" -> {
                result.compress(Bitmap.CompressFormat.PNG, 100, ios)
            }
        }
        ios?.flush()
        ios?.close()
    }
}