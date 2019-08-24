package com.example.demoapplication.presenter

import com.example.demoapplication.api.Api
import com.example.demoapplication.model.BlackBean
import com.example.demoapplication.presenter.view.BlackListView
import com.example.demoapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber

/**
 * author : ZFM
 * date   : 2019/8/2319:30
 * desc   :
 * version: 1.0
 */
class BlackListPresenter : BasePresenter<BlackListView>() {
    /**
     * 获取黑名单list
     */
    fun myShieldingList(hashMap: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .myShieldingList(hashMap)
            .excute(object : BaseSubscriber<BaseResp<MutableList<BlackBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<BlackBean>?>) {
                    if (t.code == 200) {
                        mView.onMyShieldingListResult(t.data)
                    } else {
                        mView.onError(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError("")
                    }
                }
            })
    }
    /**
     * 解除拉黑
     */
    fun removeBlock(hashMap: HashMap<String, Any>,position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .removeBlock(hashMap)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onRemoveBlockResult(true,position)
                    } else {
                        mView.onRemoveBlockResult(false,position)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onRemoveBlockResult(false,position)
                    }
                }
            })
    }

}
