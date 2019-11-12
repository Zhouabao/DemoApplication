package com.sdy.jitangapplication.presenter

import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.MatchListBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.presenter.view.MatchView
import com.sdy.jitangapplication.ui.dialog.HarassmentDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

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
    fun getMatchList(params: HashMap<String, Any>, exclude: MutableList<Int>? = mutableListOf()) {
        params["exclude"] = Gson().toJson(exclude)
        RetrofitFactory.instance.create(Api::class.java)
            .getMatchList(UserManager.getSignParams(params))
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
            .dontLike(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        mView.onGetDislikeResult(true, t)
                    } else {
                        mView.onGetDislikeResult(false, t)
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
    fun likeUser(params: HashMap<String, Any>, matchBean: MatchBean) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .addLike(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        mView.onGetLikeResult(true, t, matchBean)
                    } else {
                        mView.onGetLikeResult(false, t, matchBean)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(CommonFunction.getErrorMsg(context))
                }
            })
    }


    /**
     * 打招呼
     */
    fun greet(token: String, accid: String, target_accid: String, tag_id: Int, matchBean: MatchBean) {
        if (!checkNetWork()) {
            return
        }

        val params = UserManager.getBaseParams()
        params["tag_id"] = tag_id
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .greet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        mView.onGreetSResult(true, t.code, matchBean)
                    } else if (t.code == 401) {
                        HarassmentDialog(context, HarassmentDialog.CHATHI).show()
                        mView.onGreetSResult(false, t.code, matchBean)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onGreetSResult(false, t.code, matchBean)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(CommonFunction.getErrorMsg(context))
                }
            })
    }


    /**
     * 判断当前能否打招呼
     */
    fun greetState(token: String, accid: String, target_accid: String, matchBean: MatchBean) {
        val params = UserManager.getBaseParams()
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .greetState(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<GreetBean?>>(mView) {
                override fun onNext(t: BaseResp<GreetBean?>) {
                    if (t.code == 200) {
                        mView.onGreetStateResult(t.data, matchBean)
                    } else {
                        mView.onGreetStateResult(t.data, matchBean)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onGreetStateResult(null, matchBean)
                        CommonFunction.toast("请求失败，请重试")
                    }
                }
            })
    }
}