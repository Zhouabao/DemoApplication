package com.sdy.jitangapplication.ui.dialog

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.RefreshCandyMessageEvent
import com.sdy.jitangapplication.model.GiftStateBean
import com.sdy.jitangapplication.model.SendGiftOrderBean
import com.sdy.jitangapplication.nim.attachment.SendGiftAttachment
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_receive_candy_gift.*
import org.greenrobot.eventbus.EventBus


/**
 *    author : ZFM
 *    date   : 2019/11/99:44
 *    desc   :接收糖果礼物弹窗
 *    const GIFT_SEND_WAIT = 1;//待领取状态
const GIFT_SUCCESS = 2;//领取成功 or 发送成功
const GIFT_TIMEOUT_BACK = 3;//超时退回
 *    version: 1.0
 *

 */
class ReceiveCandyGiftDialog(
    val isReceive: Boolean,
    val giftStatus: Int,
    val giftStateBean: GiftStateBean,
    val orderId: Int,
    val context1: Context,
    val target_accid: String
) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_receive_candy_gift)
        initWindow()

        initView()
    }

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
        if (!isReceive) {
            if (giftStatus != giftStateBean.state) {
                when (giftStateBean.state) {
                    SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL, SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN -> {
                        EventBus.getDefault().post(
                            RefreshCandyMessageEvent(
                                orderId,
                                SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN
                            )
                        )
                    }
                    else -> {
                        EventBus.getDefault().post(
                            RefreshCandyMessageEvent(
                                orderId,
                                SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED
                            )
                        )
                    }

                }
            }
        }


        //接收礼物
        receiveGiftBtn.onClick {
            if (isReceive && giftStateBean.state == SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL) {
                getGift()
            } else {
                dismiss()
            }

        }
        //暂不接收
        tempRefuseBtn.onClick {
            dismiss()
        }

        GlideUtil.loadImg(context1, giftStateBean.icon, giftImg)
        giftName.text = giftStateBean.title
        giftCandyAmount.text = "${giftStateBean.amount}"

        when (giftStateBean.state) {
            SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL -> {
                if (!isReceive) {
                    tempRefuseBtn.visibility = View.INVISIBLE
                    receiveGiftBtn.text = "确定"
                    startReceivedAnimation()
                    receiveCandyReceived.isVisible = true
                    receiveCandyReceived.setImageResource(R.drawable.icon_gift_waitreceive)
                } else {
                    tempRefuseBtn.isVisible = true
                    receiveGiftBtn.text = "接收糖果礼物"
                    startWaitReceiveAnimation()
                    receiveCandyReceived.isVisible = false
                }
            }
            SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN -> {
                receiveGiftBtn.text = "确定"
                tempRefuseBtn.visibility = View.INVISIBLE
                receiveCandyReceived.setImageResource(R.drawable.icon_gift_received)
                startReceivedAnimation()
            }
            SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED -> {
                startReceivedAnimation()
                receiveGiftBtn.text = "确定"
                tempRefuseBtn.visibility = View.INVISIBLE
                receiveCandyReceived.setImageResource(R.drawable.icon_gift_has_returned)
            }
        }
    }

    private fun startWaitReceiveAnimation() {
        //底部light的旋转动画
        val rotateLight = ObjectAnimator.ofFloat(receiveCandyLight, "rotation", 0.0f, 360.0f)
        //设定动画的旋转周期
        rotateLight.duration = 4000L
        //设置动画的插值器，这个为匀速旋转
        rotateLight.interpolator = LinearInterpolator()
        //设置动画为无限重复
        rotateLight.repeatCount = -1
        rotateLight.start()


        //糖果礼物信息的平移动画
        val translateContent =
            ObjectAnimator.ofFloat(receiveCandyCl, "translationY", SizeUtils.dp2px(-160F).toFloat())
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
        val translateTop =
            ObjectAnimator.ofFloat(receiveCandyTop, "translationY", SizeUtils.dp2px(-10F).toFloat())
        translateTop.duration = 300L
        translateTop.interpolator = LinearInterpolator()
        translateTop.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                receiveCandyCl.postDelayed({
                    translateContent.start()
                }, 150L)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        receiveCandyTop.postDelayed({
            translateTop.start()
        }, 500L)

    }


    private fun startReceivedAnimation() {
        //糖果礼物信息的平移动画
        val translateContent =
            ObjectAnimator.ofFloat(receiveCandyCl, "translationY", SizeUtils.dp2px(-160F).toFloat())
        translateContent.duration = 20L
        translateContent.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {

            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                giftCandyAmount.isVisible = true
            }

        })

        //盖子的向上平移
        val translateTop =
            ObjectAnimator.ofFloat(receiveCandyTop, "translationY", SizeUtils.dp2px(-10F).toFloat())
        translateTop.duration = 20L
        translateTop.interpolator = LinearInterpolator()
        translateTop.start()
        translateContent.start()

    }


    /**
     * 领取赠送虚拟礼物
     */
    private fun getGift() {
        RetrofitFactory.instance.create(Api::class.java)
            .getGift(UserManager.getSignParams(hashMapOf<String, Any>("id" to orderId)))
            .excute(object : BaseSubscriber<BaseResp<SendGiftOrderBean?>>(null) {
                override fun onStart() {
                    super.onStart()
                    receiveGiftBtn.isEnabled = false
                }

                override fun onNext(t: BaseResp<SendGiftOrderBean?>) {
                    super.onNext(t)
                    receiveGiftBtn.isEnabled = true
                    if (t.code == 200) {
                        if ((t.data?.ret_tips_arr ?: mutableListOf()).isNotEmpty())
                            CommonFunction.sendTips(
                                target_accid,
                                t.data?.ret_tips_arr ?: mutableListOf()
                            )
                        if (giftStatus != giftStateBean.state || giftStateBean.state == SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL) {
                            when (giftStateBean.state) {
                                SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL, SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN -> {
                                    EventBus.getDefault().post(
                                        RefreshCandyMessageEvent(
                                            orderId,
                                            SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN
                                        )
                                    )
                                }
                                else -> {
                                    EventBus.getDefault().post(
                                        RefreshCandyMessageEvent(
                                            orderId,
                                            SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED
                                        )
                                    )
                                }

                            }
                        }


                        dismiss()
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    receiveGiftBtn.isEnabled = true
                }

                override fun onCompleted() {
                    super.onCompleted()
                    receiveGiftBtn.isEnabled = true
                }
            })

    }

    override fun show() {
        super.show()
        //如果点开是过期状态
        if (giftStatus != giftStateBean.state) {
            if (giftStateBean.state == SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED) {
                EventBus.getDefault().post(
                    RefreshCandyMessageEvent(
                        orderId,
                        SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED
                    )
                )
            }
        }
//        else if (giftStateBean.state == SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL) {
//            EventBus.getDefault().post(
//                RefreshCandyMessageEvent(
//                    orderId,
//                    SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN
//                )
//            )
//        }
    }


}