package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.presenter.view.OpenVipView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/5/716:26
 *    desc   :
 *    version: 1.0
 */
class OpenVipPresenter : BasePresenter<OpenVipView>() {

    /**
     * 请求支付方式
     */
    fun productLists() {
        RetrofitFactory.instance.create(Api::class.java)
            .productLists(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(it: BaseResp<ChargeWayBeans?>) {
                    if (it.code == 200) {
                        mView.onProductListsResult(it.data!!)
                    } else {
                        CommonFunction.toast(it.msg)

                    }
                }

                override fun onError(e: Throwable?) {
                    if (e != null && e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }

}