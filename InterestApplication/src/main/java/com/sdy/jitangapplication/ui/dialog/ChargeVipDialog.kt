package com.sdy.jitangapplication.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.alipay.sdk.app.PayTask
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.ui.adapter.VipBannerAdapter
import com.sdy.jitangapplication.ui.adapter.VipChargeAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.wxapi.PayResult
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.dialog_charge_vip.*

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 充值会员底部对话框
 *    version: 1.0
 */
class ChargeVipDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {
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
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
//        params?.y = SizeUtils.dp2px(20F)

        window?.attributes = params
    }


    private val vipChargeAdapter by lazy { VipChargeAdapter() }
    /**
     * 设置支付价格的数据
     */
    private fun setChargeWayData(chargeWays: MutableList<ChargeWayBean>) {
        vipChargeAdapter.setNewData(chargeWays)
    }


    /**
     * 设置VIP的权益广告栏
     */
    private val vipBannerAdapter by lazy { VipBannerAdapter() }
    public var position: Int = 0
    public fun setCurrent(position: Int) {
        bannerVip.setCurrentItem(position, true)
    }

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
        bannerVip.setCurrentItem(position, true)
    }


    private fun initView() {
        //支付价格
        vipChargeRv.layoutManager = LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)

        vipChargeRv.adapter = vipChargeAdapter
        vipChargeAdapter.setOnItemClickListener { _, _, position ->
            for (data in vipChargeAdapter.data.withIndex()) {
                data.value.check = data.index == position
            }
            vipChargeAdapter.notifyDataSetChanged()
            ToastUtils.showShort("${position}")
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
    }

    //pay_id 	    是	支付方式id	展开
    //product_id 	是	购买产品id	展开
    //order_id		是	非必串参数。例如同一商品切换支付方式就需要传
    //payment_type 支付类型 1支付宝 2微信支付 3余额支付
    private fun createOrder(payment_type: Int) {
        val params = hashMapOf<String, Any>()
        params["token"] = UserManager.getToken()
        params["accid"] = UserManager.getAccid()
        for (payway in payways) {
            if (payway.payment_type == payment_type) {
                params["pay_id"] = payway.id
                break
            }
        }
        for (charge in vipChargeAdapter.data) {
            if (charge.check) {
                params["product_id"] = charge.id
                break
            }
        }
        RetrofitFactory.instance.create(Api::class.java)
            .createOrder(params)
            .excute(object : BaseSubscriber<BaseResp<PayBean>>(null) {
                override fun onNext(t: BaseResp<PayBean>) {
                    if (t.code == 200) {
                        //发起微信
                        start2Pay(payment_type, t.data)
                    } else {
                        ToastUtils.showShort(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    ToastUtils.showShort(CommonFunction.getErrorMsg(context1))
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
                        /**
                         * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                         */
                        val resultInfo = payResult.result// 同步返回需要验证的信息
                        val resultStatus = payResult.resultStatus
                        // 判断resultStatus 为9000则代表支付成功
                        if (TextUtils.equals(resultStatus, "9000")) {
                            // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                            showAlert(context1, "支付成功:$resultInfo")
                        } else {
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            showAlert(context1, "支付失败:$resultInfo")

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
                ToastUtils.showShort("你没有安装微信")
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


    /**
     * 请求支付方式
     */
    private fun initChargeWay() {
        RetrofitFactory.instance.create(Api::class.java)
            .productLists(
                hashMapOf(
                    "token" to UserManager.getToken(),
                    "accid" to UserManager.getAccid(),
                    "_sign" to "",
                    "_timestamp" to System.currentTimeMillis()
                )
            )
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(it: BaseResp<ChargeWayBeans?>) {
                    if (it.data != null) {
                        if (!it.data!!.list.isNullOrEmpty()) {
                            it.data!!.list?.get(0)?.check = true
                        }
                        setChargeWayData(it.data!!.list ?: mutableListOf())
                        initVipPowerData(it.data!!.icon_list ?: mutableListOf())
                        payways.addAll(it.data!!.paylist ?: mutableListOf())
                        initPayWay()
                        loading.visibility = View.GONE
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
            }
            if (payway.payment_type == 2) {
                wechatPayBtn.visibility = View.VISIBLE
            }
            if (payway.payment_type == 3) {
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


    private fun showAlert(ctx: Context, info: String) {
        showAlert(ctx, info, null)
    }

    private fun showAlert(ctx: Context, info: String, onDismiss: DialogInterface.OnDismissListener?) {
        AlertDialog.Builder(ctx)
            .setMessage(info)
            .setPositiveButton("确定", null)
            .setOnDismissListener(onDismiss)
            .show()
    }


}