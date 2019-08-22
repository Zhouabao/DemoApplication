package com.example.demoapplication.presenter

import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.SquareListBean
import com.example.demoapplication.presenter.view.MyCollectionView
import com.example.demoapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/7/3112:02
 *    desc   : 我的动态
 *    version: 1.0
 */
class MyCollectionPresenter : BasePresenter<MyCollectionView>() {

    fun getMySquare(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .aboutMeSquare(params)
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
                    else  {
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
                    else  {
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
                    else  {
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