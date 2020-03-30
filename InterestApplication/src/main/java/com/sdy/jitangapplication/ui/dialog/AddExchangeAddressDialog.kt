package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.GetAddressvent
import com.sdy.jitangapplication.model.AddressBean
import com.sdy.jitangapplication.model.ExchangeOrderBean
import com.sdy.jitangapplication.model.MyAddressBean
import com.sdy.jitangapplication.ui.activity.AddAddressActivity
import com.sdy.jitangapplication.ui.adapter.AddressAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_add_exchange_address.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 管理添加选择收货地址
 *    version: 1.0
 */
class AddExchangeAddressDialog(var context1: Context, val goods_id: Int) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_exchange_address)
        initWindow()
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

    private val addressAdapter = AddressAdapter()

    fun initview() {
        rvAddress.layoutManager = LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)
        rvAddress.adapter = addressAdapter

        addressAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.addAddressIv -> {
                    if (addressAdapter.data.size - 1 < max_count) {
                        (context1 as Activity).startActivity<AddAddressActivity>()
                    } else
                        CommonFunction.toast("最多可以拥有$max_count 地址")

                }
                R.id.addressCl -> {
                    for (data in addressAdapter.data.withIndex()) {
                        data.value.checked = data.index == position
                    }
                    addressAdapter.notifyDataSetChanged()
                }
            }
        }

        addressAdapter.addData(AddressBean())

        exchangeBtn.onClick {
            createGoods()
        }

        getAddress()
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
    fun onGetAddressvent(event: GetAddressvent) {
        addressAdapter.addData(event.address)
        addressAdapter.notifyDataSetChanged()
    }

    private var max_count = 0
    /**
     * 获取收货地址
     */
    fun getAddress() {
        RetrofitFactory.instance.create(Api::class.java)
            .getAddress(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MyAddressBean?>>(null) {
                override fun onNext(t: BaseResp<MyAddressBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        max_count = t.data?.max_cnt ?: 0
                        if (!t.data?.list.isNullOrEmpty())
                            t.data?.list?.get(0)!!.checked = true
                        addressAdapter.addData(t.data?.list ?: mutableListOf<AddressBean>())
                        addressAdapter.notifyDataSetChanged()
                    }
                }
            })
    }

    /**
     * 兑换商品
     */

    fun createGoods() {
        val params = hashMapOf<String, Any>()
        for (data in addressAdapter.data) {
            if (data.checked) {
                params["address_id"] = data.id
                break
            }
        }

        params["goods_id"] = goods_id
        if (orderRemark.text.trim().isNotEmpty())
            params["remark"] = orderRemark.text.trim().toString()

        RetrofitFactory.instance.create(Api::class.java)
            .createGoods(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<ExchangeOrderBean?>>(null) {
                override fun onNext(t: BaseResp<ExchangeOrderBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        dismiss()
                        ExchangeSuccessDialog(context1, t.data!!).show()
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context1).show()
                    }
                }

            })

    }

}