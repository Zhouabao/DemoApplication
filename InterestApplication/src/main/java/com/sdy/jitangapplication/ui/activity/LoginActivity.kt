package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SpanUtils
import com.chuanglan.shanyan_sdk.OneKeyLoginManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
import com.sdy.jitangapplication.ui.dialog.ChooseLoginWayDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import java.lang.ref.WeakReference
import java.util.*


//(判断用户是否登录过，如果登录过，就直接跳主页面，否则就进入登录页面)
class LoginActivity : BaseMvpActivity<LoginPresenter>(), LoginView, MediaPlayer.OnErrorListener/*,
    UMAuthListener*/ {
    private var syCode = 0

    companion object {
        public var weakrefrece: WeakReference<LoginActivity>? = null
        const val RC_GOOGLE_SIGN_IN = 1100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        syCode = intent.getIntExtra("syCode", 0)

        BarUtils.setStatusBarColor(this, Color.TRANSPARENT)
        BarUtils.setStatusBarLightMode(this, false)
        initView()
//        showVideoPreview()
        mPresenter.getRegisterProcessType()
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


        //加入奢媛
        onekeyLoginBtn.clickWithTrigger(1000L) {
            touristBtn.isEnabled = false
            ChooseLoginWayDialog(this, syCode).show()
        }

        SpanUtils.with(userAgreement)
            .append(getString(R.string.login_presents_you_agree))
            .append(getString(R.string.user_protocol))
            .setClickSpan(Color.parseColor("#FF6796FA"), true) {
                startActivity<ProtocolActivity>("type" to ProtocolActivity.TYPE_USER_PROTOCOL)
            }
            .append(getString(R.string.login_tip_and))
            .append(getString(R.string.privacy_protocol))
            .setClickSpan(Color.parseColor("#FF6796FA"), true) {
                startActivity<ProtocolActivity>("type" to ProtocolActivity.TYPE_PRIVACY_PROTOCOL)
            }
//            .append(getString(R.string.privacy_for_share))
            .create()


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

    override fun onStart() {
        super.onStart()
        loginFlashLottie.playAnimation()

    }

    override fun onPause() {
        super.onPause()
        loginFlashLottie.pauseAnimation()
    }

    override fun onRestart() {
        super.onRestart()
        loginFlashLottie.playAnimation()
    }

    override fun onResume() {
        super.onResume()
        loginFlashLottie.resumeAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
//        UMShareAPI.get(this).release()
        loginFlashLottie.cancelAnimation()
    }

    override fun onGetRegisterProcessType(data: RegisterFileBean?) {
        if (data != null) {
            onekeyLoginBtn.isEnabled = true
            UserManager.registerFileBean = data
            UserManager.overseas = data.region == 2
            Constants.ASSISTANT_ACCID = if (data.region == 2) {
                "002"
            } else {
                "01"
            }
//            UserManager.overseas = data.region == 2
            if (data.tourists) {
                touristBtn.visibility = View.VISIBLE
            } else {
                if (syCode == 1022) {
                    touristBtn.isInvisible = true
                } else {
                    touristBtn.isVisible = false
                }
            }
        } else {
            onekeyLoginBtn.isEnabled = false
            Constants.ASSISTANT_ACCID = "01"
            mPresenter.getRegisterProcessType()
        }

    }

    private var data: LoginBean? = null
    override fun onConfirmVerifyCode(data: LoginBean?, b: Boolean) {
        if (b) {
            this.data = data
            mPresenter.loginIM(LoginInfo(data!!.accid, data!!.extra_data?.im_token))
        } else {
            OneKeyLoginManager.getInstance().setLoadingVisibility(false)
            mPresenter.loading.dismiss()

        }
    }


    override fun onIMLoginResult(nothing: LoginInfo?, success: Boolean) {
        if (success) {
            mPresenter.loading.dismiss()
            UserManager.startToPersonalInfoActivity(this, nothing, data)
            OneKeyLoginManager.getInstance().finishAuthActivity()
            OneKeyLoginManager.getInstance().removeAllListener()
        } else {
            mPresenter.loading.dismiss()
            CommonFunction.toast(resources.getString(R.string.login_error))
            OneKeyLoginManager.getInstance().setLoadingVisibility(false)
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.d("what", "$what,$extra")
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                if (task != null && task.isSuccessful) {
                    val account = task.getResult(ApiException::class.java)!!
//                    firebaseAuthWithGoogle(account.idToken!!)
                    Log.e(
                        TAG1,
                        "google---${account},idToken = ${account.idToken},id = ${account.id}"
                    )
                    mPresenter.checkVerifyCode(
                        account.idToken!!,
                        VerifyCodeActivity.TYPE_LOGIN_GOOGLE
                    )
                }
            } catch (e: ApiException) {
                Log.e(TAG1, "google error---${e}")
                mPresenter.loading.dismiss()
                CommonFunction.toast(getString(R.string.login_error))
            }
        }
    }


    private val auth by lazy { FirebaseAuth.getInstance() }
    private fun firebaseAuthWithGoogle(idToken: String) {
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.e("VVV", "google success---${task.result}")
                    val user = auth.currentUser!!
                    Log.e(
                        "VVV",
                        "google success---${user.displayName},${user.photoUrl},${user.email},${user.isEmailVerified}，${user.getIdToken(
                            true
                        )}   user id = ${user.uid}"
                    )
                } else {
                    Log.e("VVV", "google failure---")
                }

            }
            .addOnFailureListener {
                Log.e("VVV", "google addOnFailureListener --- $it")
            }
    }


    /**
     * 谷歌登录
     */
    fun googleLogin() {
        //初始化gso
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
//            .requestId().requestProfile()
            .requestIdToken(getString(R.string.server_client_id))
            .build()
        //初始化google登录实例，activity为当前activity
        val googleSignInClient = GoogleSignIn.getClient(this, options)
        val intent = googleSignInClient.signInIntent
        mPresenter.loading.show()
        startActivityForResult(intent, RC_GOOGLE_SIGN_IN)
//        }
    }


    fun initFaceBook() {
        LoginManager.getInstance().apply {
            registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    if (result != null && result.accessToken != null) {
                        Log.e("VVV", "onSuccess===facebook,${result.accessToken}")
                        mPresenter.checkVerifyCode(
                            result.accessToken.token,
                            VerifyCodeActivity.TYPE_LOGIN_FACEBOOK
                        )
                    }
                }

                override fun onCancel() {
                    Log.e("VVV", "onCancel===facebook")
                }

                override fun onError(error: FacebookException?) {
                    CommonFunction.toast(error.toString())
                    Log.e("VVV", "onError===facebook,${error.toString()}")
                }

            })
        }
    }

    fun facebookLogin() {
        initFaceBook()
//        val accessToken: AccessToken? = AccessToken.getCurrentAccessToken()
//        if (accessToken != null && !accessToken.isExpired) {
//            mPresenter.checkVerifyCode(
//                accessToken.token,
//                VerifyCodeActivity.TYPE_LOGIN_FACEBOOK
//            )
//        } else
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"))
    }


    /**
     * facebook登录 twitter登录（暂时没做）
     * @param platform 三方登录平台
     */
