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
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

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
                        mView.getSignTemplateResult(t.code, t.data)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.getSignTemplateResult(t.code, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    mView.getSignTemplateResult(-1, null)
                }

            })
    }



    /**
     *  获取标签的  特质/模板/意向  type  1 2 3
     */
    fun getTagTraitInfo(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getTagTraitInfo(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<LabelQualityBean>?>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<MutableList<LabelQualityBean>?>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.getTagTraitInfoResult(true, t.data ?: mutableListOf())
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.getTagTraitInfoResult(false, null)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    mView.getTagTraitInfoResult(false, null)
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }
}