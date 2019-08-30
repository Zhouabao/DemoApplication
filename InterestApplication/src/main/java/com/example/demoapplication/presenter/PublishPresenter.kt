package com.example.demoapplication.presenter

import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.UploadEvent
import com.example.demoapplication.presenter.view.PublishView
import com.example.demoapplication.ui.dialog.TickDialog
import com.example.demoapplication.utils.QNUploadManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.qiniu.android.storage.UpProgressHandler
import com.qiniu.android.storage.UploadOptions
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 *    author : ZFM
 *    date   : 2019/7/814:52
 *    desc   :
 *    version: 1.0
 */
class PublishPresenter : BasePresenter<PublishView>() {

    //from 1 来自广场  2来自用户中心
    public var from = 1


    /**
     * 广场发布
     */
    fun publishContent(type: Int, params: HashMap<String, Any>, checkIds: Array<Int?>, keyList: Array<String?>?) {
        RetrofitFactory.instance.create(Api::class.java)
            .squareAnnounce(params, checkIds, keyList)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                    if (type == 0 && from == 1) {
                        EventBus.getDefault().postSticky(UploadEvent(1, 1, 0.0))
                    }
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        if (type == 0 && from == 1) {
                            EventBus.getDefault().postSticky(UploadEvent(1, 1, 1.0))
                        }
                        mView.onSquareAnnounceResult(type, true)
                    } else {
                        mView.onError(t.msg)
                        mView.onSquareAnnounceResult(type, false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                    mView.onError(context.getString(R.string.retry_net_error))
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
        if (!checkNetWork()) {
            return
        }
        QNUploadManager.getInstance().put(
            file,
            imagePath,
            SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                if (info != null) {
                    if (!info.isOK) {
                        mView.onQnUploadResult(false, type, key)
                    }
                }
            },
            UploadOptions(
                null, null, false,
                UpProgressHandler { key, percent ->
                    if (from == 1)
                        EventBus.getDefault().postSticky(UploadEvent(totalCount, currentCount, percent))
                    if (percent == 1.0) {
                        mView.onQnUploadResult(true, type, key)
                    }
                }, null
            )
        )
    }
}