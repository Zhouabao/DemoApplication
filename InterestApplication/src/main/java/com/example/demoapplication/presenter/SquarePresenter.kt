package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.FriendListBean
import com.example.demoapplication.model.SquareListBean
import com.example.demoapplication.presenter.view.SquareView
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

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
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    }
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
            .excute(object : BaseSubscriber<BaseResp<SquareListBean>>(mView) {
                override fun onStart() {
                    if (firstIn) {
                        mView.showLoading()
                    }
                }

                override fun onNext(t: BaseResp<SquareListBean>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetSquareListResult(t.data, true, isRefresh)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareListResult(t.data, false, isRefresh)
                    }
                }

                override fun onError(e: Throwable?) {
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
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareLikeResult(position, false)
                    }

                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onGetSquareLikeResult(position, false)
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
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareCollectResult(position, t)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onGetSquareCollectResult(position, null)
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
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetSquareReport(t, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onError(context.getString(R.string.service_error))
                    mView.onGetSquareReport(null, position)
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
                    //todo  showloading
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
                    mView.onError("服务器错误~")
                    mView.onGetSquareListResult(null, false)
                }
            })
    }


}