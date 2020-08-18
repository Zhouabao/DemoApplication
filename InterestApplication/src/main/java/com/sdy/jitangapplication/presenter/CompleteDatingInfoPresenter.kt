package com.sdy.jitangapplication.presenter

import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.DatingOptionsBean
import com.sdy.jitangapplication.presenter.view.CompleteDatingInfoView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
import java.io.File

/**
 *    author : ZFM
 *    date   : 2020/8/1214:19
 *    desc   :
 *    version: 1.0
 */
class CompleteDatingInfoPresenter : BasePresenter<CompleteDatingInfoView>() {


    /**
     * 获取发布选项
     */
    fun datingOptions() {
        RetrofitFactory.instance.create(Api::class.java)
            .datingOptions(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<DatingOptionsBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<DatingOptionsBean?>) {
                    super.onNext(t)
                    mView.onDatingOptionsResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }


            })
    }


    /**
     * QN上传照片
     * 文件名格式：ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     * type 1图片 2视频 3音频
     */
    private val loadingDialog by lazy { LoadingDialog(context) }
    fun uploadFile(filePath: String, imagePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            mView.onSquareAnnounceResult(false)
            return
        }
        if (!checkNetWork()) {
            CommonFunction.toast("网络不可用")
            mView.onSquareAnnounceResult(false)
            return
        }
        Log.d("OkHttp", filePath)
        Log.d("OkHttp", "${file.absolutePath},${file.length() / 1024f / 1024F}")
        loadingDialog.show()
        QNUploadManager.getInstance().put(
            filePath,
            imagePath,
            SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                if (info != null && info.isOK) {
                    mView.onQnUploadResult(true, key)
                } else {
                    mView.onSquareAnnounceResult(false)
                    loadingDialog.dismiss()
                }
            }, null
        )
    }


    /**
     * 发布约会
     */
    fun releaseDate(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .releaseDate(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                    if (!loadingDialog.isShowing) {
                        loadingDialog.show()
                    }
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    loadingDialog.dismiss()
                    mView.onSquareAnnounceResult(t.code == 200, t.code)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                    mView.onSquareAnnounceResult(false)
                }

            })
    }

}