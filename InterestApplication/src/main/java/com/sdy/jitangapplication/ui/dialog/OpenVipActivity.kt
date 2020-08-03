package com.sdy.jitangapplication.ui.dialog

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseRegVipEvent
import com.sdy.jitangapplication.model.ChargeWayBean
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.PaywayBean
import com.sdy.jitangapplication.ui.activity.LoginActivity
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.activity.RegisterInfoActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_forever_vip.*
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
class OpenVipActivity : BaseActivity() {
    val moreMatch by lazy { intent.getSerializableExtra("morematchbean") as MoreMatchBean? }
    val from by lazy { intent.getIntExtra("from", FROM_REGISTER_OPEN_VIP) }
    val peopleAmount by lazy { intent.getIntExtra("peopleAmount", -1) }

    companion object {
        const val FROM_REGISTER_OPEN_VIP = 1//注册开通vip页面
        const val FROM_P2P_CHAT = 3 //详细聊天页面

        //    val context1: Context,
//    var moreMatch: MoreMatchBean? = null,
//    var from: Int = FROM_REGISTER_OPEN_VIP,
//    var peopleAmount: Int = -1
        fun start(
            context: Context,
            moreMatchBean: MoreMatchBean? = null,
            from: Int = FROM_REGISTER_OPEN_VIP,
            peopleAmount: Int = -1
        ) {
            context.startActivity<OpenVipActivity>(
                "morematchbean" to moreMatchBean,
                "from" to from,
                "peopleAmount" to peopleAmount
            )
            (context as Activity).finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forever_vip)
        initWindow()
        initView()
        productLists()
        EventBus.getDefault().register(this)
        setSwipeBackEnable(from != FROM_REGISTER_OPEN_VIP)

    }

    private fun initView() {
        refuseBtn.isVisible = from != FROM_REGISTER_OPEN_VIP
                || UserManager?.registerFileBean?.threshold != true
                || UserManager.getGender() != 1
        //状态栏透明和间距处理
//        StatusBarUtil.immersive(context1 as Activity)
        nowPrice.typeface = Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")
        val params = pictureBg.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
        params.height = (285 / 375F * params.width).toInt()
//        params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        pictureBg.layoutParams = params



        when (from) {
            FROM_REGISTER_OPEN_VIP -> {
//                shareFriendsBtn.isVisible = !moreMatch?.share_btn.isNullOrEmpty()
//                shareFriendsBtn.text = moreMatch?.share_btn
                if (UserManager.getGender() == 1) {
                    if (UserManager?.registerFileBean?.threshold == true) {
                        moreInfoText.text = "成为会员和她们亲密接触"
                        val params = moreInfoTitle.layoutParams as ConstraintLayout.LayoutParams
                        params.topMargin = SizeUtils.dp2px(20f)
                        moreInfoTitle.layoutParams = params
                    } else {
                        moreInfoText.text = "她们在翘首期待你的加入"
                        val params = moreInfoTitle.layoutParams as ConstraintLayout.LayoutParams
                        params.topMargin = SizeUtils.dp2px(90f)
                        moreInfoTitle.layoutParams = params
                    }
                    SpanUtils.with(moreInfoTitle)
                        .append("在${moreMatch?.city_name}共有")
                        .setFontSize(16, true)
                        .create()
                    standardPeople.text = "${moreMatch?.people_amount}个糖宝女孩"
                    moreInfoText.isVisible = true
                    standardPeople.dance()

                } else {
                    SpanUtils.with(moreInfoTitle)
                        .append("在${moreMatch?.city_name}找到符合标准的")
                        .setFontSize(16, true)
                        .create()
                    SpanUtils.with(standardPeople)
                        .append("${moreMatch?.people_amount}名${moreMatch?.gender_str}")
                        .setFontSize(22, true)
                        .setBold()
                        .create()
                    moreInfoText.isVisible = false
                    standardPeople.dance()
                }

                //男性并且付费门槛开启
                if (UserManager?.registerFileBean?.threshold == true && UserManager.getGender() == 1) {
                    vipChargeCl.isVisible = true
                    openVipBtn.text = "成为会员"
                    payExplain.isVisible = true
                } else {
                    vipChargeCl.visibility = View.INVISIBLE
                    refuseBtn.isVisible = false
                    openVipBtn.text = "立即加入"
                    payExplain.isVisible = false
                }

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

        //余额支付
        openVipBtn.clickWithTrigger {
            if (UserManager?.registerFileBean?.threshold == true && UserManager.getGender() == 1) {//门槛开启（成为会员）
                if (chargeWayBeans.isNotEmpty()) {
                    ConfirmPayCandyDialog(
                        this,
                        chargeWayBeans[0],
                        payways
                    ).show()
                }
            } else {//门槛关闭（立即加入）
                if (UserManager.getGender() == 1) {
                    startActivity<RegisterInfoActivity>()
                } else
                    startActivity<MainActivity>()
            }
        }


        //取消支付
        refuseBtn.clickWithTrigger {
            finish()
        }


        //分享给好友
        shareFriendsBtn.clickWithTrigger {
            WhyPayDialog(this).show()
//            startActivity<ShareFriendsActivity>(
//                "chargeWayBeans" to chargeWayBeans,
//                "payways" to payways
//            )
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
            .getThreshold(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(it: BaseResp<ChargeWayBeans?>) {
                    if (it.code == 200) {
                        if (it.data != null) {
                            chargeWayBeans = it.data!!.list ?: mutableListOf()
                            setPurchaseType()
                            payways.addAll(it.data!!.paylist ?: mutableListOf())
                            sameSexCnt.text = "已有${it.data!!.same_sex_cnt}名高端男士加入"
                        }
                    } else {
                        CommonFunction.toast(it.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e != null && e is BaseException) {
                        TickDialog(this@OpenVipActivity).show()
                    }
                }
            })
    }


    private var chargeWayBeans: MutableList<ChargeWayBean> = mutableListOf()
    private var payways: MutableList<PaywayBean> = mutableListOf()


    private fun setPurchaseType() {
        if (chargeWayBeans.isNotEmpty()) {
            SpanUtils.with(originalPrice)
                .append("原价¥${chargeWayBeans[0].original_price}")
                .setFontSize(12, true)
                .setBold()
                .setStrikethrough()
                .create()

            SpanUtils.with(nowPrice)
                .append("¥")
                .setFontSize(14, true)
                .append(
                    "${if (chargeWayBeans[0].type == 1) {
                        chargeWayBeans[0].original_price
                    } else {
                        chargeWayBeans[0].discount_price
                    }}"
                )
                .setFontSize(30, true)
                .setBold()
                .append("/永久")
                .setFontSize(14,true)
                .create()
        }
    }


    override fun onStop() {
        super.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        if (from != FROM_REGISTER_OPEN_VIP) {
            super.onBackPressed()
        }
    }

    override fun finish() {
        super.finish()
        if (LoginActivity.weakrefrece != null && LoginActivity.weakrefrece!!.get() != null) {
            (LoginActivity.weakrefrece!!.get() as LoginActivity).finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseDialogEvent(event: CloseRegVipEvent) {
        if (from == FROM_REGISTER_OPEN_VIP) {
            startActivity<RegisterInfoActivity>()
        }
        finish()
    }
}