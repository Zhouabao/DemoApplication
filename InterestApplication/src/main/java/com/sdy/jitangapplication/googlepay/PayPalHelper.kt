package com.sdy.jitangapplication.googlepay

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.LogUtils
import com.paypal.android.sdk.payments.*
import com.sdy.jitangapplication.common.Constants
import org.json.JSONException
import java.math.BigDecimal

/**
 * author : ZFM
 * date   : 2021/1/199:36
 * desc   :
 * version: 1.0
 */
class PayPalHelper {
    /**
     * 启动PayPal服务
     *
     * @param context
     */
    fun startPayPalService(context: Context) {
        val intent = Intent(context, PayPalService::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
        context.startService(intent)
    }

    /**
     * 停止PayPal服务  sdfsdfsdssaaass
     *
     * @param context
     */
    fun stopPayPalService(context: Context) {
        context.stopService(Intent(context, PayPalService::class.java))
    }

    /**
     * 开始执行支付操作
     *
     * @param context
     */
    fun doPayPalPay(context: Context, totalPrice: String, descr: String) {
        /*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
//        val thingToBuy = getStuffToBuy(PayPalPayment.PAYMENT_INTENT_SALE)

        val thingToBuy = getThingToBuy(totalPrice, descr, PayPalPayment.PAYMENT_INTENT_SALE)
        /*
         * See getStuffToBuy(..) for examples of some available payment options.
         */
        val intent = Intent(context, PaymentActivity::class.java)

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy)
        (context as Activity).startActivityForResult(
            intent,
            REQUEST_CODE_PAYMENT
        )
    }

    /*
     * This method shows use of optional payment details and item list.
     * 直接给PP创建支付的信息，支付对象实体信息
     * @param   paymentIntent
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
     */
    private fun getThingToBuy(
        totalPrice: String,
        descr: String,
        paymentIntent: String
    ): PayPalPayment {
        return PayPalPayment(BigDecimal(totalPrice), "USD", descr, paymentIntent)
    }

    /**
     * 处理支付之后的结果
     *
     * @param context
     * @param requestCode
     * @param resultCode
     * @param data
     */
    fun confirmPayResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        doResult: DoResult
    ) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                val confirm: PaymentConfirmation? =
                    data?.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
                if (confirm != null) {
                    try {
                        LogUtils.i(confirm.toJSONObject().toString(4))
                        LogUtils.i(confirm.payment.toJSONObject().toString(4))
                        // 这里直接跟服务器确认支付结果，支付结果确认后回调处理结果
                        val jsonObject = confirm.toJSONObject()
                        if (jsonObject != null) {
                            val response =
                                jsonObject.optJSONObject("response")
                            if (response != null) {
                                val id = response.optString("id")
                                doResult.confirmSuccess(id)
                            }
                        }
                    } catch (e: JSONException) {
                        LogUtils.i("an extremely unlikely failure occurred:$e ")
                        doResult.confirmNetWorkError()
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                LogUtils.i("The user canceled.")
                doResult.customerCanceled()
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                doResult.invalidPaymentConfiguration()
                LogUtils.i("An invalid Payment or PayPalConfiguration was submitted. Please see the docs.")
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                val auth: PayPalAuthorization? =
                    data?.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION)
                if (auth != null) {
                    try {
                        doResult.confirmFuturePayment()
                        LogUtils.i(auth.toJSONObject().toString(4))
                        val authorization_code = auth.authorizationCode
                        LogUtils.i(authorization_code)
//                        sendAuthorizationToServer(auth);
//                        displayResultText("Future Payment code received from PayPal");
                    } catch (e: JSONException) {
                        doResult.confirmNetWorkError()
                        LogUtils.e("an extremely unlikely failure occurred:$e ")
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                LogUtils.i("The user canceled.")
                doResult.customerCanceled()
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                doResult.invalidPaymentConfiguration()
                LogUtils.i("Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.")
            }
        } else if (requestCode == REQUEST_CODE_PROFILE_SHARING) {
            if (resultCode == Activity.RESULT_OK) {
                val auth: PayPalAuthorization? =
                    data?.getParcelableExtra(PayPalProfileSharingActivity.EXTRA_RESULT_AUTHORIZATION)
                if (auth != null) {
                    try {
                        LogUtils.i(auth.toJSONObject().toString(4))
                        val authorization_code = auth.authorizationCode
                        LogUtils.i(authorization_code)

//                        sendAuthorizationToServer(auth);
//                        displayResultText("Profile Sharing code received from PayPal");
                    } catch (e: JSONException) {
                        LogUtils.e("an extremely unlikely failure occurred:$e ")
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                LogUtils.i("The user canceled.")
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                LogUtils.i("Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.")
            }
        }
    }

    /**
     * c处理完结果之后回调
     */
    interface DoResult {
        //与服务确认支付成功
        fun confirmSuccess(orderBackId: String)

        //网络异常或者json返回有问题
        fun confirmNetWorkError()

        //用户取消支付
        fun customerCanceled()

        //授权支付
        fun confirmFuturePayment()

        //订单支付验证无效
        fun invalidPaymentConfiguration()
    }

    companion object {
        private const val TAG = "PayPalHelper"

        // 配置何种支付环境，一般沙盒，正式
        private const val CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION

        // note that these credentials will differ between live & sandbox environments.
        //你所注册的APP Id
        const val REQUEST_CODE_PAYMENT = 1
        const val REQUEST_CODE_FUTURE_PAYMENT = 2
        const val REQUEST_CODE_PROFILE_SHARING = 3
        private val config = PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(Constants.PAYPAL_CONFIG_CLIENT_ID)

        //以下配置是授权支付的时候用到的
        //            .merchantName("Example Merchant")
        //            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
        //            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));
        private var payPalHelper: PayPalHelper? = null
        val instance: PayPalHelper
            get() {
                if (payPalHelper == null) {
                    synchronized(PayPalHelper::class.java) { payPalHelper = PayPalHelper() }
                }
                return payPalHelper!!
            }
    }
}