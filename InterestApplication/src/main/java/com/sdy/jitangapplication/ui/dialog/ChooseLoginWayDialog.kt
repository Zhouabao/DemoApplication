package com.sdy.jitangapplication.ui.dialog

//import com.facebook.CallbackManager
//import com.facebook.FacebookCallback
//import com.facebook.FacebookException
//import com.facebook.login.LoginManager
//import com.facebook.login.LoginResult
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.OnekeyLoginActivity
import com.sdy.jitangapplication.ui.activity.PhoneActivity
import com.sdy.jitangapplication.wxapi.WXEntryActivity
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
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
    Dialog(context1, R.style.MyDialog), UMAuthListener {
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




    //    private val manager by lazy { CallbackManager.Factory.create() }
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

        //手机号码登录
        if (syCode == 1022) {
            loginWithPhoneBtn.text = "本机号码一键登录"
        } else {
            loginWithPhoneBtn.text = "手机号码"
        }

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
            if (!UMShareAPI.get(context1).isInstall(context1 as Activity, SHARE_MEDIA.FACEBOOK)) {
                CommonFunction.toast(context1.getString(R.string.install_face_book_first))
                return@clickWithTrigger
            }
//            Log.e("VVV", "onError===${UmengTool.getSignature(context1)}")
            UMShareAPI.get(context1).getPlatformInfo(context1, SHARE_MEDIA.FACEBOOK, this)
        }

//UMShareConfig config = new UMShareConfig();
//config.isNeedAuthOnGetUserInfo(true);
//UMShareAPI.get(InfoDetailActivity.this).setShareConfig(config);

        //Twitter登陆
        loginWithTwitterBtn.clickWithTrigger {
            if (!UMShareAPI.get(context1).isInstall(context1 as Activity, SHARE_MEDIA.FACEBOOK)) {
                CommonFunction.toast(context1.getString(R.string.install_twitter_first))
                return@clickWithTrigger
            }

            UMShareAPI.get(context1)
                .getPlatformInfo(context1 as Activity, SHARE_MEDIA.TWITTER, this)
        }

        //google账号登陆
        loginWithGoogleBtn.clickWithTrigger {
            if (!UMShareAPI.get(context1).isInstall(context1 as Activity, SHARE_MEDIA.GOOGLEPLUS)) {
                CommonFunction.toast(context1.getString(R.string.install_google_first))
                return@clickWithTrigger
            }

            UMShareAPI.get(context1).getPlatformInfo(context1, SHARE_MEDIA.GOOGLEPLUS, this)
        }

    }



    //UShare封装后字段名 QQ原始字段名 微信原始字段名 新浪原始字段名             字段含义                      备注
    // uid                 openid         unionid      id                   用户唯一标识        uid能否实现Android与iOS平台打通，目前QQ只能实现同APPID下用户ID匹配
    // name               screen_name    nickname     screen_name           用户昵称
    //gender              gender           sex        gender                用户性别                 该字段会直接返回男女
    //iconurl         profile_image_url  headimgurl  profile_image_url      用户头像
    /**
     * @desc 授权成功的回调
     * @param platform 平台名称
     * @param action 行为序号，开发者用不上
     * @param data 用户资料返回  {uid=123175219579566, iconurl=https://graph.facebook.com/123175219579566/picture?height=200&width=200&migration_overrides=%7Boctober_2012%3Atrue%7D,
     * name=Tang JI, last_name=JI, expiration=Sat Jan 16 19:13:04 GMT+08:00 2021, id=123175219579566, middle_name=,
     * accessToken=EAAyZA2cm8I9IBAMZA0zIsbPPZA5dYEZBdrxSuI6pyf5Q4bgoQFZBRAYfKByV1nI8ZCL0xNDGZAznzyJxZCZB6ZA9m9v88W4aaStlQAAuJVtN474SPWJz7HHbgTmZBrSzDmwOFz66QnDLc7vUZB6VaH4CcSr43mejCZCqtw0STlFvVzpmanae6pgksZBYH4cPPwZAeSbFEiw7jX1ZAvL5OVywt0WxKI0AZB71hy5YSEMYSNFikadb5TWS10EwsNjsD,
     * first_name=Tang, profilePictureUri=https://graph.facebook.com/123175219579566/picture?height=200&width=200&migration_overrides=%7Boctober_2012%3Atrue%7D, linkUri=}
     */
    override fun onComplete(platform: SHARE_MEDIA, action: Int, data: MutableMap<String, String>) {
        Log.e("VVV", "onComplete===$platform,$action,$data")
    }

    override fun onCancel(p0: SHARE_MEDIA, p1: Int) {
        Log.e("VVV", "onCancel===$p0,$p1")

    }

    override fun onError(p0: SHARE_MEDIA, p1: Int, p2: Throwable) {
        Log.e("VVV", "onError===$p0,$p1")

    }

    override fun onStart(p0: SHARE_MEDIA) {
        Log.e("VVV", "onStart===$p0")

    }

}