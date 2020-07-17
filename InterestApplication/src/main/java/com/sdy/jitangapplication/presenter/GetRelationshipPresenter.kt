package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.MyTapsBean
import com.sdy.jitangapplication.presenter.view.GetRelationshipView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/5/714:11
 *    desc   :
 *    version: 1.0
 */
class GetRelationshipPresenter : BasePresenter<GetRelationshipView>() {

    private val loading by lazy { LoadingDialog(context) }

    fun getMyTaps() {
        RetrofitFactory.instance.create(Api::class.java)
            .getMyTaps(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MutableList<MyTapsBean>?>>() {
                override fun onStart() {
                    super.onStart()
                    loading.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loading.dismiss()
                }

                override fun onNext(t: BaseResp<MutableList<MyTapsBean>?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetMyTaps(t.data ?: mutableListOf<MyTapsBean>())
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loading.dismiss()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }


    fun addWant(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .addWant(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MoreMatchBean?>>() {

                override fun onStart() {
                    super.onStart()
                    loading.show()
                }
                override fun onNext(t: BaseResp<MoreMatchBean?>) {
                    super.onNext(t)
                    mView.onAddWant(t.code == 200,t.data)
                    loading.dismiss()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loading.dismiss()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }
}