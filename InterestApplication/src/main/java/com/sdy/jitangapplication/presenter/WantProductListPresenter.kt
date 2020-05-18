package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.WantFriendBean
import com.sdy.jitangapplication.presenter.view.WantProductListView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/3/2517:37
 *    desc   :
 *    version: 1.0
 */
class WantProductListPresenter : BasePresenter<WantProductListView>() {

    /**
     * 想要商品列表
     */
    fun goodsWishList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .goodsWishList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<WantFriendBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<WantFriendBean>?>) {
                    super.onNext(t)
                    mView.onGoodsWishList(t.code == 200, t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onGoodsWishList(false, null)
                    }
                }
            })
    }

}