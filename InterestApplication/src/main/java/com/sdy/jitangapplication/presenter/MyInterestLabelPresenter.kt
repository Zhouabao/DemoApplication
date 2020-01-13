package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.view.MyInterestLabelView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/11/279:47
 *    desc   :
 *    version: 1.0
 */
class MyInterestLabelPresenter : BasePresenter<MyInterestLabelView>() {
    /**
     * 获取我感兴趣的标签
     */

    fun getMyInterestTagsList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getMyInterestList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MutableList<LabelQualityBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<LabelQualityBean>?>) {
                    if (t.code == 200) {
                        mView.getMyTagsListResult(true, t.data)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.getMyTagsListResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {

                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.getMyTagsListResult(false, null)
                    }
                }
            })
    }

    /**
     * 删除我感兴趣的标签
     */
    fun delMyInterest(tag_id: Int, position: Int) {
        val params = hashMapOf<String, Any>("tag_id" to tag_id)
        RetrofitFactory.instance.create(Api::class.java)
            .delMyInterest(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.delTagResult(true, position)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    }
                    CommonFunction.toast(t.msg)
                }

                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }


}
