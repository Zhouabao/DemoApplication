package com.sdy.jitangapplication.ui.activity

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.alipay.sdk.app.PayTask
import com.blankj.utilcode.util.SnackbarUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.PayBean
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.presenter.VipPowerPresenter
import com.sdy.jitangapplication.presenter.view.VipPowerView
import com.sdy.jitangapplication.ui.adapter.AllVipPowerAdapter
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.sdy.jitangapplication.wxapi.PayResult
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.activity_vip_power1.*
import kotlinx.android.synthetic.main.layout_actionbar.*

class VipPowerActivity() :
    BaseMvpActivity<VipPowerPresenter>(), VipPowerView {
    val currentPosition: Int by lazy { intent.getIntExtra("type", VipPowerBean.TYPE_NORMAL_VIP) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vip_power1)
        initView()
        mPresenter.getChargeData()
    }

    private fun initView() {
        mPresenter = VipPowerPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        StatusBarUtil.immersive(this)
        llTitle.setBackgroundColor(Color.TRANSPARENT)
//        llTitle.setBackgroundColor(Color.parseColor("#FF1D1F21"))
        btnBack.setImageResource(R.drawable.icon_back_white)
        divider.isVisible = false
        hotT1.setTextColor(resources.getColor(R.color.colorWhite))
        hotT1.text = "会员权益"
        btnBack.onClick { finish() }

        initVp2()
    }

    private val adapter by lazy { AllVipPowerAdapter() }
    private fun initVp2() {
        vpPower.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(vpPower)
        vpPower.adapter = adapter
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.wechatPayBtn -> {
                    createOrder(position, PAY_WECHAT)
                }

                R.id.zhiPayBtn -> {
                    createOrder(position, PAY_ALI)
                }
            }
        }

    }


    override fun getChargeDataResult(data: ChargeWayBeans?) {
        if (data != null) {
            adapter.threshold_btn = data?.threshold_btn?:false
            if (!data.list.isNullOrEmpty()) {
                //判断是否有选中推荐的，没有的话就默认选中第一个价格。
                var ispromote = false
                for (charge in data.list) {
                    if (charge.is_promote) {
                        ispromote = true
                        break
                    }
                }
                if (!ispromote && !data.list.isNullOrEmpty()) {
                    data.list[0].is_promote = true
                }

                adapter.addData(
                    VipPowerBean(
                        data.list,
                        data.icon_list,
                        data.isvip,
                        data.vip_express,
                        data.paylist,
                        VipPowerBean.TYPE_NORMAL_VIP
                    )
                )
            }
            if (!data.pt_list.isNullOrEmpty()) {
                //判断是否有选中推荐的，没有的话就默认选中第一个价格。
                var ispromote = false
                for (charge in data.pt_list) {
                    if (charge.is_promote) {
                        ispromote = true
                        break
                    }
                }
                if (!ispromote && !data.pt_list.isNullOrEmpty()) {
                    data.pt_list[0].is_promote = true
                }
                adapter.addData(
                    VipPowerBean(
                        data.pt_list,
                        data.pt_icon_list,
                        data.isplatinum,
                        data.platinum_vip_express,
                        data.paylist,
                        VipPowerBean.TYPE_PT_VIP
                    )
                )
            }
            if (adapter.data.size > currentPosition)
                vpPower.postDelayed({
                    vpPower.scrollToPosition(currentPosition)
                }, 200L)
        }
    }

    private val mHandler by lazy {
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    SDK_PAY_FLAG -> {
                        run {
                            val payResult = PayResult(msg.obj as Map<String, String>)
                            /**
                             * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                             */
                            val resultStatus = payResult.resultStatus
                            // 判断resultStatus 为9000则代表支付成功
                            if (TextUtils.equals(resultStatus, "9000")) {
                                // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                                showAlert("支付成功！", true)
                            } else if (TextUtils.equals(resultStatus, "8000")) {
                                // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                                showAlert("支付结果确认中！", false)
                            } else if (TextUtils.equals(resultStatus, "6001")) {
                                // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                                showAlert("支付取消！", false)
                            } else {
                                // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                                showAlert("支付失败！", false)
                            }

                        }
                    }
                }
            }
        }
    }

    //pay_id 	    是	支付方式id	展开
    //product_id 	是	购买产品id	展开
    //order_id		是	非必串参数。例如同一商品切换支付方式就需要传
    //payment_type 支付类型 1支付宝 2微信支付 3余额支付
    private val PAY_ALI = 1 //支付宝支付
    private val PAY_WECHAT = 2//微信支付
    private fun createOrder(position: Int, payment_type: Int) {
        val params = hashMapOf<String, Any>()
        for (payway in adapter.data[position].paylist ?: mutableListOf()) {
            if (payway.payment_type == payment_type) {
                params["pay_id"] = payway.id
                break
            }
        }
        for (charge in adapter.data[position].list ?: mutableListOf()) {
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
                    CommonFunction.toast(CommonFunction.getErrorMsg(this@VipPowerActivity))
                }
            })
    }


    /**
     * 开始支付
     *     //payment_type 支付类型 1支付宝 2微信支付 3余额支付
     */
    private val SDK_PAY_FLAG = 1

    private fun start2Pay(payment_type: Int, data: PayBean) {
        if (payment_type == PAY_WECHAT) {
            //微信支付注册
            val wxapi = WXAPIFactory.createWXAPI(this, null)
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
                Looper.prepare()
                val alipay = PayTask(this)
                val result: Map<String, String> = alipay.payV2(data.reqstr, true)
                Log.i("msp", result.toString())

                val msg = Message()
                msg.what = SDK_PAY_FLAG
                msg.obj = result
                mHandler.sendMessage(msg)
                Looper.loop()
            }).start()
        }
    }


    private fun showAlert(info: String, result: Boolean) {
        CommonAlertDialog.Builder(this)
            .setTitle("支付结果")
            .setContent(info)
            .setCancelIconIsVisibility(false)
            .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                override fun onClick(dialog: Dialog) {
                    dialog.cancel()
                    if (result) {
                        CommonFunction.payResultNotify(this@VipPowerActivity)
                        SnackbarUtils.dismiss()
                    }
                }

            })
            .create()
            .show()
    }


}
