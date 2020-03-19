package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.core.view.isVisible
import com.blankj.utilcode.util.*
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.UpdateLikeMeReceivedEvent
import com.sdy.jitangapplication.model.GreetTimesBean
import com.sdy.jitangapplication.model.ResidueCountBean
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.ui.activity.GreetReceivedActivity
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.Rotate3dAnimation
import kotlinx.android.synthetic.main.dialog_say_hi.*
import org.greenrobot.eventbus.EventBus


/**
 *    author : ZFM
 *    date   : 2019/11/99:44
 *    desc   :打招呼dialog
 *    version: 1.0
 */
class SayHiDialog(
    val target_accid: String,
    val userName: String,
    val context1: Context,
    var isfriend: Boolean = false
) : Dialog(context1, R.style.MyDialog), ModuleProxy {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_say_hi)
        initWindow()

        initView()
    }

    private val loadingDialog by lazy { LoadingDialog(context1) }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        params?.windowAnimations = R.style.MyDialogLeftBottomAnimation
        window?.attributes = params
        setCanceledOnTouchOutside(true)
    }

    private fun initView() {
        letterCloseRight.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        sayHitargetName.text = userName
        sayHiClose.onClick {
            KeyboardUtils.hideSoftInput(sayHiContent)
            dismiss()
        }
        sayHiBtn.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                sayHiContent.clearFocus()
                KeyboardUtils.hideSoftInput(sayHiContent)
                greet()
            }
        })


        sayHiContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable) {
                sayHiBtn.isEnabled = p0.trim().isNotEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })


    }

    private fun startAnimation() {
        //信封内容的缩放动画
        val scaleAnimation = ScaleAnimation(
            1f,
            0.6f,
            1F,
            0.6f,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5F,
            ScaleAnimation.RELATIVE_TO_SELF,
            0.5F
        )
        scaleAnimation.duration = 1000
        scaleAnimation.fillAfter = true
        scaleAnimation.fillBefore = true
        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {

            }

            override fun onAnimationStart(p0: Animation?) {
            }

        })

        //信封的平移动画
        val translateAniBottom = TranslateAnimation(
            TranslateAnimation.RELATIVE_TO_SELF,
            -1f,
            TranslateAnimation.ABSOLUTE,
            ScreenUtils.getAppScreenWidth() / 2F - (SizeUtils.dp2px(300F) / 2F * 0.8F),
            TranslateAnimation.RELATIVE_TO_SELF,
            0F,
            TranslateAnimation.RELATIVE_TO_SELF,
            0F
        )
        translateAniBottom.duration = 1000
        translateAniBottom.fillAfter = true


        //翻转动画
        val mOpenFlipAnimation = Rotate3dAnimation(
            0f,
            -180f,
            //                0f,
            letterCloseRight.width.toFloat() / 2,
            //                ScreenUtils.getAppScreenWidth() / 2F - (SizeUtils.dp2px(300F) / 2F * 0.8F),
            //                0F,
            letterCloseRight.height.toFloat() / 2,
            400F,
            false
        )
        mOpenFlipAnimation.duration = 200
        mOpenFlipAnimation.fillAfter = true
        mOpenFlipAnimation.fillBefore = false
        mOpenFlipAnimation.repeatCount = 0
        mOpenFlipAnimation.interpolator = LinearInterpolator()


        //退场动画
        val translateAniContentOut = TranslateAnimation(
            TranslateAnimation.RELATIVE_TO_PARENT,
            0F,
            TranslateAnimation.RELATIVE_TO_PARENT,
            1f,
            TranslateAnimation.RELATIVE_TO_PARENT,
            0F,
            TranslateAnimation.RELATIVE_TO_PARENT,
            0F
        )
        translateAniContentOut.duration = 1000
        translateAniContentOut.fillAfter = true


        letterBottomCl.isVisible = true
        letterTop.isVisible = true
        letterBottomCl.startAnimation(translateAniBottom)
        letterCloseLeft.startAnimation(translateAniBottom)
        letterTop.startAnimation(translateAniBottom)

        translateAniBottom.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {


            }

            override fun onAnimationEnd(p0: Animation?) {
                rootView.postDelayed({
                    letterCloseRight.isVisible = false
                    letterCloseLeft.isVisible = true
                    rootView.startAnimation(translateAniContentOut)
                }, 100)
//                letterCloseRight.startAnimation(mOpenFlipAnimation)

            }

            override fun onAnimationStart(p0: Animation?) {

                contentHi.startAnimation(scaleAnimation)
            }


        })

        mOpenFlipAnimation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) {
                //                    letterCloseLeft.postDelayed({
                //                        letterCloseRight.isVisible = false
                //                    }, 100)

            }

            override fun onAnimationRepeat(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                letterCloseRight.isVisible = false
                letterCloseLeft.isVisible = true
                rootView.postDelayed({
                    rootView.startAnimation(translateAniContentOut)
                }, 500)

            }
        })

        translateAniContentOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                this@SayHiDialog.hide()
                this@SayHiDialog.dismiss()
            }

            override fun onAnimationStart(p0: Animation?) {

            }

        })
    }


    /**
     *  点击聊天
     *  1. 好友 直接聊天 已经匹配过了 ×
     *
     *  2. 不是好友 判断是否打过招呼
     *
     *     2.1 打过招呼 且没有过期  直接直接聊天
     *
     *     2.2 未打过招呼 判断招呼剩余次数
     *
     *         2.2.1 有次数 直接打招呼
     *
     *         2.2.2 无次数 其他操作--如:请求充值会员
     */

    /**
     * 打招呼
     * code  201 次数使用完毕，请充值次数
     * code  202  你就弹框（该用户当日免费接收次数完毕，请充值会员获取）
     * code  203  招呼次数用完,认证获得次数
     * code  401  发起招呼失败,对方开启了招呼认证,您需要通过人脸认证
     */
    fun greet() {
        if (!NetworkUtils.isConnected()) {
            CommonFunction.toast("请连接网络！")
            loadingDialog.dismiss()
            return
        }

        val params = UserManager.getBaseParams()
        params["target_accid"] = target_accid
        params["content"] = sayHiContent.text.trim().toString()
        RetrofitFactory.instance.create(Api::class.java)
            .greet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<GreetTimesBean?>>(null) {
                override fun onNext(t: BaseResp<GreetTimesBean?>) {
                    if (t.code == 200) {
                        onGreetSResult(true)
                    } else if (t.code == 403) {//登录异常
                        loadingDialog.dismiss()
                        UserManager.startToLogin(context1 as Activity)
                    } else if (t.code == 401) { //弹真人认证
                        loadingDialog.dismiss()
                        HarassmentDialog(context1, HarassmentDialog.CHATHI).show() //开启招呼提示
                    } else if (t.code == 202) { //对方普通招呼收到上限F
                        loadingDialog.dismiss()
                        GreetLimitlDialog(context1).show()
                    } else {
                        loadingDialog.dismiss()
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    loadingDialog.dismiss()
                    CommonFunction.toast(context1.getString(R.string.service_error))
                }
            })
    }

    /**
     * 打招呼结果（先请求服务器）
     */
    fun onGreetSResult(greetBean: Boolean) {
        if (greetBean) {
            sendChatHiMessage()
        }
    }


    private fun sendMsgRequest(content: IMMessage) {
        val params = UserManager.getBaseParams()
        params["content"] = content.content
        params["type"] = content.msgType.value
        params["target_accid"] = target_accid

        RetrofitFactory.instance.create(Api::class.java)
            .sendMsgRequest(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<ResidueCountBean?>>(null) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<ResidueCountBean?>) {
                    super.onNext(t)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })

    }


    /*--------------------------消息代理------------------------*/

    private fun sendChatHiMessage() {
        val container = Container(context1 as Activity, target_accid, SessionTypeEnum.P2P, this, true)
        val chatHiAttachment = ChatHiAttachment(ChatHiAttachment.CHATHI_HI)
        val config = CustomMessageConfig()
        config.enableUnreadCount = false
        config.enablePush = false
        val message = MessageBuilder.createCustomMessage(
            target_accid,
            SessionTypeEnum.P2P,
            "",
            chatHiAttachment,
            config
        )
        container.proxy.sendMessage(message)
    }


    private fun sendTextMessage() {
        val container = Container(context1 as Activity, target_accid, SessionTypeEnum.P2P, this, true)
        val message = MessageBuilder.createTextMessage(
            target_accid,
            SessionTypeEnum.P2P,
            sayHiContent.text.trim().toString()
        )
        val config = CustomMessageConfig()
        config.enableUnreadCount = false
        config.enablePush = false
        message.config = config
        container.proxy.sendMessage(message)
    }

    override fun sendMessage(msg: IMMessage): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                if (msg.msgType == MsgTypeEnum.text) {
//                    sendMsgRequest(msg)
                    loadingDialog.dismiss()
                    startAnimation()
                    //发送通知修改招呼次数
                    if (ActivityUtils.isActivityAlive(GreetReceivedActivity::class.java.newInstance())) {
                        EventBus.getDefault().post(UpdateLikeMeReceivedEvent())
                    }
                } else {
                    sendTextMessage()
                }
            }

            override fun onFailed(code: Int) {
                loadingDialog.dismiss()

            }

            override fun onException(exception: Throwable) {

            }
        })
        return true
    }

    override fun onInputPanelExpand() {

    }

    override fun shouldCollapseInputPanel() {

    }

    override fun isLongClickEnabled(): Boolean {
        return false
    }

    override fun onItemFooterClick(message: IMMessage?) {

    }

}