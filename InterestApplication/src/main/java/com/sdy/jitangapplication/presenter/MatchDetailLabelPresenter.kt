package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.OtherLabelsBean
import com.sdy.jitangapplication.presenter.view.MatchDetailLabelView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/11/2816:19
 *    desc   :
 *    version: 1.0
 */
class MatchDetailLabelPresenter : BasePresenter<MatchDetailLabelView>() {
    fun getOtherTags(target_accid: String) {
        val params = hashMapOf<String, Any>("target_accid" to target_accid)
        RetrofitFactory.instance.create(Api::class.java)
            .getOtherTags(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<OtherLabelsBean?>>(mView) {
                override fun onStart() {

                }

                override fun onNext(t: BaseResp<OtherLabelsBean?>) {
                    if (t.code == 200) {
                        mView.getOtherTagsResult(true, t.data)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.getOtherTagsResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.getOtherTagsResult(false, null)
                }
            })

    }
}