package com.example.demoapplication.presenter

import android.app.Activity
import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.api.Api
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.SquareMsgListBean
import com.example.demoapplication.presenter.view.MessageLikeMeView
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
class MessageLikeMePresenter : BasePresenter<MessageLikeMeView>() {

    fun likeLists(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .likeLists(params)
            .excute(object : BaseSubscriber<BaseResp<SquareMsgListBean?>>(mView) {
                override fun onNext(t: BaseResp<SquareMsgListBean?>) {
                    if (t.code == 200) {
                        SPUtils.getInstance(Constants.SPNAME).put("isvip", t.data?.isvip ?: 0)
                        mView.onLikeListsResult(t.data?.list ?: mutableListOf())
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