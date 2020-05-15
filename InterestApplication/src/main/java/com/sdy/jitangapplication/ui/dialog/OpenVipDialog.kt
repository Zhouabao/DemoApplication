package com.sdy.jitangapplication.ui.dialog

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.PaywayBean
import com.sdy.jitangapplication.ui.activity.GetRelationshipActivity
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.adapter.VipChargeAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_open_vip.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2020/5/99:45
 *    desc   :
 *    version: 1.0
 */
class OpenVipDialog(
    val context1: Context,
    var moreMatch: MoreMatchBean? = null,
    var from: Int = FROM_REGISTER_OPEN_VIP,
    var peopleAmount: Int = -1,
    var force_vip: Boolean = false
) :
    Dialog(context1, R.style.MyDialog) {


    companion object {
        const val FROM_REGISTER_OPEN_VIP = 1//注册开通vip页面
        const val FROM_NEAR_CHAT_GREET = 2//招呼弹窗
        const val FROM_P2P_CHAT = 3 //详细聊天页面
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_vip)
        initWindow()
        initView()
        productLists()
        EventBus.getDefault().register(this)

    }

    private fun initView() {
        setCancelable(!force_vip)
        setCanceledOnTouchOutside(!force_vip)
        refuseBtn.isVisible = !force_vip
        //状态栏透明和间距处理
//        StatusBarUtil.immersive(context1 as Activity)
        when (from) {
            FROM_NEAR_CHAT_GREET -> {
                openVipBtn.text = "成为会员立即聊天"
                SpanUtils.with(moreInfoTitle)
                    .append("解锁无限制畅聊")
                    .setFontSize(22, true)
                    .setBold()
                    .create()
                SpanUtils.with(standardPeople)
                    .append("与${peopleAmount}优质异性无限畅聊\n解锁无限可能")
                    .setFontSize(16, true)
                    .create()

                refuseBtn.text = "不，谢谢"
            }
            FROM_REGISTER_OPEN_VIP -> {
                openVipBtn.text = if (UserManager.getGender() == 1) {
                    "成为会员获取精准配对"
                } else {
                    "成为会员"
                }
                if (moreMatch != null) {
                    SpanUtils.with(moreInfoTitle)
                        .append("在${moreMatch?.city_name}找到符合标准的")
                        .setFontSize(16, true)
                        .create()

                    SpanUtils.with(standardPeople)
                        .append("${moreMatch?.people_amount}名${moreMatch?.gender_str}")
                        .setFontSize(22, true)
                        .setBold()
                        .create()

                    standardPeople.dance()
                }
                refuseBtn.text = "稍后询问"
            }
            FROM_P2P_CHAT -> {
                openVipBtn.text = if (UserManager.getGender() == 1) {
                    "成为会员无限畅聊"
                } else {
                    "成为会员"
                }
                standardPeople.text = "只有会员用户才可以聊天哦\n立即充值无限畅聊"
                refuseBtn.text = "不，谢谢"
            }
        }


        val params = pictureBg.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
//        params.height = (285 / 375F * params.width).toInt()
        params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        pictureBg.layoutParams = params


        if (UserManager.getGender() == 1) {
            pictureBg.imageAssetsFolder = "images_open_vip_girl"
            pictureBg.setAnimation("data_open_vip_girl.json")
        } else {
            pictureBg.imageAssetsFolder = "images_open_vip_boy"
            pictureBg.setAnimation("data_open_vip_boy.json")
        }


        val translateAnimation =
            ObjectAnimator.ofFloat(
                clVipPrice,
                "translationY",
                ScreenUtils.getScreenHeight().toFloat(),
                0F
            )
        translateAnimation.duration = 750L
        translateAnimation.interpolator = DecelerateInterpolator()
        translateAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                pictureBg.playAnimation()
            }

        })

        translateAnimation.start()

//        clVipPrice


        vipChargeRv.layoutManager = LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)
        vipChargeRv.adapter = vipChargeAdapter
        vipChargeAdapter.setOnItemClickListener { _, _, position ->
            for (data in vipChargeAdapter.data.withIndex()) {
                data.value.is_promote = data.index == position
            }
            vipChargeAdapter.notifyDataSetChanged()
        }

        //余额支付
        openVipBtn.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                for (charge in vipChargeAdapter.data) {
                    if (charge.is_promote) {
                        ConfirmPayCandyDialog(context1, charge, payways).show()
                        break
                    }
                }
            }
        })


        //取消支付
        refuseBtn.onClick {
            if (context1 is GetRelationshipActivity)
                context1.startActivity<MainActivity>()
            else
                dismiss()
//            dismiss()

        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
//        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
//        params?.windowAnimations = R.style.MyDialogBottomAnimation
//        params?.y = SizeUtils.dp2px(20F)

        window?.attributes = params

    }


    /**
     * 请求支付方式
     */
    fun productLists() {
        RetrofitFactory.instance.create(Api::class.java)
            .productLists(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(it: BaseResp<ChargeWayBeans?>) {
                    if (it.code == 200) {
                        if (it.data != null) {
                            chargeWayBeans = it.data
                            setPurchaseType()
                            payways.addAll(chargeWayBeans!!.paylist ?: mutableListOf())
                        }
                    } else {
                        CommonFunction.toast(it.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e != null && e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }


    private var chargeWayBeans: ChargeWayBeans? = null
    private var payways: MutableList<PaywayBean> = mutableListOf()


    private val vipChargeAdapter by lazy { VipChargeAdapter() }
    private fun setPurchaseType() {
        //判断是否有选中推荐的，没有的话就默认选中第一个价格。
        var ispromote = false
        for (charge in chargeWayBeans!!.list ?: mutableListOf()) {
            if (charge.is_promote) {
                ispromote = true
                break
            }
        }
        if (!ispromote && (chargeWayBeans!!.list ?: mutableListOf()).isNotEmpty()) {
            chargeWayBeans!!.list!![0].is_promote = true
        }
        vipChargeAdapter.setNewData(chargeWayBeans!!.list)
    }


    override fun show() {
        super.show()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseDialogEvent(event: CloseDialogEvent) {
        if (isShowing) {
            dismiss()
        }
    }
}