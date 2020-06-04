package com.sdy.jitangapplication.ui.dialog

import android.animation.Animator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.RefreshTodayFateEvent
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.nim.attachment.SendGiftAttachment
import com.sdy.jitangapplication.ui.adapter.TodayFateAdapter
import com.sdy.jitangapplication.ui.adapter.VisitUserAvatorAdater
import com.sdy.jitangapplication.utils.UserManager
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.dialog_today_fate.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *    author : ZFM
 *    date   : 2020/4/2717:00
 *    desc   : 今日缘分
 *    version: 1.0
 */
class TodayFateDialog(
    val context1: Context,
    val nearBean: NearBean?,
    val data: TodayFateBean?
) :
    Dialog(context1, R.style.MyDialog), CardStackListener, OnLazyClickListener {
    private val userAvatorAdapter by lazy { VisitUserAvatorAdater(true) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_today_fate)
        initWindow()
        initView()
    }


    private fun initView() {

        guideMarkCl.isVisible = !UserManager.isShowGuideMarkLike()
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        myCandyCnt.typeface = Typeface.createFromAsset(context1.assets, "DIN_Alternate_Bold.ttf")
        myCandyCnt.text = "${data?.mycandy_amount ?: 0}"

        SpanUtils.with(guideTip)
            .append("左滑")
            .append("不喜欢")
            .setForegroundColor(Color.parseColor("#ff6318"))
            .append("，右滑")
            .append("喜欢")
            .setForegroundColor(Color.parseColor("#ff6318"))
            .create()
        okBtn.onClick {
            guideMarkCl.isVisible = false
            markLeftCount.text = "还有${data?.list?.size ?: 0}位待你选择"
            markTitle.text = "${if (UserManager.getGender() == 1) {
                "她"
            } else {
                "他"
            }}们想认识你"
            UserManager.saveShowGuideMarkLike(true)
        }

        //跳过充值
        stepOutBtn.setOnClickListener(this)
        //一键批量赠送礼物
        sendGiftBtn.setOnClickListener(this)
        //糖果充值
        rechargeCandyBtn.setOnClickListener(this)

        markedUserRv.layoutManager = LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)
        markedUserRv.adapter = userAvatorAdapter

        iv1.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                dismiss()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })

        initialize()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
