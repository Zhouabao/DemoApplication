package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.presenter.view.SquareView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/6/2420:27
 *    desc   :
 *    version: 1.0
 */
class SquarePresenter : BasePresenter<SquareView>() {

    /**
     * 获取广场列表
     */
    fun squareNewestLists(
        params: HashMap<String, Any>,
        isRefresh: Boolean,
        firstIn: Boolean = false
    ) {
        if (UserManager.touristMode) {
            addDisposable(  RetrofitFactory.instance.create(Api::class.java)
                .thresholdSquareList(UserManager.getSignParams(params))
                .excute(object : BaseSubscriber<BaseResp<SquareListBean?>>(mView) {
                    override fun onStart() {
                        if (firstIn) {
                            mView.showLoading()
                        }
                    }

                    override fun onNext(t: BaseResp<SquareListBean?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            mView.onGetSquareListResult(t.data, true, isRefresh)
                        } else {
                            mView.onGetSquareListResult(t.data, false, isRefresh)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else {
                            mView.onGetSquareListResult(null, false, isRefresh)
                        }
                    }
                }))

        } else {
            addDisposable(    RetrofitFactory.instance.create(Api::class.java)
                .squareNewestLists(UserManager.getSignParams(params))
                .excute(object : BaseSubscriber<BaseResp<SquareListBean?>>(mView) {
                    override fun onStart() {
                        if (firstIn) {
                            mView.showLoading()
                        }
                    }

                    override fun onNext(t: BaseResp<SquareListBean?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            mView.onGetSquareListResult(t.data, true, isRefresh)
                        } else {
                            mView.onGetSquareListResult(t.data, false, isRefresh)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else {
                            mView.onGetSquareListResult(null, false, isRefresh)
                        }
                    }
                }))

        }
    }

}