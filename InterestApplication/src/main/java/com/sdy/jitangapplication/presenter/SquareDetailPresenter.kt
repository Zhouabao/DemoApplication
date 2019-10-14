package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.AllCommentBean
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.presenter.view.SquareDetailView
import com.sdy.jitangapplication.ui.dialog.TickDialog

/**
 *    author : ZFM
 *    date   : 2019/6/2717:22
 *    desc   :
 *    version: 1.0
 */
class SquareDetailPresenter : BasePresenter<SquareDetailView>() {
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
                    } else {
                        mView.onGetSquareInfoResults(null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 获取评论列表
     */
    fun getCommentList(params: HashMap<String, Any>, refresh: Boolean) {
        RetrofitFactory.instance.create(Api::class.java)
            .getCommentLists(params)
            .excute(object : BaseSubscriber<BaseResp<AllCommentBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<AllCommentBean?>) {
                    super.onNext(t)
                    if (t.code == 200 && t.data != null) {
                        mView.onGetCommentListResult(t.data!!, refresh)
                    } else  {
                        mView.onError(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {

                        mView.onError(context.getString(R.string.service_error))
                        mView.onGetCommentListResult(null, refresh)
                    }
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
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onGetSquareLikeResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                    }
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
                    else {
                        mView.onGetSquareCollectResult(t)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
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
                    else  {
                        mView.onGetSquareReport(t)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
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
                    else  {
                        CommonFunction.toast(t.msg)
                        mView.onAddCommentResult(t, false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
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
                    else  {
                        mView.onLikeCommentResult(t, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
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
                    else {
                        mView.onDeleteCommentResult(t, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
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
                    else  {
                        mView.onReportCommentResult(t, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 广场删除
     */
    fun removeMySquare(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .removeMySquare(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onRemoveMySquareResult(true)
                    else {
                        mView.onRemoveMySquareResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onRemoveMySquareResult(false)
                        mView.onError(context.getString(R.string.service_error))
                    }
                }
            })
    }





    /**
     * 打招呼
     */
    fun greet(token: String, accid: String, target_accid: String, tag_id: Int) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .greet(token, accid, target_accid, tag_id)
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        mView.onGreetSResult(true)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onGreetSResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 判断当前能否打招呼
     */
    fun greetState(token: String, accid: String, target_accid: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .greetState(token, accid, target_accid)
            .excute(object : BaseSubscriber<BaseResp<GreetBean?>>(mView) {
                override fun onNext(t: BaseResp<GreetBean?>) {
                    if (t.code == 200) {
                        mView.onGreetStateResult(t.data)
                    } else {
                        mView.onGreetStateResult(t.data)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGreetStateResult(null)
                }
            })
    }
}