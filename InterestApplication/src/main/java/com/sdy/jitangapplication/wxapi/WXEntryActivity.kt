package com.sdy.jitangapplication.wxapi

import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.common.AppManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.nim.sp.UserPreferences
import com.sdy.jitangapplication.ui.activity.LabelsActivity
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.activity.PhoneActivity
import com.sdy.jitangapplication.ui.activity.SetInfoActivity
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager
import com.tencent.mm.opensdk.constants.ConstantsAPI.COMMAND_SENDAUTH
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.umeng.socialize.weixin.view.WXCallbackActivity
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

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
        RetrofitFactory.instance.create(Api::class.java)
            .loginOWithWechat(scene = "3", wxcode = code)
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
            SPUtils.getInstance(Constants.SPNAME).put("imToken", nothing?.token)
            SPUtils.getInstance(Constants.SPNAME).put("imAccid", nothing?.account)

            SPUtils.getInstance(Constants.SPNAME).put("qntoken", data?.qntk)
            SPUtils.getInstance(Constants.SPNAME).put("token", data?.token)
            SPUtils.getInstance(Constants.SPNAME).put("accid", data?.accid)
            DemoCache.setAccount(nothing?.account)
            //初始化消息提醒配置
            initNotificationConfig()


            if (data == null
                || (data != null && data!!.userinfo == null)
                || (data != null && data!!.userinfo != null
                        && (data!!.userinfo!!.nickname.isNullOrEmpty()
                        || data!!.userinfo!!.avatar.isNullOrEmpty()
                        || data!!.userinfo!!.gender == 0
                        || data!!.userinfo!!.birth == 0L))
            ) {//个人信息没有填写
                startActivity<SetInfoActivity>()
            } else {
                UserManager.saveUserInfo(data!!)
                if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels") == null || SPUtils.getInstance(
                        Constants.SPNAME
                    ).getStringSet("checkedLabels").isEmpty()
                ) {//标签没有选择
                    startActivity<LabelsActivity>()
                } else {//跳到主页
                    AppManager.instance.finishAllActivity()
                    startActivity<MainActivity>()
                }
            }
        } else {
            toast("登录失败！请重试")
        }
    }

    private fun initNotificationConfig() {
        // 初始化消息提醒
        NIMClient.toggleNotification(UserPreferences.getNotificationToggle())
        // 加载状态栏配置
        var statusBarNotificationConfig = UserPreferences.getStatusConfig()
        if (statusBarNotificationConfig == null) {
            statusBarNotificationConfig = DemoCache.getNotificationConfig()
            UserPreferences.setStatusConfig(statusBarNotificationConfig)
        }
        //更新配置
        NIMClient.updateStatusBarNotificationConfig(statusBarNotificationConfig)

    }

}
