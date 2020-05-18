package com.sdy.jitangapplication.presenter

import android.app.Activity
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.TopicBean
import com.sdy.jitangapplication.presenter.view.AllTitleView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

class AllTitlePresenter : BasePresenter<AllTitleView>() {

    /**
     * 获取标题菜单
     */
    fun getTitleMenuList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getTitleMenuList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MutableList<LabelQualityBean>?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<MutableList<LabelQualityBean>?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onGetTitleMenuListResult(true, t.data)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetTitleMenuListResult(false, null)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.onGetTitleMenuListResult(false, null)
                    }

                }
            })
    }


    /**
     * 获取标题内容
     */
    fun getTitleLists(page: Int, tag_id: Int) {
        val params = hashMapOf<String, Any>("page" to page, "id" to tag_id)
        RetrofitFactory.instance.create(Api::class.java)
            .getTitleLists(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<TopicBean>?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<MutableList<TopicBean>?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onGetTitleListsResult(true, t.data)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onGetTitleListsResult(false, null)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {

                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.onGetTitleListsResult(false, null)

                    }
                }
            })
    }
}