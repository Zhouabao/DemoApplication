package com.sdy.jitangapplication.presenter

import com.blankj.utilcode.util.ToastUtils
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.presenter.view.SettingsView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/8/19:44
 *    desc   :
 *    version: 1.0
 */
class SettingsPresenter : BasePresenter<SettingsView>() {


    /**
     * 屏蔽通讯录
     */
    fun blockedAddressBook(accid: String, token: String, content: Array<String?>? = null) {
        RetrofitFactory.instance.create(Api::class.java)
            .blockedAddressBook(token, accid, content)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onBlockedAddressBookResult(true)
                    } else {
                        mView.onBlockedAddressBookResult(false)
                    }
                    ToastUtils.showShort(t.msg)

                }

                override fun onError(e: Throwable?) {
                    if (e != null && e is BaseException) {
                        TickDialog(context).show()
                    }

                }
            })
    }


    /**
     * 屏蔽距离
     */
    fun isHideDistance(accid: String, token: String, state: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .isHideDistance(token, accid, state)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onHideDistanceResult(true)
                    } else {
                        mView.onHideDistanceResult(false)
                    }
                    ToastUtils.showShort(t.msg)
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }

}