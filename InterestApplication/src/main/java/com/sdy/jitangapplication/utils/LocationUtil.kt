package com.sdy.jitangapplication.utils

import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.io.IOException
import java.util.*

/**
 *    author : ZFM
 *    date   : 2021/1/1610:08
 *    desc   :
 *    version: 1.0
 */
class LocationUtil {
    private val mLocationRequest: LocationRequest
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    var myLocationCallback: MyLocationCallback? = null

    constructor() {
        mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //高精度定位
//        mLocationRequest.interval = 2000 //2秒 定位一次 定位间隔
//        mLocationRequest.fastestInterval = 2000 //最快的定位间隔
    }

    private val mLocationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                if (p0 != null) {
                    myLocationCallback?.locationSuccess(p0.lastLocation)
                    UserManager.saveLocation(
                        "${p0.lastLocation.latitude}",
                        "${p0.lastLocation.longitude}"
                    )
                    //逆地址编码 通过经纬度去获取地址信息
                    Thread {
                        try {
                            val addresses = Geocoder(
                                ActivityUtils.getTopActivity(),
                                Locale.getDefault()
                            ).getFromLocation(
                                p0.lastLocation.latitude,
                                p0.lastLocation.longitude,
                                1
                            )

                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                LogUtils.d(address)
                                UserManager.saveLocation(
                                    "${address.latitude}",
                                    "${address.longitude}",
                                    address.adminArea,
                                    address.locality,
                                    address.thoroughfare,
                                    address.countryCode
                                )
                            }
                        } catch (e: IOException) {
                            LogUtils.e(e.message)
                        } catch (illegalArgumentException: IllegalArgumentException) {
                            LogUtils.e(illegalArgumentException.message)
                        }
                    }.start()

//                    val intent = Intent(
//                        ActivityUtils.getTopActivity(),
//                        FetchAddressIntentService::class.java
//                    )
//                    intent.putExtra(Constants.RECEIVER, receiver)
//                    intent.putExtra(Constants.LOCATION_DATA_EXTRA, p0!!.lastLocation)
//                    ActivityUtils.getTopActivity().startService(intent)
                    stopLocation()
                } else {
                    myLocationCallback?.locationFailure()
                }

                LogUtils.e("定位结果：po = ${p0}")
            }
        }
    }


    /**
     * 开始定位
     */
    fun requestLocationUpdate(context: Context) {
        if (mFusedLocationClient == null) {
            if (context is Activity) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            } else {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            }
        }


        // 使用位置请求创建LocationSettingsRequest对象
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
        // 检查是否满足位置设置
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                LogUtils.e("可以定位-----------------------------")
                startLocationUpdates()

            }
            .addOnCompleteListener {
                LogUtils.e("条件检测完成-----------------------------${it.isSuccessful}")
            }
            .addOnFailureListener { e ->
                LogUtils.e("不可定位---------------------------- ${e.message}")
                if (e is ApiException) {
                    when (e.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            LogUtils.e("不满足定位要求，请求设置")
                            try {
                                // 通过调用startResolutionForResult（）显示对话框，然后检查 产生onActivityResult（）。
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(context as Activity, 789)
                            } catch (sie: Exception) {
                                LogUtils.e("PendingIntent无法执行请求。")
                            }
                        }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            LogUtils.e("无法更改设置")
                        }
                    }
                }
            }
    }

    private fun startLocationUpdates() {
        mFusedLocationClient?.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    fun stopLocation() {
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }
}

interface MyLocationCallback {
    fun locationFailure()
    fun locationSuccess(location: Location)
}