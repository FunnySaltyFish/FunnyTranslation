package com.funny.translation.helper

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
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
fun Uri.writeText(context: Context, text: String) {
    // 加上这一行之后保存不会多保存一段?
    // 我不理解啊tmd
    // 20220121 然并卵，尝试加上锁约束
//    val path = UriUtils.getFileAbsolutePath(context, this)
//    Log.d(TAG, "writeText: path:$path")

    val pfd: AssetFileDescriptor? = context.contentResolver.openAssetFileDescriptor(this, "w")

//    Log.d(TAG, "writeText: $text")
    if (pfd != null) {
        synchronized(pfd) {
            val fileWriter = FileWriter(pfd.fileDescriptor)
            fileWriter.write(text)
            try {
                fileWriter.close()
                pfd.close()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

    }
//    FileUtils.
}

