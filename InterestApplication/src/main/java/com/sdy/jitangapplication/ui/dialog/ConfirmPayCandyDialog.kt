package com.sdy.jitangapplication.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.alipay.sdk.app.PayTask
import com.android.billingclient.api.Purchase
import com.blankj.utilcode.util.ActivityUtils
import com.braintreepayments.api.BraintreeFragment
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.braintreepayments.api.exceptions.InvalidArgumentException
import com.braintreepayments.api.models.PayPalRequest
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.event.CloseRegVipEvent
import com.sdy.jitangapplication.event.PayPalResultEvent
import com.sdy.jitangapplication.googlepay.GooglePayUtils
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.sdy.jitangapplication.wxapi.PayResult
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.customer_alert_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_confirm_recharge_candy.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal


/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 确认糖果支付按钮
 *    version: 1.0
 */
class ConfirmPayCandyDialog(
    val myContext: Context,
    val chargeBean: ChargeWayBean,
    val payways: MutableList<PaywayBean>, val source_type: Int = -1
) : Dialog(myContext, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_confirm_recharge_candy)
        initWindow()
        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation

        window?.attributes = params

        setCanceledOnTouchOutside(false)
    }


    private fun initView() {
        price.text = "${CommonFunction.getNowMoneyUnit()}${
        if (!chargeBean.isfirst) {
            if (BigDecimal(chargeBean.discount_price) > BigDecimal.ZERO) {
                chargeBean.discount_price
            } else {
                chargeBean.original_price
            }
        } else {
            chargeBean.discount_price
        }}"
        price.typeface = Typeface.createFromAsset(myContext.assets, "DIN_Alternate_Bold.ttf")
        close.onClick {
            if (ActivityUtils.getTopActivity() is OpenVipActivity && UserManager.registerFileBean?.experience_state == true) {
                // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                EventBus.getDefault().post(CloseRegVipEvent(false))
            }
            dismiss()
        }
        wechatCl.onClick {
            wechatCheck.isChecked = true
            alipayCheck.isChecked = false
        }
        alipayCl.onClick {
            alipayCheck.isChecked = true
            wechatCheck.isChecked = false
        }

        if (UserManager.overseas) {
            alipayIv.setImageResource(R.drawable.icon_pay_paypal)
            wechatIv.setImageResource(R.drawable.icon_pay_google)
            alipayTv.text = myContext.getString(R.string.pay_paypal)
            wechatTv.text = myContext.getString(R.string.pay_google)
        } else {
            alipayTv.text = myContext.getString(R.string.pay_alipay)
            wechatTv.text = myContext.getString(R.string.pay_wechat)
            alipayIv.setImageResource(R.drawable.icon_alipay)
            wechatIv.setImageResource(R.drawable.icon_wechat1)
        }

        confrimBtn.clickWithTrigger(1000L) {
            if (UserManager.overseas) {
                if (alipayCheck.isChecked) {
                    //PayPal支付
//                    initBraintreeFragment()
                    payCreate(source_type, chargeBean.id)
                } else {
                    //谷歌支付
                    googlePay(chargeBean.product_id)
                }
            } else {
                if (alipayCheck.isChecked) {
                    createOrder(PAY_ALI)
                } else {
                    createOrder(PAY_WECHAT)
                }
            }
        }

        showOtherWayBtn.clickWithTrigger {
            wechatCl.isVisible = true
            showOtherWayBtn.isVisible = false
        }
    }


    //pay_id 	    是	支付方式id	展开
    //product_id 	是	购买产品id	展开
    //order_id		是	非必串参数。例如同一商品切换支付方式就需要传
    //payment_type 支付类型 1支付宝 2微信支付 3余额支付
    private val loadingDialog by lazy { LoadingDialog(myContext) }
    private fun createOrder(payment_type: Int) {
        val params = hashMapOf<String, Any>()
        for (payway in payways) {
            if (payway.payment_type == payment_type) {
                params["pay_id"] = payway.id
                break
            }
        }
        if (source_type != -1) {
            params["source_type"] = source_type
        }
        params["product_id"] = chargeBean.id
        RetrofitFactory.instance.create(Api::class.java)
            .createOrder(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<PayBean>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<PayBean>) {
                    loadingDialog.dismiss()
                    if (t.code == 200) {
                        //发起微信
                        start2Pay(payment_type, t.data)
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    loadingDialog.dismiss()
                    CommonFunction.toast(CommonFunction.getErrorMsg(myContext))
                }
            })
    }

    /**
     * 开始支付
     * payment_type 支付类型 1支付宝 2微信支付 3余额支付
     */
    companion object {
        const val PAY_ALI = 1 //支付宝支付
        const val PAY_WECHAT = 2//微信支付
        const val PAY_PAYPAL = 3//paypal支付
        const val PAY_GOOGLE = 4//google支付
        const val REQUEST_CODE_PAYPAL = 888 //paypal支付回调
    }

    private fun start2Pay(payment_type: Int, data: PayBean) {
        if (payment_type == PAY_WECHAT) {
            //微信支付
            wechatPay(data)
        } else if (payment_type == PAY_ALI) {
            //支付宝支付
            aliPay(data)
        }
    }


    /************************   支付宝支付   ***************************/
    private fun aliPay(data: PayBean) {
        //必须异步调用
        Thread(Runnable {
            val alipay = PayTask(myContext as Activity)
            val result: Map<String, String> = alipay.payV2(data.reqstr, true)
            Log.i("msp", result.toString())

            val msg = Message()
            msg.what = SDK_PAY_FLAG
            msg.obj = result
            mHandler.sendMessage(msg)
        }).start()
    }


    private val SDK_PAY_FLAG = 1

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
                            showAlert(myContext, myContext.getString(R.string.pay_success), true)
                        } else if (TextUtils.equals(resultStatus, "8000")) {
                            showAlert(
                                myContext,
                                myContext.getString(R.string.pay_checking),
                                false
                            )
                        } else if (TextUtils.equals(resultStatus, "6001")) {
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            showAlert(
                                myContext,
                                myContext.getString(R.string.pay_cancel),
                                false, true
                            )


                        } else {
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            showAlert(myContext, myContext.getString(R.string.pay_fail), false)
                        }

                    }
                }
            }
        }
    }


    /************************   微信支付   ***************************/
    private fun wechatPay(data: PayBean) {
        //微信支付注册
        val wxapi = WXAPIFactory.createWXAPI(myContext, null)
        wxapi.registerApp(Constants.WECHAT_APP_ID)
        if (!wxapi.isWXAppInstalled) {
            CommonFunction.toast(myContext.getString(R.string.unload_wechat))
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
    }


    /************************   GOOGLE支付   ***************************/
    private fun googlePay(order_id: String) {
        GooglePayUtils(myContext, order_id,
            mListener = object : GooglePayUtils.OnPurchaseCallback {
                override fun onPaySuccess(purchaseToken: String) {
                    androidCheck(order_id, purchaseToken)
                }

                override fun onConsumeFail(purchase: Purchase) {
                    UserManager.savePurchaseToken(purchase.purchaseToken)
                }

                override fun onConsumeSuccess(purchase: Purchase) {

                }

                override fun onUserCancel() {
                    showAlert(myContext, myContext.getString(R.string.pay_cancel), false, true)
                }

                override fun responseCode(msg: String, errorCode: Int) {
                    showAlert(myContext, msg, false)
                }

            }).initConnection()
    }


    /************************   Paypal   ***************************/
    lateinit var mBraintreeFragment: BraintreeFragment

    /**
     * 初始化braintreeFragment
     */
    fun initBraintreeFragment() {
        try {
            mBraintreeFragment =
                BraintreeFragment.newInstance(
                    myContext as AppCompatActivity,
                    Constants.TOKENIZATION_KEYS
                )
        } catch (e: InvalidArgumentException) {

        }

    }


    private var clientToken = PaypalTokenBean()

    /**
     * paypal支付先从服务器获取clientToken
     */
    private fun payCreate(source_type: Int, product_id: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .payCreate(
                UserManager.getSignParams(
                    hashMapOf(
                        "source_type" to source_type,
                        "product_id" to product_id
                    )
                )
            )
            .excute(object : BaseSubscriber<BaseResp<PaypalTokenBean>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<PaypalTokenBean>) {
                    loadingDialog.dismiss()
                    clientToken = t.data
                    if (!clientToken.clientoken.isEmpty()) {
                        onBrainTreeSubmit()
                    } else {
                        payCreate(source_type, chargeBean.id)
                    }
                }

                override fun onError(e: Throwable?) {
                    loadingDialog.dismiss()
                    CommonFunction.toast(CommonFunction.getErrorMsg(myContext))
                }
            })
    }


    /**
     * paypal支付回传结果
     */
    private fun checkNotify(nonce: String, order_id: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .checkNotify(
                UserManager.getSignParams(
                    hashMapOf(
                        "nonce" to nonce,
                        "order_id" to order_id
                    )
                )
            )
            .excute(object : BaseSubscriber<BaseResp<PaypalTokenBean>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<PaypalTokenBean>) {
                    loadingDialog.dismiss()
//                    clientToken = t.data
//                    onBrainTreeSubmit()
                }

                override fun onError(e: Throwable?) {
                    loadingDialog.dismiss()
                    CommonFunction.toast(CommonFunction.getErrorMsg(myContext))
                }
            })
    }


    /**
     * Google支付回传结果
     */
    private fun androidCheck(purchase_id: String, purchase_token: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .androidCheck(
                UserManager.getSignParams(
                    hashMapOf(
                        "purchase_token" to purchase_token,
                        "purchase_id" to purchase_id
                    )
                )
            )
            .excute(object : BaseSubscriber<BaseResp<GoogleTokenBean>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<GoogleTokenBean>) {
                    loadingDialog.dismiss()
                    showAlert(myContext, myContext.getString(R.string.pay_success), true)
//                    clientToken = t.data
//                    onBrainTreeSubmit()
                }

                override fun onError(e: Throwable?) {
                    loadingDialog.dismiss()
                    CommonFunction.toast(CommonFunction.getErrorMsg(myContext))
                }
            })
    }


    /**
     * 发起订单支付
     */
    private fun onBrainTreeSubmit() {
        val paypalRequest = PayPalRequest(
            "${if (!chargeBean.isfirst) {
                if (BigDecimal(chargeBean.discount_price) > BigDecimal.ZERO) {
                    chargeBean.discount_price
                } else {
                    chargeBean.original_price
                }
            } else {
                chargeBean.discount_price
            }}"
        )
            .currencyCode("USD")
            .intent(PayPalRequest.INTENT_SALE)
        val dropInRequest = DropInRequest()
            .paypalRequest(paypalRequest)
            .clientToken(clientToken.clientoken)


        (myContext as Activity).startActivityForResult(
            dropInRequest.getIntent(myContext),
            REQUEST_CODE_PAYPAL
        )
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPayPalResultEvent(event: PayPalResultEvent) {
        if (event.requestCode == REQUEST_CODE_PAYPAL) {
            if (event.resultCode == RESULT_OK) {
                val result: DropInResult? =
                    event.data?.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT)
                if (result != null) {
                    result.paymentMethodNonce
                    Log.d("onPayPalResultEvent", result.toString())
                    Log.d(
                        "onPayPalResultEvent",
                        "nonce=${result!!.paymentMethodNonce!!.nonce},order_id=${clientToken.order_id}"
                    )
                    //todo 上传给服务器端 支付成功
                    checkNotify(result!!.paymentMethodNonce!!.nonce, clientToken.order_id)
                }
                showAlert(myContext, myContext.getString(R.string.pay_success), true)
            } else if (event.resultCode == RESULT_CANCELED) {
                Log.d("onPayPalResultEvent", "用户取消支付")
                showAlert(myContext, myContext.getString(R.string.pay_cancel), false, true)
            } else {
                val error: Exception? =
                    event.data?.getSerializableExtra(DropInActivity.EXTRA_ERROR) as Exception?
                Log.d("onPayPalResultEvent", error.toString())
                showAlert(myContext, error.toString(), false)
            }
        }
    }


    private val alertDialog by lazy {
        CommonAlertDialog.Builder(myContext)
            .setTitle(myContext.getString(R.string.pay_result))
            .create()
    }

    private fun showAlert(
        ctx: Context,
        info: String,
        result: Boolean,
        userCancel: Boolean = false
    ) {

        if (userCancel && ActivityUtils.getTopActivity() is OpenVipActivity && UserManager.registerFileBean?.experience_state == true) {
            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
            EventBus.getDefault().post(CloseRegVipEvent(false))
            dismiss()
        } else {
            alertDialog.message.text = info
            alertDialog.setCancelBtnable(false)
            alertDialog.confirm.clickWithTrigger {
                alertDialog.cancel()
                if (result) {
                    CommonFunction.payResultNotify(myContext)
                    dismiss()
                }
            }
            alertDialog.show()
        }
    }


    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun dismiss() {
        super.dismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseDialogEvent(event: CloseDialogEvent) {
        if (isShowing) {
            dismiss()
        }
    }

}