package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.chuanglan.shanyan_sdk.OneKeyLoginManager
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.dialog.PrivacyDialog
import com.sdy.jitangapplication.utils.AMapManager
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_privacy.*
import org.jetbrains.anko.startActivity

/**
 * 启动页面
 */
class SplashActivity : BaseActivity() {

    private val privacyDialog by lazy { PrivacyDialog(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        StatusBarUtil.immersive(this)


        if (!UserManager.getAlertProtocol()) {
            privacyDialog.show()
            privacyDialog.agree.clickWithTrigger {
                UserManager.saveAlertProtocol(true)
                privacyDialog.dismiss()
                initPermissionsAndlogin()
            }
        } else {
            initPermissionsAndlogin()
        }


    }

    fun initPermissionsAndlogin() {
        PermissionUtils.permissionGroup(
            PermissionConstants.STORAGE,
            PermissionConstants.LOCATION,
            PermissionConstants.PHONE
        )
            .callback { isAllGranted, granted, deniedForever, denied -> start2login() }
            .request()
    }


    private fun start2login() {
        initUmeng()
        AMapManager.initLocation(this@SplashActivity)

        //闪验SDK预取号
        OneKeyLoginManager.getInstance().getPhoneInfo { p0, p1 ->
            contentView.postDelayed({
                startActivity<LoginActivity>("syCode" to p0)
                finish()
            }, 1000L)
        }


    }


    //初始化Umeng
    private fun initUmeng() {
//        if (ThreadUtils.isMainThread()) {
//            if (UserManager.getAlertProtocol())
//                CommonFunction.initUMeng(this)
//            else
//                UMConfigure.preInit(this, Constants.UMENG_APPKEY, ChannelUtils.getChannel(this))
//
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
