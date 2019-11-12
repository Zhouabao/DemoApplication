package com.sdy.jitangapplication.presenter

import android.app.Activity
import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.qiniu.android.storage.UpProgressHandler
import com.qiniu.android.storage.UploadOptions
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UploadEvent
import com.sdy.jitangapplication.model.FriendListBean
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.presenter.view.SquareView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
import org.greenrobot.eventbus.EventBus
import java.io.File

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
        val params1 = UserManager.getBaseParams()
        params1.putAll(params)
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareFriends(UserManager.getSignParams(params1))
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
            .getSquareList(UserManager.getSignParams(params))
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
                    else {
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
            .getSquareLike(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onGetSquareLikeResult(position, true)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(t.msg)
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
            .getSquareCollect(UserManager.getSignParams(params))
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
            .getSquareReport(UserManager.getSignParams(params))
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
            .someoneSquare(UserManager.getSignParams(params))
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
     * 获取广场列表
     */
    fun checkBlock(token: String, accid: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .checkBlock(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
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
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError("服务器错误~")
                        mView.onCheckBlockResult(false)
                    }
                }
            })
    }


    /**
     * 广场举报
     */
    fun removeMySquare(params: HashMap<String, Any>, position: Int) {

        RetrofitFactory.instance.create(Api::class.java)
            .removeMySquare(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onRemoveMySquareResult(true, position)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        CommonFunction.toast(t.msg)
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


    /**
     * 广场发布
     */
    fun publishContent(type: Int, params: HashMap<String, Any>, checkIds: MutableList<Int>, keyList: MutableList<String> = mutableListOf()) {
        params["tags"] = Gson().toJson(checkIds)
        params["comment"] = Gson().toJson(keyList)
        RetrofitFactory.instance.create(Api::class.java)
            .squareAnnounce(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                    if (type == 0) {
                        EventBus.getDefault().postSticky(UploadEvent(1, 1, 0.0))
                    }
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        if (type == 0) {
                            EventBus.getDefault().postSticky(UploadEvent(1, 1, 1.0))
                        }
                        mView.onSquareAnnounceResult(type, true, 200)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onSquareAnnounceResult(type, false, t.code)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onSquareAnnounceResult(type, false)
                    }
                }
            })
    }


    /**
     * QN上传照片
     * 文件名格式：ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     * type 1图片 2视频 3音频
     */
    fun uploadFile(totalCount: Int, currentCount: Int, filePath: String, imagePath: String, type: Int) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.d("OkHttp", "文件不存在")
            return
        }
        QNUploadManager.getInstance().put(
            file,
            imagePath,
            SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                if (info != null) {
                    if (!info.isOK) {
                        mView.onSquareAnnounceResult(1, false)
                    }
                }
            },
            UploadOptions(
                null, null, false,
                UpProgressHandler { key, percent ->
                    EventBus.getDefault().postSticky(UploadEvent(totalCount, currentCount, percent))
                    if (percent == 1.0) {
                        mView.onQnUploadResult(true, type, key)
                    }
                }, UserManager.cancellationHandler
            )
        )
    }
}