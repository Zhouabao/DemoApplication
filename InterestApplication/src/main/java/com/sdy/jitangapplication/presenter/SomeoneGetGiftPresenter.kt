package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.SomeoneGiftBean
import com.sdy.jitangapplication.presenter.view.SomeoneGetGiftView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/4/214:49
 *    desc   :
 *    version: 1.0
 */
class SomeoneGetGiftPresenter : BasePresenter<SomeoneGetGiftView>() {

    fun getSomeoneGiftList(target_accid: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSomeoneGiftList(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
            .excute(object : BaseSubscriber<BaseResp<SomeoneGiftBean?>>(mView) {
                override fun onNext(t: BaseResp<SomeoneGiftBean?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSomeoneGiftList(true, t.data)
                    else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.onGetSomeoneGiftList(false, null)
                    }

                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onGetSomeoneGiftList(false, null)
                    }
                }
            })
    }

}