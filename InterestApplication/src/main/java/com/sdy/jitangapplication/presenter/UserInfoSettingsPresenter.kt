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
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.UserInfoSettingBean
import com.sdy.jitangapplication.presenter.view.UserInfoSettingsView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.QNUploadManager

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
    fun personalInfo(params: HashMap<String, String>) {
        RetrofitFactory.instance.create(Api::class.java)
            .personalInfo(params)
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
                        TickDialog(context).show()
                    } else
                        mView.onError("")
                }
            })
    }


    /**
     * 保存个人信息
     */
    fun savePersonal(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .savePersonal(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onSavePersonalResult(true, 1)
                    } else {
                        mView.onSavePersonalResult(false, 1)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onSavePersonalResult(false, 1)
                }
            })
    }

    /**
     * 保存头像
     */
    fun addPhotos(token: String, accid: String, photos: Array<String?>) {
        RetrofitFactory.instance.create(Api::class.java)
            .addPhotos(token, accid, photos)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onSavePersonalResult(true, 2)
                    } else {
                        mView.onSavePersonalResult(false, 2)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onSavePersonalResult(false, 2)
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