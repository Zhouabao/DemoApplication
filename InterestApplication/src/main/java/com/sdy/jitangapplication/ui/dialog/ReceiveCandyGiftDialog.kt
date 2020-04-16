package com.sdy.jitangapplication.ui.dialog

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ext.onClick
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.UpdateLikeMeReceivedEvent
import com.sdy.jitangapplication.ui.activity.GreetReceivedActivity
import kotlinx.android.synthetic.main.dialog_receive_candy_gift.*
import org.greenrobot.eventbus.EventBus


/**
 *    author : ZFM
 *    date   : 2019/11/99:44
 *    desc   :接收糖果礼物弹窗
 *    version: 1.0
 */
class ReceiveCandyGiftDialog(val context1: Context) :
    Dialog(context1, R.style.MyDialog), ModuleProxy {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_receive_candy_gift)
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
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        setCanceledOnTouchOutside(true)
    }

    private fun initView() {
        startAnimation()

        //接收礼物
        receiveGiftBtn.onClick {
            //todo 接收礼物

        }
        //暂不接收
        tempRefuseBtn.onClick {
            dismiss()
        }
    }

    private fun startAnimation() {
        //底部light的旋转动画
        val rotateLight = ObjectAnimator.ofFloat(receiveCandyLight, "rotation", 0.0f, 360.0f)
        //设定动画的旋转周期
        rotateLight.duration = 300L
        //设置动画的插值器，这个为匀速旋转
        rotateLight.interpolator = LinearInterpolator()
        //设置动画为无限重复
        rotateLight.repeatCount = -1
        rotateLight.start()


        //糖果礼物信息的平移动画
        val translateContent = ObjectAnimator.ofFloat(receiveCandyCl, "translationY", SizeUtils.dp2px(-160F).toFloat())
        translateContent.duration = 300L
        translateContent.interpolator = LinearInterpolator()
        translateContent.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {

            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                receiveCandyCl.postDelayed({
                    giftCandyAmount.isVisible = true
                }, 200L)
            }

        })

        //盖子的向上平移
        val translateTop = ObjectAnimator.ofFloat(receiveCandyTop, "translationY", SizeUtils.dp2px(-10F).toFloat())
        translateTop.duration = 300L
        translateTop.interpolator = LinearInterpolator()
        translateTop.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                translateContent.start()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
//                receiveCandyTop.postDelayed({
//                    translateContent.start()
//                }, 200L)

            }

        })
        receiveCandyTop.postDelayed({
            translateTop.start()
        }, 500L)

    }


    /*--------------------------消息代理------------------------*/
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