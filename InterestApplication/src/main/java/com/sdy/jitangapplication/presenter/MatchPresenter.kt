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
import com.sdy.jitangapplication.model.MatchListBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.presenter.view.MatchView
import com.sdy.jitangapplication.ui.dialog.ChargeLabelDialog
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
     * 根据兴趣来获取新的用户数据
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
                    } else if (t.code == 410) {
                        ChargeLabelDialog(context, params["tag_id"] as Int, ChargeLabelDialog.FROM_INDEX).show()
                        mView.onGetMatchListResult(true, t.data)

                    } else
                        mView.onGetMatchListResult(false, t.data)
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
}