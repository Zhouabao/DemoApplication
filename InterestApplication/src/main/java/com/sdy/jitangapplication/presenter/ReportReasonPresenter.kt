package com.sdy.jitangapplication.presenter

import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.presenter.view.ReportResonView
import com.sdy.jitangapplication.utils.QNUploadManager

/**
 *    author : ZFM
 *    date   : 2019/10/3119:11
 *    desc   :
 *    version: 1.0
 */
class ReportReasonPresenter : BasePresenter<ReportResonView>() {

    /**
     * 举报用户
     */
    fun reportUser(params: HashMap<String, Any>, photoList: Array<String?>) {
        RetrofitFactory.instance.create(Api::class.java)
            .reportUserV2(params, photoList)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    mView.onGetUserActionResult(t.code == 200, t.msg)
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }

    fun getReportMsg() {
        RetrofitFactory.instance.create(Api::class.java)
            .getReportMsg()
            .excute(object : BaseSubscriber<BaseResp<MutableList<String>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<String>?>) {
                    if (t.code != 200) {
                        CommonFunction.toast(t.msg)
                    }
                    mView.onGetReportMsgResult(t.code == 200, t.data)
                }

                override fun onError(e: Throwable?) {
                    mView.onError(CommonFunction.getErrorMsg(context))

                }
            })
    }


    /**
     * 上传照片
     * imageName 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     */
    fun uploadProfile(filePath: String, imageName: String, index: Int) {
        if (!checkNetWork()) {
            return
        }

        QNUploadManager.getInstance().put(
            filePath, imageName, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                if (info != null) {
                    mView.uploadImgResult(info.isOK, key, index)
                }
            }, null
        )
    }
}