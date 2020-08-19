package com.sdy.jitangapplication.presenter

import android.app.Activity
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.model.LikeBean
import com.sdy.jitangapplication.presenter.view.DatingDetailView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/8/1711:48
 *    desc   :
 *    version: 1.0
 */
class DatingDetailPresenter : BasePresenter<DatingDetailView>() {

    fun datingInfo(dating_id: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .datingInfo(UserManager.getSignParams(hashMapOf<String, Any>("dating_id" to dating_id)))
            .excute(object : BaseSubscriber<BaseResp<DatingBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<DatingBean?>) {
                    super.onNext(t)
                    if (t.code == 400) {
                        CommonFunction.toast(t.msg)
                        (context as Activity).finish()
                    } else
                        mView.datingInfoResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    mView.datingInfoResult(null)
                }
            })
    }


    fun doLike(dating_id: Int, type: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .doLike(
                UserManager.getSignParams(
                    hashMapOf<String, Any>(
                        "dating_id" to dating_id,
                        "type" to type
                    )
                )
            )
            .excute(object : BaseSubscriber<BaseResp<LikeBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<LikeBean?>) {
                    super.onNext(t)
                    //likeState
                    if (t.code == 200) {
                        mView.doLikeResult(t.code == 200, t.data!!.isliked)
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }

    fun reportDating(dating_id: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .reportDating(UserManager.getSignParams(hashMapOf<String, Any>("dating_id" to dating_id)))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    loadingDialog.dismiss()
                    CommonFunction.toast(t.msg)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                }
            })
    }

    private val loadingDialog by lazy { LoadingDialog(context) }

    fun delDating(dating_id: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .delDating(UserManager.getSignParams(hashMapOf<String, Any>("dating_id" to dating_id)))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    loadingDialog.dismiss()
                    mView.deleteResult(t.code == 200)
                }

                override fun onError(e: Throwable?) {
                    loadingDialog.dismiss()
                    super.onError(e)
                }
            })
    }
}