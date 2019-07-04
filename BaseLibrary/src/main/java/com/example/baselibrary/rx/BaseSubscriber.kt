package com.kotlin.base.rx

import android.util.Log
import com.kotlin.base.presenter.view.BaseView
import rx.Subscriber

/*
    Rx订阅者默认实现
 */
open class BaseSubscriber<T>(val baseView:BaseView):Subscriber<T>() {

    override fun onCompleted() {
        baseView.hideLoading()
    }

    override fun onNext(t: T) {
        Log.i("retrofit", t.toString())
    }

    override fun onError(e: Throwable?) {
        baseView.hideLoading()
        if (e is BaseException){
            baseView.onError(e.msg)
        }
    }
}
