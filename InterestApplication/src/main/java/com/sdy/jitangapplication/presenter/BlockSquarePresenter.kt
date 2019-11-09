package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.BlockListBean
import com.sdy.jitangapplication.presenter.view.BlockSquareView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/7/2114:41
 *    desc   :
 *    version: 1.0
 */
class BlockSquarePresenter : BasePresenter<BlockSquareView>() {

    fun squarePhotosList(params: HashMap<String, Any>) {
        params.putAll(UserManager.getBaseParams())

        RetrofitFactory.instance.create(Api::class.java)
            .squarePhotosList(params)
            .excute(object : BaseSubscriber<BaseResp<BlockListBean?>>(mView) {

                override fun onNext(t: BaseResp<BlockListBean?>) {
                    if (t.code == 200 && t.data != null) {
                        mView.getBlockSquareResult(true, t.data!!.list ?: mutableListOf())
                    } else {
                        mView.getBlockSquareResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.getBlockSquareResult(false, null)
                }

            })
    }
}