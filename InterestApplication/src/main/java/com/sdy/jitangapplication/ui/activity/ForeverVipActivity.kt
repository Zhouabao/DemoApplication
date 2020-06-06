package com.sdy.jitangapplication.ui.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
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
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.ChargeWayBean
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.PaywayBean
import com.sdy.jitangapplication.ui.dialog.ConfirmPayCandyDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.utils.UserManager
import com.sina.weibo.sdk.share.BaseActivity
import kotlinx.android.synthetic.main.activity_forever_vip.*
import org.jetbrains.anko.startActivity

/**
 * 终身会员
 */
class ForeverVipActivity : BaseActivity() {
    val city_name: String by lazy { intent.getStringExtra("city_name") }
    val gender_str: String by lazy { intent.getStringExtra("gender_str") }
    val peopleAmount: Int by lazy { intent.getIntExtra("people_amount", 0) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forever_vip)
        initView()
        getThreshold()
    }

    private fun initView() {
        //状态栏透明和间距处理
        StatusBarUtil.immersive(this)
        nowPrice.typeface = Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")
        val params = pictureBg.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
        params.height = (285 / 375F * params.width).toInt()
//        params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        pictureBg.layoutParams = params


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
                .append("在${city_name}共有")
                .setFontSize(16, true)
                .create()
            standardPeople.text = "${peopleAmount}个糖宝女孩"
            moreInfoText.isVisible = true
            standardPeople.dance()

        } else {
            SpanUtils.with(moreInfoTitle)
                .append("在${city_name}找到符合标准的")
                .setFontSize(16, true)
                .create()
            SpanUtils.with(standardPeople)
                .append("${peopleAmount}名${gender_str}")
                .setFontSize(22, true)
                .setBold()
                .create()
            moreInfoText.isVisible = false
            standardPeople.dance()
            openVipBtn.text = "成为会员"
            refuseBtn.isVisible = true
        }

        if (UserManager?.registerFileBean?.threshold == true) {
            vipChargeCl.isVisible = true
        } else {
            vipChargeCl.visibility = View.INVISIBLE
            refuseBtn.isVisible = false
            openVipBtn.text = "立即加入"
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
            if (UserManager?.registerFileBean?.threshold == true) {
                if (chargeWayBeans.isNotEmpty()) {
                    ConfirmPayCandyDialog(
                        this@ForeverVipActivity,
                        chargeWayBeans[0],
                        payways
                    ).show()
                }
            } else {
                startActivity<MainActivity>()
            }
        }

        //跳过
        refuseBtn.clickWithTrigger {
            startActivity<MainActivity>()
        }
    }


    /**
     * 获取门槛支付列表
     */
    fun getThreshold() {
        RetrofitFactory.instance.create(Api::class.java)
            .getThreshold(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(it: BaseResp<ChargeWayBeans?>) {
                    if (it.code == 200) {
                        if (it.data != null) {
                            chargeWayBeans = it.data!!.list ?: mutableListOf()
                            setPurchaseType()
                            payways.addAll(it.data!!.paylist ?: mutableListOf())
                        }
                    } else {
                        CommonFunction.toast(it.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e != null && e is BaseException) {
                        TickDialog(this@ForeverVipActivity).show()
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
                .create()
        }
    }


}
