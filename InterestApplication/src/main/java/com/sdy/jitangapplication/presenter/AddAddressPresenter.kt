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
import com.sdy.jitangapplication.presenter.view.AddAddressView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/3/2614:06
 *    desc   :
 *    version: 1.0
 */
class AddAddressPresenter : BasePresenter<AddAddressView>() {
    /**
     * 添加收货地址
     */
    private val loading by lazy { LoadingDialog(context) }

    fun addAddress(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .addAddress(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<AddressBean?>>(mView) {

                override fun onStart() {
                    super.onStart()
                    loading.show()
                }

                override fun onNext(t: BaseResp<AddressBean?>) {
                    super.onNext(t)
                    if (t.code == 200) mView.onAddAddressResult(t.code == 200, t.data)
                    else CommonFunction.toast(t.msg)
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loading.dismiss()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loading.dismiss()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onAddAddressResult(false, null)
                }
            })

    }


    /**
     * 编辑收货地址
     */
    fun editAddress(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .editAddress(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<AddressBean?>>(null) {
                override fun onNext(t: BaseResp<AddressBean?>) {
                    super.onNext(t)
                    CommonFunction.toast(t.msg)
                    mView.onAddAddressResult(t.code == 200, t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onAddAddressResult(false, null)
                    }
                }
            })
    }



    /**
     * 删除收货地址
     */
    fun delAddress(id: Int) {
        val params = hashMapOf<String, Any>("id" to id)
        RetrofitFactory.instance.create(Api::class.java)
            .delAddress(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MyAddressBean?>>(null) {
                override fun onNext(t: BaseResp<MyAddressBean?>) {
                    super.onNext(t)
                    CommonFunction.toast(t.msg)
                    mView.delAddressResult(t.code == 200)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.delAddressResult(false)
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    }
                }
            })
    }
}