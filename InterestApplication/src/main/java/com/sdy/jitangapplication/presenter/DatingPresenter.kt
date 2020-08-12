package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.SquareTagBean
import com.sdy.jitangapplication.presenter.view.DatingView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class DatingPresenter : BasePresenter<DatingView>() {

    /**
     * 获取广场列表
     */
    fun getSquareList() {
        if (UserManager.touristMode) {
            RetrofitFactory.instance.create(Api::class.java)
                .thresholdSquareTagList(UserManager.getSignParams())
                .excute(object : BaseSubscriber<BaseResp<MutableList<SquareTagBean>?>>(mView) {

                    override fun onNext(t: BaseResp<MutableList<SquareTagBean>?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            mView.onGetSquareTagResult(t.data, true)
                        } else {
                            mView.onGetSquareTagResult(t.data, false)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else {
                            mView.onGetSquareTagResult(null, false)
                        }
                    }
                })
        } else
            RetrofitFactory.instance.create(Api::class.java)
                .squareTagList(UserManager.getSignParams())
                .excute(object : BaseSubscriber<BaseResp<MutableList<SquareTagBean>?>>(mView) {

                    override fun onNext(t: BaseResp<MutableList<SquareTagBean>?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            mView.onGetSquareTagResult(t.data, true)
                        } else {
                            mView.onGetSquareTagResult(t.data, false)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else {
                            mView.onGetSquareTagResult(null, false)
                        }
                    }
                })
    }


    /**
     * 标记置顶
     */
    fun markTag(tag_id: Int, type: Int) {
        val params = UserManager.getBaseParams()
        params["tag_id"] = tag_id
        params["type"] = type
        RetrofitFactory.instance.create(Api::class.java)
            .markTag(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetMarkTagResult(true)
                    else {
                        mView.onGetMarkTagResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetMarkTagResult(false)
                }
            })

    }


    /**
     * 获取今日意向
     */
    fun getIntention() {
        RetrofitFactory.instance.create(Api::class.java)
            .getDatingTypeList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MutableList<CheckBean>?>>() {
                override fun onNext(t: BaseResp<MutableList<CheckBean>?>) {
                    super.onNext(t)
                    mView.onGetIntentionResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    mView.onGetIntentionResult(null)
                }
            })
    }
}