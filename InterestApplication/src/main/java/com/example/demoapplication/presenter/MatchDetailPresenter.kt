package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.MatchUserDetailBean
import com.example.demoapplication.presenter.view.MatchDetailView
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/6/2610:48
 *    desc   :
 *    version: 1.0
 */
class MatchDetailPresenter : BasePresenter<MatchDetailView>() {


    /**
     * 获取详细的用户数据 包括用户的广场信息
     */
    fun getUserDetailInfo(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getMatchUserInfo(params)
            .excute(object : BaseSubscriber<BaseResp<MatchUserDetailBean?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<MatchUserDetailBean?>) {
                    if (t.code == 200) {
                        mView.onGetMatchDetailResult(true, t.data)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetMatchDetailResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onGetMatchDetailResult(false, null)
                }
            })

    }

    /**
     * 拉黑用户
     */
    fun shieldingFriend(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .shieldingFriend(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetUserActionResult(false, "")
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onGetUserActionResult(false, null)
                }
            })

    }

    /**
     * 拉黑用户
     */
    fun reportUser(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .reportUser(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetUserActionResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onGetUserActionResult(false, null)
                }
            })

    }

    /*
     * 解除匹配
     */
    fun dissolutionFriend(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .dissolutionFriend(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetUserActionResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onGetUserActionResult(false, null)
                }
            })

    }
}