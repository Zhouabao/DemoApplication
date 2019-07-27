package com.example.demoapplication.presenter

import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.api.Api
import com.example.demoapplication.common.Constants
import com.example.demoapplication.presenter.view.SetInfoView
import com.example.demoapplication.utils.QNUploadManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

class SetInfoPresenter : BasePresenter<SetInfoView>() {

    /**
     * 验证昵称是否合法
     */
    fun checkNickName() {
        RetrofitFactory.instance
            .create(Api::class.java)
            .checkNickName()
            .excute(object : BaseSubscriber<BaseResp<Array<String>>>(mView) {
                override fun onNext(t: BaseResp<Array<String>>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        var sensitive = ""
                        for (char in t.data) {
                            sensitive = sensitive.plus(char)
                        }
                        SPUtils.getInstance(Constants.SPNAME).put("sensitive", sensitive)
                    } else {
                        mView.onError(t.msg)
                    }
                }
            })
    }

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
                    }
                }
            })
    }
}