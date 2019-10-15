package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.presenter.view.NewLabelsView
import com.sdy.jitangapplication.ui.dialog.TickDialog

class NewLabelsPresenter : BasePresenter<NewLabelsView>() {


    /**
     * 获取标签
     */
    fun tagListv2(token: String, accid: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .tagListv2(token, accid)
            .excute(object : BaseSubscriber<BaseResp<MutableList<NewLabel>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<NewLabel>?>) {
                    super.onNext(t)
                    if (t.code == 200 && !t.data.isNullOrEmpty())
                        mView.onGetLabelsResult(t.data!!)
                    else if (t.code == 403)
                        TickDialog(context).show()
                    else
                        mView.onError(t.msg)
                }


                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(CommonFunction.getErrorMsg(context))
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
                        CommonFunction.toast(t.msg)
                    }
                }


                override fun onError(e: Throwable?) {

                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(CommonFunction.getErrorMsg(context))
                }
            })
    }


}