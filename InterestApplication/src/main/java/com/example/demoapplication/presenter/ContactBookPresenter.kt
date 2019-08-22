package com.example.demoapplication.presenter

import com.example.demoapplication.api.Api
import com.example.demoapplication.model.ContactDataBean
import com.example.demoapplication.presenter.view.ContactBookView
import com.example.demoapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber

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
    fun getContactLists(params: HashMap<String, String>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getContactLists(params)
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
                        mView.onError("")
                }
            })
    }
}