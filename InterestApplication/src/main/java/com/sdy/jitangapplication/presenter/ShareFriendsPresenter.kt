package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.MyInviteBean
import com.sdy.jitangapplication.presenter.view.ShareFriendsView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/7/19:27
 *    desc   :
 *    version: 1.0
 */
class ShareFriendsPresenter : BasePresenter<ShareFriendsView>() {

    private val loadingDialog by lazy { LoadingDialog(context) }
    fun myInvite() {
        RetrofitFactory.instance.create(Api::class.java)
            .myInvite(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MyInviteBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<MyInviteBean?>) {
                    super.onNext(t)
                    loadingDialog.dismiss()
                    mView.myInviteResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                }
            })
    }

}