package com.sdy.jitangapplication.presenter

import android.util.Log
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.RegisterFileBean
import com.sdy.jitangapplication.nim.uikit.api.NimUIKit
import com.sdy.jitangapplication.presenter.view.LoginView
import com.sdy.jitangapplication.ui.activity.RegisterTooManyActivity
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import org.jetbrains.anko.startActivity

class LoginPresenter : BasePresenter<LoginView>() {

    /**
     * 获取登录配置开关
     */
    fun getRegisterProcessType() {
        RetrofitFactory.instance
            .create(Api::class.java)
            .getRegisterProcessType()
            .excute(object : BaseSubscriber<BaseResp<RegisterFileBean?>>(mView) {
                override fun onNext(t: BaseResp<RegisterFileBean?>) {
                    super.onNext(t)
                    mView.onGetRegisterProcessType(t.data)
                }
            })
    }
}
