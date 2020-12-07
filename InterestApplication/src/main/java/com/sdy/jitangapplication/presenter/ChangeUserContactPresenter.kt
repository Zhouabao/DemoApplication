package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.ContactWayBean
import com.sdy.jitangapplication.presenter.view.ChangeUserContactView
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/5/2015:41
 *    desc   :
 *    version: 1.0
 */
class ChangeUserContactPresenter : BasePresenter<ChangeUserContactView>() {


    /**
     * 获取我的联系方式
     *
     */
    fun getContact() {
        RetrofitFactory.instance.create(Api::class.java)
            .getContact(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<ContactWayBean?>>() {
                override fun onNext(t: BaseResp<ContactWayBean?>) {
                    super.onNext(t)
                    mView.onGetContactResult(t.data)
                }

                override fun onStart() {
                    super.onStart()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }


    /**
     * 设置我的联系方式
     */
    fun setContact(contact_way: Int, contact_way_content: String, contact_way_hide: Int) {
        val params = hashMapOf<String, Any>(
            "contact_way" to contact_way,
            "contact_way_content" to contact_way_content,
            "contact_way_hide" to contact_way_hide
        )
        RetrofitFactory.instance.create(Api::class.java)
            .setContact(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>() {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    mView.onSetContactResult(t.code == 200)
                }

                override fun onStart() {
                    super.onStart()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }
}