package com.sdy.jitangapplication.presenter

import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.Labels
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.presenter.view.LabelsView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber

class LabelsPresenter : BasePresenter<LabelsView>() {

    /**
     * 获取标签
     */
    fun getLabels(params: HashMap<String, String>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getTagLists(params)
            .excute(object : BaseSubscriber<BaseResp<Labels>>(mView) {
                override fun onNext(t: BaseResp<Labels>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onGetLabelsResult(t.data.data)
                    else if (t.code == 403)
                        TickDialog(context).show()
                    else
                        mView.onError(t.msg)
                }


                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


    /**
     * 上传用户标签
     */
    fun uploadLabels(params: HashMap<String, String>, tags: Array<Int?>) {

        RetrofitFactory.instance.create(Api::class.java)
            .uploadTagLists(params, tags)
            .excute(object : BaseSubscriber<BaseResp<LoginBean?>>(mView) {
                override fun onNext(t: BaseResp<LoginBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onUploadLabelsResult(true, t.data)
                    }
                }


                override fun onError(e: Throwable?) {

                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


}