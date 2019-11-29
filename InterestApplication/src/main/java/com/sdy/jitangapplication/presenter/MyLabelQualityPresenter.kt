package com.sdy.jitangapplication.presenter

import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.presenter.view.MyLabelQualityView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/11/2611:59
 *    desc   :
 *    version: 1.0
 */
class MyLabelQualityPresenter : BasePresenter<MyLabelQualityView>() {


    /**
     *  获取标签的  特质/模板/意向  type  1 2 3
     */
    fun getTagTraitInfo(params: HashMap<String, Any>, type: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getTagTraitInfo(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<LabelQualityBean>?>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<MutableList<LabelQualityBean>?>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.getTagTraitInfoResult(type, true, t.data ?: mutableListOf())
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.getTagTraitInfoResult(type, false, null)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    mView.getTagTraitInfoResult(type, false, null)
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }


    /**
     * 添加标签
     */
    fun addClassifyTag(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .addClassifyTag(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<LoginBean?>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<LoginBean?>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.addTagResult(true, t.data)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.addTagResult(false, null)
                    }

                }


                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    mView.addTagResult(false, null)
                }
            })
    }


    /**
     * 删除标签
     */
    fun delMyTags(tag_id: Int) {
        val params = hashMapOf<String, Any>("tag_ids" to Gson().toJson(mutableListOf(tag_id)))
        RetrofitFactory.instance.create(Api::class.java)
            .delMyTags(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<Any>) {
                    mView.hideLoading()
                    if (t.code == 200) {
                        mView.delTagResult(true)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    }
                    CommonFunction.toast(t.msg)
                }

                override fun onError(e: Throwable?) {
                    mView.hideLoading()
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                }
            })
    }
}