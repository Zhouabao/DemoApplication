package com.sdy.jitangapplication.utils

import com.qiniu.android.common.FixedZone
import com.qiniu.android.storage.Configuration
import com.qiniu.android.storage.UploadManager


/**
 *    author : ZFM
 *    date   : 2019/7/411:48
 *    desc   :七牛文件上传工具类
 *    version: 1.0
 */
object QNUploadManager {
    private fun getConfiguration(): Configuration {
        return Configuration.Builder()
            .chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
            .putThreshold(1024 * 1024)      // 启用分片上传阀值。默认512K
            .connectTimeout(10)           // 链接超时。默认10秒
            .useHttps(true)               // 是否使用https上传域名
            .responseTimeout(60)          // 服务器响应超时。默认60秒
            .zone(FixedZone.zone0)        // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
            .build()
    }

    fun getInstance(): UploadManager {
        return UploadManager(getConfiguration())
    }

}