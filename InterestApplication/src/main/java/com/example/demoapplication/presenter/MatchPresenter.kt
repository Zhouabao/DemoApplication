package com.example.demoapplication.presenter

import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.GreetBean
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.model.MatchListBean
import com.example.demoapplication.model.StatusBean
import com.example.demoapplication.presenter.view.MatchView
import com.example.demoapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
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
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<MatchListBean?>) {
                    if (t.code == 200) {
                        if (t.data != null && t.data!!.list != null) {
                            mView.onGetMatchListResult(true, t.data)
                        }
                    } else {
                        mView.onGetMatchListResult(false, t.data)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetMatchListResult(false, null)
                }
            })

    }

    /**
     * 不喜欢
     */
    fun dislikeUser(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }

        RetrofitFactory.instance.create(Api::class.java)
            .dontLike(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetDislikeResult(true)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetDislikeResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 喜欢
     */
    fun likeUser(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .addLike(params)
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        mView.onGetLikeResult(true, t.data ?: null)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetLikeResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 打招呼
     */
    fun greet(token: String, accid: String, target_accid: String, tag_id: Int) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .greet(token, accid, target_accid, tag_id)
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        mView.onGreetSResult(true)
                    } else {
                        mView.onGreetSResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 判断当前能否打招呼
     */
    fun greetState(token: String, accid: String, target_accid: String, matchBean: MatchBean) {
        RetrofitFactory.instance.create(Api::class.java)
            .greetState(token, accid, target_accid)
            .excute(object : BaseSubscriber<BaseResp<GreetBean?>>(mView) {
                override fun onNext(t: BaseResp<GreetBean?>) {
                    if (t.code == 200) {
                        mView.onGreetStateResult(t.data, matchBean)
                    } else {
                        mView.onGreetStateResult(t.data, matchBean)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGreetStateResult(null, matchBean)
                }
            })
    }
}