package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.HiMessageBean
import com.example.demoapplication.presenter.view.MessageHiView
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
class MessageHiPresenter : BasePresenter<MessageHiView>() {

    /**
     * 获取打招呼列表
     */
    fun greatLists(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .greatLists(params)
            .excute(object : BaseSubscriber<BaseResp<MutableList<HiMessageBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<HiMessageBean>?>) {
                    when {
                        t.code == 200 -> mView.onGreatListResult(t)
                        t.code==403 -> UserManager.startToLogin(context as Activity)
                        else -> mView.onError(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError("")
                }
            })
    }
}