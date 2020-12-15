package com.sdy.jitangapplication.presenter

import android.app.Activity
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.presenter.view.CustomChatUpContentView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/11/2611:59
 *    desc   :
 *    version: 1.0
 */
class CustomChatUpContentPresenter : BasePresenter<CustomChatUpContentView>() {
    /**
     * 保存注册信息
     */
    fun saveChatupMsg(aboutme: String) {
        val params = hashMapOf<String, Any>()
        params["content"] = aboutme

        RetrofitFactory.instance.create(Api::class.java)
            .saveChatupMsg(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any>>(mView) {
                override fun onStart() {
                    super.onStart()
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<Any>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.onSaveChatupMsg(true, t.msg)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onSaveChatupMsg(false, t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.onSaveChatupMsg(false, CommonFunction.getErrorMsg(context))
                    }
                }
            })
    }
}