package com.example.demoapplication.api

import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.*
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
    @POST("Open_Api/SendSms${Constants.END_BASE_URL}")
    fun getVerifyCode(@Query("phone") phone: String, @Query("scene") scene: String): Observable<BaseResp<Any?>>


    /**
     * 检查验证码是否一致,即登录
     */
    @POST("Open_Api/LoginOrAlloc${Constants.END_BASE_URL}")
    fun loginOrAlloc(
        @Query("uni_account") phone: String, @Query("type") scene: String = "1", @Query("password") password: String = "", @Query(
            "code"
        ) code: String
    ): Observable<BaseResp<LoginBean>>


    /**
     * 验证短信(已经过期)
     */
    @POST("Open_Api/CheckSms${Constants.END_BASE_URL}")
    fun checkVerifyCode(@Path("phone") phone: String, @Path("scene") scene: String, @Path("code") code: String): Observable<BaseResp<CheckBean>>


    /**
     * 验证昵称是否正确
     */
    @POST("open_api/nickFilteRrule${Constants.END_BASE_URL}")
    fun checkNickName(): Observable<BaseResp<Array<String>>>


    /**
     * 上传个人信息
     */
    @FormUrlEncoded
    @POST("member_info/SetProfile${Constants.END_BASE_URL}")
    fun setProfile(@FieldMap params: Map<String, String>): Observable<BaseResp<Any?>>


    /**
     * 获取标签列表
     */
    @FormUrlEncoded
    @POST("tags/TagsLists${Constants.END_BASE_URL}")
    fun getTagLists(@FieldMap params: Map<String, String>): Observable<BaseResp<Labels>>


    /**
     * 获取标签列表
     */
    @FormUrlEncoded
    @POST("tags/addTag${Constants.END_BASE_URL}")
    fun uploadTagLists(@FieldMap params: HashMap<String, String>, @Field("tags[]") idList: Array<Int?>): Observable<BaseResp<LoginBean?>>


    /**
     * 获取广场好友列表
     */
    @FormUrlEncoded
    @POST("square/squareFriends${Constants.END_BASE_URL}")
    fun getSquareFriends(@FieldMap params: HashMap<String, String>): Observable<BaseResp<FriendListBean?>>


    /**
     * 获取广场列表
     */
    @FormUrlEncoded
    @POST("square/squareLists${Constants.END_BASE_URL}")
    fun getSquareList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareListBean>>


    /**
     * 获取广场好友最近的动态列表
     */
    @FormUrlEncoded
    @POST("square/getLatelySquareInfo${Constants.END_BASE_URL}")
    fun getLatelySquareInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareRecentlyListBean?>>


    /**
     * 广场点赞/取消点赞
     */
    @FormUrlEncoded
    @POST("square/squareLikes${Constants.END_BASE_URL}")
    fun getSquareLike(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 广场评论
     */
    @FormUrlEncoded
    @POST("square/addComment${Constants.END_BASE_URL}")
    fun addComment(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 广场收藏
     */
    @FormUrlEncoded
    @POST("square/squareCollect${Constants.END_BASE_URL}")
    fun getSquareCollect(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>



    /**
     * 广场举报
     */
    @FormUrlEncoded
    @POST("square/squareReport${Constants.END_BASE_URL}")
    fun getSquareReport(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 获取广场的评论列表
     */
    @FormUrlEncoded
    @POST("square/commentLists${Constants.END_BASE_URL}")
    fun getCommentLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<AllCommentBean?>>

    /**
     * 删除评论
     */
    @FormUrlEncoded
    @POST("square/destoryComment${Constants.END_BASE_URL}")
    fun destoryComment(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 评论点赞
     */
    @FormUrlEncoded
    @POST("square/commentLikes${Constants.END_BASE_URL}")
    fun commentLikes(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    @GET
    fun getFileFromNet(@Url url: String): Observable<ResponseBody>

}