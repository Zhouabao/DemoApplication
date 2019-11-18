package com.sdy.jitangapplication.presenter

import android.util.Log
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.presenter.view.LoginHelpResonView
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/10/3119:11
 *    desc   :
 *    version: 1.0
 */
class LoginHelpResonPresenter : BasePresenter<LoginHelpResonView>() {

    /**
     * 举报用户
     */
    fun feedback(descr: String, phone: String, photoList: MutableList<String>) {
        val params = hashMapOf<String, Any>()
        params["photo"] = Gson().toJson(photoList)
        params["descr"] = descr
        params["phone"] = phone
        RetrofitFactory.instance.create(Api::class.java)
            .addFeedback(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    mView.onGetUserActionResult(t.code == 200, t.msg)
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }


    /**
     * 上传照片
     * imageName 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     */
    fun uploadProfile(filePath: String, imageName: String, index: Int, qntoken: String) {
        if (!checkNetWork()) {
            return
        }

        QNUploadManager.getInstance().put(
            filePath, imageName, qntoken,
            { key, info, response ->
                Log.d("response", response.toString())
                if (info != null) {
                    mView.uploadImgResult(info.isOK, key, index)
                }
            }, null
        )
    }
}