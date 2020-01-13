package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.AccountBean
import com.sdy.jitangapplication.presenter.view.AccountAboutView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/8/19:44
 *    desc   :
 *    version: 1.0
 */
class AccountAboutPresenter : BasePresenter<AccountAboutView>() {


    /**
     * 获取账户信息
     */
    fun getAccountInfo() {
        RetrofitFactory.instance.create(Api::class.java)
            .getAccountInfo(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<AccountBean>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<AccountBean>) {
                    if (t.code == 200) {
                        mView.hideLoading()
                        mView.getAccountResult(t.data)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(t.msg)
                    }

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(CommonFunction.getErrorMsg(context))
                }
            })
    }

    /**
     * 微信解绑
     */
    fun unbundWeChat() {
        RetrofitFactory.instance.create(Api::class.java)
            .unbundWeChat(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<Any>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any>) {
                    if (t.code == 200) {
                        mView.unbundWeChatResult(true)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.unbundWeChatResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }

}