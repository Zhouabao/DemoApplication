package com.sdy.jitangapplication.googlepay

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/11/2310:36
 *    desc   :购买注意事项：如果商品购买成功，系统还会生成购买令牌，它是一个唯一标识符，表示用户及其所购应用内商品的商品 ID。
 *    您的应用可以在用户设备上存储购买令牌，理想情况下，也可以将购买令牌传递到安全的后端服务器，
 *    以便用于验证购买交易及防范欺诈行为。购买令牌对于一次性商品的每笔购买交易和每个奖励产品都是唯一的。
 *    不过，由于订阅是一次性购买并按固定的结算周期自动续订，因此订阅的购买令牌在各个结算周期内保持不变。
 *    <p>
 *    用户还会收到包含交易收据的电子邮件，其中包含订单 ID 或交易的唯一 ID。用户每次购买一次性商品时，
 *    都会收到包含唯一订单 ID 的电子邮件。此外，用户最初购买订阅时以及后续定期自动续订时，也会收到这样的电子邮件。
 *    您可以在 Google Play 管理中心内使用订单 ID 来管理退款。有关详情，请参阅查看应用的订单和订阅及办理退款。
 *    version: 1.0
 */
class GooglePayUtils(
    val context: Context,
    val orderId: String = "",
    val purchaseId: String = "",
    var purchaseType: String = BillingClient.SkuType.INAPP,//购买类型:内购、 订阅，   默认为内购
    var mListener: OnPurchaseCallback? = null//支付结果回调接口
) : PurchasesUpdatedListener {
    val TAG: String = GooglePayUtils::class.java.simpleName
    private var mBillingClient: BillingClient? = null

    //是否已经建立连接
    private var isClientInit = false


    //1.建立连接
    fun initConnection(): GooglePayUtils {
        mBillingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        //连接服务
        mBillingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isClientInit = false
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.e(TAG, "onBillingSetupFinished   code = ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isClientInit = true

                    if (!isClientInit) {
                        if (mListener != null) {
                            mListener!!.responseCode(context.getString(R.string.connect_network))
                        }
                        return
                    }
                    //每次进行重连的时候都应该消耗之前缓存的商品，不然可能会导致用户支付不了
                    if (UserManager.getPurchaseToken().isNullOrEmpty())
                        queryAndPayPurchases(purchaseId)
                    else
                        queryAndConsumePurchase(true)
                }
            }

        })
        return this
    }


    //2.查询应用内商品详情,并发起支付
    private fun queryAndPayPurchases(purchaseId: String) {
        val skuList = arrayListOf<String>()
        skuList.add(purchaseId)
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(purchaseType)
            .build()
        mBillingClient!!.querySkuDetailsAsync(params, object : SkuDetailsResponseListener {
            override fun onSkuDetailsResponse(
                billingResult: BillingResult,
                skuDetailsList: MutableList<SkuDetails>
            ) {
                Log.e(TAG, "onSkuDetailsResponse code = ${billingResult.responseCode}")
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    onFail("商品不存在")
                    return
                }

                if (skuDetailsList.isNullOrEmpty()) {
                    mListener?.responseCode("商品不存在")
                    return
                }

                var skuDetail: SkuDetails? = null
                for (details in skuDetailsList) {
                    Log.e(TAG, "onSkuDetailsResponse skuDetails = $details")
                    if (purchaseId == details.sku) {
                        skuDetail = details
                    }
                }

                if (skuDetail != null) {
                    pay(skuDetail)
                } else {
                    mListener?.responseCode("商品不存在")
                }
            }

        })

    }


    //3.调起支付
    fun pay(skuDetails: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val code = mBillingClient!!.launchBillingFlow(context as Activity, flowParams).responseCode
        if (BillingClient.BillingResponseCode.OK != code) {
            onFail("支付失败")
        }
    }

    //4.支付回调
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        Log.e(TAG, "onPurchasesUpdated ")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                //支付成功，去消费此次支付，支付成功后，不消费会自动退款
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    //确认购买交易，不然三天后会退款给用户
                    if (!purchase.isAcknowledged) {
                        consumePurchase(purchase)
                    }
                }

            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            mListener?.onUserCancel()
        } else {
            mListener?.responseCode("支付失败", billingResult.responseCode)
        }
    }


    /**
     * 消耗用户购买的一次性商品，不然用户无法进行二次购买
     * //todo 先请求服务器，成功返回后再进行 consume
     */
    private fun consumePurchase(purchase: Purchase) {
        //确认购买交易，不然三天后会退款给用户
        if (purchaseType == BillingClient.SkuType.INAPP) {
            consumeInApp(purchase)
        } else {
            consumeSubs(purchase)
        }
    }

    private fun consumeInApp(purchase: Purchase) {
        //消耗品，开始消耗
        //一种是消耗性的也就是购买类型是内购。
        //对于消耗型商品，请使用客户端 API 中的 consumeAsync()
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        mBillingClient!!.consumeAsync(consumeParams) { billingResult, purchaseToken ->
            Log.e(
                TAG,
                "onConsumeResponse code = ${billingResult.responseCode},msg = ${billingResult.debugMessage},purchaseToken = $purchaseToken"
            )
            // 消费成功  处理自己的流程，我选择先存入数据库
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                mListener?.onPaySuccess(purchase.purchaseToken)
            } else {
                //消费失败，再次消费
                // 消费失败,后面查询消费记录后再次消费，否则，就只能等待退款
                mListener?.onConsumeFail(purchase)
            }
        }
    }


    /**
     * 查询最近的购买交易，并且消耗商品，
     * 避免三天后退款给用户
     */
    fun queryAndConsumePurchase(isReadyPay: Boolean = true) {
        mBillingClient!!.queryPurchaseHistoryAsync(purchaseType) { billingResult, purchaseHistoryRecordList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (!purchaseHistoryRecordList.isNullOrEmpty()) {
                    val purchaseHistoryRecord =
                        purchaseHistoryRecordList.find { it.purchaseToken == UserManager.getPurchaseToken() }
                    if (purchaseHistoryRecord != null) {
                        //确认购买交易
                        val purchase = Purchase(
                            purchaseHistoryRecord.originalJson,
                            purchaseHistoryRecord.signature
                        )
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                            //消耗品，开始消耗
                            consumePurchase(purchase)
                        } else if (isReadyPay) {
                            queryAndPayPurchases(purchaseId)
                        }
                    } else if (isReadyPay) {
                        queryAndPayPurchases(purchaseId)
                    }
                } else if (isReadyPay) {
                    queryAndPayPurchases(purchaseId)
                }

            }
        }

    }


    /**
     * 确认“订阅商品”交易
     */
    private fun consumeSubs(purchase: Purchase) {
        //一种是非消耗性型，购买类型是订阅
        //对于非消耗型商品，请使用客户端 API 中的 acknowledgePurchase()
        val acknowledgePutchaseParams =
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .setDeveloperPayload(orderId)
                .build()
        mBillingClient!!.acknowledgePurchase(acknowledgePutchaseParams) { billingResult ->
            Log.e(
                TAG,
                "onAcknowledgePurchaseResponse code = ${billingResult.responseCode},msg = ${billingResult.debugMessage},purchaseToken = ${purchase.purchaseToken}"
            )
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // 消费成功  处理自己的流程，我选择先存入数据库
                mListener?.onPaySuccess(purchase.purchaseToken)
            } else {
                // 消费失败,后面查询消费记录后再次消费，否则，就只能等待退款
                mListener?.onConsumeFail(purchase)
            }
        }
    }


    interface OnPurchaseCallback {
        //支付成功
        fun onPaySuccess(purchaseToken: String)

        fun onConsumeFail(purchase: Purchase)

        //用户取消购买流程引起的错误
        fun onUserCancel()

        //处理其它的error
        fun responseCode(msg: String, errorCode: Int = -1)
    }


    fun onFail(str: String) {
        mListener?.responseCode(str)
    }

}