package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.ToastUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.utils.UserManager
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity

/**
 * 欢迎页
 */

//todo(判断用户是否登录过，如果登录过，就直接跳主页面，否则就进入登录页面)
class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ScreenUtils.setFullScreen(this)

        userAgreement.text = SpanUtils.with(userAgreement).append("积糖用户协议").setUnderline().create()
        privacyPolicy.text = SpanUtils.with(privacyPolicy).append("隐私协议").setUnderline().create()


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
            startActivity<PhoneActivity>()
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



    override fun onDestroy() {
        super.onDestroy()
        UMShareAPI.get(this).release()
    }
}
