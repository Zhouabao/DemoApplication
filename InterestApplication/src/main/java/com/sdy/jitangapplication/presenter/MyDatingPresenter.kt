package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.presenter.view.MyDatingView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/7/3112:02
 *    desc   : 我的动态
 *    version: 1.0
 */
class MyDatingPresenter : BasePresenter<MyDatingView>() {

    fun myDating(){
        addDisposable(
            RetrofitFactory.instance.create(Api::class.java)
                .myDating(UserManager.getSignParams())
                .excute(object : BaseSubscriber<BaseResp<MutableList<DatingBean>?>>(mView) {

                    override fun onNext(t: BaseResp<MutableList<DatingBean>?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            mView.onGetMyDatingResult(t.data)
                        } else {
                            mView.onGetMyDatingResult(t.data)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else {
                            mView.onGetMyDatingResult(null)
                        }
                    }
                })
        )

    }

}