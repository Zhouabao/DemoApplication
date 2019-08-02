package com.example.demoapplication.presenter

import android.app.Activity
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.presenter.view.MyJobView
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

/**
 *    author : ZFM
 *    date   : 2019/8/119:29
 *    desc   :
 *    version: 1.0
 */
class MyJobPresenter : BasePresenter<MyJobView>() {
    fun getJobList(params: HashMap<String, String>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getJobList(params)
            .excute(object : BaseSubscriber<BaseResp<MutableList<LabelBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<LabelBean>?>) {
                    if (t.code == 200) {
                        mView.onGetJobListResult(t.data ?: mutableListOf())
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onError("")
                    }

                }

                override fun onError(e: Throwable?) {
                    mView.onError("")
                }
            })
    }


    /**
     * 保存个人信息
     */
    fun savePersonal(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .savePersonal(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onSavePersonal(true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        mView.onSavePersonal(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.onSavePersonal(false)
                }
            })
    }
}