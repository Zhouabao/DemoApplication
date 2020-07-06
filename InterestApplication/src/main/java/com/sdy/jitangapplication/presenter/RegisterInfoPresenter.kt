package com.sdy.jitangapplication.presenter

import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.presenter.view.RegisterInfoView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager

class RegisterInfoPresenter : BasePresenter<RegisterInfoView>() {

    val loadingDialg by lazy { LoadingDialog(context) }
    // 必传参数 1昵称 2生日 3性别 4头像
    fun setProfileCandy(step: Int, params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }

        params["province_name"] = UserManager.getProvince()
        params["city_name"] = UserManager.getCity()
        params["lng"] = UserManager.getlongtitude().toFloat()
        params["lat"] = UserManager.getlatitude().toFloat()
        params["step"] = step

        RetrofitFactory.instance.create(Api::class.java)
            .setProfileCandy(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MoreMatchBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                    loadingDialg.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loadingDialg.dismiss()
                }

                override fun onNext(t: BaseResp<MoreMatchBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onUploadUserInfoResult(true, t.msg, t.data)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onUploadUserInfoResult(false, t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    loadingDialg.dismiss()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {

                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.onUploadUserInfoResult(false, null)
                    }
                }
            })
    }


    /**
     * 上传照片
     * imagePath 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     */
    fun uploadProfile(filePath: String, imageName: String) {
        if (!checkNetWork()) {
            return
        }

        QNUploadManager.getInstance().put(
            filePath, imageName, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                Log.d(
                    "OkHttp",
                    "token = ${SPUtils.getInstance(Constants.SPNAME).getString("qntoken")}"
                )
                Log.d("OkHttp", "key=$key\ninfo=$info\nresponse=$response")
                if (info != null) {
                    mView.uploadImgResult(info.isOK, key)
                    if (!info.isOK) {
                        if (loadingDialg.isShowing)
                            loadingDialg.dismiss()
                    }
                }
            }, null
        )
    }

}