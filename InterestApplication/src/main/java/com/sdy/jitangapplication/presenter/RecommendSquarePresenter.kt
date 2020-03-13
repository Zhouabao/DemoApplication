package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.presenter.view.RecommendSquareView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class RecommendSquarePresenter : BasePresenter<RecommendSquareView>() {

    /**
     * 获取推荐广场列表
     */
    fun squareEliteList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .squareEliteList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<RecommendSquareListBean?>>(mView) {
                override fun onStart() {

                }

                override fun onNext(t: BaseResp<RecommendSquareListBean?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareRecommendResult(t.data, true)
                    else
                        mView.onGetSquareRecommendResult(t.data, false)

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetSquareRecommendResult(null, false)
                }
            })
    }


}