package com.example.demoapplication.presenter

import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.GreetBean
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.model.StatusBean
import com.example.demoapplication.presenter.view.MatchDetailView
import com.example.demoapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
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
            .excute(object : BaseSubscriber<BaseResp<MatchBean?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<MatchBean?>) {
                    if (t.code == 200) {
                        mView.onGetMatchDetailResult(true, t.data)
                    } else {
                        mView.onGetMatchDetailResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetMatchDetailResult(false, null)
                }
            })

    }

    /**
     * 拉黑用户
     */
    fun shieldingFriend(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .shieldingFriend(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetUserActionResult(false, "")
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                        mView.onGetUserActionResult(false, null)
                    }
                }
            })

    }

    /**
     * 举报用户
     */
    fun reportUser(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .reportUser(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    }else {
                        mView.onError(t.msg)
                        mView.onGetUserActionResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {

                        mView.onError(context.getString(R.string.service_error))
                        mView.onGetUserActionResult(false, null)
                    }
                }
            })

    }

    /*
     * 解除匹配
     */
    fun dissolutionFriend(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .dissolutionFriend(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    } else {
                        mView.onError(t.msg)
                        mView.onGetUserActionResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                        mView.onGetUserActionResult(false, null)
                    }
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
                        mView.onGetLikeResult(true, t)
                    }else {
                        mView.onError(t.msg)
                        mView.onGetLikeResult(false, t)
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
                        ToastUtils.showShort(t.msg)
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
    fun greetState(token: String, accid: String, target_accid: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .greetState(token, accid, target_accid)
            .excute(object : BaseSubscriber<BaseResp<GreetBean?>>(mView) {
                override fun onNext(t: BaseResp<GreetBean?>) {
                    if (t.code == 200) {
                        mView.onGreetStateResult(t.data)
                    } else {
                        mView.onGreetStateResult(t.data)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGreetStateResult(null)
                }
            })
    }
}