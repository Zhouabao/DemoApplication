package com.sdy.jitangapplication.utils

import android.content.Context
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils

/**
 *    author : ZFM
 *    date   : 2019/7/239:27
 *    desc   :
 *    version: 1.0
 */
object AMapManager {
    //设置定位
    fun initLocation(context: Context) {
        //定位权限
        PermissionUtils.permissionGroup(PermissionConstants.LOCATION)
            .callback(object : PermissionUtils.SimpleCallback {
                override fun onGranted() {

//                    initLocationClient(context)
//                    startLocation()
                    LocationUtil().requestLocationUpdate(context)

//                    initGoogleMap(context)
                }

                override fun onDenied() {
                }
            })
            .request()
    }

}