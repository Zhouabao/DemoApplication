package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.SquareLabelsBean
import com.sdy.jitangapplication.presenter.view.ChooseLabelView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class ChooseLabelPresenter : BasePresenter<ChooseLabelView>() {

    /**
     * 获取我的兴趣
     */
    fun getSquareTag() {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareTag(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<SquareLabelsBean?>>(mView) {
                override fun onNext(t: BaseResp<SquareLabelsBean?>) {
                    if (t.code == 200) {
                        mView.getSquareTagListResult(true, t.data)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.getSquareTagListResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.getSquareTagListResult(false, null)
                    }
                }
            })
    }

}