package com.sdy.jitangapplication.ui.activity

import android.net.Uri
import android.os.Bundle
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.RegisterFileBean
import com.sdy.jitangapplication.presenter.LoginPresenter
import com.sdy.jitangapplication.presenter.view.LoginView
import com.sdy.jitangapplication.utils.StatusBarUtil
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.wxapi.WXEntryActivity
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity

//(判断用户是否登录过，如果登录过，就直接跳主页面，否则就进入登录页面)
class LoginActivity : BaseMvpActivity<LoginPresenter>(), LoginView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
//        BarUtils.setStatusBarLightMode(this,true)
//        ScreenUtils.setFullScreen(this)
        initView()
        showVideoPreview()
        mPresenter.getRegisterProcessType()
    }

    private fun initView() {
        mPresenter = LoginPresenter()
        mPresenter.context = this
        mPresenter.mView = this


        StatusBarUtil.immersive(this)

        userAgreement.text = SpanUtils.with(userAgreement).append("积糖用户协议").setUnderline().create()
        privacyPolicy.text = SpanUtils.with(privacyPolicy).append("隐私协议").setUnderline().create()


        //判断是否有登录
        //移除老用户的兴趣
        if (!SPUtils.getInstance(Constants.SPNAME).getStringSet("newCheckedLabels").isNullOrEmpty())
            SPUtils.getInstance(Constants.SPNAME).remove("newCheckedLabels")

        if (UserManager.getToken().isNotEmpty()) {//token不为空说明登录过
            if (UserManager.isUserInfoMade()) {//是否填写过用户信息
                startActivity<MainActivity>()
                finish()
            } else {
                UserManager.clearLoginData()
                //                startActivity<SetInfoActivity>()
            }
        }


        //手机登录
        phoneLoginBtn.clickWithTrigger {
            startActivity<PhoneActivity>()
        }

        //微信登录
        wechatLoginBtn.clickWithTrigger {
            CommonFunction.wechatLogin(this, WXEntryActivity.WECHAT_LOGIN)
        }

        //隐私协议
        privacyPolicy.clickWithTrigger {
            startActivity<ProtocolActivity>("type" to ProtocolActivity.TYPE_PRIVACY_PROTOCOL)
        }
        //用户协议
        userAgreement.clickWithTrigger {
            startActivity<ProtocolActivity>("type" to ProtocolActivity.TYPE_USER_PROTOCOL)
        }
    }


    private fun showVideoPreview() {
//        videoPreview.setMediaController(MediaController(this))
        videoPreview.setVideoURI(Uri.parse("android.resource://com.sdy.jitangapplication/${R.raw.login_video}"))
        videoPreview.setOnCompletionListener {
            videoPreview.start()
        }

        videoPreview.start()
    }

    override fun onPause() {
        super.onPause()
//        videoPreview.pause()
    }

    override fun onResume() {
        super.onResume()
        if (!videoPreview.isPlaying)
            videoPreview.start()
//        videoPreview.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        UMShareAPI.get(this).release()
        videoPreview.stopPlayback()
    }

    override fun onGetRegisterProcessType(data: RegisterFileBean?) {
        if (data != null) {
            //todo 切换配置
            data!!.threshold = false
            data!!.supplement = 1
            UserManager.registerFileBean = data
        }

    }
}
