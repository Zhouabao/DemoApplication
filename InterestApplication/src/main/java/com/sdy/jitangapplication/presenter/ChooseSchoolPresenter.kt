package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.presenter.view.ChooseSchoolView

/**
 *    author : ZFM
 *    date   : 2019/11/113:55
 *    desc   :
 *    version: 1.0
 */
class ChooseSchoolPresenter : BasePresenter<ChooseSchoolView>() {


    /**
     * 获取学校列表
     */
    fun getSchoolList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getSchoolList()
            .excute(object : BaseSubscriber<BaseResp<MutableList<String>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<String>?>) {
                    if (t.code == 200)
                        mView.onGetSchoolListResult(true, t.data)
                    else {
                        CommonFunction.toast(t.msg)
                        mView.onGetSchoolListResult(false, null)
                    }

                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    mView.onGetSchoolListResult(false, null)
                }
            })
    }


    /**
     * 获取职业列表
     */
    fun getOccupationList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getOccupationList()
            .excute(object : BaseSubscriber<BaseResp<MutableList<String>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<String>?>) {
                    if (t.code == 200)
                        mView.onGetSchoolListResult(true, t.data)
                    else {
                        CommonFunction.toast(t.msg)
                        mView.onGetSchoolListResult(false, null)
                    }

                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    mView.onGetSchoolListResult(false, null)
                }
            })
    }


}