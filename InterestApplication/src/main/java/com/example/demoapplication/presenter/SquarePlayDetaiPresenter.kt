package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.model.SquareRecentlyListBean
import com.example.demoapplication.presenter.view.SquarePlayDetailView
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/7/417:57
 *    desc   :
 *    version: 1.0
 */
class SquarePlayDetaiPresenter : BasePresenter<SquarePlayDetailView>() {


    /**
     * 获取最近的好友动态列表
     */
    fun getRencentlySquares(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getLatelySquareInfo(params)
            .excute(object : BaseSubscriber<BaseResp<SquareRecentlyListBean?>>(mView) {
                override fun onNext(t: BaseResp<SquareRecentlyListBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        if (t != null && t.data != null && t.data!!.list != null && t.data!!.list!!.size > 0) {
                            mView.onGetRecentlySquaresResults(t.data!!.list!!)
                        }
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 获取某一广场详情
     */
    fun getSquareInfo(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareInfo(params)
            .excute(object : BaseSubscriber<BaseResp<SquareBean?>>(mView) {
                override fun onNext(t: BaseResp<SquareBean?>) {
                    if (t.code == 200) {
                        mView.onGetSquareInfoResults(t.data)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 点赞 取消点赞
     * 1 点赞 2取消点赞
     */
    fun getSquareLike(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareLike(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onGetSquareLikeResult(position, true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareLikeResult(position, false)
                    }

                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }

    /**
     * 点赞 取消点赞
     * 1 收藏 2取消收藏
     */
    fun getSquareCollect(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareCollect(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareCollectResult(position, t)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareCollectResult(position, t)
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
    fun addComment(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .addComment(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onAddCommentResult(position, t)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onAddCommentResult(position, t)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 广场删除
     */
    fun removeMySquare(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .removeMySquare(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onRemoveMySquareResult(true, position)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onRemoveMySquareResult(false, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onRemoveMySquareResult(false, position)
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
                        mView.onGetSquareReport(true)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareReport(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onGetSquareReport(false)
//                    mView.onError(context.getString(R.string.service_error))
                }
            })
    }

}