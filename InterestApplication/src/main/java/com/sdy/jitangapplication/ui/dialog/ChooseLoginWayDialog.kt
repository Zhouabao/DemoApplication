package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.NetworkUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.LoginActivity
import com.sdy.jitangapplication.ui.activity.OnekeyLoginActivity
import com.sdy.jitangapplication.ui.activity.PhoneActivity
import com.sdy.jitangapplication.utils.UserManager
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
        if (UserManager.overseas) {
            loginWithFacebookBtn.isVisible = true
            loginWithGoogleBtn.isVisible = true
            loginWithPhoneBtn.isVisible = true
            loginWithWechatBtn.isVisible = false


            //google账号登陆
            loginWithPhoneBtn.text = context1.getString(R.string.continue_with_google)
            loginWithPhoneIv.setImageResource(R.drawable.icon_login_with_google)
            loginWithPhoneBtn.clickWithTrigger {
                (context1 as LoginActivity).googleLogin()
                dismiss()
            }


            //facebook登录
            loginWithFacebookBtn.clickWithTrigger {
                (context1 as LoginActivity).umengThirdLogin(SHARE_MEDIA.FACEBOOK)
                dismiss()
            }


            //手机号码登录
            loginWithGoogleBtn.text = context1.getString(R.string.login_with_phone)
            loginWithGoogleIv.setImageResource(R.drawable.icon_login_way_phone)
            if (syCode == 1022) {
                loginWithGoogleBtn.text = context1.getString(R.string.one_key_login)
            } else {
                loginWithGoogleBtn.text = context1.getString(R.string.account_phone_num)
            }
            loginWithGoogleBtn.clickWithTrigger {
                if (syCode == 1022) {
                    if (!NetworkUtils.getMobileDataEnabled()) {
                        CommonFunction.toast(context1.getString(R.string.open_internet))
                        return@clickWithTrigger
                    }
                    context1.startActivity<OnekeyLoginActivity>()
                } else {
                    context1.startActivity<PhoneActivity>("type" to "1")
                }
                dismiss()
            }
        } else {
            loginWithPhoneBtn.isVisible = true
            loginWithWechatBtn.isVisible = true
            loginWithFacebookBtn.isVisible = false
            loginWithGoogleBtn.isVisible = false


            //手机号码登录
            loginWithPhoneBtn.clickWithTrigger {

                if (syCode == 1022) {
                    if (!NetworkUtils.getMobileDataEnabled()) {
                        CommonFunction.toast(context1.getString(R.string.open_internet))
                        return@clickWithTrigger
                    }
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



            if (syCode == 1022) {
                loginWithPhoneBtn.text = context1.getString(R.string.one_key_login)
            } else {
                loginWithPhoneBtn.text = context1.getString(R.string.account_phone_num)
            }


        }




    }

}