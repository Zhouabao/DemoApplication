package com.sdy.jitangapplication.utils

import android.content.Context
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.sdy.jitangapplication.event.UpdateRoamingLocationEvent
import com.sdy.jitangapplication.ui.activity.RoamingLocationActivity
import org.greenrobot.eventbus.EventBus

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