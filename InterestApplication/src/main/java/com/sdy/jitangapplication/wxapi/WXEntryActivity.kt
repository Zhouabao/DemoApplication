package com.sdy.jitangapplication.wxapi

import android.os.Bundle
import android.util.Log
import com.ishumei.smantifraud.SmAntiFraud
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.ui.activity.PhoneActivity
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager
import com.tencent.mm.opensdk.constants.ConstantsAPI.COMMAND_SENDAUTH
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.umeng.socialize.weixin.view.WXCallbackActivity
import org.jetbrains.anko.startActivity

/**
 * 微信回调界面
 */
class WXEntryActivity : WXCallbackActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wxentry)
    }


    override fun onResp(resp: BaseResp) {
        if (resp.type == COMMAND_SENDAUTH) {
            if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                val code = (resp as SendAuth.Resp).code
                Log.d("OkHttp", code)
                loginWithWechat(code)
            } else {
                finish()
            }
        } else {
            super.onResp(resp)//一定要加super，实现我们的方法，否则不能回调
        }
    }


    /**
     * 微信登录
     */
    private fun loginWithWechat(code: String) {
        val params = hashMapOf<String, Any>("type" to "3", "wxcode" to code, "device_id" to SmAntiFraud.getDeviceId())
        RetrofitFactory.instance.create(Api::class.java)
            .loginOWithWechat(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<com.kotlin.base.data.protocol.BaseResp<LoginBean?>>(null) {
                override fun onStart() {
                    loading.show()
                }

                override fun onNext(t: com.kotlin.base.data.protocol.BaseResp<LoginBean?>) {
                    if (t.code == 202) {
                        startActivity<PhoneActivity>("wxcode" to code, "type" to "3")
                        finish()
                    } else if (t.code == 200) {
                        data = t.data
                        loginIM(LoginInfo(data?.accid, data?.extra_data?.im_token))
                    } else if (t.code == 400) {
                        CommonFunction.toast(t.msg)
                    }
                    loading.dismiss()
                }

                override fun onError(e: Throwable?) {
                    loading.dismiss()
                    finish()
                }
            })

    }

    private val loading by lazy { LoadingDialog(this) }

    private var data: LoginBean? = null

    /**
     * 登录IM
     */
    fun loginIM(info: LoginInfo) {
        val callback = object : RequestCallback<LoginInfo> {
            override fun onSuccess(param: LoginInfo) {
                onIMLoginResult(param, true)
            }

            override fun onFailed(code: Int) {
                Log.d("OkHttp", "=====$code")
                onIMLoginResult(null, false)
            }

            override fun onException(exception: Throwable?) {
                Log.d("OkHttp", exception.toString())
            }

        }
        NimUIKit.login(info, callback)

    }


    /**
     * IM登录
     */
    fun onIMLoginResult(nothing: LoginInfo?, success: Boolean) {
        if (success) {
            UserManager.startToPersonalInfoActivity(this, nothing, data)
        } else {
            CommonFunction.toast("登录失败！请重试")
        }
    }


}
