package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.core.view.isVisible
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.GreetEvent
import com.sdy.jitangapplication.event.UpdateHiCountEvent
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
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
        sayHitargetName.text = userName
        sayHiClose.onClick {
            dismiss()
        }
        sayHiBtn.onClick {
            sayHiContent.clearFocus()
            KeyboardUtils.hideSoftInput(sayHiContent)
            greetState()
        }

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
            180f,
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
//                letterCloseRight.isVisible = false
//                letterCloseLeft.isVisible = true
//                rootView.postDelayed({
//                    rootView.startAnimation(translateAniContentOut)
//                }, 500)
                letterCloseRight.startAnimation(mOpenFlipAnimation)

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


    /*----------------------------打招呼请求逻辑--------------------------------*/
//todo  这里要判断是不是VIP用户 如果是VIP 直接进入聊天界面
    //1.首先判断是否有次数，
    // 若有 就打招呼
    // 若无 就弹充值
    /**
     * 判断当前能否打招呼
     */
    private fun greetState() {
        if (!NetworkUtils.isConnected()) {
            CommonFunction.toast("请连接网络！")
            return
        }

        val params = UserManager.getBaseParams()
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .greetState(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<GreetBean?>>(null) {
                override fun onStart() {

                }

                override fun onNext(t: BaseResp<GreetBean?>) {
                    if (t.code == 200) {
                        val greetBean = t.data
                        if (greetBean != null && greetBean.lightningcnt != -1) {
                            if (greetBean.isfriend || greetBean.isgreet) {
                                ChatActivity.start(context1 as Activity, target_accid ?: "")
                                dismiss()
                            } else {
                                UserManager.saveLightingCount(greetBean.lightningcnt)
                                UserManager.saveCountDownTime(greetBean.countdown)
                                if (greetBean.lightningcnt > 0) {
                                    greet()
                                } else {
                                    ChargeVipDialog(
                                        ChargeVipDialog.DOUBLE_HI,
                                        context1,
                                        if (UserManager.isUserVip()) {
                                            ChargeVipDialog.PURCHASE_GREET_COUNT
                                        } else {
                                            ChargeVipDialog.PURCHASE_VIP
                                        }
                                    ).show()
                                    EventBus.getDefault().post(GreetEvent(context1,true))
                                }
                            }
                        } else {
                            CommonFunction.toast(t.msg)
                        }
                    } else {
                        EventBus.getDefault().post(GreetEvent(context1,true))
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context1).show()
                    }
                }
            })
    }

    /** todo
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
     */
    fun greet() {
        if (!NetworkUtils.isConnected()) {
            CommonFunction.toast("请连接网络！")
            return
        }

        val params = UserManager.getBaseParams()
        params["tag_id"] = UserManager.getGlobalLabelId()
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .greet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(null) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        onGreetSResult(true)
                    } else if (t.code == 403) {//登录异常
                        UserManager.startToLogin(context1 as Activity)
                    } else if (t.code == 401) {
                        EventBus.getDefault().post(GreetEvent(context1,true))
                        HarassmentDialog(context1, HarassmentDialog.CHATHI).show() //开启招呼提示
                    } else {
                        EventBus.getDefault().post(GreetEvent(context1,true))
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
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

    /*--------------------------消息代理------------------------*/

    private fun sendChatHiMessage() {
        val container = Container(context1 as Activity, target_accid, SessionTypeEnum.P2P, this, true)
        val chatHiAttachment = ChatHiAttachment(
            UserManager.getGlobalLabelName(),
            ChatHiAttachment.CHATHI_HI
        )
        val message = MessageBuilder.createCustomMessage(
            target_accid,
            SessionTypeEnum.P2P,
            "",
            chatHiAttachment,
            CustomMessageConfig()
        )
        container.proxy.sendMessage(message)
    }

    override fun sendMessage(msg: IMMessage): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                startAnimation()
                //发送通知修改招呼次数
                UserManager.saveLightingCount(UserManager.getLightingCount() - 1)
                EventBus.getDefault().postSticky(UpdateHiCountEvent())
                EventBus.getDefault().post(GreetEvent(context1,true))
            }

            override fun onFailed(code: Int) {

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