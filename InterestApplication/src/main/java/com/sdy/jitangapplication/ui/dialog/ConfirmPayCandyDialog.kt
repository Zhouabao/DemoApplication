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
import com.braintreepayments.api.BraintreeFragment
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.braintreepayments.api.exceptions.InvalidArgumentException
import com.braintreepayments.api.interfaces.*
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
import com.sdy.jitangapplication.event.PayPalResultEvent
import com.sdy.jitangapplication.googlepay.GooglePayUtils
import com.sdy.jitangapplication.model.ChargeWayBean
import com.sdy.jitangapplication.model.PayBean
import com.sdy.jitangapplication.model.PaywayBean
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.sdy.jitangapplication.wxapi.PayResult
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.dialog_confirm_recharge_candy.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


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
        price.text = "¥${
        if (!chargeBean.isfirst) {
            if (chargeBean.discount_price != 0.0) {
                chargeBean.discount_price
            } else {
                chargeBean.original_price
            }
        } else {
            chargeBean.discount_price
        }}"
        price.typeface = Typeface.createFromAsset(myContext.assets, "DIN_Alternate_Bold.ttf")
        close.onClick {
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
            alipayTv.text = myContext.getString(R.string.pay_paypal)
            wechatTv.text = myContext.getString(R.string.pay_google)
        } else {
            alipayTv.text = myContext.getString(R.string.pay_alipay)
            wechatTv.text = myContext.getString(R.string.pay_wechat)
        }

        confrimBtn.clickWithTrigger {
            if (UserManager.overseas) {
                if (alipayCheck.isChecked) {
                    createOrder(PAY_PAYPAL)
                } else {
                    createOrder(PAY_GOOGLE)
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
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            showAlert(myContext, myContext.getString(R.string.pay_checking), false)
                        } else if (TextUtils.equals(resultStatus, "6001")) {
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            showAlert(myContext, myContext.getString(R.string.pay_cancel), false)
                        } else {
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            showAlert(myContext, myContext.getString(R.string.pay_fail), false)
                        }

                    }
                }
            }
        }
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
        } else if (payment_type == PAY_GOOGLE) {
            //谷歌支付
            googlePay(data)
        } else {
            //todo 获取clientToken 然后开始支付
            //PayPal支付
            initBraintreeFragment(data.reqstr)
            onBrainTreeSubmit(data.reqstr)


        }

    }

    private fun onBrainTreeSubmit(clienToken: String) {
        val dropInRequest = DropInRequest().clientToken(clienToken)
        (myContext as Activity).startActivityForResult(
            dropInRequest.getIntent(myContext),
            REQUEST_CODE_PAYPAL
        )
    }

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

    private fun googlePay(payBean: PayBean) {
        GooglePayUtils(myContext, payBean.order_id!!,mListener =  object : GooglePayUtils.OnPurchaseCallback {
            override fun onPaySuccess(purchaseToken: String) {
                showAlert(myContext, myContext.getString(R.string.pay_success), true)
            }

            override fun onConsumeFail(purchase: Purchase) {
                UserManager.savePurchaseToken(purchase.purchaseToken)

            }

            override fun onUserCancel() {
                showAlert(myContext, myContext.getString(R.string.pay_cancel), false)
            }

            override fun responseCode(msg: String, errorCode: Int) {
                showAlert(myContext, msg, false)
            }

        }).initConnection()
    }

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

    lateinit var mBraintreeFragment: BraintreeFragment
    private val paymentMethodNonceCreatedListener by lazy {
        PaymentMethodNonceCreatedListener {
            it.nonce
            it.description
        }
    }
    private val braintreeCancelListener by lazy {
        BraintreeCancelListener {
            showAlert(myContext, "支付取消 ${it}", false)
        }
    }
    private val braintreeErrorListener by lazy {
        BraintreeErrorListener {
            showAlert(myContext, "支付错误 ${it}", false)
        }
    }

    private val brainConfigurationListener by lazy {
        ConfigurationListener {
            it.merchantId

        }
    }
    private val paymentResultListener by lazy {
        BraintreePaymentResultListener {
            it.describeContents()
        }
    }

    fun initBraintreeFragment(tokenization: String) {
        try {
            mBraintreeFragment =
                BraintreeFragment.newInstance(
                    myContext as AppCompatActivity,
                    Constants.TOKENIZATION_KEYS
                )

            mBraintreeFragment.addListener(brainConfigurationListener)
            //支付完成监听
            mBraintreeFragment.addListener(paymentMethodNonceCreatedListener)
//            mBraintreeFragment.addListener(paymentResultListener)
            //支付取消监听
            mBraintreeFragment.addListener(braintreeCancelListener)
            //错误监听
            mBraintreeFragment.addListener(braintreeErrorListener)

        } catch (e: InvalidArgumentException) {

        }

    }


    private fun showAlert(ctx: Context, info: String, result: Boolean) {
        CommonAlertDialog.Builder(ctx)
            .setTitle(myContext.getString(R.string.pay_result))
            .setContent(info)
            .setCancelIconIsVisibility(false)
            .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                override fun onClick(dialog: Dialog) {
                    dialog.cancel()
                    if (result) {
                        CommonFunction.payResultNotify(myContext)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPayPalResultEvent(event: PayPalResultEvent) {
        if (event.requestCode == REQUEST_CODE_PAYPAL) {
            if (event.resultCode == RESULT_OK) {
                val result: DropInResult? =
                    event.data?.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT)
                if (result != null) {
                    Log.d("onPayPalResultEvent", result.toString())
                }
                showAlert(myContext, myContext.getString(R.string.pay_success), true)
            } else if (event.resultCode == RESULT_CANCELED) {
                Log.d("onPayPalResultEvent", "用户取消支付")
                showAlert(myContext, myContext.getString(R.string.pay_cancel), false)
            } else {
                val error: Exception? =
                    event.data?.getSerializableExtra(DropInActivity.EXTRA_ERROR) as Exception?
                Log.d("onPayPalResultEvent", error.toString())
                showAlert(myContext, error.toString(), false)
            }
        }
    }


}