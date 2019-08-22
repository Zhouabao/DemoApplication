package com.example.demoapplication.presenter

import com.example.demoapplication.api.Api
import com.example.demoapplication.model.HiMessageBean
import com.example.demoapplication.presenter.view.MessageHiView
import com.example.demoapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.model.RecentContact

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
                        else -> mView.onError(t.msg)
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


    /**
     * 删除过期消息
     */
    fun delTimeoutGreet(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .delTimeoutGreet(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200)
                        mView.onDelTimeoutGreetResult(true)
                    else {
                        mView.onDelTimeoutGreetResult(false)
                    }
                }


                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onDelTimeoutGreetResult(false)
                }
            })
    }


    /**
     * 获取云信最近联系人
     */
    fun getRecentContacts(t: BaseResp<MutableList<HiMessageBean>?>) {
        NIMClient.getService(MsgService::class.java)
            .queryRecentContacts()
            .setCallback(object : RequestCallbackWrapper<MutableList<RecentContact>>() {
                override fun onResult(code: Int, result: MutableList<RecentContact>?, exception: Throwable?) {
                    if (code != ResponseCode.RES_SUCCESS.toInt() || result == null) {
                        return
                    }
                    mView.onGetRecentContactResults(result, t)
                }

            })
    }
}