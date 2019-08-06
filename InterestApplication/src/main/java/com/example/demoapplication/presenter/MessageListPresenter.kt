package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.MessageListBean1
import com.example.demoapplication.presenter.view.MessageListView
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
class MessageListPresenter : BasePresenter<MessageListView>() {

    fun messageCensus(params: HashMap<String, String>) {
        RetrofitFactory.instance.create(Api::class.java)
            .messageCensus(params)
            .excute(object : BaseSubscriber<BaseResp<MessageListBean1?>>(mView) {
                override fun onNext(t: BaseResp<MessageListBean1?>) {
                    if (t.code == 200 && t.data != null) {
                        mView.onMessageCensusResult(t.data)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError("")
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError("")
                }
            })
    }
}