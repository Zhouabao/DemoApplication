package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.BillBean
import com.sdy.jitangapplication.model.MyBillBeans
import com.sdy.jitangapplication.presenter.view.CandyRecordView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/3/259:43
 *    desc   :
 *    version: 1.0
 */
class CandyRecordPresenter : BasePresenter<CandyRecordView>() {

    /**
     * 获取交易流水
     */
    fun myRedWithdrawLog(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .myRedWithdrawLog(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MyBillBeans?>>(mView) {
                override fun onNext(t: BaseResp<MyBillBeans?>) {
                    super.onNext(t)
                    mView.onMyRedWithdrawLog(t.code == 200, t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else mView.onMyRedWithdrawLog(false, null)
                }
            })


    }

}