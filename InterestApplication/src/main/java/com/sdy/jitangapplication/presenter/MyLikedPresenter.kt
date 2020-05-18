package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.MyLikedBean
import com.sdy.jitangapplication.presenter.view.MyLikedView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/7/3120:55
 *    desc   :
 *    version: 1.0
 */
class MyLikedPresenter : BasePresenter<MyLikedView>() {
    /**
     * 获取我喜欢的列表
     */
    fun myLikedLis(params: HashMap<String, Any>, refresh: Boolean) {
        RetrofitFactory.instance.create(Api::class.java)
            .myLikedLis(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<MyLikedBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<MyLikedBean>?>) {
                    super.onNext(t)
                    if (t.code == 200 && t.data != null) {
                        mView.onGetCommentListResult(t.data?: mutableListOf<MyLikedBean>(), refresh)
                    } else  {
                        mView.onError(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                    }
                }
            })
    }

}