package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.view.ModelAboutMeView

/**
 *    author : ZFM
 *    date   : 2019/11/117:28
 *    desc   :
 *    version: 1.0
 */
class ModelAboutMePresenter : BasePresenter<ModelAboutMeView>() {
    /**
     * 获取关于我的模板示例
     */
    fun getSignTemplate(page: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSignTemplate(page)
            .excute(object : BaseSubscriber<BaseResp<MutableList<LabelQualityBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<LabelQualityBean>?>) {
                    if (t.code == 200) {
                        mView.getTagTraitInfoResult(true, t.data)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.getTagTraitInfoResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    mView.getTagTraitInfoResult(false, null)
                }

            })
    }

}