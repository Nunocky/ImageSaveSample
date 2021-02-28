package org.nunocky.imagesavesample

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class ImageUtil {
    companion object {
        private const val TAG = "ImageUtil"

        /**
         * アセットからファイルを読み込む
         *
         * @param context Activityもしくは Application
         * @param assetName ファイル名
         *
         * @return 成功時ファイル全体のバイト列。失敗時 null
         */
        @JvmStatic
        fun loadAsset(
            context: Context?,
            assetName: String
        ): ByteArray? {
            context ?: run {
                Log.d(TAG, "context == null")
                return null
            }

            val inputStream = context.assets.open(assetName)
            return inputStream.readBytes()
        }

        /**
         * 画像をギャラリーに追加する
         *
         * @param context Activityもしくは Application
         * @param filename ファイル名
         * @param imageBytes 保存する画像のバイト列
         *
         */
        @JvmStatic
        fun addToGallery(
            context: Context?,
            filename: String,
            mimeType: String,
            imageBytes: ByteArray
        ) {
            val contentResolver = context?.contentResolver ?: run {
                Log.d(TAG, "context == null")
                return
            }

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
                    put(MediaStore.Images.Media.IS_PENDING, 1)
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
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(item, values, null, null)
            }
        }
    }
}