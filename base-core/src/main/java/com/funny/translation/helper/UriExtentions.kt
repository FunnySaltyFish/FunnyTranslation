package com.funny.translation.helper

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.net.Uri
import com.funny.translation.core.R
import java.io.*

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
fun Uri.readByteArray(ctx: Context): ByteArray {
    var byteArray: ByteArray
    ctx.contentResolver.openInputStream(this).use { inputStream ->
        byteArray = inputStream?.readBytes() ?: byteArrayOf()
    }
    return byteArray
}

@Throws(IOException::class)
fun Uri.writeText(context: Context, text: String) {
    val pfd: AssetFileDescriptor? = context.contentResolver.openAssetFileDescriptor(this, "w")

    if (pfd != null) {
        val fileWriter = FileWriter(pfd.fileDescriptor)
        fileWriter.write(text)
        try {
            fileWriter.close()
            pfd.close()
        }catch (e:Exception) {
            context.toastOnUi(string(R.string.err_write_text))
            e.printStackTrace()
        }
    }
}

