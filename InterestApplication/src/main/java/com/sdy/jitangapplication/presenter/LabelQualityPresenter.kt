package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.AddLabelResultBean
import com.sdy.jitangapplication.model.LabelQualitysBean
import com.sdy.jitangapplication.presenter.view.LabelQualityView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/11/2611:59
 *    desc   :
 *    version: 1.0
 */
class LabelQualityPresenter : BasePresenter<LabelQualityView>() {

/**
     *  获取兴趣的  特质/模板/意向  type  1 2 3
     */
    fun getLabelQuality(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getQualityList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<LabelQualitysBean?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<LabelQualitysBean?>) {
                    if (t.code == 200) {
                        mView.getQualityResult(true, t.data)
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.getQualityResult(false, null)
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.getQualityResult(false, null)
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    }
                }
            })
    }


    /**
     * 添加兴趣
     */
    fun saveMyQuality(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .saveMyQuality(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<AddLabelResultBean?>>(mView) {
                override fun onStart() {
                    mView.showLoading()
                }

                override fun onNext(t: BaseResp<AddLabelResultBean?>) {
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
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                        mView.addTagResult(false, null)
                    }
                }
            })
    }

}