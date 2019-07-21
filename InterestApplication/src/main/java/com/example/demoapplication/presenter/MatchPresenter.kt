package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.MatchListBean
import com.example.demoapplication.model.StatusBean
import com.example.demoapplication.presenter.view.MatchView
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/6/2117:26
 *    desc   :
 *    version: 1.0
 */
class MatchPresenter : BasePresenter<MatchView>() {

    /**
     * 根据标签来获取新的用户数据
     */
    fun getMatchList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getMatchList(params)
            .excute(object : BaseSubscriber<BaseResp<MatchListBean?>>(mView) {
                override fun onStart() {

                    //todo showloading
                }


                override fun onNext(t: BaseResp<MatchListBean?>) {
                    if (t.code == 200) {
                        if (t.data != null && t.data!!.list != null)
                            mView.onGetMatchListResult(true, t.data)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetMatchListResult(false, t.data)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onGetMatchListResult(false, null)
                }
            })

    }

    /**
     * 不喜欢
     */
    fun dislikeUser(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .dontLike(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetDislikeResult(true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetDislikeResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 喜欢
     */
    fun likeUser(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .addLike(params)
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        mView.onGetLikeResult(true,t.data?:null)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetLikeResult(false,null)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }
}