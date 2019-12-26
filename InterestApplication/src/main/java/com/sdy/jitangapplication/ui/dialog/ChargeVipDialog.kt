package com.sdy.jitangapplication.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.alipay.sdk.app.PayTask
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.adapter.VipBannerAdapter
import com.sdy.jitangapplication.ui.adapter.VipChargeAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.sdy.jitangapplication.wxapi.PayResult
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.dialog_charge_vip.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 充值会员底部对话框
 *    version: 1.0
 */
class ChargeVipDialog(
    private var currentPos: Int, val context1: Context,
    private var purchaseType: Int = PURCHASE_VIP
) :
    Dialog(context1, R.style.MyDialog) {
    companion object {
        const val INFINITE_SLIDE = 0//无限滑动
        const val VIP_LOGO = 1//会员logo
        const val FILTER = 2//独享筛选
        const val LOOKED_ME = 3//看过我的
        const val LIKED_ME = 4//喜欢我的
        const val DOUBLE_HI = 5//双倍招呼

        //购买类型
        const val PURCHASE_VIP = 100//VIP购买
        const val PURCHASE_GREET_COUNT = 200//招呼次数购买
        const val PURCHASE_RENEW_VIP = 300//vip续费

    }


    private var payways: MutableList<PaywayBean> = mutableListOf()
    private val SDK_PAY_FLAG = 1

    private val PAY_WECHAT = 2//微信支付
    private val PAY_ALI = 1 //支付宝支付
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_charge_vip)
        initWindow()
        initView()
        initChargeWay()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
