package com.sdy.jitangapplication.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.presenter.LoginPresenter
import com.sdy.jitangapplication.presenter.view.LoginView
import com.sdy.jitangapplication.utils.AMapManager
import kotlinx.android.synthetic.main.dialog_permissions.view.*
import org.jetbrains.anko.startActivity

/**
 * 启动页面
 */
class SplashActivity : BaseMvpActivity<LoginPresenter>(), LoginView {

    private val handler = Handler()

    private val dialog: AlertDialog by lazy {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_permissions, null, false)
        view.allowPermissionBtn.onClick {
            requestPermissions()
        }
        AlertDialog.Builder(this).setView(view).setCancelable(false).create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = LoginPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        mPresenter.checkNickName()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
            (!PermissionUtils.isGranted(PermissionConstants.LOCATION) ||
                    !PermissionUtils.isGranted(PermissionConstants.PHONE) ||
                    !PermissionUtils.isGranted(PermissionConstants.STORAGE)
                    )
        ) {
            //定位权限
            PermissionUtils.permission(PermissionConstants.STORAGE) //内存卡
                .callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        if (PermissionUtils.isGranted(PermissionConstants.PHONE)) {//手機狀態
                            if (PermissionUtils.isGranted(PermissionConstants.LOCATION)) {//定位
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
                                        if (PermissionUtils.isGranted(PermissionConstants.LOCATION)) {
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
                        if (PermissionUtils.isGranted(PermissionConstants.PHONE)) {//手機狀態
                            if (PermissionUtils.isGranted(PermissionConstants.LOCATION)) {//定位
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
                                        if (PermissionUtils.isGranted(PermissionConstants.LOCATION)) {
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
        handler.postDelayed({
            startActivity<LoginActivity>()
            finish()
        }, 2000)
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
            Manifest.permission.ACCESS_COARSE_LOCATION
//            ,
//            Manifest.permission.READ_PHONE_STATE
        )
        ActivityCompat.requestPermissions(this, permissions, 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
                dialog.dismiss()
                start2login()
            }
        }

    }

    private fun showAlertDialog() {
        dialog.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
