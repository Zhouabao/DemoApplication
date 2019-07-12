package com.example.demoapplication.utils

import android.content.Context
import android.provider.MediaStore

/**
 *    author : ZFM
 *    date   : 2019/7/129:32
 *    desc   :
 *    version: 1.0
 */
object UriUtils {

     fun getDuration(path: String, context: Context): Int {
        var duration = 0
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val columns = arrayOf(
            MediaStore.Audio.Media._ID, // 歌曲ID
            MediaStore.Audio.Media.DURATION
        )// 歌曲的总播放时长
        val mResolver = context.contentResolver
        val selection = MediaStore.Audio.Media.DATA + "=?"
        val selectionArgs = arrayOf(path)
        val cursor = mResolver.query(contentUri, null, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
            cursor.close()
        }

        return duration
    }
}