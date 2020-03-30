package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.ProductMsgBean
import com.sdy.jitangapplication.presenter.view.MessageView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/3/2517:54
 *    desc   :
 *    version: 1.0
 */
class MessagePresenter : BasePresenter<MessageView>() {
    /**
     * 想要商品列表
     */
    fun goodsMsgList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .goodsMsgList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<ProductMsgBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<ProductMsgBean>?>) {
                    super.onNext(t)
                    mView.onGoodsMsgListList(t.code == 200, t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onGoodsMsgListList(false, null)
                    }
                }
            })
    }
}