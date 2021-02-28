package org.nunocky.imagesavesample

import android.app.Application
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import java.io.File
import java.io.FileOutputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    /**
     * アセットからファイルを読み込む
     */
    fun loadAsset(assetName: String): ByteArray {
        val inputStream = context.assets.open(assetName)
        return inputStream.readBytes()
    }

    /**
     * 画像をギャラリーに追加する
     *
     * @param filename ファイル名
     * @param imageBytes 保存する画像のバイト列
     *
     */
    fun addImageToGallery(filename: String, mimeType: String, imageBytes: ByteArray) {
        val contentResolver = context.contentResolver

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            val picturesDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val outputFile = File(picturesDirectory, filename)

            FileOutputStream(outputFile).use { outputStream ->
                outputStream.write(imageBytes)
            }

        } else {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.IS_PENDING, 1) // IS_PENDINGを 1に
            }

            val collection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val item = contentResolver.insert(collection, values)!!

            contentResolver.openFileDescriptor(item, "w", null).use {
                FileOutputStream(it!!.fileDescriptor).use { outputStream ->
                    outputStream.write(imageBytes)
                }
            }

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0) // IS_PENDINGを 0に
            contentResolver.update(item, values, null, null)
        }

    }
}