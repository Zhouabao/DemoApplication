package com.example.demoapplication.api

import com.example.demoapplication.model.CheckBean
import com.example.demoapplication.model.LoginBean
import com.example.demoapplication.model.MatchBean
import com.kotlin.base.data.protocol.BaseResp
import okhttp3.ResponseBody
import retrofit2.http.*
import rx.Observable

interface Api {


    @POST
    fun login(shipUserName: String, shipUserMobile: String, shipAddress: String): Observable<Boolean>


    /**
     * 发送验证码
     */
    @POST("Open_Api/SendSms")
    fun getVerifyCode(@Query("phone") phone: String, @Query("scene") scene: String): Observable<BaseResp<Array<String>?>>


    /**
     * 检查验证码是否一致,即登录
     */
    @POST("Open_Api/LoginOrAlloc")
    fun loginOrAlloc(
        @Query("uni_account") phone: String, @Query("type") scene: String = "1", @Query("password") password: String = "", @Query(
            "code"
        ) code: String
    ): Observable<BaseResp<LoginBean>>


    @POST("Open_Api/CheckSms")
    fun checkVerifyCode(@Path("phone") phone: String, @Path("scene") scene: String, @Path("code") code: String): Observable<BaseResp<CheckBean>>


    @POST("/member_info/SetProfile")
    fun setProfile(@FieldMap params: Map<String, String>): Observable<BaseResp<MatchBean>>


    @GET
    fun getFileFromNet(@Url url: String): Observable<ResponseBody>

}