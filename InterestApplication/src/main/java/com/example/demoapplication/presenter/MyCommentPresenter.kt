package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.AllCommentBean
import com.example.demoapplication.presenter.view.MyCommentView
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/7/3120:55
 *    desc   :
 *    version: 1.0
 */
class MyCommentPresenter : BasePresenter<MyCommentView>() {



    /**
     * 获取评论列表
     */
    fun getCommentList(params: HashMap<String, Any>, refresh: Boolean) {
        RetrofitFactory.instance.create(Api::class.java)
            .getCommentLists(params)
            .excute(object : BaseSubscriber<BaseResp<AllCommentBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                    //todo showLoading
                }

                override fun onNext(t: BaseResp<AllCommentBean?>) {
                    super.onNext(t)
                    if (t.code == 200 && t.data != null) {
                        mView.onGetCommentListResult(t.data!!, refresh)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onGetCommentListResult(null, refresh)
                }
            })
    }

}