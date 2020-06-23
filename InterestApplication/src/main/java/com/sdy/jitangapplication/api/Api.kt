package com.sdy.jitangapplication.api

import com.kotlin.base.data.protocol.BaseResp
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.*
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

interface Api {
    /****************登录板块**********************/

    /**
     * 获取登录配置开关信息
     */
    @POST("OpenApi/getRegisterProcessType${Constants.END_BASE_URL}")
    fun getRegisterProcessType(): Observable<BaseResp<RegisterFileBean?>>


    /**
     * 获取消息总的个数汇总数据
     */
    @FormUrlEncoded
    @POST("Index/msgListV2${Constants.END_BASE_URL}")
    fun msgList(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<AllMsgCount?>>


    /**
     * 每天首次开屏推荐（男性）
     */
    @FormUrlEncoded
    @POST("Home/todayRecommend${Constants.END_BASE_URL}")
    fun todayRecommend(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<TodayFateBean?>>

    /**
     * 每天首次开屏推荐（女性）
     * Tidings/chatupList
     */
    @FormUrlEncoded
    @POST("Tidings/chatupList${Constants.END_BASE_URL}")
    fun chatupList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<AccostListBean?>>

    /**
     * 批量送礼物成为好友
     */
    @FormUrlEncoded
    @POST("Home/batchSendCandy${Constants.END_BASE_URL}")
    fun batchGreet(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<BatchSendGiftBean?>>

    /**
     * 批量送礼物成为好友
     */
    @FormUrlEncoded
    @POST("Home/batchChatup${Constants.END_BASE_URL}")
    fun batchGreetWoman(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<MutableList<BatchGreetBean>?>>


    /**
     * 获取我的糖果
     */
    @FormUrlEncoded
    @POST("Candy/getMyCandy${Constants.END_BASE_URL}")
    fun getMyCandy(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<GiftStateBean?>>


    /**
     * 获取调查问卷
     */
    @POST("OpenApi/getHelpCenter${Constants.END_BASE_URL}")
    fun getHelpCenter(): Observable<BaseResp<HelpBean?>>


    /**
     * 启动统计
     */
    @FormUrlEncoded
    @POST("MemberInfo/startupRecord${Constants.END_BASE_URL}")
    fun startupRecord(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<NearCountBean?>>


    /**
     * 版本更新
     */
    @FormUrlEncoded
    @POST("OpenApi/getVersion${Constants.END_BASE_URL}")
    fun getVersion(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<VersionBean?>>


    /**
     * 人工审核
     * 1 人工认证 2重传头像或则取消
     */
    @FormUrlEncoded
    @POST("member_info/humanAduit/v1.json${Constants.END_BASE_URL}")
    fun humanAduit(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 检查验证码是否一致,即登录
     * type 是	登陆方式 1,短信 2,QQ 3,微信 4闪验
     */
    @FormUrlEncoded
    @POST("Open_Api/LoginOrAlloc${Constants.END_BASE_URL}")
    fun loginOrAlloc(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<LoginBean?>>


    /**
     * 验证昵称是否正确
     */
    @POST("open_api/nickFilteRrule${Constants.END_BASE_URL}")
    fun checkNickName(): Observable<BaseResp<Array<String>>>


    /**
     * 上传个人信息
     */
    @FormUrlEncoded
    @POST("MemberInfo/setProfileCandy${Constants.END_BASE_URL}")
    fun setProfileCandy(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MoreMatchBean?>>


    /**
     * 验证照片是否合规
     */
    @FormUrlEncoded
    @POST("MemberInfo/checkAvatarV22${Constants.END_BASE_URL}")
    fun checkAvatar(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 注册完善用户的交友意向
     */
    @FormUrlEncoded
    @POST("MemberInfo/getMyTaps${Constants.END_BASE_URL}")
    fun getMyTaps(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<MyTapsBean>?>>


    /**
     * 添加交友目的
     */
    @FormUrlEncoded
    @POST("MemberInfo/addWant${Constants.END_BASE_URL}")
    fun addWant(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MoreMatchBean?>>


    /************************广场列表*****************************/
    /**
     * 广场获取兴趣
     *
     */
    @FormUrlEncoded
    @POST("Threshold/squareTagList${Constants.END_BASE_URL}")
    fun thresholdSquareTagList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<SquareTagBean>?>>

    /**
     * 推荐广场
     * Threshold/squareEliteList/v1.json   推荐
     *
     */
    @FormUrlEncoded
    @POST("Threshold/squareEliteList${Constants.END_BASE_URL}")
    fun thresholdSquareEliteList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<RecommendSquareListBean?>>


    /**
     * 兴趣广场详情列表
     * Threshold/squareNearly
     */
    @FormUrlEncoded
    @POST("Threshold/squareTagInfo${Constants.END_BASE_URL}")
    fun thresholdSquareTagInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<TagSquareListBean?>>

    /**
     * 游客获取最新列表
     */
    @FormUrlEncoded
    @POST("Threshold/squareList${Constants.END_BASE_URL}")
    fun thresholdSquareList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareListBean?>>

    /**
     * 广场获取兴趣
     *
     */
    @FormUrlEncoded
    @POST("Square/squareTagList${Constants.END_BASE_URL}")
    fun squareTagList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<SquareTagBean>?>>

    /**
     * 兴趣置顶或者置底
     *
     */
    @FormUrlEncoded
    @POST("Tags/markTag${Constants.END_BASE_URL}")
    fun markTag(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 推荐广场
     *
     */
    @FormUrlEncoded
    @POST("Square/squareEliteList${Constants.END_BASE_URL}")
    fun squareEliteList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<RecommendSquareListBean?>>


    /**
     * 兴趣广场详情列表
     */
    @FormUrlEncoded
    @POST("Square/squareTagInfo${Constants.END_BASE_URL}")
    fun squareTagInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<TagSquareListBean?>>

    /**
     * 获取最新广场列表
     */
    @FormUrlEncoded
    @POST("Square/squareNewestLists${Constants.END_BASE_URL}")
    fun squareNewestLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareListBean?>>


    /**
     * 获取某一广场详情
     */
    @FormUrlEncoded
    @POST("square/squareInfoV13${Constants.END_BASE_URL}")
    fun getSquareInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareBean?>>


    /**
     * 广场点赞/取消点赞
     */
    @FormUrlEncoded
    @POST("square/squareLikes${Constants.END_BASE_URL}")
    fun getSquareLike(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 分享成功调用
     */
    @FormUrlEncoded
    @POST("square/addShare${Constants.END_BASE_URL}")
    fun addShare(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


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
     * 广场发布
     */
    @FormUrlEncoded
    @POST("square/announceV13${Constants.END_BASE_URL}")
    fun squareAnnounce(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 通知添加特质
     */
    @FormUrlEncoded
    @POST("MemberInfo/needNotice${Constants.END_BASE_URL}")
    fun needNotice(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 验证解锁联系方式
     */
    @FormUrlEncoded
    @POST("Candy/checkUnlockContact${Constants.END_BASE_URL}")
    fun checkUnlockContact(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<UnlockCheckBean?>>

    /**
     * 验证解锁视频介绍
     */
    @FormUrlEncoded
    @POST("Candy/checkUnlockMv${Constants.END_BASE_URL}")
    fun checkUnlockMv(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<UnlockCheckBean?>>


    /**
     * 解锁联系方式
     */
    @FormUrlEncoded
    @POST("Candy/unlockContact${Constants.END_BASE_URL}")
    fun unlockContact(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<UnlockBean?>>


    /**
     * 解锁联系方式
     */
    @FormUrlEncoded
    @POST("Candy/unlockMv${Constants.END_BASE_URL}")
    fun unlockMv(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<UnlockCheckBean?>>


    /**********************匹配**************************/

    /**
     * 完成引导
     * indexV2
     */
    @FormUrlEncoded
    @POST("Index/completeGuide${Constants.END_BASE_URL}")
    fun completeGuide(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 同城
     *
     */
    @FormUrlEncoded
    @POST("Home/theSameCity${Constants.END_BASE_URL}")
    fun theSameCity(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<NearBean?>>


    /**
     * 推荐
     * recommendIndex
     */
    @FormUrlEncoded
    @POST("Home/recommendIndex${Constants.END_BASE_URL}")
    fun recommendIndex(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<NearBean?>>


    /**
     * 游客模式首页数据
     */
    @FormUrlEncoded
    @POST("Threshold/index${Constants.END_BASE_URL}")
    fun thresholdIndex(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<NearBean?>>


    /**
     * 获取今日意向
     */
    @FormUrlEncoded
    @POST("Index/getIntention${Constants.END_BASE_URL}")
    fun getIntention(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<CheckBean>?>>


    /**
     * 添加今日意向
     */
    @FormUrlEncoded
    @POST("Index/addIntention${Constants.END_BASE_URL}")
    fun addIntention(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 匹配详情数据
     */
    @FormUrlEncoded
    @POST("MemberInfo/userInfoCandy${Constants.END_BASE_URL}")
    fun getMatchUserInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MatchBean?>>


    /**
     * 验证是否送礼物
     * Home/checkSendCandy
     */
    @FormUrlEncoded
    @POST("Home/checkSendCandy${Constants.END_BASE_URL}")
    fun checkSendCandy(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 赠送礼物达成好友关系
     */
    @FormUrlEncoded
    @POST("Home/SendCandy${Constants.END_BASE_URL}")
    fun sendCandy(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SendGiftBean?>>


    /**
     * 登录反馈
     */
    @FormUrlEncoded
    @POST("OpenApi/addFeedback${Constants.END_BASE_URL}")
    fun addFeedback(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 举报用户新版
     */
    @FormUrlEncoded
    @POST("member_info/reportUserV2${Constants.END_BASE_URL}")
    fun reportUserV2(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 获取举报理由
     */
    @POST("OpenApi/getReportMsg${Constants.END_BASE_URL}")
    fun getReportMsg(): Observable<BaseResp<MutableList<String>?>>


    /**
     * 拉黑用户
     */
    @FormUrlEncoded
    @POST("StrageBlock/blockMember${Constants.END_BASE_URL}")
    fun shieldingFriend(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 解除拉黑
     */
    @FormUrlEncoded
    @POST("StrageBlock/removeBlock${Constants.END_BASE_URL}")
    fun removeBlock(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 解除匹配
     */
    @FormUrlEncoded
    @POST("relationship/dissolutionFriend${Constants.END_BASE_URL}")
    fun dissolutionFriend(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 获取用户广场列表
     */
    @FormUrlEncoded
    @POST("square/someoneSquareCandy${Constants.END_BASE_URL}")
    fun someoneSquareCandy(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<RecommendSquareListBean?>>


    /*******************************个人中心*****************************************/
    /**
     * 获取联系方式
     */
    @FormUrlEncoded
    @POST("MemberInfo/getContact${Constants.END_BASE_URL}")
    fun getContact(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ContactWayBean?>>

    /**
     * 设置联系方式
     */
    @FormUrlEncoded
    @POST("MemberInfo/setContact${Constants.END_BASE_URL}")
    fun setContact(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 个人中心
     */
    @FormUrlEncoded
    @POST("MemberInfo/myInfoCandyV201${Constants.END_BASE_URL}")
    fun myInfoCandy(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<UserInfoBean?>>


    /**
     * 我的动态
     */
    @FormUrlEncoded
    @POST("square/aboutMeSquareV13${Constants.END_BASE_URL}")
    fun aboutMeSquare(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareListBean?>>


    /**
     * 我的动态糖果版
     */
    @FormUrlEncoded
    @POST("square/aboutMeSquareCandy${Constants.END_BASE_URL}")
    fun aboutMeSquareCandy(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<RecommendSquareListBean?>>


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
    @POST("MemberInfo/personalInfoCandy${Constants.END_BASE_URL}")
    fun personalInfoCandy(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<UserInfoSettingBean?>>


    /**
     * 修改个人信息
     */
    @FormUrlEncoded
    @POST("memberInfo/savePersonal${Constants.END_BASE_URL}")
    fun savePersonal(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     *   保存相册信息
     */
    @FormUrlEncoded
    @POST("MemberInfo/savePersonalCandy${Constants.END_BASE_URL}")
    fun addPhotoV2(@FieldMap params: MutableMap<String, Any?>?): Observable<BaseResp<Any?>>


    /**
     * 单张相册上传
     */
    @FormUrlEncoded
    @POST("MemberInfo/addPhotoWall${Constants.END_BASE_URL}")
    fun addPhotoWall(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MyPhotoBean?>>


    /**
     * 注册上传头像和相册
     * moreMatchBean: MoreMatchBean? = null
     */
    @FormUrlEncoded
    @POST("MemberInfo/registerAddPhoto${Constants.END_BASE_URL}")
    fun registerAddPhoto(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MoreMatchBean?>>


    /**
     * 获取职业列表
     */
    @POST("OpenApi/getOccupationList${Constants.END_BASE_URL}")
    fun getOccupationList(): Observable<BaseResp<MutableList<String>?>>

    /**
     * 获取模板签名
     */
    @FormUrlEncoded
    @POST("OpenApi/getSignTemplate${Constants.END_BASE_URL}")
    fun getSignTemplate(@Field("page") page: Int, @Field("gender") gender: Int): Observable<BaseResp<MutableList<LabelQualityBean>?>>

    /****************************消息************************************/


    /**
     * 所有的消息列表
     */
    @FormUrlEncoded
    @POST("Tidings/messageCensusCandyV21${Constants.END_BASE_URL}")
    fun messageCensus(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MessageListBean1?>>

    /**
     * 广场消息列表
     */
    @FormUrlEncoded
    @POST("tidings/squareListsV2${Constants.END_BASE_URL}")
    fun squareLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<SquareMsgBean>?>>

    /**
     * 标记广场消息已读
     * （type  1点赞   2评论）
     */
    @FormUrlEncoded
    @POST("Tidings/markSquareRead${Constants.END_BASE_URL}")
    fun markSquareRead(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 删除广场消息
     */
    @FormUrlEncoded
    @POST("Tidings/delSquareMsg${Constants.END_BASE_URL}")
    fun delSquareMsg(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 获取通讯录
     */
    @FormUrlEncoded
    @POST("relationship/getLists${Constants.END_BASE_URL}")
    fun getContactLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ContactDataBean?>>


    /**
     * 获取黑名单
     */
    @FormUrlEncoded
    @POST("StrageBlock/blackList${Constants.END_BASE_URL}")
    fun myShieldingList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<BlackBean>?>>


    /**
     * 招呼认证
     * 防骚扰
     */
    @FormUrlEncoded
    @POST("Relationship/greetApprove${Constants.END_BASE_URL}")
    fun greetApprove(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 发布动态验证是否被禁封
     */
    @FormUrlEncoded
    @POST("Square/checkBlock${Constants.END_BASE_URL}")
    fun checkBlock(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 设置开关
     */
    @FormUrlEncoded
    @POST("MemberInfo/mySettings${Constants.END_BASE_URL}")
    fun mySettings(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<SettingsBean?>>


    /**
     * 广场点赞评论提醒开关
     */
    @FormUrlEncoded
    @POST("Relationship/squareNotifySwitch${Constants.END_BASE_URL}")
    fun squareNotifySwitch(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 招呼的开关
     */
    @FormUrlEncoded
    @POST("Relationship/greetSwitch${Constants.END_BASE_URL}")
    fun greetSwitch(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 屏蔽通讯录
     */
    @FormUrlEncoded
    @POST("StrageBlock/blockedAddressBook${Constants.END_BASE_URL}")
    fun blockedAddressBook(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 屏蔽距离
     */
    @FormUrlEncoded
    @POST("StrageBlock/isHideDistance${Constants.END_BASE_URL}")
    fun isHideDistance(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>


    /*---------------------聊天界面请求--------------------------------*/
    /**
     * 聊天界面添加好友
     */
    @FormUrlEncoded
    @POST("Relationship/addFriend${Constants.END_BASE_URL}")
    fun addFriend(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 添加星标好友
     */
    @FormUrlEncoded
    @POST("Relationship/addStarTarget${Constants.END_BASE_URL}")
    fun addStarTarget(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 移除星标好友
     */
    @FormUrlEncoded
    @POST("Relationship/removeStarTarget${Constants.END_BASE_URL}")
    fun removeStarTarget(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 删除招呼
     */
    @FormUrlEncoded
    @POST("Relationship/removeGreet${Constants.END_BASE_URL}")
    fun removeGreet(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 聊天界面获取信息
     */
    @FormUrlEncoded
    @POST("MemberInfo/getTargetInfoCandyEnd${Constants.END_BASE_URL}")
    fun getTargetInfoCandy(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<NimBean?>>


    /**
     * 发消息请求服务器
     */
    @FormUrlEncoded
    @POST("Tidings/sendMsgV21${Constants.END_BASE_URL}")
    fun sendMsgRequest(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ResidueCountBean?>>

    /**
     * 小助手发消息请求服务器
     */
    @FormUrlEncoded
    @POST("Tidings/aideSendMsg${Constants.END_BASE_URL}")
    fun aideSendMsg(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ResidueCountBean?>>
    /*--------------------------------会员充值---------------------------------*/

    /**
     * 获取会员支付方式
     */
    @FormUrlEncoded
    @POST("pay_order/productLists${Constants.END_BASE_URL}")
    fun productLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ChargeWayBeans?>>


    /**
     * 门槛支付列表
     */
    @FormUrlEncoded
    @POST("PayOrder/getThreshold${Constants.END_BASE_URL}")
    fun getThreshold(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ChargeWayBeans?>>


    /**
     * 获取订单信息
     */
    @FormUrlEncoded
    @POST("pay_order/createOrder${Constants.END_BASE_URL}")
    fun createOrder(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<PayBean>>


    /**
     * 获取兴趣的支付方式
     */
    @FormUrlEncoded
    @POST("Tags/getTagsPrice${Constants.END_BASE_URL}")
    fun getTagsPrice(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<LabelChargeWayBean?>>


    /**
     * 获取兴趣订单信息
     */
    @FormUrlEncoded
    @POST("pay_order/createTagsOrder${Constants.END_BASE_URL}")
    fun createTagsOrder(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<PayBean>>


    /*--------------------------------账号相关---------------------------------*/

    /**
     * 获取账号相关信息
     */
    @FormUrlEncoded
    @POST("Account/getAccountInfo${Constants.END_BASE_URL}")
    fun getAccountInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<AccountBean>>


    /**
     * 更改手机号
     */
    @FormUrlEncoded
    @POST("Account/changeAccount${Constants.END_BASE_URL}")
    fun changeAccount(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any>>


    /**
     * 微信解绑
     */
    @FormUrlEncoded
    @POST("Account/unbundWeChat${Constants.END_BASE_URL}")
    fun unbundWeChat(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any>>


    /**
     * 微信绑定
     */
    @FormUrlEncoded
    @POST("Account/bundWeChat${Constants.END_BASE_URL}")
    fun bundWeChat(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<WechatNameBean>>


    /**
     * 发送短信验证码(新)
     */
    @FormUrlEncoded
    @POST("OpenApi/SendSms${Constants.END_BASE_URL}")
    fun sendSms(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any>>

    /**
     * 注销原因(新)
     */
    @FormUrlEncoded
    @POST("Account/getCauseList${Constants.END_BASE_URL}")
    fun getCauseList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<loginOffCauseBean>>

    /**
     * 注销账号
     */
    @FormUrlEncoded
    @POST("Account/cancelAccount${Constants.END_BASE_URL}")
    fun cancelAccount(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any>>


    /**
     * 兴趣接口
     */
    @FormUrlEncoded
    @POST("Tags/tagClassifyListV12${Constants.END_BASE_URL}")
    fun tagClassifyList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<AddLabelBean>>


    /**
     * 获取兴趣的  1介绍模板 2.兴趣特质 3.兴趣意向  4.兴趣标题
     */
    @FormUrlEncoded
    @POST("Tags/getTagTraitInfo${Constants.END_BASE_URL}")
    fun getTagTraitInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<LabelQualityBean>?>>


    /**
     * 获取所有的标题
     */
    @FormUrlEncoded
    @POST("Tags/getTagTitleListV13${Constants.END_BASE_URL}")
    fun getTagTitleList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ChooseTitleBean?>>


    /**
     * 获取兴趣的  1介绍模板 2.兴趣特质 3.标签意向  4.兴趣标题
     */
    @FormUrlEncoded
    @POST("Tags/getQualityList${Constants.END_BASE_URL}")
    fun getQualityList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<LabelQualitysBean?>>

    /**
     * 修改或者新增兴趣的特质
     */
    @FormUrlEncoded
    @POST("Tags/saveMyQualityV12${Constants.END_BASE_URL}")
    fun saveMyQuality(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<AddLabelResultBean?>>

    /**
     * 获取我的兴趣
     */
    @FormUrlEncoded
    @POST("Tags/getMyTagsListV12${Constants.END_BASE_URL}")
    fun getMyTagsList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MyLabelsBean?>>

    /**
     * 获取发布兴趣
     */
    @FormUrlEncoded
    @POST("Tags/getSquareTag${Constants.END_BASE_URL}")
    fun getSquareTag(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareLabelsBean?>>

    /**
     * 删除我的兴趣
     */
    @FormUrlEncoded
    @POST("Tags/delMyTagsV12${Constants.END_BASE_URL}")
    fun delMyTags(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<TagBean>?>>


    /**
     * 注册用户完善意向/关于我的信息
     */
    @FormUrlEncoded
    @POST("Tags/saveRegisterInfo${Constants.END_BASE_URL}")
    fun saveRegisterInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Userinfo?>>


    /**
     * 保存我的感兴趣的列表
     */
    @FormUrlEncoded
    @POST("Tags/addMyTagsV12${Constants.END_BASE_URL}")
    fun addMyTags(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<TagBean>?>>


    /**
     *聊天举报
     */
    @FormUrlEncoded
    @POST("Tidings/chatReport${Constants.END_BASE_URL}")
    fun chatReport(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    //----------------------------商城模块------------------------------
    /**
     * 流水记录
     */
    @FormUrlEncoded
    @POST("Candy/myBillList${Constants.END_BASE_URL}")
    fun myBillList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<BillBean>?>>


    /**
     * 商城首页列表
     */
    @FormUrlEncoded
    @POST("Goods/goodsList${Constants.END_BASE_URL}")
    fun goodsList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<GoodsListBean?>>


    /**
     * 商城首页分类商品
     * Goods/goodsCategoryList
     */
    @FormUrlEncoded
    @POST("Goods/goodsCategoryList${Constants.END_BASE_URL}")
    fun goodsCategoryList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<GoodsCategoryBeans?>>

    /**
     * 商品加入心愿
     */
    @FormUrlEncoded
    @POST("Goods/goodsAddWish${Constants.END_BASE_URL}")
    fun goodsAddWish(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 商品取消加入心愿
     */
    @FormUrlEncoded
    @POST("Goods/goodsDelWish${Constants.END_BASE_URL}")
    fun goodsDelWish(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 商品详情
     */
    @FormUrlEncoded
    @POST("Goods/goodsInfo${Constants.END_BASE_URL}")
    fun goodsInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ProductDetailBean?>>

    /**
     * 获取某一商品的心愿列表
     * Goods/goodsWishList/v1.json
     */
    @FormUrlEncoded
    @POST("Goods/goodsWishList${Constants.END_BASE_URL}")
    fun goodsWishList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<WantFriendBean>?>>


    /**
     * 商品留言列表
     */
    @FormUrlEncoded
    @POST("Goods/goodsMsgList${Constants.END_BASE_URL}")
    fun goodsMsgList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<ProductMsgBean>?>>

    /**
     * 商品评论详情
     */
    @FormUrlEncoded
    @POST("Goods/goodscommentsList${Constants.END_BASE_URL}")
    fun goodscommentsList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<ProductCommentBean>?>>

    /**
     * 获取收货地址
     */
    @FormUrlEncoded
    @POST("Goods/getAddress${Constants.END_BASE_URL}")
    fun getAddress(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MyAddressBean?>>

    /**
     * 删除收货地址
     */
    @FormUrlEncoded
    @POST("Goods/delAddress${Constants.END_BASE_URL}")
    fun delAddress(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MyAddressBean?>>

    /**
     * 编辑收货地址
     */
    @FormUrlEncoded
    @POST("Goods/editAddress${Constants.END_BASE_URL}")
    fun editAddress(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<AddressBean?>>

    /**
     * 添加收货地址
     */
    @FormUrlEncoded
    @POST("Goods/addAddress${Constants.END_BASE_URL}")
    fun addAddress(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<AddressBean?>>

    /**
     * 兑换商品
     */
    @FormUrlEncoded
    @POST("Goods/createGoods${Constants.END_BASE_URL}")
    fun createGoods(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ExchangeOrderBean?>>

    /**
     * 我的商品订单
     */
    @FormUrlEncoded
    @POST("Goods/myGoodsList${Constants.END_BASE_URL}")
    fun myGoodsList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<MyOrderBean>?>>

    /**
     * 兑换商品后添加评论
     */
    @FormUrlEncoded
    @POST("Goods/goodsAddcomments${Constants.END_BASE_URL}")
    fun goodsAddcomments(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * Goods/goodsAddMsg/v1.json
     * 添加商品留言
     */
    @FormUrlEncoded
    @POST("Goods/goodsAddMsg${Constants.END_BASE_URL}")
    fun goodsAddMsg(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    //--------------------礼物来往记录------------------------------
    /**
     * 提现
     */
    @FormUrlEncoded
    @POST("Candy/withdraw${Constants.END_BASE_URL}")
    fun withdraw(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<WithDrawSuccessBean?>>

    /**
     * 拉起提现
     * Candy/pullWithdraw
     */
    @FormUrlEncoded
    @POST("Candy/myCandy${Constants.END_BASE_URL}")
    fun myCadny(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<PullWithdrawBean?>>

    /**
     * 充值价格列表
     */
    @FormUrlEncoded
    @POST("PayOrder/candyRechargeList${Constants.END_BASE_URL}")
    fun candyRechargeList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<ChargeWayBeans?>>

    /**
     * 绑定支付宝账号
     * Candy/saveWithdrawAccount
     */
    @FormUrlEncoded
    @POST("Candy/saveWithdrawAccount${Constants.END_BASE_URL}")
    fun saveWithdrawAccount(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Alipay?>>

    /**
     * 获取礼物列表
     * Gift/getGiftList
     */
    @FormUrlEncoded
    @POST("Gift/getGiftList${Constants.END_BASE_URL}")
    fun getGiftList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<GiftBeans?>>


    /**
     * 获取礼物列表
     * Gift/getGreetGiftList
     */
    @FormUrlEncoded
    @POST("Gift/getGreetGiftList${Constants.END_BASE_URL}")
    fun getGreetGiftList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<GiftBeans?>>

    /**
     * 获取礼物列表
     * MemberInfo/getGiftList/
     */
    @FormUrlEncoded
    @POST("MemberInfo/getSomeoneGiftList${Constants.END_BASE_URL}")
    fun getSomeoneGiftList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<SomeOneGetGiftBean>?>>

    /**
     * 获取礼物列表
     * MemberInfo/getGiftList/
     */
    @FormUrlEncoded
    @POST("Gift/giveGift${Constants.END_BASE_URL}")
    fun giveGift(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SendGiftOrderBean?>>

    /**
     * 助力心愿礼物
     * ppsns/Candy/wishHelp
     */
    @FormUrlEncoded
    @POST("Candy/wishHelp${Constants.END_BASE_URL}")
    fun wishHelp(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SendGiftOrderBean?>>

    /**
     * 领取赠送虚拟礼物
     * Gift/getGift
     */
    @FormUrlEncoded
    @POST("Gift/getGift${Constants.END_BASE_URL}")
    fun getGift(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SendGiftOrderBean?>>

    /**
     * 查询礼物领取状态
     * Gift/checkGiftState
     */
    @FormUrlEncoded
    @POST("Gift/checkGiftState${Constants.END_BASE_URL}")
    fun checkGiftState(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<GiftStateBean?>>


    /**
     * 上传视频介绍
     */
    @FormUrlEncoded
    @POST("Home/uploadMv${Constants.END_BASE_URL}")
    fun uploadMv(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 获取上传视频的标准视频
     */
    @FormUrlEncoded
    @POST("Home/normalMv${Constants.END_BASE_URL}")
    fun normalMv(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<CopyMvBean?>>

    /**
     * 视频认证模板
     */
    @FormUrlEncoded
    @POST("Home/getMvNormalCopy${Constants.END_BASE_URL}")
    fun getMvNormalCopy(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<CopyMvBean?>>

    /**
     * 推荐10个
     */
    @FormUrlEncoded
    @POST("Home/indexTop${Constants.END_BASE_URL}")
    fun indexTop(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<IndexListBean?>>

    /**
     * 游客模式推荐10个
     */
    @FormUrlEncoded
    @POST("Threshold/indexTop${Constants.END_BASE_URL}")
    fun indexTopThreshold(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<IndexListBean?>>


    /*-----------------------购买置顶券---------------------------*/

    /**
     * 获取制置顶券
     */
    @FormUrlEncoded
    @POST("Ticket/getList${Constants.END_BASE_URL}")
    fun getList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<TicketBean?>>

    /**
     * 使用置顶券
     */
    @FormUrlEncoded
    @POST("Ticket/expendTicket${Constants.END_BASE_URL}")
    fun expendTicket(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 购买置顶券
     */
    @FormUrlEncoded
    @POST("Ticket/buyTicket${Constants.END_BASE_URL}")
    fun buyTicket(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     *
     * 男性解锁搭讪聊天消息
     *
     */
    @FormUrlEncoded
    @POST("Ticket/lockChatup${Constants.END_BASE_URL}")
    fun lockChatup(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


}