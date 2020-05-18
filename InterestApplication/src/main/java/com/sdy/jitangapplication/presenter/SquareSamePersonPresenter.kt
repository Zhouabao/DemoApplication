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
import com.sdy.jitangapplication.model.SamePersonListBean
import com.sdy.jitangapplication.presenter.view.SquareSamePersonView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class SquareSamePersonPresenter : BasePresenter<SquareSamePersonView>() {
    /**
     * 获取当前标题下的广场内容
     */
    fun getTitleInfo(page: Int, title_id: Int) {
        val params by lazy { hashMapOf<String, Any>("page" to page, "title_id" to title_id) }

        RetrofitFactory.instance.create(Api::class.java)
            .getTitleInfo(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<SamePersonListBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<SamePersonListBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onGetTitleInfoResult(true, t.data)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)

                    } else {
                        mView.onGetTitleInfoResult(false, null)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {

                        mView.onGetTitleInfoResult(false, null)
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    }
                }
            })
    }


    /**
     * 获取广场列表
     */
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(context) }

    fun checkBlock() {
        RetrofitFactory.instance.create(Api::class.java)
            .checkBlock(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    if (!loadingDialog.isShowing)
                        loadingDialog.show()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (loadingDialog.isShowing)
                        loadingDialog.dismiss()
                    if (t.code == 200)
                        mView.onCheckBlockResult(true)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onCheckBlockResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (loadingDialog.isShowing)
                        loadingDialog.dismiss()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.onCheckBlockResult(false)
                    }
                }
            })
    }
}