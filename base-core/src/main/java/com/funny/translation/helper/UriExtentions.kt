package com.funny.translation.helper

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

private const val TAG = "UriExtensions"

@Throws(IOException::class)
fun Uri.readText(ctx: Context): String {
    val stringBuilder = StringBuilder()
    ctx.contentResolver.openInputStream(this).use { inputStream ->
        BufferedReader(
            InputStreamReader(inputStream)
        ).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
                stringBuilder.append("\n")
            }
        }
    }
    return stringBuilder.toString()
}

@Throws(IOException::class)
fun Uri.writeText(context: Context, text: String) {
    val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(this, "w")
    Log.d(TAG, "writeText: $text")
    if (pfd != null) {
        val fileOutputStream = FileOutputStream(pfd.fileDescriptor)
        fileOutputStream.write(text.encodeToByteArray())
        fileOutputStream.close()
        pfd.close()
    }
}

