package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.LikeMeBean
import com.example.demoapplication.model.LikeMeOneDayBean
import com.example.demoapplication.presenter.view.MessageLikeMeOneDayView
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
class MessageLikeMeOneDayPresenter : BasePresenter<MessageLikeMeOneDayView>() {

    fun likeListsCategory(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .likeListsCategory(params)
            .excute(object : BaseSubscriber<BaseResp<MutableList<LikeMeOneDayBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<LikeMeOneDayBean>?>) {
                    if (t.code == 200) {
                        mView.onLikeListResult(t.data ?: mutableListOf())
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