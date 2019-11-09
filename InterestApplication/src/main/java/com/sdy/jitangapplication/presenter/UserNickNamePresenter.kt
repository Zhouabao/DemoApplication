package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.presenter.view.UserNickNameView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager

class UserNickNamePresenter : BasePresenter<UserNickNameView>() {
    // 必传参数 1昵称 2生日 3性别 4头像
    fun uploadUserInfo(step: Int, params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }

        params["province_name"] = UserManager.getProvince()
        params["city_name"] = UserManager.getCity()
        params["lng"] = UserManager.getlongtitude().toFloat()
        params["lat"] = UserManager.getlatitude().toFloat()
        params["step"] = step
        params.putAll(UserManager.getBaseParams())

        RetrofitFactory.instance.create(Api::class.java)
            .setProfileV2(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onUploadUserInfoResult(true, t.msg)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onUploadUserInfoResult(false, t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    mView.onUploadUserInfoResult(false, null)
                }
            })
    }




    public val loadingDialg by lazy { LoadingDialog(context) }


}