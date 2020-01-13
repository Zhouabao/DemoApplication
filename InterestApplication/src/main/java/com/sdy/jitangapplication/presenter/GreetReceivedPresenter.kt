package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.GreetedListBean
import com.sdy.jitangapplication.presenter.view.GreetReceivedView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class GreetReceivedPresenter : BasePresenter<GreetReceivedView>() {

    /**
     * 获取打招呼列表
     */
    fun greatLists(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .myGreetList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<GreetedListBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<GreetedListBean>?>) {
                    when {
                        t.code == 200 -> mView.onGreatListResult(t)
                        else -> mView.onError(t.msg)
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
     * 招呼的左右滑动
     */
    fun likeOrGreetState(greet_id: String, type: Int) {
        val params = hashMapOf<String, Any>()
        params["greet_id"] = greet_id
        params["type"] = type
        RetrofitFactory.instance.create(Api::class.java)
            .likeOrGreetState(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    when {
                        t.code == 200 -> mView.onLikeOrGreetStateResult(true, type)
                        else -> {
                            mView.onLikeOrGreetStateResult(false, type)
                            CommonFunction.toast(t.msg)
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onLikeOrGreetStateResult(false, type)
                }
            })
    }
}