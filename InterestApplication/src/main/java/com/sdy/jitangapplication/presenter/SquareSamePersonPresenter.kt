package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.SamePersonBean
import com.sdy.jitangapplication.presenter.view.SquareSamePersonView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class SquareSamePersonPresenter : BasePresenter<SquareSamePersonView>() {
    fun getTitleInfo(page: Int, title_id: Int) {
        val params by lazy { hashMapOf<String, Any>("page" to page, "title_id" to title_id) }

        RetrofitFactory.instance.create(Api::class.java)
            .getTitleInfo(UserManager.getSignParams(params))
            .excute(object :BaseSubscriber<BaseResp<MutableList<SamePersonBean>?>>(mView){
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<MutableList<SamePersonBean>?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onGetTitleInfoResult(true,t.data)
                    }else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.onGetTitleInfoResult(false,null)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onGetTitleInfoResult(false,null)
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }

}