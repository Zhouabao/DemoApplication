package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.presenter.view.MyCollectionView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/7/3112:02
 *    desc   : 我的动态
 *    version: 1.0
 */
class MyCollectionPresenter : BasePresenter<MyCollectionView>() {

    fun getMySquare(params: HashMap<String, Any>) {

        RetrofitFactory.instance.create(Api::class.java)
            .aboutMeSquare(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<SquareListBean?>>(mView) {
                override fun onNext(t: BaseResp<SquareListBean?>) {
                    if (t.code == 200) {
                        mView.onGetSquareListResult(t.data)
                    } else {
                        mView.onError("")
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    } else
                        mView.onError("")
                }
            })
    }



}