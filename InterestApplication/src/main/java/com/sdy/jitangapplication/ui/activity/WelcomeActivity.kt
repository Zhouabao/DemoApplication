package com.sdy.jitangapplication.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.*
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.utils.AMapManager
import com.sdy.jitangapplication.utils.UserManager
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.umeng.socialize.UMShareAPI
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

        userAgreement.text = SpanUtils.with(userAgreement).append("积糖用户协议").setUnderline().create()
        privacyPolicy.text = SpanUtils.with(privacyPolicy).append("隐私协议").setUnderline().create()

        if (Build.VERSION.SDK_INT >= 23) {
            //动态申请权限
            if (!SPUtils.getInstance(Constants.SPNAME).getBoolean("autoPermissions", false)) {
                showAlertDialog()
            }else {
                //进行定位
                AMapManager.initLocation(this)
            }
        } else {
//            进行定位
            AMapManager.initLocation(this)
        }


        //判断是否有登录
        if (UserManager.getToken().isNotEmpty()) {//token不为空说明登录过
            if (UserManager.isUserInfoMade()) {//是否填写过用户信息
                if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels").isEmpty()) {//是否选择过标签
                    UserManager.clearLoginData()
//                    startActivity<LabelsActivity>()
                } else {
                    startActivity<MainActivity>()
                }
                finish()
            } else {
                UserManager.clearLoginData()
//                startActivity<SetInfoActivity>()
            }
        }


        //手机登录
        phoneLoginBtn.onClick {
            startActivity<LoginActivity>()
        }

        //微信登录
        wechatLoginBtn.onClick {
            wechatLogin()
        }

        //隐私协议
        privacyPolicy.onClick {
            startActivity<ProtocolActivity>("type" to 1)
        }
        //用户协议
        userAgreement.onClick {
            startActivity<ProtocolActivity>("type" to 2)
        }

    }

    private fun wechatLogin() {
        val wxapi = WXAPIFactory.createWXAPI(this, null)
        wxapi.registerApp(Constants.WECHAT_APP_ID)
        if (!wxapi.isWXAppInstalled) {
            ToastUtils.showShort("你没有安装微信")
            return
        }
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "wechat_sdk_demo_test"
        wxapi.sendReq(req)
//        UMShareAPI.get(this).getPlatformInfo(this, SHARE_MEDIA.WEIXIN, umAuthListener)
    }


    /**
     * 动态申请权限，不给权限就直接退出APP
     */
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.GET_ACCOUNTS,
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
            }
        }

    }

    private fun showAlertDialog() {
        dialog.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        UMShareAPI.get(this).release()
    }
}
