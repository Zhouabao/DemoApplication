package com.sdy.jitangapplication.ui.activity

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.SpanUtils
import com.chuanglan.shanyan_sdk.OneKeyLoginManager
import com.huawei.hms.framework.common.PackageUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.RegisterFileBean
import com.sdy.jitangapplication.presenter.LoginPresenter
import com.sdy.jitangapplication.presenter.view.LoginView
import com.sdy.jitangapplication.ui.dialog.ChooseLoginWayDialog
import com.sdy.jitangapplication.utils.AbScreenUtils
import com.sdy.jitangapplication.utils.UserManager
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


//(判断用户是否登录过，如果登录过，就直接跳主页面，否则就进入登录页面)
class LoginActivity : BaseMvpActivity<LoginPresenter>(), LoginView, MediaPlayer.OnErrorListener {
    private var syCode = 0

    companion object {
        public var weakrefrece: WeakReference<LoginActivity>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        syCode = intent.getIntExtra("syCode", 0)
//        BarUtils.setStatusBarLightMode(this, true)
//        ScreenUtils.setFullScreen(this)
        AbScreenUtils.hideBottomUIMenu(this)
        initView()
        showVideoPreview()
        mPresenter.getRegisterProcessType()
        getHash()
    }

    private fun initView() {
//        EventBus.getDefault().register(this)
        weakrefrece = WeakReference(this)
        mPresenter = LoginPresenter()
        mPresenter.context = this
        mPresenter.mView = this


        //闪验预取号code为1022即为成功,失败了则再次获取，确保准确
        if (syCode == 0) {
            OneKeyLoginManager.getInstance().getPhoneInfo { p0, p1 ->
                syCode = p0
            }
        }

        userAgreement.text =
            SpanUtils.with(userAgreement).append(resources.getString(R.string.user_protocol))
                .setUnderline().create()
        privacyPolicy.text =
            SpanUtils.with(privacyPolicy).append(resources.getString(R.string.privacy_protocol))
                .setUnderline().create()

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


        //加入积糖
        onekeyLoginBtn.clickWithTrigger(1000L) {
            touristBtn.isEnabled = false
            if (!NetworkUtils.getMobileDataEnabled()) {
                CommonFunction.toast(resources.getString(R.string.open_internet))
                return@clickWithTrigger
            }
            ChooseLoginWayDialog(this, syCode).show()
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


    private fun showVideoPreview() {
//        videoPreview.setMediaController(MediaController(this))
        videoPreview.setVideoURI(Uri.parse("android.resource://com.sdy.jitangapplication/${R.raw.login_video}"))
        videoPreview.setOnCompletionListener {
            videoPreview.start()
        }
        videoPreview.setOnErrorListener(this)


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
        if (!videoPreview.isPlaying) {
            videoPreview.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        UMShareAPI.get(this).release()
        videoPreview.stopPlayback()
//        EventBus.getDefault().unregister(this)
    }

    override fun onGetRegisterProcessType(data: RegisterFileBean?) {
        if (data != null) {
            UserManager.registerFileBean = data
            if (data?.tourists) {
                touristBtn.visibility = View.VISIBLE
            } else {
                if (syCode == 1022) {
                    touristBtn.isInvisible = true
                } else {
                    touristBtn.isVisible = false
                }
            }
        }

    }


    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.d("what", "$what,$extra")
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
    }


    fun getHash() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(
                AppUtils.getAppPackageName(),
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }


}
