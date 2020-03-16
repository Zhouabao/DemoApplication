package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.ChooseTitleBean
import com.sdy.jitangapplication.presenter.view.ChooseTitleView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class ChooseTitlePresenter : BasePresenter<ChooseTitleView>() {
    /**
     *  获取兴趣的  特质/模板/意向/标题  type  1 2 3 4
     */
    fun getTagTitleList(page: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getTagTitleList(UserManager.getSignParams(hashMapOf("page" to page)))
            .excute(object : BaseSubscriber<BaseResp<ChooseTitleBean?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<ChooseTitleBean?>) {
                    if (t.code == 200) {
                        mView.getTagTraitInfoResult(true, t.data?.list ?: mutableListOf(), t.data?.limit_cnt ?: 0)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.getTagTraitInfoResult(false, null,0)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.getTagTraitInfoResult(false, null,0)
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    }
                }
            })
    }

}