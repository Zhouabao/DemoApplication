package com.example.demoapplication.api

import com.example.demoapplication.model.CheckBean
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.model.LoginBean
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


    /**
     * 验证短信(已经过期)
     */
    @POST("Open_Api/CheckSms")
    fun checkVerifyCode(@Path("phone") phone: String, @Path("scene") scene: String, @Path("code") code: String): Observable<BaseResp<CheckBean>>


    /**
     * 验证昵称是否正确
     */
    @POST("open_api/nickFilteRrule")
    fun checkNickName(): Observable<BaseResp<Array<String>>>


    /**
     * 上传个人信息
     */
    @FormUrlEncoded
    @POST("member_info/SetProfile")
    fun setProfile(@FieldMap params: Map<String, String>): Observable<BaseResp<String>>


    /**
     * 获取标签列表
     */
    @FormUrlEncoded
    @POST("tags/TagsLists")
    fun getTagLists(@FieldMap params: Map<String, String>): Observable<BaseResp<MutableList<LabelBean>>>


    @GET
    fun getFileFromNet(@Url url: String): Observable<ResponseBody>

}