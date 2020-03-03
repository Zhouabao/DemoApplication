package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.AddLabelBean
import com.sdy.jitangapplication.presenter.view.FindByTagView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class FindByTagPresenter : BasePresenter<FindByTagView>() {


    //1 兴趣列表 2我的兴趣列表
    fun lookForAllTags() {
        RetrofitFactory.instance.create(Api::class.java)
            .lookForAllTags(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<AddLabelBean>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<AddLabelBean>) {
                    if (t.code == 200) {
                        mView.onTagClassifyListResult(true, t.data)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onTagClassifyListResult(false, t.data)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
//                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.onTagClassifyListResult(false, null)
                    }
                }

            })
    }
}