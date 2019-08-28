package com.example.demoapplication.utils

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.demoapplication.model.MediaBean
import java.io.File

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


    //录音展示时间格式化
    fun getShowTime(countTime: Int): String {
        var result = ""
        if (countTime < 10)
            result = "00:0$countTime"
        else if (countTime < 60)
            result = "00:$countTime"
        else {
            val minute = countTime / 60
            val mod = countTime % 60
            if (minute < 10)
                result += "0$minute:"
            else {
                result += "$minute:"
            }
            if (mod < 10)
                result += "0$mod"
            else {
                result += mod
            }

        }
        return result
    }


    //录音展示时间格式化
    fun stringToTimeInt(countTime: String): Int {
        var result = countTime.split(":")
        return result[0].toInt() * 60 + result[1].toInt()
    }


    fun ms2HMS(longMills: Int): String {
        var mills = longMills
        val HMStime: String
        mills /= 1000
        val hour = mills / 3600
        val mint = mills % 3600 / 60
        val sed = mills % 60
        var hourStr = hour.toString()
        if (hour < 10) {
            hourStr = "0$hourStr"
        }
        var mintStr = mint.toString()
        if (mint < 10) {
            mintStr = "0$mintStr"
        }
        var sedStr = sed.toString()
        if (sed < 10) {
            sedStr = "0$sedStr"
        }
        HMStime = "$hourStr:$mintStr:$sedStr"
        return HMStime
    }


    /**
     * 读取手机中所有图片信息
     */
    fun getAllPhotoInfo(context: Context): MutableList<MediaBean> {
        val medias = mutableListOf<MediaBean>()
//        LoaderManager.getInstance(this).initLoader(1, null, this)
        Thread(Runnable {
            val mediaBeen = ArrayList<MediaBean>()
            val mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projImage = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DISPLAY_NAME
            )
            val mCursor = context.contentResolver.query(
                mImageUri,
                projImage,
                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                arrayOf("image/jpeg", "image/png", "image/jpg"),
                MediaStore.Images.Media.DATE_MODIFIED + " desc"
            )

            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    // 获取图片的路径
                    val id = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media._ID))
                    val path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    val size = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media.SIZE)) / 1024L
                    val displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                    //用于展示相册初始化界面
                    mediaBeen.add(MediaBean(id, MediaBean.TYPE.IMAGE, path, displayName, "", 0, size))
                }
                mCursor.close()
            }
            medias.addAll(mediaBeen)
        }).start()

        return medias
    }


    /**
     * 获取手机中所有视频的信息
     */
    fun getAllVideoInfos(context: Context): MutableList<MediaBean> {
        val medias = mutableListOf<MediaBean>()
        Thread(Runnable {
            val mediaBeen = ArrayList<MediaBean>()
            val mImageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val proj = arrayOf(
                MediaStore.Video.Thumbnails._ID,
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_MODIFIED
            )
            val mCursor = context.contentResolver.query(
                mImageUri, proj, null, null,
                //                MediaStore.Video.Media.MIME_TYPE + "=?",
                //                arrayOf("video/mp4"),
                MediaStore.Video.Media.DATE_MODIFIED + " desc"
            )
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    // 获取视频的路径
                    val videoId = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val duration = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                    var size = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Video.Media.SIZE)) / 1024 //单位kb
                    if (size < 0) {
                        //某些设备获取size<0，直接计算
                        Log.e("dml", "this video size < 0 $path")
                        size = File(path).length() / 1024
                    }
                    val displayName =
                        if (mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)) != null) {
                            mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                        } else {
                            ""
                        }

                    //提前生成缩略图，再获取：http://stackoverflow.com/questions/27903264/how-to-get-the-video-thumbnail-path-and-not-the-bitmap
                    MediaStore.Video.Thumbnails.getThumbnail(
                        context.contentResolver,
                        videoId.toLong(),
                        MediaStore.Video.Thumbnails.MICRO_KIND,
                        null
                    )
                    val projection = arrayOf(MediaStore.Video.Thumbnails._ID, MediaStore.Video.Thumbnails.DATA)
                    val cursor = context.contentResolver.query(
                        MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        projection,
                        MediaStore.Video.Thumbnails.VIDEO_ID + "=?",
                        arrayOf(videoId.toString() + ""),
                        null
                    )
                    var thumbPath = ""
                    while (cursor!!.moveToNext()) {
                        thumbPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA))
                    }
                    cursor.close()
                    if (duration > 0)
                        mediaBeen.add(
                            MediaBean(
                                videoId,
                                MediaBean.TYPE.VIDEO,
                                path,
                                displayName,
                                thumbPath,
                                duration,
                                size
                            )
                        )

                }
                mCursor.close()
            }
            medias.addAll(mediaBeen)
        }).start()

        return medias
    }


}