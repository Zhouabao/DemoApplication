package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.BillBean
import com.sdy.jitangapplication.model.GoodsCategoryBeans
import com.sdy.jitangapplication.model.PullWithdrawBean
import com.sdy.jitangapplication.presenter.view.MyCandyView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/3/2410:17
 *    desc   :
 *    version: 1.0
 */
class MyCandyPresenter : BasePresenter<MyCandyView>() {

    /**
     * 获取特定种类的商品
     */
    fun goodsCategoryList(params: HashMap<String, Any>) {

        RetrofitFactory.instance.create(Api::class.java)
            .goodsCategoryList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<GoodsCategoryBeans?>>(mView) {
                override fun onNext(t: BaseResp<GoodsCategoryBeans?>) {
                    super.onNext(t)
                    mView.ongoodsCategoryList(t.code == 200, t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.ongoodsCategoryList(false, null)

                }
            })

    }

    /**
     * 查询我的糖果
     */
    fun myCadny() {
        RetrofitFactory.instance.create(Api::class.java)
            .myCadny(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<PullWithdrawBean?>>(null) {
                override fun onNext(t: BaseResp<PullWithdrawBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        mView.onMyCadnyResult(t.data)
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onMyCadnyResult(null)

                }
            })
    }


    /**
     * 获取交易流水
     */
    fun myBillList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .myBillList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<BillBean>??>>(mView) {
                override fun onNext(t: BaseResp<MutableList<BillBean>??>) {
                    super.onNext(t)
                    mView.onMyBillList(t.code == 200, t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else mView.onMyBillList(false, null)
                }
            })


    }
}