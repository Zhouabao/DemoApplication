package com.example.demoapplication.presenter

import com.example.demoapplication.api.Api
import com.example.demoapplication.model.BlockListBean
import com.example.demoapplication.presenter.view.BlockSquareView
import com.example.demoapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/7/2114:41
 *    desc   :
 *    version: 1.0
 */
class BlockSquarePresenter : BasePresenter<BlockSquareView>() {

    fun squarePhotosList(params: HashMap<String, Any>) {
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