package com.sdy.jitangapplication.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.event.GetAddressvent
import com.sdy.jitangapplication.event.RefreshAddressvent
import com.sdy.jitangapplication.model.AddressBean
import com.sdy.jitangapplication.model.ExchangeOrderBean
import com.sdy.jitangapplication.model.MyAddressBean
import com.sdy.jitangapplication.ui.activity.AddAddressActivity
import com.sdy.jitangapplication.ui.activity.AddressManagerActivity
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
    BottomSheetDialog(context1, R.style.BottomSheetDialog) {

    private var addressBean: AddressBean? = null

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


    fun initview() {
        exchangeBtn.onClick {
            createGoods()
        }

        /**
         * 添加地址
         */
        addAdressBtn.onClick {
            if (addressBean == null) {
                context1.startActivity<AddAddressActivity>()
            } else {
                context1.startActivity<AddressManagerActivity>("address" to addressBean)
            }
        }
        /**
         * 去地址管理界面
         */
        addressCl.onClick {
            if (addressBean == null) {
                context1.startActivity<AddAddressActivity>()
            } else {
                context1.startActivity<AddressManagerActivity>("address" to addressBean)
            }
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
        addressBean = event.address
        addressNameAndPhone.text = "${addressBean!!.nickname}\t\t${addressBean!!.phone}"
        addressDetail.text =
            "${addressBean!!.province_name}${addressBean!!.city_name}${addressBean!!.area_name}${addressBean!!.full_address}"
        exchangeBtn.isEnabled = true
        exchangeBtn.setBackgroundResource(R.drawable.gradient_rectangle_orange_27dp)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshAddressvent(event: RefreshAddressvent) {
        getAddress()
    }


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
                        if (!t.data?.list.isNullOrEmpty()) {
                            for (data in t.data?.list ?: mutableListOf()) {
                                if (data.is_default) {
                                    addressBean = data
                                    break
                                } else
                                    addressBean = t.data?.list?.get(0)!!
                            }

                            addressCl.isVisible = true
                            addAdressBtn.isVisible = false
                            addressNameAndPhone.text =
                                "${addressBean!!.nickname}\t\t${addressBean!!.phone}"
                            addressDetail.text =
                                "${addressBean!!.province_name}${addressBean!!.city_name}${addressBean!!.area_name}${addressBean!!.full_address}"

                            exchangeBtn.isEnabled = true
                            exchangeBtn.setBackgroundResource(R.drawable.gradient_rectangle_orange_27dp)
                        } else {
                            addressCl.isVisible = false
                            addAdressBtn.isVisible = true
                            exchangeBtn.isEnabled = false
                            exchangeBtn.setBackgroundResource(R.drawable.shape_rectangle_ccc_27dp)
                        }
                    }
                }
            })
    }

    /**
     * 兑换商品
     */

    fun createGoods() {
        val params = hashMapOf<String, Any>()
        if (addressBean != null)
            params["address_id"] = addressBean!!.id
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