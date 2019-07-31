package com.kotlin.base.rx

import android.util.Log
import com.kotlin.base.presenter.view.BaseView
import rx.Subscriber

/*
    Rx订阅者默认实现
 */
open class BaseSubscriber<T>(val baseView: BaseView?) : Subscriber<T>() {

    override fun onStart() {

    }

    override fun onCompleted() {

    }

    override fun onNext(t: T) {
        Log.i("retrofit", t.toString())
    }

    override fun onError(e: Throwable?) {
        if (e is BaseException) {
            baseView?.onError(e.msg)
        } else {
            baseView?.onError(e?.message ?: "服务器异常！请重试")
        }
    }
}
