package com.example.demoapplication.api

import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.*
import com.kotlin.base.data.protocol.BaseResp
import okhttp3.ResponseBody
import retrofit2.http.*
import rx.Observable

interface Api {

    /****************登录板块**********************/

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
    fun setProfile(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 获取标签列表
     */
    @FormUrlEncoded
    @POST("tags/TagsLists${Constants.END_BASE_URL}")
    fun getTagLists(@FieldMap params: Map<String, String>): Observable<BaseResp<Labels>>


    /**
     * 上传标签列表
     */
    @FormUrlEncoded
    @POST("tags/addTag${Constants.END_BASE_URL}")
    fun uploadTagLists(@FieldMap params: HashMap<String, String>, @Field("tags[]") idList: Array<Int?>): Observable<BaseResp<LoginBean?>>


    /************************广场列表*****************************/

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
     * 获取某一广场详情
     */
    @FormUrlEncoded
    @POST("square/squareInfo${Constants.END_BASE_URL}")
    fun getSquareInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareBean?>>


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


    /**
     * 评论举报
     */
    @FormUrlEncoded
    @POST("square/replyReport${Constants.END_BASE_URL}")
    fun commentReport(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 评论举报
     */
    @FormUrlEncoded
    @POST("square/announce${Constants.END_BASE_URL}")
    fun squareAnnounce(@FieldMap params: MutableMap<String, Any>, @Field("tags[]") tagList: Array<Int?>, @Field("comment[]") keyList: Array<String?>?): Observable<BaseResp<Any?>>


    /**********************匹配**************************/

    /**
     * 匹配首页数据
     */
    @FormUrlEncoded
    @POST("Index/index${Constants.END_BASE_URL}")
    fun getMatchList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MatchListBean?>>


    /**
     * 匹配首页数据
     */
    @FormUrlEncoded
    @POST("member_info/usrinfo${Constants.END_BASE_URL}")
    fun getMatchUserInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MatchBean?>>


    /**
     * 不喜欢、左滑
     */
    @FormUrlEncoded
    @POST("relationship/dontLike${Constants.END_BASE_URL}")
    fun dontLike(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 喜欢、右滑
     */
    @FormUrlEncoded
    @POST("relationship/addLike${Constants.END_BASE_URL}")
    fun addLike(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<StatusBean?>>


    /**
     * 举报用户
     */
    @FormUrlEncoded
    @POST("member_info/reportUser${Constants.END_BASE_URL}")
    fun reportUser(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 拉黑用户
     */
    @FormUrlEncoded
    @POST("relationship/shieldingFriend${Constants.END_BASE_URL}")
    fun shieldingFriend(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 解除匹配
     */
    @FormUrlEncoded
    @POST("relationship/dissolutionFriend${Constants.END_BASE_URL}")
    fun dissolutionFriend(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 获取用户相册
     */
    @FormUrlEncoded
    @POST("member_info/squarePhotosList${Constants.END_BASE_URL}")
    fun squarePhotosList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<BlockListBean?>>


    /**
     * 获取用户广场列表
     */
    @FormUrlEncoded
    @POST("square/someoneSquare${Constants.END_BASE_URL}")
    fun someoneSquare(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareListBean?>>


    /*******************************个人中心*****************************************/
    /**
     * 个人中心
     */
    @FormUrlEncoded
    @POST("member_info/myInfo${Constants.END_BASE_URL}")
    fun myInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<UserInfoBean?>>

    /**
     * 获取会员支付方式
     */
    @FormUrlEncoded
    @POST("pay_order/productLists${Constants.END_BASE_URL}")
    fun productLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ChargeWayBeans?>>


    /**
     * 系统推荐问题
     */
    @FormUrlEncoded
    @POST("Questions_Circle/promoteQuestion${Constants.END_BASE_URL}")
    fun promoteQuestion(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ChargeWayBeans?>>


    /**
     * 我的动态
     */
    @FormUrlEncoded
    @POST("square/aboutMeSquare${Constants.END_BASE_URL}")
    fun aboutMeSquare(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareListBean?>>


    /**
     * 删除我的动态
     */
    @FormUrlEncoded
    @POST("square/removeMySquare${Constants.END_BASE_URL}")
    fun removeMySquare(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 看过我的
     */
    @FormUrlEncoded
    @POST("memberInfo/myVisitedList${Constants.END_BASE_URL}")
    fun myVisitedList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<VisitorBean>?>>


    /**
     * 我的评论
     */
    @FormUrlEncoded
    @POST("square/myCommentList${Constants.END_BASE_URL}")
    fun myCommentList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MyCommentList?>>


    /**
     * 个人信息
     */
    @FormUrlEncoded
    @POST("memberInfo/personalInfo${Constants.END_BASE_URL}")
    fun personalInfo(@FieldMap params: MutableMap<String, String>): Observable<BaseResp<UserInfoSettingBean?>>


    /**
     * 修改个人信息
     */
    @FormUrlEncoded
    @POST("memberInfo/savePersonal${Constants.END_BASE_URL}")
    fun savePersonal(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 修改个人信息
     */
    @FormUrlEncoded
    @POST("memberInfo/addPhotos${Constants.END_BASE_URL}")
    fun addPhotos(@Field("token") token: String, @Field("accid") accid: String, @Field("photos[]") tagList: Array<String?>): Observable<BaseResp<Any?>>


    /**
     * 获取职业列表
     */
    @FormUrlEncoded
    @POST("jobs/getJobList${Constants.END_BASE_URL}")
    fun getJobList(@FieldMap params: MutableMap<String, String>): Observable<BaseResp<MutableList<LabelBean>?>>

    /****************************消息************************************/
    /**
     * 给我打招呼的列表
     */
    @FormUrlEncoded
    @POST("tidings/greetLists${Constants.END_BASE_URL}")
    fun greatLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<HiMessageBean>?>>


    /**
     * 所有的消息列表
     */
    @FormUrlEncoded
    @POST("Tidings/messageCensus${Constants.END_BASE_URL}")
    fun messageCensus(@FieldMap params: MutableMap<String, String>): Observable<BaseResp<MessageListBean1?>>

    /**
     * 广场消息列表
     */
    @FormUrlEncoded
    @POST("tidings/squareLists${Constants.END_BASE_URL}")
    fun squareLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareLitBean?>>

    /**
     * 喜欢我的列表（所有日期的）
     */
    @FormUrlEncoded
    @POST("relationship/likeLists${Constants.END_BASE_URL}")
    fun likeLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<LikeMeListBean?>>

    /**
     * 喜欢我的列表（某一天的）
     */
    @FormUrlEncoded
    @POST("relationship/likeListsCategory${Constants.END_BASE_URL}")
    fun likeListsCategory(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<LikeMeOneDayBean>?>>

    /**
     * 获取通讯录
     */
    @FormUrlEncoded
    @POST("relationship/getLists${Constants.END_BASE_URL}")
    fun getContactLists(@FieldMap params: MutableMap<String, String>): Observable<BaseResp<ContactDataBean?>>

    @GET
    fun getFileFromNet(@Url url: String): Observable<ResponseBody>




}