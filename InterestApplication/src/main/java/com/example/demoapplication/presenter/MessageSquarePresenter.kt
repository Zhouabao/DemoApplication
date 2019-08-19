package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.SquareMsgBean
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
    /**
     * 获取广场消息列表
     */
    fun squareLists(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .squareLists(params)
            .excute(object : BaseSubscriber<BaseResp<MutableList<SquareMsgBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<SquareMsgBean>?>) {
                    if (t.code == 200)
                        mView.onSquareListsResult(t.data ?: null)
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


    /**
     * 标记广场消息已读
     */
    fun markSquareRead(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .markSquareRead(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                }

                override fun onError(e: Throwable?) {
                }
            })
    }

    /**
     * 删除广场消息
     */
    fun delSquareMsg(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .delSquareMsg(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200)
                        mView.onDelSquareMsgResult(true)
                    else
                        mView.onDelSquareMsgResult(false)
                }

                override fun onError(e: Throwable?) {
                    mView.onDelSquareMsgResult(false)
                }
            })
    }


}