package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.SquareLitBean
import com.example.demoapplication.presenter.view.MessageSquareView
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
class MessageSquarePresenter : BasePresenter<MessageSquareView>() {
    fun squareLists(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .squareLists(params)
            .excute(object : BaseSubscriber<BaseResp<SquareLitBean?>>(mView) {
                override fun onNext(t: BaseResp<SquareLitBean?>) {
                    if (t.code == 200)
                        mView.onSquareListsResult(t.data?:null)
                    else if (t.code == 403)
                        UserManager.startToLogin(context as Activity)
                    else
                        mView.onError("")
                }

                override fun onError(e: Throwable?) {
                    mView.onError("")
                }
            })
    }
}