package com.sdy.jitangapplication.presenter

import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.presenter.view.SetInfoView
import com.sdy.jitangapplication.utils.QNUploadManager

class SetInfoPresenter : BasePresenter<SetInfoView>() {


    /**
     * 上传照片
     * imagePath 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     */
    fun uploadProfile(filePath: String, imagePath: String) {
        if (!checkNetWork()) {
            return
        }

        QNUploadManager.getInstance().put(
            filePath, imagePath, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                Log.d("OkHttp", "token = ${SPUtils.getInstance(Constants.SPNAME).getString("qntoken")}")
                Log.d("OkHttp", "key=$key\ninfo=$info\nresponse=$response")
                if (info != null) {
                    if (!info.isOK) {
                        mView.onError("头像上传失败！")
                    } else {
                        mView.onUploadUserAvatorResult(key)
                    }
                }
            }, null
        )
    }


    fun uploadUserInfo(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }

        RetrofitFactory.instance.create(Api::class.java)
            .setProfile(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onUploadUserInfoResult(true)
                    } else {
                        mView.onError(t.msg)
                        mView.onUploadUserInfoResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    mView.onUploadUserInfoResult(false)
                }
            })
    }
}