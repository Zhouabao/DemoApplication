package com.kotlin.base.presenter

import android.content.Context
import com.kotlin.base.presenter.view.BaseView
import com.kotlin.base.utils.NetWorkUtils
import rx.Subscription

/*
    MVP中P层 基类
 */
open class BasePresenter<T : BaseView> {
    val compositeDisposable: MutableList<Subscription> = mutableListOf()

    lateinit var mView: T

    lateinit var context: Context


    /*
        检查网络是否可用
     */
    fun checkNetWork(): Boolean {
        if (NetWorkUtils.isNetWorkAvailable(context)) {
            return true
        }
        mView.onError("网络不可用")
        return false
    }

    fun detachView() {
        for (subs in compositeDisposable) {
            subs.unsubscribe()
        }

    }

    fun addDisposable(subscription: Subscription) {
        compositeDisposable.add(subscription)
    }
}
