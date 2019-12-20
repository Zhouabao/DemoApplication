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
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.view.AddLabelSuccessView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
import java.io.File

class AddLabelSuccessPresenter : BasePresenter<AddLabelSuccessView>() {


    /**
     *  获取标签的  特质/模板/意向/标题  type  1 2 3 4
     */
    fun getTagTraitInfo(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getTagTraitInfo(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<LabelQualityBean>?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<MutableList<LabelQualityBean>?>) {
                    if (t.code == 200) {
                        mView.getTagTraitInfoResult(true, t.data ?: mutableListOf())
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.getTagTraitInfoResult(false, null)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.getTagTraitInfoResult(false, null)
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }


    /**
     * 广场发布
     */
    fun publishContent(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            CommonFunction.toast("网络不可用")
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .squareAnnounce(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.onSquareAnnounceResult(true, 200)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(t.msg)
                        mView.onSquareAnnounceResult(false, t.code)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    mView.onError(CommonFunction.getErrorMsg(context))

                }
            })
    }


    /**
     * QN上传照片
     * 文件名格式：ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     * type 1图片 2视频 3音频
     */
    fun uploadFile(filePath: String, imagePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            mView.onUploadImgResult(false, "")
            return
        }
        if (!checkNetWork()) {
            CommonFunction.toast("网络不可用")
            mView.onUploadImgResult(false, "")
            return
        }
        Log.d("OkHttp", filePath)
        Log.d("OkHttp", "${file.absolutePath},${file.length() / 1024f / 1024F}")
        mView.showLoading()
        QNUploadManager.getInstance().put(
            filePath,
            imagePath,
            SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                Log.d("OkHttp", response.toString())
                Log.d("OkHttp", "key=$key\ninfo=$info\nresponse=$response")
                if (info != null) {
                    mView.onUploadImgResult(info.isOK, key)
                } else {
                    mView.onUploadImgResult(false, key)
                    mView.hideLoading()
                }
            }, null
        )
    }
}