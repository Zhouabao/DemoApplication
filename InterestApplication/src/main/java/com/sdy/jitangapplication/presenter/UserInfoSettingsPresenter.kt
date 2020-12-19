package com.sdy.jitangapplication.presenter

import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.MyPhotoBean
import com.sdy.jitangapplication.model.UserInfoSettingBean
import com.sdy.jitangapplication.presenter.view.UserInfoSettingsView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/8/110:05
 *    desc   :
 *    version: 1.0
 */
class UserInfoSettingsPresenter : BasePresenter<UserInfoSettingsView>() {

    /**
     * 获取个人信息
     */
    fun personalInfo(param: HashMap<String, String>) {
        val params = UserManager.getBaseParams()
        params.putAll(param)
        RetrofitFactory.instance.create(Api::class.java)
            .personalInfoCandy(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<UserInfoSettingBean?>>(mView) {
                override fun onNext(t: BaseResp<UserInfoSettingBean?>) {
                    if (t.code == 200) {
                        mView.onPersonalInfoResult(t.data)
                    } else {
                        mView.onError("")
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    } else
                        mView.onError("")
                }
            })
    }



    /**
     * 保存相册
     */
    fun addPhotoV2(
        params: HashMap<String, Any?> = hashMapOf(),
        photos: MutableList<Int?>,
        type: Int
    ) {

        params["photos"] = Gson().toJson(photos)
        RetrofitFactory.instance.create(Api::class.java)
            .addPhotoV2(UserManager.getSignParams1(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    CommonFunction.toast(t.msg)
                    if (t.code == 200) {
                        mView.onSavePersonalResult(true, 2, type)
                    } else {
                        mView.onSavePersonalResult(false, 2, type)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    } else
                        mView.onSavePersonalResult(false, 2, type)
                }
            })
    }


    /**
     * 保存头像
     */
    fun addPhotoWall(replaceAvator: Boolean, token: String, accid: String, key: String) {
        val params = UserManager.getBaseParams()
        params["photo"] = key
        RetrofitFactory.instance.create(Api::class.java)
            .addPhotoWall(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MyPhotoBean?>>(mView) {
                override fun onNext(t: BaseResp<MyPhotoBean?>) {
                    if (t.code == 200 && t.data != null) {
                        mView.onAddPhotoWallResult(replaceAvator, t.data!!)
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    } else
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }

    /**
     * 上传照片
     * imagePath 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     */
    fun uploadProfile(filePath: String, imagePath: String, replaceAvator: Boolean = false) {
        if (!checkNetWork()) {
            return
        }

        QNUploadManager.getInstance().put(
            filePath, imagePath, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                Log.d("OkHttp", "token = ${SPUtils.getInstance(Constants.SPNAME).getString("qntoken")}")
                Log.d("OkHttp", "key=$key\ninfo=$info\nresponse=$response")
                if (info != null) {
                    mView.uploadImgResult(info.isOK, key, replaceAvator)
                }
            }, null
        )
    }

}