/*    fun umengThirdLogin(platform: SHARE_MEDIA) {
        if (!UMShareAPI.get(this).isInstall(this as Activity, platform)) {
            when (platform) {
                SHARE_MEDIA.FACEBOOK -> {
                    CommonFunction.toast(this.getString(R.string.install_face_book_first))
                }
                SHARE_MEDIA.TWITTER -> {
                    CommonFunction.toast(this.getString(R.string.install_twitter_first))
                }
            }
            return
        }

//        UMShareAPI.get(this).getPlatformInfo(this, platform, this)
    }*/


    /**
     * @desc 授权成功的回调
     * @param platform 平台名称
     * @param action 行为序号，开发者用不上
     * @param data 用户资料返回  {uid=123175219579566, iconurl=https://graph.facebook.com/123175219579566/picture?height=200&width=200&migration_overrides=%7Boctober_2012%3Atrue%7D,
     * name=Tang JI, last_name=JI, expiration=Sat Jan 16 19:13:04 GMT+08:00 2021, id=123175219579566, middle_name=,
     * accessToken=EAAyZA2cm8I9IBAMZA0zIsbPPZA5dYEZBdrxSuI6pyf5Q4bgoQFZBRAYfKByV1nI8ZCL0xNDGZAznzyJxZCZB6ZA9m9v88W4aaStlQAAuJVtN474SPWJz7HHbgTmZBrSzDmwOFz66QnDLc7vUZB6VaH4CcSr43mejCZCqtw0STlFvVzpmanae6pgksZBYH4cPPwZAeSbFEiw7jX1ZAvL5OVywt0WxKI0AZB71hy5YSEMYSNFikadb5TWS10EwsNjsD,
     * first_name=Tang, profilePictureUri=https://graph.facebook.com/123175219579566/picture?height=200&width=200&migration_overrides=%7Boctober_2012%3Atrue%7D, linkUri=}
     */
//    override fun onComplete(platform: SHARE_MEDIA, action: Int, data: MutableMap<String, String>) {
//        Log.e("VVV", "onComplete===$platform,$action,$data")
//        if (platform == SHARE_MEDIA.FACEBOOK && data["accessToken"] != null) {
//            mPresenter.checkVerifyCode(
//                "${data["accessToken"]}",
//                VerifyCodeActivity.TYPE_LOGIN_FACEBOOK
//            )
//        }
//    }
//
//    override fun onCancel(p0: SHARE_MEDIA, p1: Int) {
//        Log.e("VVV", "onCancel===$p0,$p1")
//
//    }
//
//    override fun onError(p0: SHARE_MEDIA, p1: Int, p2: Throwable) {
//        Log.e("VVV", "onError===$p0,$p1,$p2")
//        CommonFunction.toast(getString(R.string.network_is_not_available))
//
//    }
//
//
//    override fun onStart(p0: SHARE_MEDIA) {
//        Log.e("VVV", "onStart===$p0")
//
//    }

}
