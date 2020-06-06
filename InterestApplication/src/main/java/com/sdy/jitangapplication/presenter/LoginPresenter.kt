package com.sdy.jitangapplication.presenter

import android.util.Log
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.RegisterFileBean
import com.sdy.jitangapplication.presenter.view.LoginView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class LoginPresenter : BasePresenter<LoginView>() {

    /**
     * 获取登录配置开关
     */
    fun getRegisterProcessType() {
        RetrofitFactory.instance
            .create(Api::class.java)
            .getRegisterProcessType()
            .excute(object : BaseSubscriber<BaseResp<RegisterFileBean?>>(mView) {
                override fun onNext(t: BaseResp<RegisterFileBean?>) {
                    super.onNext(t)
                    mView.onGetRegisterProcessType(t.data)
                }
            })
    }





    /**
     * 对比验证码是否正确，正确即登录
     */
    fun checkVerifyCode(flash_token: String, type: String) {
        if (!checkNetWork()) {
            return
        }

        val params = hashMapOf<String, Any>(
            "flash_token" to flash_token,
            "type" to type
        )
        RetrofitFactory.instance.create(Api::class.java)
            .loginOrAlloc(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<LoginBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }
                override fun onNext(t: BaseResp<LoginBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onConfirmVerifyCode(t.data, true)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onConfirmVerifyCode(t.data, false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.onConfirmVerifyCode(null, false)
                    }

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
