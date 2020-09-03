package com.sdy.baselibrary.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/**
 *    author : ZFM
 *    date   : 2020/8/2116:12
 *    desc   :
 *    version: 1.0
 */
object ChannelUtils {
    fun getChannel(context: Context): String {
        try {
            val pm: PackageManager = context.packageManager
            val appInfo: ApplicationInfo =
                pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            return appInfo.metaData.getString("UMENG_CHANNEL") ?: ""
        } catch (ignored: PackageManager.NameNotFoundException) {
            return "debug"
        }
    }
}