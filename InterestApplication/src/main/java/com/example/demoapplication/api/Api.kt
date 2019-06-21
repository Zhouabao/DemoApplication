package com.example.demoapplication.api

import com.kotlin.base.data.protocol.BaseResp
import rx.Observable

interface Api {
    fun login(shipUserName: String, shipUserMobile: String, shipAddress: String): Observable<Boolean>


    fun getVerifyCode(shipUserName: String, shipUserMobile: String, shipAddress: String): Observable<BaseResp<String>>

}