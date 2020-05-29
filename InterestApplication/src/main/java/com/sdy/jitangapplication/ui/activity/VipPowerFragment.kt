package com.sdy.jitangapplication.ui.activity

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alipay.sdk.app.PayTask
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SnackbarUtils.dismiss
import com.google.android.flexbox.*
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UpdateOffsetEvent
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.PayBean
import com.sdy.jitangapplication.model.PaywayBean
import com.sdy.jitangapplication.presenter.VipPowerPresenter
import com.sdy.jitangapplication.presenter.view.VipPowerView
import com.sdy.jitangapplication.ui.adapter.VipChargeAdapter
import com.sdy.jitangapplication.ui.adapter.VipPowerAdapter
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.sdy.jitangapplication.wxapi.PayResult
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.fragment_vip_power.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 会员权益
 */
class VipPowerFragment(
    var type: Int = TYPE_VIP
) :
    BaseMvpFragment<VipPowerPresenter>(),
    VipPowerView,
    View.OnClickListener {
    companion object {
        const val TYPE_VIP = 0
        const val TYPE_PT_VIP = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vip_power, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        mPresenter.getChargeData()
    }


    private fun initData() {
        GlideUtil.loadCircleImg(activity!!, UserManager.getAvator(), vipPowerAvator)
        vipPowerNickname.text = SPUtils.getInstance(Constants.SPNAME).getString("nickname")
    }

    private val vipChargeAdapter by lazy {
        VipChargeAdapter().apply {
            purchaseType = if (type == TYPE_VIP) {
                ChargeVipDialog.PURCHASE_RENEW_VIP
            } else {
                ChargeVipDialog.PURCHASE_PT_VIP
            }
        }
    }
    private val vipPowerAdapter by lazy { VipPowerAdapter(type) }

    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = VipPowerPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        zhiPayBtn.setOnClickListener(this)
        wechatPayBtn.setOnClickListener(this)

//        statePower.retryBtn.onClick {
//            statePower.viewState = MultiStateView.VIEW_STATE_LOADING
//            mPresenter.getChargeData()
//        }


        //支付价格
        vipChargeRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        vipChargeRv.adapter = vipChargeAdapter
        vipChargeAdapter.setOnItemClickListener { _, _, position ->
            for (data in vipChargeAdapter.data.withIndex()) {
                data.value.is_promote = data.index == position
            }
            vipChargeAdapter.notifyDataSetChanged()
//            setupPrice()
        }

        val manager = FlexboxLayoutManager(activity!!, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.CENTER
        manager.justifyContent = JustifyContent.CENTER
        vipPowerRv.layoutManager = manager
        vipPowerRv.adapter = vipPowerAdapter

        val params = (powerUserBg.layoutParams as ConstraintLayout.LayoutParams)
        params.height = (170 / 325F * (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(30F))).toInt()
        powerUserBg.layoutParams = params

        when (type) {
            TYPE_PT_VIP -> {
                powerUserBgExtend.setBackgroundResource(R.drawable.rectangle_left_vip_10dp)
                vipTypeLogo.setTextColor(Color.parseColor("#FF5E6473"))
                vipOutTime.setTextColor(Color.parseColor("#FF5E6473"))
                vipPowerNickname.setTextColor(Color.parseColor("#FF5E6473"))
                vipPowerText.setImageResource(R.drawable.icon_pt_vip_power_text)
                powerUserBg.setImageResource(R.drawable.icon_pt_vip_power_user_bg)
                vipTypeLogo.text = "钻石会员"
                vipTypeLogo.setCompoundDrawablesWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.icon_power_pt_vip_logo),
                    null,
                    null,
                    null
                )
            }
            TYPE_VIP -> {
                powerUserBgExtend.setBackgroundResource(R.drawable.rectangle_left_ptvip_10dp)
                vipTypeLogo.setTextColor(Color.parseColor("#ffcd7e14"))
                vipOutTime.setTextColor(Color.parseColor("#ffcd7e14"))
                vipPowerNickname.setTextColor(Color.parseColor("#ffcd7e14"))
                vipPowerText.setImageResource(R.drawable.icon_vip_power_text)
                powerUserBg.setImageResource(R.drawable.icon_vip_power_user_bg)
                vipTypeLogo.text = "黄金会员"
                vipTypeLogo.setCompoundDrawablesWithIntrinsicBounds(
                    resources.getDrawable(R.drawable.icon_power_vip_logo),
                    null,
                    null,
                    null
                )
            }
        }

    }

    private fun setupPrice() {
        for (data in vipChargeAdapter.data) {
            if (data.is_promote) {
                zhiPayPrice.text = "以${if ((data.discount_price ?: 0F) == 0F) {
                    data.original_price ?: 0F
                } else {
                    data.discount_price ?: 0F
                }}元续费"
                wechatPayPrice.text = zhiPayPrice.text
                break
            }
        }
    }


    override fun getChargeDataResult(data: ChargeWayBeans?) {
        if (data != null) {
//            statePower.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (type == TYPE_VIP) {
                if (data!!.isvip)
                    vipOutTime.text = "${data!!.vip_express}到期"
                else
                    vipOutTime.text = "立即升级享受更多特权"

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
                }

                vipChargeAdapter.setNewData(data.list)
                vipPowerAdapter.setNewData(data.icon_list)
            } else {
                if (data!!.isplatinum)
                    vipOutTime.text = "${data!!.platinum_vip_express}到期"
                else
                    vipOutTime.text = "立即升级享受更多特权"

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
                }

                vipChargeAdapter.setNewData(data.pt_list)
                vipPowerAdapter.setNewData(data.pt_icon_list)
            }
            payways.addAll(data!!.paylist ?: mutableListOf())
            initPayWay()
        } else {
//            statePower.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }


    override fun onClick(v: View) {
        when (v) {
            wechatPayBtn -> {
                createOrder(PAY_WECHAT)
            }

            zhiPayBtn -> {
                createOrder(PAY_ALI)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateOffsetEvent(event: UpdateOffsetEvent) {
//        powerUserBgExtend.isVisible = event.show
    }


    private fun initPayWay() {
        for (payway in payways) {
            if (payway.payment_type == 1) {
                zhiPayBtn.visibility = View.VISIBLE
            } else if (payway.payment_type == 2) {
                wechatPayBtn.visibility = View.VISIBLE
            }
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
    private var payways: MutableList<PaywayBean> = mutableListOf()
    private val PAY_ALI = 1 //支付宝支付
    private val PAY_WECHAT = 2//微信支付
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
                    CommonFunction.toast(CommonFunction.getErrorMsg(activity!!))
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
            val wxapi = WXAPIFactory.createWXAPI(activity!!, null)
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
                val alipay = PayTask(activity!!)
                val result: Map<String, String> = alipay.payV2(data.reqstr, true)
                Log.i("msp", result.toString())

                val msg = Message()
                msg.what = SDK_PAY_FLAG
                msg.obj = result
                mHandler.sendMessage(msg)
            }).start()
        }
    }


    private fun showAlert(info: String, result: Boolean) {
        CommonAlertDialog.Builder(activity!!)
            .setTitle("支付结果")
            .setContent(info)
            .setCancelIconIsVisibility(false)
            .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                override fun onClick(dialog: Dialog) {
                    dialog.cancel()
                    if (result) {
                        CommonFunction.payResultNotify(activity!!)
                        dismiss()
                    }
                }

            })
            .create()
            .show()
    }


}
