package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.AddressBean
import com.sdy.jitangapplication.model.MyAddressBean
import com.sdy.jitangapplication.presenter.view.AddressManagerView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/4/717:14
 *    desc   :
 *    version: 1.0
 */
class AddressManagerPresenter : BasePresenter<AddressManagerView>() {

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
                        mView.getAddressResult(t.data)

                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }

    /**
     * 删除收货地址
     */
    fun delAddress(id: Int, position: Int) {
        val params = hashMapOf<String, Any>("id" to id)
        RetrofitFactory.instance.create(Api::class.java)
            .delAddress(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MyAddressBean?>>(null) {
                override fun onNext(t: BaseResp<MyAddressBean?>) {
                    super.onNext(t)
                    CommonFunction.toast(t.msg)
                    mView.delAddressResult(t.code == 200, position)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.delAddressResult(false, position)
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    }
                }
            })
    }

    /**
     * 设为默认收货地址
     */
    fun editAddress(id: Int, position: Int) {
        val params = hashMapOf<String, Any>("is_default" to 1, "id" to id)
        RetrofitFactory.instance.create(Api::class.java)
            .editAddress(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<AddressBean?>>(null) {
                override fun onNext(t: BaseResp<AddressBean?>) {
                    super.onNext(t)
                    CommonFunction.toast(t.msg)
                    mView.defaultAddressResult(t.code == 200, position)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.defaultAddressResult(false, position)
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    }
                }
            })
    }

}