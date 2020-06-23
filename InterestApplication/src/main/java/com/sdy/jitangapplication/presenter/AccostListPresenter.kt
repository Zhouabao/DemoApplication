package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.AccostBean
import com.sdy.jitangapplication.model.AccostListBean
import com.sdy.jitangapplication.presenter.view.AccostListView
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/6/914:25
 *    desc   :
 *    version: 1.0
 */
class AccostListPresenter : BasePresenter<AccostListView>() {

    /**
     * 搭讪列表
     */
    fun chatupList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .chatupList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<AccostListBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<AccostListBean?>) {
                    super.onNext(t)
                    mView.onChatupListResult(t.data?.list?: mutableListOf())
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }



    /**
     * 获取云信最近联系人
     */
    fun getRecentContacts(data: MutableList<AccostBean>) {
        NIMClient.getService(MsgService::class.java)
            .queryRecentContacts()
            .setCallback(object : RequestCallbackWrapper<MutableList<RecentContact>>() {
                override fun onResult(code: Int, result: MutableList<RecentContact>?, exception: Throwable?) {
                    if (code != ResponseCode.RES_SUCCESS.toInt() || result == null) {
                        return
                    }
                    mView.onGetRecentContactResults(result,data)
                }

            })
    }
}