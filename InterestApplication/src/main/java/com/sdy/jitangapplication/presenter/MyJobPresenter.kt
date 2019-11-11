package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.presenter.view.MyJobView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/8/119:29
 *    desc   :
 *    version: 1.0
 */
class MyJobPresenter : BasePresenter<MyJobView>() {
    /**
     * 获取职业列表
     */
    fun getOccupationList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getOccupationList()
            .excute(object : BaseSubscriber<BaseResp<MutableList<String>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<String>?>) {
                    if (t.code == 200) {
                        mView.onGetJobListResult(t.data ?: mutableListOf())
                    } else {
                        mView.onError("")
                    }

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError("")
                }
            })
    }


    /**
     * 保存个人信息
     */
    fun savePersonal(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .savePersonal(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onSavePersonal(true)
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