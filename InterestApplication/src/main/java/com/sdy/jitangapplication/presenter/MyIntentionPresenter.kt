package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.view.MyIntentionView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class MyIntentionPresenter : BasePresenter<MyIntentionView>() {

    /**
     * 获取我的意向列表
     */
    fun getIntentionList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getIntentionList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MutableList<LabelQualityBean>?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<MutableList<LabelQualityBean>?>) {
                    if (t.code == 200) {
                        mView.onGetIntentionListResult(t.data)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onGetIntentionListResult(null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {

                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.onGetIntentionListResult(null)
                    }
                }
            })
    }


    /**
     * 保存注册信息
     */
    fun saveRegisterInfo(intention_id: Int? = null, aboutme: String? = null) {

        val params = hashMapOf<String, Any>()
        if (intention_id != null) {
            params["intention_id"] = intention_id
        }
        if (aboutme != null && aboutme.trim().isNotEmpty()) {
            params["aboutme"] = aboutme.trim()
        }

        RetrofitFactory.instance.create(Api::class.java)
            .saveRegisterInfo(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onSaveRegisterInfo(true)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onSaveRegisterInfo(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    mView.onSaveRegisterInfo(false)
                }
            })
    }
}