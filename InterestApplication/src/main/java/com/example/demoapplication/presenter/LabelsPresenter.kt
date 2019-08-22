package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.Labels
import com.example.demoapplication.model.LoginBean
import com.example.demoapplication.presenter.view.LabelsView
import com.example.demoapplication.utils.UserManager
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
                        UserManager.startToLogin(context as Activity)
                    else
                        mView.onError(t.msg)
                }


                override fun onError(e: Throwable?) {
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
                    } else {
                        mView.onError(t.msg)
                    }
                }


                override fun onError(e: Throwable?) {
                    if (e is BaseException)
                        UserManager.startToLogin(context as Activity)
                    else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }


}