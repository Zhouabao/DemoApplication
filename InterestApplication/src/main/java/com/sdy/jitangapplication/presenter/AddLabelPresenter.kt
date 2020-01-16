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
import com.sdy.jitangapplication.model.TagBean
import com.sdy.jitangapplication.presenter.view.AddLabelView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/11/269:34
 *    desc   :
 *    version: 1.0
 */
class AddLabelPresenter : BasePresenter<AddLabelView>() {

    //1 兴趣列表 2我的兴趣列表
    fun tagClassifyList(type: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .tagClassifyList(UserManager.getSignParams(hashMapOf("type" to type)))
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
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.onTagClassifyListResult(false, null)
                    }
                }

            })
    }


    /**
     * 保存我感兴趣的标签
     */
    fun saveInterestTag(tag_ids: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .addMyTags(UserManager.getSignParams(hashMapOf("tags" to tag_ids)))
            .excute(object : BaseSubscriber<BaseResp<MutableList<TagBean>?>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<MutableList<TagBean>?>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.saveMyTagResult(true, t.data)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else
                        CommonFunction.toast(t.msg)
                }

                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    }
                }
            })
    }

}