package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ActivityUtils
import com.chuanglan.shanyan_sdk.OneKeyLoginManager
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.RegisterFileBean
import com.sdy.jitangapplication.presenter.OneKeyLoginPresenter
import com.sdy.jitangapplication.presenter.view.OneKeyLoginView
import com.sdy.jitangapplication.utils.ConfigUtils
import com.sdy.jitangapplication.utils.UserManager
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import java.lang.ref.WeakReference


//(判断用户是否登录过，如果登录过，就直接跳主页面，否则就进入登录页面)
class OnekeyLoginActivity : BaseMvpActivity<OneKeyLoginPresenter>(), OneKeyLoginView {

    private var syCode = 0

    companion object {
        public var weakrefrece: WeakReference<OnekeyLoginActivity>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_onekey_login)
        syCode = intent.getIntExtra("syCode", 0)
//        BarUtils.setStatusBarLightMode(this,true)
//        ScreenUtils.setFullScreen(this)
        initView()
    }

    private fun initView() {
        weakrefrece = WeakReference(this)
        mPresenter = OneKeyLoginPresenter()
        mPresenter.context = this
        mPresenter.mView = this
        //判断是否有登录
        if (UserManager.getToken().isNotEmpty()) {//token不为空说明登录过
            if (UserManager.isUserInfoMade()) {//是否填写过用户信息
                startActivity<MainActivity>()
                finish()
            } else {
                UserManager.clearLoginData()
                //                startActivity<SetInfoActivity>()
            }
        }

        OneKeyLoginManager.getInstance().setAuthThemeConfig(ConfigUtils.getCJSConfig(applicationContext))
        openLoginActivity()
    }

    private fun openLoginActivity() {
        //拉起授权页方法
        OneKeyLoginManager.getInstance().openLoginAuth(false,
            //getOpenLoginAuthStatus
            { code, result ->
                when (code) {
                    1000 -> {
                        //拉起授权页成功
                        Log.e(
                            "VVV",
                            "拉起授权页成功： _code==$code   _result==$result"
                        )
                    }
                    else -> {
                        //拉起授权页失败
                        startActivity<PhoneActivity>("type" to "1")
                        Log.e(
                            "VVV",
                            "拉起授权页失败： _code==$code   _result==$result"
                        )
                    }

                }

            },
            //OneKeyLoginListener
            { code, result ->
                when (code) {
                    1011 -> {//点击返回，用户取消免密登录
                        OneKeyLoginManager.getInstance().finishAuthActivity()
                        OneKeyLoginManager.getInstance().removeAllListener()
                        finish()
                    }
                    1000 -> {//一键登录成功，解析result，可得到网络请求参数
                        Log.e("VVV", "用户点击登录获取token成功： _code==$code   _result==$result")
                        mPresenter.checkVerifyCode(
                            JSONObject(result).optString("token"),
                            VerifyCodeActivity.TYPE_LOGIN_SY
                        )

                    }
                    else -> {
                        Log.e("VVV", "用户点击登录获取token失败： _code==$code   _result==$result")
                    }
                }

            })


    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private var data: LoginBean? = null
    override fun onConfirmVerifyCode(data: LoginBean?, b: Boolean) {
        if (b) {
            this.data = data
            mPresenter.loginIM(LoginInfo(data!!.accid, data!!.extra_data?.im_token))
        } else {
            OneKeyLoginManager.getInstance().setLoadingVisibility(false)
        }
    }


    override fun onIMLoginResult(nothing: LoginInfo?, success: Boolean) {
        if (success) {
            UserManager.startToPersonalInfoActivity(this, nothing, data)
            OneKeyLoginManager.getInstance().setLoadingVisibility(false)
            OneKeyLoginManager.getInstance().finishAuthActivity()
            OneKeyLoginManager.getInstance().removeAllListener()
            finish()
        } else {
            CommonFunction.toast(resources.getString(R.string.login_error))
            OneKeyLoginManager.getInstance().setLoadingVisibility(false)
        }
    }

}
