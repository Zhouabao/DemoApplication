package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.ContactDataBean
import com.sdy.jitangapplication.presenter.view.ContactBookView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/8/613:59
 *    desc   :
 *    version: 1.0
 */
class ContactBookPresenter : BasePresenter<ContactBookView>() {

    /**
     * 获取通讯录
     */
    fun getContactLists(param: HashMap<String, String>) {
        val params = UserManager.getBaseParams()
        params.putAll(param)
        RetrofitFactory.instance.create(Api::class.java)
            .getContactLists(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<ContactDataBean?>>(mView) {
                override fun onNext(t: BaseResp<ContactDataBean?>) {
                    if (t.code == 200) {
                        mView.onGetContactListResult(t.data)
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
}