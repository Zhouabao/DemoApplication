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
import com.sdy.jitangapplication.model.TagSquareListBean
import com.sdy.jitangapplication.presenter.view.TagDetailCategoryView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class TagDetailCategoryPresenter : BasePresenter<TagDetailCategoryView>() {

    /**
     * 获取推荐广场列表
     */
    fun squareTagInfo(params: HashMap<String, Any>) {
        if (UserManager.touristMode) {
            RetrofitFactory.instance.create(Api::class.java)
                .thresholdSquareTagInfo(UserManager.getSignParams(params))
                .excute(object : BaseSubscriber<BaseResp<TagSquareListBean?>>(mView) {
                    override fun onStart() {

                    }

                    override fun onNext(t: BaseResp<TagSquareListBean?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            mView.onGetSquareRecommendResult(t.data, true)
                        } else {
                            mView.onGetSquareRecommendResult(t.data, false)
                        }

                    }

                    override fun onError(e: Throwable?) {
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else {
                            mView.onGetSquareRecommendResult(null, false)
                        }
                    }
                })
        } else
            RetrofitFactory.instance.create(Api::class.java)
                .squareTagInfo(UserManager.getSignParams(params))
                .excute(object : BaseSubscriber<BaseResp<TagSquareListBean?>>(mView) {
                    override fun onStart() {

                    }

                    override fun onNext(t: BaseResp<TagSquareListBean?>) {
                        super.onNext(t)
                        if (t.code == 200) {
                            mView.onGetSquareRecommendResult(t.data, true)
                        } else {
                            mView.onGetSquareRecommendResult(t.data, false)
                        }

                    }

                    override fun onError(e: Throwable?) {
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else {
                            mView.onGetSquareRecommendResult(null, false)
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