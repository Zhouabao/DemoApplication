package com.example.demoapplication.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.utils.AMapManager
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.dialog_permissions.view.*
import org.jetbrains.anko.startActivity

/**
 * 欢迎页
 */

//todo(判断用户是否登录过，如果登录过，就直接跳主页面，否则就进入登录页面)
class WelcomeActivity : BaseActivity() {
    private val dialog: AlertDialog by lazy {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_permissions, null, false)
        view.allowPermissionBtn.onClick {
            requestPermissions()
        }
        AlertDialog.Builder(this).setView(view).setCancelable(false).create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        ScreenUtils.setFullScreen(this)

        //动态申请权限
        if (!SPUtils.getInstance(Constants.SPNAME).getBoolean("autoPermissions", false)) {
            showAlertDialog()
        } else {
            //进行定位
            AMapManager.initLocation(this)
        }

        //判断是否有登录
        if (UserManager.getToken().isNotEmpty()) {//token不为空说明登录过
            if (UserManager.isUserInfoMade()) {//是否填写过用户信息
                if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels").isEmpty()) {//是否选择过标签
                    startActivity<LabelsActivity>()
                } else {
                    startActivity<MainActivity>()
                }
            } else {
                startActivity<SetInfoActivity>()
            }
            finish()
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


    /**
     * 动态申请权限，不给权限就直接退出APP
     */
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
        ActivityCompat.requestPermissions(this, permissions, 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100) {
            if (grantResults.isNotEmpty()) {
                for (i in 1 until grantResults.size) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        AppUtils.exitApp()
                        break
                    }
                }
                //获取权限成功进行定位
                AMapManager.initLocation(this)
                SPUtils.getInstance(Constants.SPNAME).put("autoPermissions", true)
                dialog.dismiss()
            }
        }

    }

    private fun showAlertDialog() {
        dialog.show()
    }
}
