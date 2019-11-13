package com.sdy.jitangapplication.presenter

import android.util.Log
import com.ishumei.smantifraud.SmAntiFraud
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.presenter.view.VerifyCodeView
import com.sdy.jitangapplication.utils.UserManager

class VerifyCodePresenter : BasePresenter<VerifyCodeView>() {


    /**
     * 对比验证码是否正确，正确即登录
     */
    fun checkVerifyCode(wxcode: String, type: String, phone: String, verifyCode: String) {
        if (!checkNetWork()) {
            return
        }

        val params = hashMapOf<String, Any>(
            "uni_account" to phone,
            "type" to type,
            "password" to "",
            "code" to verifyCode,
            "wxcode" to wxcode,
            "device_id" to SmAntiFraud.getDeviceId()
        )
        RetrofitFactory.instance.create(Api::class.java)
            .loginOrAlloc(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<LoginBean>>(mView) {
                override fun onNext(t: BaseResp<LoginBean>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onConfirmVerifyCode(t.data, true)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onConfirmVerifyCode(t.data, false)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    mView.onConfirmVerifyCode(null, false)

                }
            })
    }


    /**
     * 重新获取验证码
     */
    fun getVerifyCode(mobile: String) {
        if (!checkNetWork()) {
            return
        }

        val params = hashMapOf<String, Any>(
            "phone" to mobile,
            "scene" to "register"
        )
        RetrofitFactory.instance
            .create(Api::class.java)
            .getVerifyCode(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    mView.onGetVerifyCode(t)
                }

                override fun onError(e: Throwable?) {
                    mView.onGetVerifyCode(null)
                }
            })


    }


    /**
     * 登录IM
     */
    fun loginIM(info: LoginInfo) {
        val callback = object : RequestCallback<LoginInfo> {
            override fun onSuccess(param: LoginInfo) {
                mView.onIMLoginResult(param, true)
            }

            override fun onFailed(code: Int) {
                Log.d("OkHttp", "=====$code")
                mView.onIMLoginResult(null, false)
            }

            override fun onException(exception: Throwable?) {
                Log.d("OkHttp", exception.toString())
            }

        }
        NimUIKit.login(info, callback)

    }
}