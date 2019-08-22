package com.example.demoapplication.presenter

import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.api.Api
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.LikeMeListBean
import com.example.demoapplication.presenter.view.MessageLikeMeView
import com.example.demoapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
class MessageLikeMePresenter : BasePresenter<MessageLikeMeView>() {

    fun likeLists(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .likeLists(params)
            .excute(object : BaseSubscriber<BaseResp<LikeMeListBean?>>(mView) {
                override fun onNext(t: BaseResp<LikeMeListBean?>) {
                    if (t.code == 200) {
                        SPUtils.getInstance(Constants.SPNAME).put("isvip", t.data?.isvip ?: 0)
                        mView.onLikeListsResult(t.data?.list ?: mutableListOf())
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.onError("")
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError("")
                }
            })
    }


    /**
     * 标记喜欢我的为已读
     */
    fun markLikeRead(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .markLikeRead(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }
}