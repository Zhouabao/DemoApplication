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
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.presenter.view.MySquareView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/7/3112:02
 *    desc   : 我的动态
 *    version: 1.0
 */
class MySquarePresenter : BasePresenter<MySquareView>() {

    fun getMySquare(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .aboutMeSquare(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<SquareListBean?>>(mView) {
                override fun onNext(t: BaseResp<SquareListBean?>) {
                    if (t.code == 200) {
                        mView.onGetSquareListResult(t.data)
                    } else {
                        mView.onError("")
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
     * 获取推荐广场列表
     */
    fun squareEliteList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .squareEliteList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<RecommendSquareListBean?>>(mView) {
                override fun onStart() {

                }

                override fun onNext(t: BaseResp<RecommendSquareListBean?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareRecommendResult(t.data, true)
                    else
                        mView.onGetSquareRecommendResult(t.data, false)

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetSquareRecommendResult(null, false)
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