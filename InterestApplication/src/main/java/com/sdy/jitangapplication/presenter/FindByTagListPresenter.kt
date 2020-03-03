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
import com.sdy.jitangapplication.model.AddSinlgLabelBean
import com.sdy.jitangapplication.model.FindByTagBean
import com.sdy.jitangapplication.presenter.view.FindByTagListView
import com.sdy.jitangapplication.ui.dialog.ChargeLabelDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class FindByTagListPresenter : BasePresenter<FindByTagListView>() {
    /**
     * 根据标签来获取新的用户数据
     */
    fun lookForPeopleTag(params: HashMap<String, Any>, exclude: MutableList<Int>? = mutableListOf()) {
        params["exclude"] = Gson().toJson(exclude)
        RetrofitFactory.instance.create(Api::class.java)
            .lookForPeopleTag(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<FindByTagBean?>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<FindByTagBean?>) {
                    if (t.code == 200) {
                        if (t.data != null && t.data!!.list != null) {
                            mView.onGetTitleInfoResult(true, t.data)
                        }
                    } else if (t.code == 410) {
                        ChargeLabelDialog(context, params["tag_id"] as Int, ChargeLabelDialog.FROM_INDEX).show()
                        mView.onGetTitleInfoResult(true, t.data)

                    } else
                        mView.onGetTitleInfoResult(false, t.data)
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetTitleInfoResult(false, null)
                }
            })

    }

    /**
     * 添加标签
     */
    fun addMyTagsSingle(tag_id: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .addMyTagsSingle(UserManager.getSignParams(hashMapOf<String, Any>("tag_id" to tag_id)))
            .excute(object : BaseSubscriber<BaseResp<AddSinlgLabelBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<AddSinlgLabelBean?>) {
                    CommonFunction.toast(t.msg)
                    mView.onAddLabelResult(t.code == 200,t.data)
                }

                override fun onError(e: Throwable?) {
                    mView.onAddLabelResult(false,null)
                }
            })
    }


}