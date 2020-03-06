package com.sdy.jitangapplication.presenter

import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.MyLabelsBean
import com.sdy.jitangapplication.model.TagBean
import com.sdy.jitangapplication.presenter.view.MyLabelView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/11/279:47
 *    desc   :
 *    version: 1.0
 */
class MyLabelPresenter : BasePresenter<MyLabelView>() {
    /**
     * 获取我的兴趣
     */
    fun getMyTagsList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getMyTagsList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MyLabelsBean?>>(mView) {
                override fun onNext(t: BaseResp<MyLabelsBean?>) {
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
     * 删除兴趣
     */
    fun delMyTags(tag_id: Int, position: Int) {
        val params = hashMapOf<String, Any>("tag_ids" to Gson().toJson(mutableListOf(tag_id)))
        RetrofitFactory.instance.create(Api::class.java)
            .delMyTags(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<TagBean>?>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<MutableList<TagBean>?>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.delTagResult(true, position, t.data)
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
