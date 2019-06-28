package com.example.demoapplication.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_welcome.*
import org.jetbrains.anko.startActivity

/**
 * 欢迎页
 */

//todo(判断用户是否登录过，如果登录过，就直接跳主页面，否则就进入登录页面)
class WelcomeActivity : BaseActivity() {
    private val rxPermissions by lazy { RxPermissions(this) }

    private val dialog: AlertDialog by lazy {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_permissions, null, false)
        (view.findViewById(R.id.allowPermissionBtn) as Button).onClick {
            requestForPermissions()
        }
        AlertDialog.Builder(this).setView(view).setCancelable(false).create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        //动态申请权限
        if (!SPUtils.getInstance(Constants.SPNAME).getBoolean("autoPermissions")) {
            showAlertDialog()
        }

        //手机登录
        phoneLoginBtn.onClick {
            startActivity<LoginActivity>()
        }
        // QQ登录
        qqLoginBtn.onClick {

        }
        //微信登录
        wechatLoginBtn.onClick {

        }

    }

    /*
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.CAMERA"/> <!-- 用于进行网络定位 -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/> <!-- 用于访问GPS定位 -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/> <!-- 用于获取运营商信息，用于支持提供运营商信息相关的接口 -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/> <!-- 用于访问wifi网络信息，wifi信息会用于进行网络定位 -->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/> <!-- 用于获取wifi的获取权限，wifi信息会用来进行网络定位 -->
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/> <!-- 用于访问网络，网络定位需要上网 -->
    <uses-permission android:name="android.permission.INTERNET"/> <!-- 用于读取手机当前的状态 -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/> <!-- 用于写入缓存数据到扩展存储卡 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/> <!-- 用于申请调用A-GPS模块 -->
    <uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS"/> <!-- 用于申请获取蓝牙信息进行室内定位 -->
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>

    * */
    private fun requestForPermissions() {
        rxPermissions
            .request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe {
                if (!it) AppUtils.exitApp()
                else {
                    dialog.dismiss()
                    SPUtils.getInstance(Constants.SPNAME).put("autoPermissions", true)
                }
            }
    }


    private fun showAlertDialog() {
        dialog.show()
    }
}
