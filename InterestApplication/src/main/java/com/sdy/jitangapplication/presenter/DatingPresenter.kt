package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.presenter.view.DatingView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class DatingPresenter : BasePresenter<DatingView>() {

    /**
     * 获取广场列表
     */
    fun getDatingList(page: Int, type_id: Int) {
        if (UserManager.touristMode) {
            RetrofitFactory.instance.create(Api::class.java)
                .datingList(
                    UserManager.getSignParams(
                        hashMapOf(
                            "page" to page,
                            "pagesize" to Constants.PAGESIZE
                        )
                    )
                )
                .excute(object : BaseSubscriber<BaseResp<MutableList<DatingBean>?>>(mView) {

                    override fun onNext(t: BaseResp<MutableList<DatingBean>?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            mView.onGetSquareDatingResult(t.data, true)
                        } else {
                            mView.onGetSquareDatingResult(t.data, false)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else {
                            mView.onGetSquareDatingResult(null, false)
                        }
                    }
                })
        } else
            RetrofitFactory.instance.create(Api::class.java)
                .datingList(
                    UserManager.getSignParams(
                        hashMapOf(
                            "page" to page,
                            "type_id" to type_id,
                            "pagesize" to Constants.PAGESIZE
                        )
                    )
                )
                .excute(object : BaseSubscriber<BaseResp<MutableList<DatingBean>?>>(mView) {

                    override fun onNext(t: BaseResp<MutableList<DatingBean>?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            mView.onGetSquareDatingResult(t.data, true)
                        } else {
                            mView.onGetSquareDatingResult(t.data, false)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else {
                            mView.onGetSquareDatingResult(null, false)
                        }
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