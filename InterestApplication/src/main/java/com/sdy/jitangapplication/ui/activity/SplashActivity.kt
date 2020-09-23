package com.sdy.jitangapplication.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.chuanglan.shanyan_sdk.OneKeyLoginManager
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.utils.AMapManager
import com.sina.weibo.sdk.share.BaseActivity
import org.jetbrains.anko.startActivity

/**
 * 启动页面
 */
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        StatusBarUtil.immersive(this)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            BarUtils.setStatusBarVisibility(this, false)
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
            (!PermissionUtils.isGranted(*PermissionConstants.getPermissions(PermissionConstants.LOCATION)) ||
                    !PermissionUtils.isGranted(
                        *PermissionConstants.getPermissions(
                            PermissionConstants.PHONE
                        )
                    ) ||
                    !PermissionUtils.isGranted(
                        *PermissionConstants.getPermissions(
                            PermissionConstants.STORAGE
                        )
                    )
                    )
        ) {
            //定位权限
            PermissionUtils.permission(PermissionConstants.STORAGE) //内存卡
                .callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        if (PermissionUtils.isGranted(
                                *PermissionConstants.getPermissions(
                                    PermissionConstants.PHONE
                                )
                            )
                        ) {//手機狀態
                            if (PermissionUtils.isGranted(
                                    *PermissionConstants.getPermissions(
                                        PermissionConstants.LOCATION
                                    )
                                )
                            ) {//定位
                                AMapManager.initLocation(this@SplashActivity)
                                start2login()
                            } else {
                                //请求phone_state权限
                                PermissionUtils.permission(
                                    PermissionConstants.LOCATION
                                )
                                    .callback(object : PermissionUtils.SimpleCallback {
                                        override fun onGranted() {
                                            AMapManager.initLocation(this@SplashActivity)
                                            start2login()
                                        }

                                        override fun onDenied() {
                                            AMapManager.initLocation(this@SplashActivity)
                                            start2login()
                                        }
                                    }).request()
                            }
                        } else {
                            //请求phone_state权限
                            PermissionUtils.permission(PermissionConstants.PHONE)
                                .callback(object : PermissionUtils.SimpleCallback {
                                    override fun onGranted() {
                                        if (PermissionUtils.isGranted(
                                                *PermissionConstants.getPermissions(
                                                    PermissionConstants.LOCATION
                                                )
                                            )
                                        ) {
                                            AMapManager.initLocation(this@SplashActivity)
                                            start2login()
                                        } else {
                                            //请求phone_state权限
                                            PermissionUtils.permission(PermissionConstants.LOCATION)
                                                .callback(object : PermissionUtils.SimpleCallback {
                                                    override fun onGranted() {
                                                        AMapManager.initLocation(this@SplashActivity)
                                                        start2login()
                                                    }

                                                    override fun onDenied() {
                                                        AMapManager.initLocation(this@SplashActivity)
                                                        start2login()
                                                    }
                                                }).request()
                                        }
                                    }

                                    override fun onDenied() {
                                        AMapManager.initLocation(this@SplashActivity)
                                        start2login()
                                    }
                                }).request()
                        }

                    }

                    override fun onDenied() {
                        if (PermissionUtils.isGranted(
                                *PermissionConstants.getPermissions(
                                    PermissionConstants.PHONE
                                )
                            )
                        ) {//手機狀態
                            if (PermissionUtils.isGranted(
                                    *PermissionConstants.getPermissions(
                                        PermissionConstants.LOCATION
                                    )
                                )
                            ) {//定位
                                AMapManager.initLocation(this@SplashActivity)
                                start2login()
                            } else {
                                //请求phone_state权限
                                PermissionUtils.permission(PermissionConstants.LOCATION)
                                    .callback(object : PermissionUtils.SimpleCallback {
                                        override fun onGranted() {
                                            AMapManager.initLocation(this@SplashActivity)
                                            start2login()
                                        }

                                        override fun onDenied() {
                                            AMapManager.initLocation(this@SplashActivity)
                                            start2login()
                                        }
                                    }).request()
                            }
                        } else {
                            //请求phone_state权限
                            PermissionUtils.permission(PermissionConstants.PHONE)
                                .callback(object : PermissionUtils.SimpleCallback {
                                    override fun onGranted() {
                                        if (PermissionUtils.isGranted(
                                                *PermissionConstants.getPermissions(
                                                    PermissionConstants.LOCATION
                                                )
                                            )
                                        ) {
                                            AMapManager.initLocation(this@SplashActivity)
                                            start2login()
                                        } else {
                                            //请求phone_state权限
                                            PermissionUtils.permission(PermissionConstants.LOCATION)
                                                .callback(object : PermissionUtils.SimpleCallback {
                                                    override fun onGranted() {
                                                        AMapManager.initLocation(this@SplashActivity)
                                                        start2login()
                                                    }

                                                    override fun onDenied() {
                                                        AMapManager.initLocation(this@SplashActivity)
                                                        start2login()
                                                    }
                                                }).request()
                                        }
                                    }

                                    override fun onDenied() {
                                        AMapManager.initLocation(this@SplashActivity)
                                        start2login()
                                    }
                                }).request()
                        }
                    }
                })
                .request()
        } else {
            AMapManager.initLocation(this)
            start2login()
        }


    }

    private fun start2login() {
        //闪验SDK预取号
        OneKeyLoginManager.getInstance().getPhoneInfo { p0, p1 ->
            startActivity<LoginActivity>("syCode" to p0)
            finish()
        }
    }


    /**
     * 动态申请权限，不给权限就直接退出APP
     */
    private fun requestPermissions() {
        val permissions = arrayOf(
//            Manifest.permission.GET_ACCOUNTS,
//            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_CONTACTS,
//            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
        ActivityCompat.requestPermissions(this, permissions, 100)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100) {


            if (grantResults.isNotEmpty()) {
                for (i in 0 until grantResults.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED && Build.VERSION.SDK_INT >= 23) {
                        AppUtils.exitApp()
                        break
                    }
                }
                //获取权限成功进行定位
                AMapManager.initLocation(this)
                SPUtils.getInstance(Constants.SPNAME).put("autoPermissions", true)
                start2login()
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
    }
}
