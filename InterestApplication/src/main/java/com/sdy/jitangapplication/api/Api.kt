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
     * 获取消息总的个数汇总数据
     */
    @FormUrlEncoded
    @POST("Index/msgListV2${Constants.END_BASE_URL}")
    fun msgList(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<AllMsgCount?>>


    /**
     * 获取调查问卷
     */
    @FormUrlEncoded
    @POST("Survey/getQuestion${Constants.END_BASE_URL}")
    fun getQuestion(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<InvestigateBean?>>


    /**
     * 获取调查问卷
     */
    @POST("OpenApi/getHelpCenter${Constants.END_BASE_URL}")
    fun getHelpCenter(): Observable<BaseResp<HelpBean?>>


    /**
     * 回答调查问卷
     */
    @FormUrlEncoded
    @POST("Survey/answer${Constants.END_BASE_URL}")
    fun answer(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 启动统计
     */
    @FormUrlEncoded
    @POST("MemberInfo/startupRecord${Constants.END_BASE_URL}")
    fun startupRecord(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


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
     * 发送验证码
     */
    @FormUrlEncoded
    @POST("Open_Api/SendSms${Constants.END_BASE_URL}")
    fun getVerifyCode(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 检查验证码是否一致,即登录
     */
    @FormUrlEncoded
    @POST("Open_Api/LoginOrAlloc${Constants.END_BASE_URL}")
    fun loginOrAlloc(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<LoginBean>>


    /**
     * 检查验证码是否一致,即登录
     */
    @FormUrlEncoded
    @POST("Open_Api/LoginOrAlloc${Constants.END_BASE_URL}")
    fun loginOWithWechat(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<LoginBean?>>


    /**
     * 验证昵称是否正确
     */
    @POST("open_api/nickFilteRrule${Constants.END_BASE_URL}")
    fun checkNickName(): Observable<BaseResp<Array<String>>>


    /**
     * 上传个人信息
     */
    @FormUrlEncoded
    @POST("member_info/setProfileV2${Constants.END_BASE_URL}")
    fun setProfileV2(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 验证照片是否合规
     */
    @FormUrlEncoded
    @POST("MemberInfo/checkAvatar${Constants.END_BASE_URL}")
    fun checkAvatar(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /************************广场列表*****************************/
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
     * 获取广场好友最近的动态列表
     */
    @FormUrlEncoded
    @POST("square/getLatelySquareInfo${Constants.END_BASE_URL}")
    fun getLatelySquareInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SquareRecentlyListBean?>>


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


    /**********************匹配**************************/

    /**
     * 匹配首页数据
     * indexV2
     */
    @FormUrlEncoded
    @POST("Index/lookForPeople${Constants.END_BASE_URL}")
    fun getMatchList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MatchListBean?>>


    /**
     * 获取所有兴趣
     *
     */
    @FormUrlEncoded
    @POST("Tags/lookForAllTags${Constants.END_BASE_URL}")
    fun lookForAllTags(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<AddLabelBean>>


    /**
     * 兴趣找人
     *
     */
    @FormUrlEncoded
    @POST("index/lookForPeopleTag${Constants.END_BASE_URL}")
    fun lookForPeopleTag(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<FindByTagBean?>>


    /**
     * 单独添加兴趣
     *
     */
    @FormUrlEncoded
    @POST("Tags/addMyTagsSingle${Constants.END_BASE_URL}")
    fun addMyTagsSingle(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<AddSinlgLabelBean?>>


    /**
     * 打招呼还是聊天判断
     */
    @FormUrlEncoded
    @POST("Relationship/greetStateV3${Constants.END_BASE_URL}")
    fun greetState(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<GreetBean?>>

    /**
     * 匹配详情数据
     */
    @FormUrlEncoded
    @POST("MemberInfo/usrInfoCandy${Constants.END_BASE_URL}")
    fun getMatchUserInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MatchBean?>>


    /**
     * 不喜欢、左滑
     */
    @FormUrlEncoded
    @POST("relationship/dontLike${Constants.END_BASE_URL}")
    fun dontLike(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<StatusBean?>>


    /**
     * 喜欢、右滑
     */
    @FormUrlEncoded
    @POST("relationship/addLike${Constants.END_BASE_URL}")
    fun addLike(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<StatusBean?>>


    /**
     * 打招呼、上滑
     */
    @FormUrlEncoded
    @POST("Relationship/greetV13${Constants.END_BASE_URL}")
    fun greet(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<GreetTimesBean?>>


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
     * 获取用户相册
     */
    @FormUrlEncoded
    @POST("member_info/squarePhotosListV2${Constants.END_BASE_URL}")
    fun squarePhotosList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<BlockListBean?>>


    /**
     * 获取用户广场列表
     */
    @FormUrlEncoded
    @POST("square/someoneSquareCandy${Constants.END_BASE_URL}")
    fun someoneSquare(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<RecommendSquareListBean?>>


    /*******************************个人中心*****************************************/
    /**
     * 个人中心
     */
    @FormUrlEncoded
    @POST("MemberInfo/myInfoCandy${Constants.END_BASE_URL}")
    fun myInfoCandy(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<UserInfoBean?>>

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
    @POST("memberInfo/personalInfo${Constants.END_BASE_URL}")
    fun personalInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<UserInfoSettingBean?>>


    /**
     * 修改个人信息
     */
    @FormUrlEncoded
    @POST("memberInfo/savePersonal${Constants.END_BASE_URL}")
    fun savePersonal(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 获取学校列表
     */
    @POST("OpenApi/getSchoolList${Constants.END_BASE_URL}")
    fun getSchoolList(): Observable<BaseResp<MutableList<String>?>>

    /**
     * 修改个人信息
     */
    @FormUrlEncoded
    @POST("memberInfo/addPhotos${Constants.END_BASE_URL}")
    fun addPhotos(@FieldMap params: MutableMap<String, Any>, @Field("photos[]") tagList: Array<String?>): Observable<BaseResp<Any?>>


    /**
     *   保存相册信息
     */
    @FormUrlEncoded
    @POST("MemberInfo/addPhotoV2${Constants.END_BASE_URL}")
    fun addPhotoV2(@FieldMap params: MutableMap<String, Any?>?): Observable<BaseResp<Any?>>


    /**
     * 单张相册上传
     */
    @FormUrlEncoded
    @POST("MemberInfo/addPhotoWall${Constants.END_BASE_URL}")
    fun addPhotoWall(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MyPhotoBean?>>


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
    fun getSignTemplate(@Field("page") page: Int): Observable<BaseResp<MutableList<LabelQualityBean>?>>

    /****************************消息************************************/
    /**
     * 无效招呼
     */
    @FormUrlEncoded
    @POST("Tidings/greetAllLists${Constants.END_BASE_URL}")
    fun greetAllLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<HiMessageBean>?>>

    /**
     * 给我打招呼的列表
     */
    @FormUrlEncoded
    @POST("tidings/myGreetList${Constants.END_BASE_URL}")
    fun myGreetList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<GreetedListBean>?>>


    /**
     * 删除过时消息
     */
    @FormUrlEncoded
    @POST("Tidings/delTimeoutGreet${Constants.END_BASE_URL}")
    fun delTimeoutGreet(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<OuttimeBean?>>


    /**
     * 所有的消息列表
     */
    @FormUrlEncoded
    @POST("Tidings/messageCensusV13${Constants.END_BASE_URL}")
//    @POST("Tidings/messageCensusV3${Constants.END_BASE_URL}")
    fun messageCensus(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MessageListBean1?>>

    /**
     * 广场消息列表
     */
    @FormUrlEncoded
    @POST("tidings/squareListsV3${Constants.END_BASE_URL}")
    fun squareLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<SquareMsgBean>?>>

    /**
     * 标记广场消息已读
     * （type  1点赞   2评论）
     */
    @FormUrlEncoded
    @POST("Tidings/markSquareReadV3${Constants.END_BASE_URL}")
    fun markSquareRead(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 删除广场消息
     */
    @FormUrlEncoded
    @POST("Tidings/delSquareMsg${Constants.END_BASE_URL}")
    fun delSquareMsg(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 喜欢我的列表（所有日期的）
     */
    @FormUrlEncoded
    @POST("relationship/likeLists${Constants.END_BASE_URL}")
    fun likeLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<LikeMeListBean?>>


    /**
     * 喜欢我的有效列表
     */
    @FormUrlEncoded
    @POST("Relationship/likeListsV2${Constants.END_BASE_URL}")
    fun likeListsV2(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<NewLikeMeBean?>>


    /**
     * 右滑喜欢
     */
    @FormUrlEncoded
    @POST("Relationship/bindMemberHandle${Constants.END_BASE_URL}")
    fun bindMemberHandle(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 标记喜欢列表为已读
     */
    @FormUrlEncoded
    @POST("Tidings/markLikeRead${Constants.END_BASE_URL}")
    fun markLikeRead(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

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
    @POST("MemberInfo/getTargetInfoV13${Constants.END_BASE_URL}")
//    @POST("MemberInfo/getTargetInfoV3${Constants.END_BASE_URL}")
    fun getTargetInfo(@FieldMap params: HashMap<String, Any>): Observable<BaseResp<NimBean?>>


    /**
     * 发起招呼者 判断剩余消息次数
     */
    @FormUrlEncoded
    @POST("Tidings/checkGreetSendMsg${Constants.END_BASE_URL}")
    fun checkGreetSendMsg(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<CheckGreetSendBean?>>

    /**
     * 发消息请求服务器
     */
    @FormUrlEncoded
    @POST("Tidings/sendMsg${Constants.END_BASE_URL}")
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
     * 获取他人的兴趣
     */
    @FormUrlEncoded
    @POST("MemberInfo/getOtherTags${Constants.END_BASE_URL}")
    fun getOtherTags(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<OtherLabelsBean?>>

    /**
     * 获取用户意向的接口
     */
    @FormUrlEncoded
    @POST("Tags/getIntentionList${Constants.END_BASE_URL}")
    fun getIntentionList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<LabelQualityBean>?>>


    /**
     * 注册用户完善意向/关于我的信息
     */
    @FormUrlEncoded
    @POST("Tags/saveRegisterInfo${Constants.END_BASE_URL}")
    fun saveRegisterInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     * 我的感兴趣的列表
     */
    @FormUrlEncoded
    @POST("Tags/getMyInterestList${Constants.END_BASE_URL}")
    fun getMyInterestList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<LabelQualityBean>?>>

    /**
     * 我的感兴趣的列表
     */
    @FormUrlEncoded
    @POST("Tags/delMyInterest${Constants.END_BASE_URL}")
    fun delMyInterest(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

    /**
     * 保存我的感兴趣的列表
     */
    @FormUrlEncoded
    @POST("Tags/addMyTagsV12${Constants.END_BASE_URL}")
    fun addMyTags(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<TagBean>?>>

    /**
     *V2获取广场标题菜单栏目
     */
    @FormUrlEncoded
    @POST("Tags/getTitleMenuListV12${Constants.END_BASE_URL}")
    fun getTitleMenuList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<LabelQualityBean>?>>

    /**
     *V1获取标题列表详情页
     */
    @FormUrlEncoded
    @POST("Tags/getTitleInfo${Constants.END_BASE_URL}")
    fun getTitleInfo(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<SamePersonListBean?>>

    /**
     *V2获取更多标题详情
     */
    @FormUrlEncoded
    @POST("Tags/getTitleListsV12${Constants.END_BASE_URL}")
    fun getTitleLists(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<TopicBean>?>>

    /**
     *聊天举报
     */
    @FormUrlEncoded
    @POST("Tidings/chatReport${Constants.END_BASE_URL}")
    fun chatReport(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    /**
     *招呼的左右滑动
     */
    @FormUrlEncoded
    @POST("Tidings/likeOrGreetStateV13${Constants.END_BASE_URL}")
    fun likeOrGreetState(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>


    //----------------------------商城模块------------------------------
    /**
     * 流水记录
     */
    @FormUrlEncoded
    @POST("Candy/myBillList${Constants.END_BASE_URL}")
    fun myBillList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<MutableList<BillBean>??>>


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
     * 礼物来往记录
     * Gift/giftContactList/v1.json
     */
    @FormUrlEncoded
    @POST("Gift/giftContactList${Constants.END_BASE_URL}")
    fun giftContactList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

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
    @POST("PayOrder/giftRechargeList${Constants.END_BASE_URL}")
    fun giftRechargeList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<RechargeBean?>>

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
     * MemberInfo/getGiftList/
     */
    @FormUrlEncoded
    @POST("MemberInfo/getGiftList${Constants.END_BASE_URL}")
    fun getSomeoneGiftList(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<GiftBeans?>>

    /**
     * 获取礼物列表
     * MemberInfo/getGiftList/
     */
    @FormUrlEncoded
    @POST("Gift/giveGift${Constants.END_BASE_URL}")
    fun giveGift(@FieldMap params: MutableMap<String, Any>): Observable<BaseResp<Any?>>

}