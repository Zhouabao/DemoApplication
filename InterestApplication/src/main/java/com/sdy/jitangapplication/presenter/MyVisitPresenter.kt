package com.sdy.jitangapplication.presenter

import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.VisitorBean
import com.sdy.jitangapplication.presenter.view.MyVisitView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/7/319:40
 *    desc   :
 *    version: 1.0
 */
class MyVisitPresenter : BasePresenter<MyVisitView>() {
    fun myVisitedList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .myVisitedList(params)
            .excute(object : BaseSubscriber<BaseResp<MutableList<VisitorBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<VisitorBean>?>) {
                    if (t.code == 200) {
                        mView.onMyVisitResult(t.data)
                    } else  {
                        mView.onError(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError("")
                }
            })

    }
}