//        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
    }

    /*---------------------卡片参数和方法------------------------------*/
    private val manager by lazy { CardStackLayoutManager(context1, this) }
    //用户适配器
    private val adapter by lazy { TodayFateAdapter() }

    private fun initialize() {
        //卡片排列方式
        manager.setStackFrom(StackFrom.Bottom)
        //最大可见数量
        manager.setVisibleCount(3)
        //两个卡片之间的间隔
        manager.setTranslationInterval(0f)
        //卡片滑出飞阈值
        manager.setSwipeThreshold(0.3f)
        //横向纵向的旋转角度
        manager.setMaxDegree(20F)
        //滑动的方向
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(false)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())

        //右滑飞出效果
        val setting1 = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting1)

        markLikeRv.layoutManager = manager
        markLikeRv.adapter = adapter
        adapter.setNewData(data?.list ?: mutableListOf<NearPersonBean>())
        adapter.bindToRecyclerView(markLikeRv)

        markLikeRv.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    override fun onCardDisappeared(view: View?, position: Int) {
        resetAnimation()
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        //向上超级喜欢(会员就超级喜欢 否则弹起收费窗)
        when (direction) {
            //左滑时加载动画
            Direction.Left -> {
                //重置右边、上边的距离
                animation_like.alpha = 0F
                val paramsLike = animation_like.layoutParams as ConstraintLayout.LayoutParams
                paramsLike.width = 0
                paramsLike.height = 0
                animation_like.layoutParams = paramsLike

                animation_dislike.alpha = ratio
                val params = animation_dislike.layoutParams as ConstraintLayout.LayoutParams
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.leftMargin =
                    ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_dislike.layoutParams = params

            }
            //右滑时加载动画
            Direction.Right -> {
                //重置左边、上边的距离
                val paramsLike = animation_dislike.layoutParams as ConstraintLayout.LayoutParams
                paramsLike.width = 0
                paramsLike.height = 0
                animation_dislike.layoutParams = paramsLike
                animation_dislike.alpha = 0F

                animation_like.alpha = ratio
                val params = animation_like.layoutParams as ConstraintLayout.LayoutParams
                params.width = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.height = (SizeUtils.dp2px(50F) + SizeUtils.dp2px(50f) * ratio).toInt()
                params.rightMargin =
                    ((ScreenUtils.getScreenWidth() / 2F * ratio) - params.width / 2F).toInt()
                animation_like.layoutParams = params
            }
        }
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        resetAnimation()
        when (direction) {
            Direction.Left -> {
            }
            Direction.Right -> {
                userAvatorAdapter.addData(adapter.data[manager.topPosition - 1].avatar)
                markLeftCount.isVisible = userAvatorAdapter.data.size < 4
            }
        }
        markSendGiftCl.isVisible = manager.topPosition == adapter.data.size
        if (manager.topPosition == adapter.data.size) {
            markLikeRv.visibility = View.INVISIBLE
            totalMarkCnt.text = "${userAvatorAdapter.data.size}位女生被你选中"
            if (userAvatorAdapter.data.size == 0) {
                myChooseCnt.text = "没有你喜欢的女生？那明天再来看看吧"
                sendGiftBtn.text = "回到首页"
            } else {
                myChooseCnt.text =
                    "向被你选中的女生每人送出${data?.gift_amount}糖果礼物，体验女生主动撩你的感觉。${data?.out_time}小时未拆开将退回糖果"
                sendGiftBtn.text = "一键送出礼物"
            }
        } else {
            markLikeRv.visibility = View.VISIBLE
        }

    }

    private fun resetAnimation() {
        val params1 = animation_like.layoutParams
        params1.width = 0
        params1.height = 0
        animation_like.alpha = 0F
        animation_like.layoutParams = params1

        val params2 = animation_dislike.layoutParams
        params2.width = 0
        params2.height = 0
        animation_dislike.alpha = 0F
        animation_dislike.layoutParams = params2
    }

    override fun onCardCanceled() {
        resetAnimation()
    }

    override fun onCardAppeared(view: View?, position: Int) {
        markLeftCount.text = "还有${(data?.list?.size ?: 0) - (manager.topPosition)}位待你选择"
    }

    override fun onCardRewound() {

    }


    /**
     *批量送出礼物
     */
    fun batchGreet() {
        val loadingDialog = LoadingDialog(context1)
        val ids = mutableListOf<String>()
        for (data in userAvatorAdapter.data) {
            for (tdata in adapter.data) {
                if (tdata.avatar == data) {
                    ids.add(tdata.accid)
                    break
                }
            }
        }
        val params = hashMapOf<String, Any>()
        params["batch_accid"] = Gson().toJson(ids)
        RetrofitFactory.instance.create(Api::class.java)
            .batchGreet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<BatchSendGiftBean?>>() {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loadingDialog.dismiss()
                }

                override fun onNext(t: BaseResp<BatchSendGiftBean?>) {
                    super.onNext(t)
                    when (t.code) {
                        200 -> {
                            sendGiftBtn.isEnabled = false
                            for (data in (t.data?.order_ids ?: mutableListOf()).withIndex()) {
                                //发送礼物消息
                                val config1 = CustomMessageConfig()
                                config1.enableUnreadCount = true
                                config1.enablePush = false
                                val giftAtt = SendGiftAttachment(
                                    data.value.order_id,
                                    SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL
                                )
                                val giftMsg = MessageBuilder.createCustomMessage(
                                    data.value.accid,
                                    SessionTypeEnum.P2P, "",
                                    giftAtt,
                                    config1
                                )
                                NIMClient.getService(MsgService::class.java)
                                    .sendMessage(giftMsg, false)
                                    .setCallback(object : RequestCallback<Void?> {
                                        override fun onSuccess(param: Void?) {
                                            if (data.index == (t.data?.order_ids ?: mutableListOf()).size - 1) {
                                                iv1.playAnimation()
                                            }
                                        }

                                        override fun onFailed(code: Int) {
                                        }

                                        override fun onException(exception: Throwable) {

                                        }
                                    })
                            }
                        }
                        419 -> {
                            CommonFunction.toast("糖果余额不足，请充值后重试")
                            AlertCandyEnoughDialog(
                                context1,
                                AlertCandyEnoughDialog.FROM_SEND_GIFT
                            ).show()
                        }
                        else -> {
                            CommonFunction.toast(t.msg)
                        }


                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                }
            })

    }


    /**
     *获取我的糖果
     */
    fun getMyCandy() {
        RetrofitFactory.instance.create(Api::class.java)
            .getMyCandy(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<GiftStateBean?>>() {
                override fun onNext(t: BaseResp<GiftStateBean?>) {
                    super.onNext(t)
                    when (t.code) {
                        200 -> {
                            myCandyCnt.text = "${t.data?.account_candy ?: 0}"
                        }
                    }
                }
            })

    }


    override fun dismiss() {
        super.dismiss()
        if (nearBean != null && nearBean!!.today_find!!.id == -1 && !nearBean?.today_find_pull) {
            TodayWantDialog(context1, nearBean).show()
        } else if (nearBean != null && nearBean!!.complete_percent < nearBean!!.complete_percent_normal && !UserManager.showCompleteUserCenterDialog) {
            //如果自己的完善度小于标准值的完善度，就弹出完善个人资料的弹窗
            CompleteUserCenterDialog(context1).show()
        }
        UserManager.showIndexRecommend = true
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.sendGiftBtn -> {//一键批量赠送礼物
                if (userAvatorAdapter.data.size == 0) {
                    dismiss()
                } else {
                    batchGreet()
                }
            }
            R.id.rechargeCandyBtn -> {//糖果充值
                RechargeCandyDialog(context1).show()
            }
            R.id.stepOutBtn -> {//跳过充值
                dismiss()
            }
        }

    }


    //    candyCount
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshMyCandyEvent(event: RefreshTodayFateEvent) {
        getMyCandy()
    }


    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

    }
}