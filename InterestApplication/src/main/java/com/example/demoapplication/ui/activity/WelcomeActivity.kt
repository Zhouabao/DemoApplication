package com.example.demoapplication.ui.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.example.demoapplication.R
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_welcome.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * 欢迎页
 */

//todo(判断用户是否登录过，如果登录过，就直接跳主页面，否则就进入登录页面)
class WelcomeActivity : BaseActivity() {

    private val dialog: AlertDialog by lazy {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_permissions, null, false)
        (view.findViewById(R.id.allowPermissionBtn) as Button).onClick {
            dialog.dismiss()
        }
        AlertDialog.Builder(this).setView(view).create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        //动态申请权限
        //requestForPermissions()
        showAlertDialog()


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

    private fun requestForPermissions() {
        PermissionUtils.permission(
            PermissionConstants.MICROPHONE,
            PermissionConstants.STORAGE,
            PermissionConstants.CAMERA,
            PermissionConstants.LOCATION
        )
            .rationale { shouldRequest ->
                //展示提示框
            }
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: MutableList<String>?) {
                    toast("同意了！")
                }

                override fun onDenied(
                    permissionsDeniedForever: MutableList<String>,
                    permissionsDenied: MutableList<String>
                ) {
                    //多次禁止权限后，跳入
                    if (permissionsDeniedForever.isNotEmpty()) {

                    }
                }

            })
            .request()
    }


    private fun showAlertDialog() {
        dialog.show()
    }
}
