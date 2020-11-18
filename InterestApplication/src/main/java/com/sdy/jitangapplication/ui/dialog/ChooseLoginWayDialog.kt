package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.LoginActivity
import com.sdy.jitangapplication.ui.activity.OnekeyLoginActivity
import com.sdy.jitangapplication.ui.activity.PhoneActivity
import com.sdy.jitangapplication.wxapi.WXEntryActivity
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.dialog_choose_login_way.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/9/2316:45
 *    desc   : 提醒游客登录
 *    version: 1.0
 */
class ChooseLoginWayDialog(val context1: Context, val syCode: Int = 0) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_login_way)
        initWindow()
        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
//        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogBottomAnimation
//        params?.y = SizeUtils.dp2px(15F)
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }


    private fun initView() {
        //todo check language to show type
//        if (LanguageUtils.getCurrentLocale() == Locale.CHINA) {
//            loginWithPhoneBtn.isVisible = true
//            loginWithWechatBtn.isVisible = true
//            loginWithFacebookBtn.isVisible = false
//            loginWithTwitterBtn.isVisible = false
//            loginWithGoogleBtn.isVisible = false
//        } else {
//            loginWithFacebookBtn.isVisible = true
//            loginWithTwitterBtn.isVisible = true
//            loginWithGoogleBtn.isVisible = true
//            loginWithPhoneBtn.isVisible = false
//            loginWithWechatBtn.isVisible = false
//        }


        if (syCode == 1022) {
            loginWithPhoneBtn.text = "本机号码一键登录"
        } else {
            loginWithPhoneBtn.text = "手机号码"
        }
        //手机号码登录
        loginWithPhoneBtn.clickWithTrigger {
            if (syCode == 1022) {
                context1.startActivity<OnekeyLoginActivity>()
            } else {
                context1.startActivity<PhoneActivity>("type" to "1")
            }
            dismiss()
        }

        //微信登录
        loginWithWechatBtn.clickWithTrigger {
            CommonFunction.wechatLogin(context1, WXEntryActivity.WECHAT_LOGIN)
            dismiss()
        }

        //facebook登录
        loginWithFacebookBtn.clickWithTrigger {
            (context1 as LoginActivity).umengThirdLogin(SHARE_MEDIA.FACEBOOK)
            dismiss()
        }


        //Twitter登陆
        loginWithTwitterBtn.clickWithTrigger {
            (context1 as LoginActivity).umengThirdLogin(SHARE_MEDIA.TWITTER)
            dismiss()
        }

        //google账号登陆
        loginWithGoogleBtn.clickWithTrigger {
            (context1 as LoginActivity).googleLogin()
            dismiss()
        }

    }

}