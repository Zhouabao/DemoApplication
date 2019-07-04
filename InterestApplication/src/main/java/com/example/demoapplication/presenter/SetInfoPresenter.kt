package com.example.demoapplication.presenter

import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.presenter.view.SetInfoView
import com.example.demoapplication.utils.QNUploadManager
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.utils.NetWorkUtils
import com.qiniu.android.http.ResponseInfo
import com.qiniu.android.storage.UpCompletionHandler
import org.json.JSONObject

class SetInfoPresenter : BasePresenter<SetInfoView>() {

    fun checkNickName(nickName: String) {
        mView.onCheckNickNameResult(!nickName.contains("约"))

//        RetrofitFactory.instance
//            .create(Api::class.java)
//            .setProfile(params)
//            .subscribe {
//            }
    }


    /**
     * 上传照片
     */
    fun uploadProfile(filePath: String) {
        if (!NetWorkUtils.isNetWorkAvailable(context)) {
            mView.onError(context.getString(R.string.net_not_available))
        }

        QNUploadManager.getInstance().put(filePath, "${System.currentTimeMillis()}.jpg", SPUtils.getInstance(Constants.SPNAME).getString("qntoken"), object : UpCompletionHandler {
                override fun complete(key: String?, info: ResponseInfo?, response: JSONObject?) {
                    Log.i("retrofit", "token = ${SPUtils.getInstance(Constants.SPNAME).getString("qntoken")}")
                    Log.i("retrofit", "key=$key\ninfo=$info\nresponse=$response")
                    if (info != null) {
                        if (info.isOK) {
                            Log.i("retrofit", "key=$key\ninfo=$info\nresponse=$response")
                        }
                    }
                }

            }, null
        )
    }

}