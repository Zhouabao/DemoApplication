package com.sdy.jitangapplication.presenter

import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.presenter.view.SweetHeartVerifyUploadView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/9/179:23
 *    desc   :
 *    version: 1.0
 */
class SweetHeartVerifyUploadPresenter : BasePresenter<SweetHeartVerifyUploadView>() {
    //accid [string]	是
    // token [string]	是
    // public_type [int]	是	1非公开 2公开
    //type [int]	是	1资产证明 2豪车 3身材 4职业
    //photo [string]	是	json串
    private val loading by lazy { LoadingDialog(context) }
    fun uploadData(public_type: Int, type: Int, photo: String) {
        if (!checkNetWork()) {
            return
        }
        val params =
            hashMapOf<String, Any>("public_type" to public_type, "type" to type, "photo" to photo)
        RetrofitFactory.instance.create(Api::class.java)
            .uploadData(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    loading.dismiss()
                    mView.uploadDataResult(t.code == 200)
                    if (t.code != 200) {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loading.dismiss()
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    } else {
                        mView.uploadDataResult(false)
                    }
                }
            })
    }

    fun uploadPhoto(filePath: String, index: Int = 0) {
        if (!checkNetWork()) {
            return
        }
        loading.show()
        //上传图片
        val key =
            "${Constants.FILE_NAME_INDEX}${Constants.SWEETHEART}${UserManager.getAccid()}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                16
            )}"
        QNUploadManager.getInstance()
            .put(
                filePath, key, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
                { key, info, response ->
                    Log.d("response", response.toString())
                    if (info != null && info.isOK) {
                        mView.uploadImgResult(info.isOK, key, index)
                    } else {
                        mView.uploadImgResult(false, key, index)
                        loading.dismiss()
                    }
                }, null
            )

    }


    //	1 资产认证 2豪车认证 3身材 4职业
    fun getPicTpl(type: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getPicTpl(UserManager.getSignParams(hashMapOf("type" to type)))
            .excute(object : BaseSubscriber<BaseResp<ArrayList<String>?>>(mView) {
                override fun onNext(t: BaseResp<ArrayList<String>?>) {
                    super.onNext(t)
                    mView.getPicTplResult(t.data ?: arrayListOf())
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    } else {
                        mView.getPicTplResult(arrayListOf())
                    }
                }
            })

    }


}