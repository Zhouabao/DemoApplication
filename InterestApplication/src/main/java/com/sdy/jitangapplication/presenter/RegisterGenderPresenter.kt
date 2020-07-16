package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.presenter.view.RegisterGenderView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager

class RegisterGenderPresenter : BasePresenter<RegisterGenderView>() {

    val loadingDialg by lazy { LoadingDialog(context) }

    // 必传参数 1昵称 2生日 3性别 4头像
    fun setProfileCandy(gender: Int) {
        if (!checkNetWork()) {
            return
        }

        val params = UserManager.getLocationParams()
        params["gender"] = gender
        RetrofitFactory.instance.create(Api::class.java)
            .setProfileCandy(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MoreMatchBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                    loadingDialg.show()
                }


                override fun onNext(t: BaseResp<MoreMatchBean?>) {
                    super.onNext(t)
                    loadingDialg.dismiss()
                    if (t.code == 200) {
                        mView.onUploadUserInfoResult(true, t.data)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onUploadUserInfoResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    loadingDialg.dismiss()
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    mView.onUploadUserInfoResult(false, null)
                }
            })
    }

}