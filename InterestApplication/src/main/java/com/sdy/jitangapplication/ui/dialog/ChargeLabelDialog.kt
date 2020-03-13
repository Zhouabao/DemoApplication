package com.sdy.jitangapplication.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.alipay.sdk.app.PayTask
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.LabelChargeWayBean
import com.sdy.jitangapplication.model.PayBean
import com.sdy.jitangapplication.model.PaywayBean
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.sdy.jitangapplication.wxapi.PayResult
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.dialog_charge_llabel.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 充值会员底部对话框
 *    version: 1.0
 */
class ChargeLabelDialog(val context1: Context, val tag_id: Int, var from: Int = FROM_OTHER) :
    Dialog(context1, R.style.MyDialog) {
    private var payways: MutableList<PaywayBean> = mutableListOf()
    private val SDK_PAY_FLAG = 1
    private val PAY_WECHAT = 2//微信支付
    private val PAY_ALI = 1 //支付宝支付


    companion object {
        const val FROM_INDEX = 0
        const val FROM_SQUARE = 1
        const val FROM_OTHER = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_charge_llabel)
        initWindow()
        initView()
        initChargeWay()
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
//        params?.y = SizeUtils.dp2px(20F)

        window?.attributes = params
    }


    private fun initView() {
        //支付宝支付
        zhiPayBtn.onClick {
            //            showAlert(context1, "支付成功！", true)
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

        contentFl.onClick {
            dismiss()
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

        params["product_id"] = chargeWayBeans!!.id
        RetrofitFactory.instance.create(Api::class.java)
            .createTagsOrder(UserManager.getSignParams(params))
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
//            dismiss()
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


    private var chargeWayBeans: LabelChargeWayBean? = null
    /**
     * 请求支付方式
     */
    private fun initChargeWay() {
        val params = hashMapOf<String, Any>("tag_id" to tag_id)
        RetrofitFactory.instance.create(Api::class.java)
            .getTagsPrice(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<LabelChargeWayBean?>>(null) {
                override fun onNext(it: BaseResp<LabelChargeWayBean?>) {
                    if (it.data != null) {
                        chargeWayBeans = it.data
//                        setPurchaseType(false)
                        payways.addAll(chargeWayBeans!!.paylist ?: mutableListOf())


                        GlideUtil.loadRoundImgCenterCrop(
                            context1,
                            chargeWayBeans!!.icon,
                            labelIcon,
                            SizeUtils.dp2px(7F),
                            RoundedCornersTransformation.CornerType.LEFT
                        )

                        //4需要付费.付费进入  7.需要付费.已过期 9.需要付费.已删除.过期
                        if (chargeWayBeans!!.is_new) {//付费未购买
                            purchaseType.text = "兴趣购买"
                            purchaseContent.text = "该兴趣需要付费进入，付费后即可加入兴趣"
                        } else {
                            purchaseType.text = "兴趣已过期"
                            purchaseContent.text = "该兴趣已过期，续费后即可继续使用兴趣"
                        }
                        labelName.text = "兴趣「${chargeWayBeans!!.title}」${chargeWayBeans!!.duration}个月"
                        labelPrice.text = SpanUtils.with(labelPrice)
                            .append("¥")
                            .append("${chargeWayBeans!!.price}")
                            .setFontSize(31, true)
                            .setBold()
                            .append("/月")
                            .setFontSize(15, true)
                            .create()
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
                        if (ActivityUtils.getTopActivity() is AddLabelActivity) {
                            EventBus.getDefault().post(PayLabelResultEvent(true))
                        } else if (ActivityUtils.getTopActivity() is MyLabelActivity) {
                            EventBus.getDefault().post(UpdateMyLabelEvent())
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


    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
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