package com.sdy.jitangapplication.presenter

import android.app.Activity
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.SettingsBean
import com.sdy.jitangapplication.model.VersionBean
import com.sdy.jitangapplication.presenter.view.SettingsView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/8/19:44
 *    desc   :
 *    version: 1.0
 */
class SettingsPresenter : BasePresenter<SettingsView>() {


    /**
     * 屏蔽通讯录
     */
    fun blockedAddressBook(
        accid: String,
        token: String,
        content: MutableList<String?> = mutableListOf()
    ) {
        val params = hashMapOf<String, Any>("content" to Gson().toJson(content))
        RetrofitFactory.instance.create(Api::class.java)
            .blockedAddressBook(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onBlockedAddressBookResult(true)
                    } else {
                        mView.onBlockedAddressBookResult(false)
                    }
                    CommonFunction.toast(t.msg)

                }

                override fun onError(e: Throwable?) {
                    if (e != null && e is BaseException) {
                        TickDialog.getInstance(context).show()
                    }

                }
            })
    }


    /**
     * 屏蔽距离
     */
    fun isHideDistance(accid: String, token: String, state: Int) {
        val params = UserManager.getBaseParams()
        params["state"] = state
        RetrofitFactory.instance.create(Api::class.java)
            .isHideDistance(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onHideDistanceResult(true)
                    } else {
                        mView.onHideDistanceResult(false)
                    }
                    CommonFunction.toast(t.msg)
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    }
                }
            })
    }

    /**获取当前最新版本**/
    fun getVersion() {
        RetrofitFactory.instance.create(Api::class.java)
            .getVersion(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<VersionBean?>>(mView) {
                override fun onNext(t: BaseResp<VersionBean?>) {
                    if (t.code == 200) {
                        mView.onGetVersionResult(t.data)
                    }
                }
            })
    }


    /**
     * 获取我的设置
     */
    fun mySettings() {
        RetrofitFactory.instance.create(Api::class.java)
            .mySettings(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<SettingsBean?>>(mView) {
                override fun onNext(t: BaseResp<SettingsBean?>) {
                    mView.onSettingsBeanResult(t.code == 200, t.data)
                    if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)

                    } else if (t.code != 200) {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog.getInstance(context).show()
                    } else {

                        mView.onSettingsBeanResult(false, null)
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    }

                }
            })
    }


    /**
     * type  1短信 2隐身 3私聊
     * state  	短信(1开启 2关闭)
     *          隐身（1 不隐身 2离线隐身 3一直隐身 ）
     */
    fun switchSet(type: Int, state: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .switchSet(UserManager.getSignParams(hashMapOf("type" to type, "state" to state)))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    CommonFunction.toast(t.msg)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }


            })

    }

}