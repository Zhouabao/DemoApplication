package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.AllCommentBean
import com.example.demoapplication.presenter.view.SquareDetailView
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/6/2717:22
 *    desc   :
 *    version: 1.0
 */
class SquareDetailPresenter : BasePresenter<SquareDetailView>() {

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


    /**
     * 点赞 取消点赞
     * 1 点赞 2取消点赞
     */
    fun getSquareLike(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareLike(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onGetSquareLikeResult(true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareLikeResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }

    /**
     * 收藏
     * 1 收藏 2取消收藏
     */
    fun getSquareCollect(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareCollect(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareCollectResult(t)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareCollectResult(t)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 广场举报
     */
    fun getSquareReport(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareReport(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareReport(t)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareReport(t)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 添加评论
     */
    fun addComment(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .addComment(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onAddCommentResult(t, true)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onAddCommentResult(t, false)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 评论点赞
     * 1 点赞 2取消点赞
     */
    fun getCommentLike(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .commentLikes(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onLikeCommentResult(t, position)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onLikeCommentResult(t, position)
                    }
                }

                override fun onError(e: Throwable?) {

                    mView.onError(context.getString(R.string.service_error))

                }
            })
    }


    /**
     * 删除评论
     */
    fun deleteComment(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .destoryComment(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onDeleteCommentResult(t, position)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onDeleteCommentResult(t, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))


                }
            })
    }


    /**
     * 评论举报
     */
    fun commentReport(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .commentReport(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onReportCommentResult(t, position)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onReportCommentResult(t, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }


}