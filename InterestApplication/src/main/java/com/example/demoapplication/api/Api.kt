package com.example.demoapplication.api

import com.kotlin.base.data.protocol.BaseResp
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import rx.Observable

interface Api {


    @POST
    fun login(shipUserName: String, shipUserMobile: String, shipAddress: String): Observable<Boolean>


    @POST
    fun getVerifyCode(shipUserName: String, shipUserMobile: String, shipAddress: String): Observable<BaseResp<String>>

    @GET
    fun getFileFromNet(@Url url: String): Observable<ResponseBody>

}