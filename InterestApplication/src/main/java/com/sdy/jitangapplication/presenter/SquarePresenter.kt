package com.sdy.jitangapplication.presenter

import android.app.Activity
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.FriendListBean
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.presenter.view.SquareView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/6/2420:27
 *    desc   :
 *    version: 1.0
 */
class SquarePresenter : BasePresenter<SquareView>() {
    /**
     * 获取广场列表中的好友列表
     */
    fun getFrinedsList(params: HashMap<String, String>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareFriends(params)
            .excute(object : BaseSubscriber<BaseResp<FriendListBean?>>(mView) {
                override fun onNext(t: BaseResp<FriendListBean?>) {
                    super.onNext(t)
                    if (t.code == 200 && t.data != null)
                        mView.onGetFriendsListResult(t.data!!.list ?: mutableListOf())
                }

                override fun onError(e: Throwable?) {
                    mView.onGetFriendsListResult(mutableListOf())
                }
            })
    }

    /**
     * 获取广场列表
     */
    fun getSquareList(params: HashMap<String, Any>, isRefresh: Boolean, firstIn: Boolean = false) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareList(params)
            .excute(object : BaseSubscriber<BaseResp<SquareListBean?>>(mView) {
                override fun onStart() {
                    if (firstIn) {
                        mView.showLoading()
                    }
                }

                override fun onNext(t: BaseResp<SquareListBean?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareListResult(t.data, true, isRefresh)
                    else  {
                        mView.onGetSquareListResult(t.data, false, isRefresh)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetSquareListResult(null, false, isRefresh)
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
                        TickDialog(context).show()
                    } else {
                        mView.onGetSquareLikeResult(position, false)
                    }

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                        mView.onGetSquareLikeResult(position, false)
                    }
                }
            })
    }

    /**
     * 收藏
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
                        TickDialog(context).show()
                    } else {
                        mView.onGetSquareCollectResult(position, t)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                        mView.onGetSquareCollectResult(position, null)
                    }
                }
            })
    }


    /**
     * 广场举报
     */
    fun getSquareReport(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareReport(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareReport(t, position)
                    else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.onGetSquareReport(t, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                        mView.onGetSquareReport(null, position)
                    }
                }
            })
    }


    /**
     * 获取广场列表
     */
    fun getSomeoneSquare(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .someoneSquare(params)
            .excute(object : BaseSubscriber<BaseResp<SquareListBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<SquareListBean?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareListResult(t.data, true)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareListResult(t.data, false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError("服务器错误~")
                        mView.onGetSquareListResult(null, false)
                    }
                }
            })
    }


    /**
     * 广场举报
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
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                        mView.onRemoveMySquareResult(false, position)
                    }
                }
            })
    }
}