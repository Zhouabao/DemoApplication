package com.sdy.jitangapplication.utils

import android.content.Context
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.blankj.utilcode.util.ToastUtils

/**
 *    author : ZFM
 *    date   : 2019/7/239:27
 *    desc   :
 *    version: 1.0
 */
object AMapManager {
    //声明amapclienr类对象
    private var mLocationClient: AMapLocationClient? = null
    private lateinit var mLocationOption: AMapLocationClientOption

    //设置定位
    public fun initLocation(context: Context) {

        //创建AMapLocationClientOption对象
        mLocationOption = AMapLocationClientOption()
        //设置定位模式为高精度模式
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        //获取近3S精度最高的一次定位结果
        mLocationOption.isOnceLocationLatest = true
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.isNeedAddress = true
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.httpTimeOut = 20000
        //关闭缓存机制
        mLocationOption.isLocationCacheEnable = true
        /**
         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
         */
        mLocationOption.locationPurpose = AMapLocationClientOption.AMapLocationPurpose.SignIn

        mLocationClient = AMapLocationClient(context)
        //获取定位结果
        mLocationClient?.setLocationListener {
            if (it != null) {
                if (it.errorCode == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    UserManager.saveLocation(
                        "${it.latitude}",
                        "${it.longitude}",
                        it.province,
                        it.city,
                        it.district,
                        it.cityCode
                    )
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    ToastUtils.showShort("${it.errorCode},${it.errorInfo}")
                }
            }
        }

        if (null != mLocationClient) {
            mLocationClient!!.setLocationOption(mLocationOption)
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient!!.stopLocation()
            mLocationClient!!.startLocation()
        }


    }
}