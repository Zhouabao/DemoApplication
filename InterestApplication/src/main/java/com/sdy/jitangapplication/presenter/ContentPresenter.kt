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
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.UploadEvent
import com.sdy.jitangapplication.presenter.view.ContentView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
import org.greenrobot.eventbus.EventBus
import java.io.File

class ContentPresenter : BasePresenter<ContentView>() {
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(context) }
    /**
     * 获取广场列表
     */
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



    /**
     * 广场发布
     */
    fun publishContent(
        type: Int,
        params: HashMap<String, Any>,
        checkIds: MutableList<Int>,
        keyList: MutableList<String> = mutableListOf()
    ) {
        params["tag_id"] = checkIds[0]
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