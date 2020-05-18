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
import com.sdy.jitangapplication.model.loginOffCauseBean
import com.sdy.jitangapplication.presenter.view.ChangeAccountView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/8/19:44
 *    desc   :
 *    version: 1.0
 */
class ChangeAccountPresenter : BasePresenter<ChangeAccountView>() {

    /**
     * 更改手机号
     */
    fun changeAccount(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .changeAccount(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<Any>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.onChangeAccountResult(true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onChangeAccountResult(false)
                    }
                    CommonFunction.toast(t.msg)
                }

                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }

    /**
     * 发送验证码
     */
    fun sendSms(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .sendSms(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<Any>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.onSendSmsResult(true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onSendSmsResult(false)
                    }
                    CommonFunction.toast(t.msg)
                }

                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }


    /**
     * 获取注销原因
     */
    fun getCauseList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getCauseList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<loginOffCauseBean>>(null) {
                override fun onStart() {
                    mView.showLoading()

                }

                override fun onNext(t: BaseResp<loginOffCauseBean>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.onCauseListResult(t.data)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        CommonFunction.toast(t.msg)
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