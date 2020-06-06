package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SpanUtils
import com.chuanglan.shanyan_sdk.OneKeyLoginManager
import com.chuanglan.shanyan_sdk.view.CmccLoginActivity
import com.chuanglan.shanyan_sdk.view.ShanYanOneKeyActivity
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.RegisterFileBean
import com.sdy.jitangapplication.presenter.LoginPresenter
import com.sdy.jitangapplication.presenter.view.LoginView
import com.sdy.jitangapplication.utils.AbScreenUtils
import com.sdy.jitangapplication.utils.ConfigUtils
import com.sdy.jitangapplication.utils.ForebackUtils
import com.sdy.jitangapplication.utils.UserManager
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import java.lang.ref.WeakReference


//(判断用户是否登录过，如果登录过，就直接跳主页面，否则就进入登录页面)
class LoginActivity : BaseMvpActivity<LoginPresenter>(), LoginView {
    companion object {
        public var weakrefrece: WeakReference<LoginActivity>? = null
    }

    private val listener by lazy {
        object : ForebackUtils.Listener {
            override fun onApplicationEnterForeground(activity: Activity?) {
                if (activity is LoginActivity || activity is ShanYanOneKeyActivity || activity is CmccLoginActivity) {
                    AbScreenUtils.hideBottomUIMenu(activity)
                }
            }

            override fun onApplicationEnterBackground(activity: Activity?) {
                if (activity is LoginActivity || activity is ShanYanOneKeyActivity || activity is CmccLoginActivity)
                    AbScreenUtils.hideBottomUIMenu(activity)
            }

        }
    }

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
        weakrefrece = WeakReference(this)

        mPresenter = LoginPresenter()
        mPresenter.context = this
        mPresenter.mView = this
        setSwipeBackEnable(false)

        ForebackUtils.init(application.applicationContext as Application)
        ForebackUtils.unregisterListener(listener)
        ForebackUtils.registerListener(listener)
        AbScreenUtils.hideBottomUIMenu(this)

        loginCl.isVisible = !isOpenAuth

//        StatusBarUtil.immersive(this)

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


        //加入积糖
        onekeyLoginBtn.clickWithTrigger(1000L) {
            touristBtn.isEnabled = false
            if (!NetworkUtils.getMobileDataEnabled()) {
                CommonFunction.toast("请开启数据流量")
                return@clickWithTrigger
            }

            OneKeyLoginManager.getInstance()
                .setAuthThemeConfig(ConfigUtils.getCJSConfig(applicationContext))
            openLoginActivity()
            // CommonFunction.wechatLogin(this, WXEntryActivity.WECHAT_LOGIN)
        }

        //隐私协议
        privacyPolicy.clickWithTrigger {
            startActivity<ProtocolActivity>("type" to ProtocolActivity.TYPE_PRIVACY_PROTOCOL)
        }
        //用户协议
        userAgreement.clickWithTrigger {
            startActivity<ProtocolActivity>("type" to ProtocolActivity.TYPE_USER_PROTOCOL)
        }

        //游客
        touristBtn.clickWithTrigger {
            onekeyLoginBtn.isEnabled = false
            startActivity<MainActivity>()
            UserManager.touristMode = true
            touristBtn.postDelayed({
                onekeyLoginBtn.isEnabled = true
            }, 1000L)

        }


    }

    private var isOpenAuth = false
    private fun openLoginActivity() {
        //拉起授权页方法
        OneKeyLoginManager.getInstance().openLoginAuth(false,
            //getOpenLoginAuthStatus
            { code, result ->
                when (code) {
                    1000 -> {
                        isOpenAuth = true
                        val animation =
                            AnimationUtils.loadAnimation(this, R.anim.shanyan_dmeo_fade_out_anim)
                        loginCl.startAnimation(animation)
                        loginCl.isVisible = false
                        //拉起授权页成功
                        //拉起授权页成功
                        Log.e(
                            "VVV",
                            "拉起授权页成功： _code==$code   _result==$result"
                        )
                    }
                    else -> {
                        //拉起授权页失败
                        //拉起授权页失败
                        Log.e(
                            "VVV",
                            "拉起授权页失败： _code==$code   _result==$result"
                        )
                    }

                }

            },
            //OneKeyLoginListener
            { code, result ->
                touristBtn.isEnabled = true
                when (code) {
                    1011 -> {//点击返回，用户取消免密登录
                        val animation = AnimationUtils.loadAnimation(
                            this,
                            R.anim.shanyan_demo_fade_in_anim
                        )
                        loginCl.startAnimation(animation)
                        loginCl.isVisible = true

                        isOpenAuth = false

                    }
                    1000 -> {//一键登录成功，解析result，可得到网络请求参数
                        Log.e("VVV", "用户点击登录获取token成功： _code==$code   _result==$result")
                        mPresenter.checkVerifyCode(
                            JSONObject(result).optString("token"),
                            "${VerifyCodeActivity.TYPE_LOGIN_SY}"
                        )

                    }
                    else -> {
                        Log.e("VVV", "用户点击登录获取token失败： _code==$code   _result==$result")
                    }
                }

            })


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

    override fun onRestart() {
        super.onRestart()
        showVideoPreview()

    }

    override fun onResume() {
        super.onResume()
        if (!videoPreview.isPlaying)
            videoPreview.start()
        AbScreenUtils.hideBottomUIMenu(this)
//        videoPreview.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        UMShareAPI.get(this).release()
        videoPreview.stopPlayback()
        ForebackUtils.unregisterListener(listener)

    }

    override fun onGetRegisterProcessType(data: RegisterFileBean?) {
        if (data != null) {
            //todo 切换配置
//            data!!.threshold = false
//            data!!.supplement = 1
            UserManager.registerFileBean = data
            touristBtn.isVisible = data?.tourists == true
        }

    }

    private var data: LoginBean? = null
    override fun onConfirmVerifyCode(data: LoginBean?, b: Boolean) {
        if (b) {
            this.data = data
            mPresenter.loginIM(LoginInfo(data!!.accid, data!!.extra_data?.im_token))
        } else {
            isOpenAuth = false
            OneKeyLoginManager.getInstance().setLoadingVisibility(false)
        }
    }


    override fun onIMLoginResult(nothing: LoginInfo?, success: Boolean) {
        if (success) {
            UserManager.startToPersonalInfoActivity(this, nothing, data)
            //todo 跳转结果页面
            OneKeyLoginManager.getInstance().finishAuthActivity()
            OneKeyLoginManager.getInstance().removeAllListener()
            isOpenAuth = true
        } else {
            CommonFunction.toast("登录失败！请重试")
            isOpenAuth = false
            OneKeyLoginManager.getInstance().setLoadingVisibility(false)
        }
    }


}