//        params?.y = SizeUtils.dp2px(20F)

        window?.attributes = params
    }


    private val vipChargeAdapter by lazy { VipChargeAdapter() }
    /**
     * 设置支付价格的数据
     */
    private fun setChargeWayData(chargeWays: MutableList<ChargeWayBean>, purchaseType: Int) {
        if (chargeWays.size > 2) {
            chargeWays[1].is_promote = true
        } else {
            chargeWays[0].is_promote = true
        }
        vipChargeAdapter.purchaseType = purchaseType
        vipChargeAdapter.setNewData(chargeWays)
        setUpPrice()
    }


    /**
     * 设置VIP的权益广告栏
     */
    private val vipBannerAdapter by lazy { VipBannerAdapter(PURCHASE_VIP) }

    //权益栏
    private fun initVipPowerData(banners: MutableList<VipDescr>) {
        bannerVip.adapter = vipBannerAdapter
        vipBannerAdapter.setNewData(banners)
        if (bannerIndicator.childCount == 0)
            if (vipBannerAdapter.data.size > 0) {
                val size = vipBannerAdapter.data.size
                for (i in 0 until size) {
                    val indicator = RadioButton(context1)
                    indicator.width = SizeUtils.dp2px(5F)
                    indicator.height = SizeUtils.dp2px(5F)
                    indicator.buttonDrawable = null
                    indicator.background = context1.resources.getDrawable(R.drawable.selector_circle_indicator)

                    indicator.layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                    layoutParams.setMargins(0, 0, SizeUtils.dp2px(6f), 0)
                    indicator.layoutParams = layoutParams
                    indicator.isEnabled = false
                    indicator.isChecked = i == 0
                    bannerIndicator.addView(indicator)
                }
            }
        bannerVip.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (child in 0 until bannerIndicator.childCount)
                    (bannerIndicator.getChildAt(child) as RadioButton).isChecked = position == child
            }
        })
        if (banners.size > currentPos)
            bannerVip.setCurrentItem(currentPos, true)
    }


    private fun initView() {
        //支付价格
        vipChargeRv.layoutManager = LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)

        vipChargeRv.adapter = vipChargeAdapter
        vipChargeAdapter.setOnItemClickListener { _, _, position ->
            for (data in vipChargeAdapter.data.withIndex()) {
                data.value.is_promote = data.index == position
            }
            setUpPrice()
            vipChargeAdapter.notifyDataSetChanged()
        }

        //支付宝支付
        zhiPayBtn.onClick {
            createOrder(PAY_ALI)
        }

        //微信支付
        wechatPayBtn.onClick {
            createOrder(PAY_WECHAT)
        }

        //余额支付
        balancePayBtn.onClick {
            createOrder(3)
        }

        //取消支付
        refuseBtn.onClick {
            dismiss()
        }

    }

    private fun setUpPrice() {
        for (data in vipChargeAdapter.data) {
            if (data.is_promote) {
                zhiPayPrice.text = "以${if ((data.discount_price ?: 0F) == 0F) {
                    data.original_price?:0F
                } else {
                    data.discount_price ?: 0F
                }}元购买"
                wechatPayPrice.text = zhiPayPrice.text
                break
            }
        }

    }

    /**
     * 设置购买的方式
     */
    private fun setPurchaseType(switch: Boolean = true) {
        if (switch) {
            if (purchaseType == PURCHASE_VIP) {
                purchaseType = PURCHASE_GREET_COUNT
                vipBannerAdapter.type = purchaseType
                bannerIndicator.isVisible = false
                purchaseBg.setImageResource(R.drawable.icon_gradient_greet_charge_bg)
                if (chargeWayBeans != null) {
                    setChargeWayData(chargeWayBeans!!.greet_list ?: mutableListOf(), PURCHASE_GREET_COUNT)
                    initVipPowerData(chargeWayBeans!!.greet_icon_list ?: mutableListOf())
                }
            } else {
                purchaseType = PURCHASE_VIP
                vipBannerAdapter.type = purchaseType
                bannerIndicator.isVisible = true
                purchaseBg.setImageResource(R.drawable.icon_gradient_vip_charge_bg)
                if (chargeWayBeans != null) {
                    setChargeWayData(chargeWayBeans!!.list ?: mutableListOf(), PURCHASE_VIP)
                    initVipPowerData(chargeWayBeans!!.icon_list ?: mutableListOf())
                }


            }
        } else {
            if (purchaseType == PURCHASE_VIP) {
                vipBannerAdapter.type = purchaseType
                bannerIndicator.isVisible = true
                purchaseBg.setImageResource(R.drawable.icon_gradient_vip_charge_bg)
                if (chargeWayBeans != null) {
                    setChargeWayData(chargeWayBeans!!.list ?: mutableListOf(), purchaseType)
                    initVipPowerData(chargeWayBeans!!.icon_list ?: mutableListOf())
                }
            } else {
                vipBannerAdapter.type = purchaseType
                bannerIndicator.isVisible = false
                purchaseBg.setImageResource(R.drawable.icon_gradient_greet_charge_bg)
                if (chargeWayBeans != null) {
                    setChargeWayData(chargeWayBeans!!.greet_list ?: mutableListOf(), purchaseType)
                    initVipPowerData(chargeWayBeans!!.greet_icon_list ?: mutableListOf())
                }
            }

        }

    }

    //pay_id 	    是	支付方式id	展开
    //product_id 	是	购买产品id	展开
    //order_id		是	非必串参数。例如同一商品切换支付方式就需要传
    //payment_type 支付类型 1支付宝 2微信支付 3余额支付
    private fun createOrder(payment_type: Int) {
        val params = hashMapOf<String, Any>()
        for (payway in payways) {
            if (payway.payment_type == payment_type) {
                params["pay_id"] = payway.id
                break
            }
        }
        for (charge in vipChargeAdapter.data) {
            if (charge.is_promote) {
                params["product_id"] = charge.id
                break
            }
        }
        RetrofitFactory.instance.create(Api::class.java)
            .createOrder(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<PayBean>>(null) {
                override fun onNext(t: BaseResp<PayBean>) {
                    if (t.code == 200) {
                        //发起微信
                        start2Pay(payment_type, t.data)
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context1))
                }
            })
    }

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SDK_PAY_FLAG -> {
                    run {
                        val payResult = PayResult(msg.obj as Map<String, String>)
                        /**
                         * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                         */
                        val resultInfo = payResult.result// 同步返回需要验证的信息
                        val resultStatus = payResult.resultStatus
                        // 判断resultStatus 为9000则代表支付成功
                        if (TextUtils.equals(resultStatus, "9000")) {
                            // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                            showAlert(context1, "支付成功！", true)
                        } else if (TextUtils.equals(resultStatus, "8000")) {
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            showAlert(context1, "支付结果确认中！", false)
                        } else if (TextUtils.equals(resultStatus, "6001")) {
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            showAlert(context1, "支付取消！", false)
                        } else {
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            showAlert(context1, "支付失败！", false)
                        }

                    }
                }
            }
        }
    }


    /**
     * 开始支付
     *     //payment_type 支付类型 1支付宝 2微信支付 3余额支付
     */
    private fun start2Pay(payment_type: Int, data: PayBean) {
        if (payment_type == PAY_WECHAT) {
            //微信支付注册
            val wxapi = WXAPIFactory.createWXAPI(context1, null)
            wxapi.registerApp(Constants.WECHAT_APP_ID)
            if (!wxapi.isWXAppInstalled) {
                CommonFunction.toast("你没有安装微信")
                return
            }

            //封装微信支付参数
            val request = PayReq()//吊起微信APP的对象
            request.appId = data.wechat?.appid
            request.prepayId = data.wechat?.prepayid
            request.partnerId = data.wechat?.partnerid
            request.nonceStr = data.wechat?.noncestr
            request.timeStamp = data.wechat?.timestamp
            request.packageValue = data.wechat?.`package`
            request.sign = data.wechat?.sign

            //发起微信支付请求
            wxapi.sendReq(request)
        } else if (payment_type == PAY_ALI) {
            //必须异步调用
            Thread(Runnable {
                val alipay = PayTask(context1 as Activity)
                val result: Map<String, String> = alipay.payV2(data.reqstr, true)
                Log.i("msp", result.toString())

                val msg = Message()
                msg.what = SDK_PAY_FLAG
                msg.obj = result
                mHandler.sendMessage(msg)
            }).start()
        }

    }


    private var chargeWayBeans: ChargeWayBeans? = null
    /**
     * 请求支付方式
     */
    private fun initChargeWay() {
        RetrofitFactory.instance.create(Api::class.java)
            .productLists(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(it: BaseResp<ChargeWayBeans?>) {
                    if (it.data != null) {
                        chargeWayBeans = it.data

                        setPurchaseType(false)


                        payways.addAll(chargeWayBeans!!.paylist ?: mutableListOf())
                        initPayWay()
                        loading.isVisible = false
                        dialogView.visibility = View.VISIBLE
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e != null && e is BaseException) {
                        TickDialog(context1).show()
                    }
                }
            })
    }

    private fun initPayWay() {
        for (payway in payways) {
            if (payway.payment_type == 1) {
                zhiPayBtn.visibility = View.VISIBLE
            } else if (payway.payment_type == 2) {
                wechatPayBtn.visibility = View.VISIBLE
            } else if (payway.payment_type == 3) {
                balancePayBtn.visibility = View.VISIBLE
            }
        }
    }


    /**
     * 支付
     */
    private fun chargeVip() {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }


    private fun showAlert(ctx: Context, info: String, result: Boolean) {
        CommonAlertDialog.Builder(ctx)
            .setTitle("支付结果")
            .setContent(info)
            .setCancelIconIsVisibility(false)
            .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                override fun onClick(dialog: Dialog) {
                    dialog.cancel()
                    if (result) {
                        if (ActivityUtils.getTopActivity() != MainActivity::class.java) {
                            MainActivity.start(context1, Intent())
                        }
                        EventBus.getDefault().postSticky(RefreshEvent(true))
                        EventBus.getDefault().postSticky(UserCenterEvent(true))
                        dismiss()
                    }
                }

            })
            .create()
            .show()
    }


}