package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.CopyMvBean
import com.sdy.jitangapplication.presenter.view.VideoVerifyView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import java.util.*

/**
 *    author : ZFM
 *    date   : 2020/5/289:19
 *    desc   :
 *    version: 1.0
 */
class VideoVerifyPresenter : BasePresenter<VideoVerifyView>() {
    /**
     * 保存个人信息
     */
    val loadingDialog by lazy { LoadingDialog(context) }

    fun uploadMv(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .uploadMv(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {

                override fun onStart() {
                    super.onStart()
                    if (!loadingDialog.isShowing)
                        loadingDialog.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loadingDialog.dismiss()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code != 200) {
                        CommonFunction.toast(t.msg)
                    }
                    mView.onUpdateFaceInfo(t.code)
                }

                override fun onError(e: Throwable?) {
                    loadingDialog.dismiss()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast("认证审核提交失败，请重新进入认证")
                    }
                }
            })

    }

    /**
     * 模板
     */
    fun getMvNormalCopy() {
        RetrofitFactory.instance.create(Api::class.java)
            .getMvNormalCopy(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<CopyMvBean?>>(null) {
                override fun onNext(t: BaseResp<CopyMvBean?>) {
                    mView.onGetMvNormalCopy(t.data?.list ?: mutableListOf())
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })

    }

}