package com.kotlin.base.rx

import com.kotlin.base.common.ResultCode
import com.kotlin.base.data.protocol.BaseResp
import rx.Observable
import rx.functions.Func1

/*
    Boolean类型转换封装
 */
class BaseFuncBoolean<T> : Func1<BaseResp<T>, Observable<Boolean>> {
    override fun call(t: BaseResp<T>): Observable<Boolean> {
        if (t.code != ResultCode.SUCCESS) {
            return Observable.error(BaseException(t.code, t.msg))
        }

        return Observable.just(true)
    }
}
