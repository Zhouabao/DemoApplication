package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.presenter.view.SweetHeartInfoView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/9/1517:23
 *    desc   :
 *    version: 1.0
 */
class SweetHeartInfoPresenter : BasePresenter<SweetHeartInfoView>() {

    /**
     * 获取某一广场详情
     */
    fun getSquareInfo(square_id: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareInfo(UserManager.getSignParams(hashMapOf("square_id" to square_id)))
            .excute(object : BaseSubscriber<BaseResp<SquareBean?>>(mView) {
                override fun onNext(t: BaseResp<SquareBean?>) {
                    if (t.code == 200) {
                        mView.onGetSquareInfoResults(t.data)
                    } else {
                        mView.onGetSquareInfoResults(null)
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