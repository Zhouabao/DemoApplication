package com.sdy.jitangapplication.utils

import android.app.Activity
import android.content.Context
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SPUtils
import com.ishumei.smantifraud.SmAntiFraud
import com.kotlin.base.common.AppManager
import com.netease.nim.uikit.common.util.string.MD5
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.LoginInfo
import com.qiniu.android.storage.UpCancellationSignal
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.nim.sp.UserPreferences
import com.sdy.jitangapplication.ui.activity.*
import com.sdy.jitangapplication.ui.dialog.OpenVipDialog
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import java.util.*

/**
 *    author : ZFM
 *    date   : 2019/7/1115:23
 *    desc   :
 *    version: 1.0
 */
object UserManager {
    var registerFileBean: RegisterFileBean? = null
    var my_mv_url: Boolean = false //是否上传过视频介绍
    var showIndexRecommend: Boolean = false
    var touristMode: Boolean = false//是否是处于游客模式

    //每次进入APP弹完善个人资料弹窗
    var showCompleteUserCenterDialog: Boolean = false


    //手动取消上传
    var cancelUpload = false
    //帮助取消上传的handler
    val cancellationHandler = UpCancellationSignal { cancelUpload }

    //发布动态的状态
    var publishState: Int = 0  //0未发布  1进行中   -1失败--违规400  -2失败--发布失败
    //发布
    var publishParams: HashMap<String, Any> = hashMapOf()
    //发布的对象
    var mediaBeans: MutableList<MediaParamBean> = mutableListOf()
    //发布的对象keylist
    var keyList: MutableList<String> = mutableListOf()


    /**
     * 是否是异常账号
     */
    fun saveAccountDanger(danger: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("accountDanger", danger)
    }

    fun getAccountDanger(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("accountDanger", false)
    }


    /**
     * 是否是账号异常头像不通过
     */
    fun saveAccountDangerAvatorNotPass(danger: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("AccountDangerAvatorNotPass", danger)
    }

    fun getAccountDangerAvatorNotPass(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("AccountDangerAvatorNotPass", false)
    }

    /**
     * 我的兴趣最大个数
     */
    fun saveMaxMyLabelCount(count: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("maxMyLabelCount", count)
    }

    fun getMaxMyLabelCount(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("maxMyLabelCount", -1)
    }


    /**
     * 保存当前是否引导发布
     */
    fun saveShowGuidePublish(showGuide: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuidePublish", showGuide)
    }

    fun isShowGuidePublish(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuidePublish", false)
    }


    //是否已经强制替换过头像
    fun saveForceChangeAvator(isForceChangeAvator: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isForceChangeAvator", isForceChangeAvator)
    }

    fun isForceChangeAvator(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isForceChangeAvator", false)
    }

    //是否需要强制替换头像
    fun saveNeedChangeAvator(isNeedChangeAvator: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isNeedChangeAvator", isNeedChangeAvator)
    }

    fun isNeedChangeAvator(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isNeedChangeAvator", false)
    }

    //是否需要强制替换头像  7强制替换头像  11真人头像不通过弹窗
    fun saveChangeAvatorType(changeType: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("ChangeAvatorType", changeType)
    }

    fun getChangeAvatorType(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("ChangeAvatorType", 0)
    }

    //是否需要强制替换头像
    fun saveChangeAvator(isNeedChangeAvator: String) {
        SPUtils.getInstance(Constants.SPNAME).put("ChangeAvator", isNeedChangeAvator)
    }

    fun getChangeAvator(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("ChangeAvator")
    }


    //是否提示过用户协议
    fun saveAlertProtocol(isNeedChangeAvator: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("AlertProtocol", isNeedChangeAvator)
    }

    fun getAlertProtocol(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("AlertProtocol", false)
    }

    //是否提示过wifi状态
    fun saveNoticeWifiState(notice: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("notice", notice)
    }

    fun getNoticeWifiState(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("notice", false)
    }


    /**
     * 清除发布的参数
     */
    fun clearPublishParams() {
        publishState = 0
        publishParams.clear()
        mediaBeans.clear()
        keyList = mutableListOf()
        cancelUpload = false
    }


    /**
     * 跳至登录界面
     */
    fun startToLogin(activity: Activity) {
        touristMode = false
        clearLoginData()
        AppManager.instance.finishAllActivity()
//        ActivityUtils.startLauncherActivity()
        activity.startActivity<SplashActivity>()
    }

    /**
     * 登录成功保存用户信息
     */
    fun saveUserInfo(data: LoginBean?) {
        if (data?.userinfo != null) {
            SPUtils.getInstance(Constants.SPNAME).put("nickname", data.userinfo.nickname)
            SPUtils.getInstance(Constants.SPNAME).put("avatar", data.userinfo.avatar)
            data.userinfo.gender?.let { SPUtils.getInstance(Constants.SPNAME).put("gender", it) }
            SPUtils.getInstance(Constants.SPNAME).put("birth", data.userinfo.birth)
            saveUserVip(data.extra_data?.isvip ?: false)

            if (data.userinfo.isfaced != -1)
                saveUserVerify(data.userinfo.isfaced)
        }
    }

    /**
     * 登录成功保存用户信息
     */
    fun isUserInfoMade(): Boolean {
        return !(SPUtils.getInstance(Constants.SPNAME).getString("nickname").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getString("avatar").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getString("avatar").contains(Constants.DEFAULT_AVATAR) ||
                SPUtils.getInstance(Constants.SPNAME).getInt("gender") == 0 ||
                SPUtils.getInstance(Constants.SPNAME).getInt("birth", 0) == 0
//                || getSpLabels().isNullOrEmpty()
                )

    }


    /**
     * 保存位置信息
     */
    fun saveLocation(
        latitude: String?,
        longtitude: String?,
        province: String?,
        city: String?,
        district: String?,
        code: String?
    ) {
        if (latitude != null)
            SPUtils.getInstance(Constants.SPNAME).put("latitude", latitude)
        if (longtitude != null)
            SPUtils.getInstance(Constants.SPNAME).put("longtitude", longtitude)
        if (province != null)
            SPUtils.getInstance(Constants.SPNAME).put("province", province)
        if (city != null)
            SPUtils.getInstance(Constants.SPNAME).put("city", city)
        if (district != null)
            SPUtils.getInstance(Constants.SPNAME).put("district", district)
        if (code != null)
            SPUtils.getInstance(Constants.SPNAME).put("citycode", code)

    }


    /**
     * 获取维度
     */
    fun getlatitude(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("latitude", "0")
    }

    /**
     * 获取经度
     */
    fun getlongtitude(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("longtitude", "0")
    }

    /**
     * 获取城市码
     */
    fun getCityCode(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("citycode", "")
    }

    /**
     * 获取省份
     */
    fun getProvince(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("province", "")
    }

    /**
     * 获取城市
     */
    fun getCity(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("city", "")
    }

    /**
     * 获取区域
     */
    fun getDistrict(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("district", "")
    }


    fun getToken(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("token")
    }

    fun getAccid(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("accid")
    }

    fun getAvator(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("avatar")
    }

    fun getGender(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("gender", 0)
    }


    /**
     * 判断用户是否是vip
     */
    fun isUserVip(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isvip1", false)
    }

    fun saveUserVip(vip: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isvip1", vip)
    }

    /**
     * 判断用户是否是认证
     */
    fun isUserVerify(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("verify", 0)
    }

    //0未认证/认证不成功     1认证通过     2认证中
    fun saveUserVerify(verify: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("verify", verify)
    }

    /**
     * 判断用户是否添加了筛选条件
     *
     *     /**
     * 展示筛选条件对话框
     * //最小年龄  limit_age_low
     * //最大年龄  limit_age_high
     * //兴趣id
     * //是否筛选认证会员1不用 2需要筛选 audit_only
     * //1男 2女 3不限 gender
     * //toto  这里需要判断是否认证
    */
     */
    fun getFilterConditions(): HashMap<String, Any> {
        var parmas = hashMapOf<String, Any>()
        val sp = SPUtils.getInstance(Constants.SPNAME)
        if (sp.getInt("limit_age_low", -1) != -1) {
            parmas["limit_age_low"] = sp.getInt("limit_age_low", -1)
        }
        if (sp.getInt("limit_age_high", -1) != -1) {
            parmas["limit_age_high"] = sp.getInt("limit_age_high", 35)
        }

        if (sp.getInt("audit_only", -1) != -1) {
            parmas["audit_only"] = sp.getInt("audit_only", -1)
        }
        if (sp.getInt("online_only", -1) != -1) {
            parmas["online_only"] = sp.getInt("online_only", -1)
        }
        if (sp.getInt("filter_gender", -1) != -1) {
            parmas["gender"] = sp.getInt("filter_gender", -1)
        }

        return parmas
    }


    // 如果已经存在IM用户登录信息，返回LoginInfo，否则返回null即可
    fun loginInfo(): LoginInfo? {
        if (SPUtils.getInstance(Constants.SPNAME).getString("imToken") != null
            && SPUtils.getInstance(Constants.SPNAME).getString("imAccid") != null
        ) {
            DemoCache.setAccount(SPUtils.getInstance(Constants.SPNAME).getString("imAccid"))

            return LoginInfo(
                SPUtils.getInstance(Constants.SPNAME).getString("imAccid"),
                SPUtils.getInstance(Constants.SPNAME).getString("imToken")
            )
        }
        return null
    }


    /**
     * 清除登录信息
     */
    fun clearLoginData() {
        //IM信息
        SPUtils.getInstance(Constants.SPNAME).remove("imToken")
        SPUtils.getInstance(Constants.SPNAME).remove("imAccid")
        //用户信息
        SPUtils.getInstance(Constants.SPNAME).remove("accid")
        SPUtils.getInstance(Constants.SPNAME).remove("token")
        SPUtils.getInstance(Constants.SPNAME).remove("nickname")
        SPUtils.getInstance(Constants.SPNAME).remove("avatar")
        SPUtils.getInstance(Constants.SPNAME).remove("gender")
        SPUtils.getInstance(Constants.SPNAME).remove("birth")
        SPUtils.getInstance(Constants.SPNAME).remove("isvip1")
        SPUtils.getInstance(Constants.SPNAME).remove("verify")
        SPUtils.getInstance(Constants.SPNAME).remove("checkedLabels")
        SPUtils.getInstance(Constants.SPNAME).remove("globalLabelId")
        SPUtils.getInstance(Constants.SPNAME).remove("countdowntime")
        SPUtils.getInstance(Constants.SPNAME).remove("leftSlideCount")
        SPUtils.getInstance(Constants.SPNAME).remove("slideCount")
        SPUtils.getInstance(Constants.SPNAME).remove("hiCount")
        SPUtils.getInstance(Constants.SPNAME).remove("likeCount")
        SPUtils.getInstance(Constants.SPNAME).remove("likeUnreadCount")
        SPUtils.getInstance(Constants.SPNAME).remove("squareCount")
        SPUtils.getInstance(Constants.SPNAME).remove("msgCount")
        SPUtils.getInstance(Constants.SPNAME).remove("maxInterestLabelCount")
        SPUtils.getInstance(Constants.SPNAME).remove("isShowGuidePublish")


        //筛选信息
        SPUtils.getInstance(Constants.SPNAME).remove("filter_gender")
        SPUtils.getInstance(Constants.SPNAME).remove("filter_square_gender")
        SPUtils.getInstance(Constants.SPNAME).remove("limit_age_high")
        SPUtils.getInstance(Constants.SPNAME).remove("limit_age_low")
        SPUtils.getInstance(Constants.SPNAME).remove("online_only")
        SPUtils.getInstance(Constants.SPNAME).remove("city_code")
        SPUtils.getInstance(Constants.SPNAME).remove("audit_only")

        //敏感词
        SPUtils.getInstance(Constants.SPNAME).remove("sensitive")
        //草稿箱清除
        SPUtils.getInstance(Constants.SPNAME).remove("draft")

        clearPublishParams()
        /**
         * 认证相关缓存清空
         */
        SPUtils.getInstance(Constants.SPNAME).remove("isShowGuideVerify")
        SPUtils.getInstance(Constants.SPNAME).remove("AlertProtocol")
        SPUtils.getInstance(Constants.SPNAME).remove("notice")
        SPUtils.getInstance(Constants.SPNAME).remove("ChangeAvator")
        SPUtils.getInstance(Constants.SPNAME).remove("ChangeAvatorType")
        SPUtils.getInstance(Constants.SPNAME).remove("isNeedChangeAvator")
        SPUtils.getInstance(Constants.SPNAME).remove("isForceChangeAvator")
        SPUtils.getInstance(Constants.SPNAME).remove("hasFaceUrl")

        /**
         * 弹窗缓存信息清空
         */
        SPUtils.getInstance(Constants.SPNAME).remove("isShowHarassment")
        SPUtils.getInstance(Constants.SPNAME).remove("maxMyLabelCount")

        //账号异常记录清除
        SPUtils.getInstance(Constants.SPNAME).remove("accountDanger")
        SPUtils.getInstance(Constants.SPNAME).remove("AccountDangerAvatorNotPass")


        /**
         * 点赞提醒缓存
         */

        SPUtils.getInstance(Constants.SPNAME).remove("switchDianzan")
        SPUtils.getInstance(Constants.SPNAME).remove("switchComment")
        EventBus.getDefault().removeAllStickyEvents()//移除全部
    }


    /**
     * 是否展示首页的引导使用
     */
    fun isShowGuideCandy(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuideCandy", false)
    }

    fun saveShowGuideCandy(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuideCandy", isShow)
    }

    /**
     * 是否展示引导喜欢我的
     */
    fun isShowGuideLike(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuideLike", false)
    }

    fun saveShowGuideLike(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuideLike", isShow)
    }


    /**
     * 是否展示引导喜欢我的
     */
    fun isShowGuideMarkLike(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuideMarkLike", false)
    }

    fun saveShowGuideMarkLike(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuideMarkLike", isShow)
    }


    /**
     * 是否展示助力礼物
     */
    fun isShowGuideHelpWish(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuideHelpWish", false)
    }

    fun saveShowGuideHelpWish(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuideHelpWish", isShow)
    }


    /**
     * 是否展示认证提醒
     */
    fun isShowGuideVerify(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuideVerify", false)
    }

    fun saveShowGuideVerify(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuideVerify", isShow)
    }

    /**
     * 是否展示赠送礼物规则提醒
     */
    fun isShowGuideGiftProtocol(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuideGiftProtocol", false)
    }

    fun saveShowGuideGiftProtocol(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuideGiftProtocol", isShow)
    }


    /**
     * 是否展示赠送礼物规则提醒
     */
    fun isHasFaceUrl(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("hasFaceUrl", false)
    }

    fun saveHasFaceUrl(has_face_url: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("hasFaceUrl", has_face_url)
    }


    fun getBaseParams(): HashMap<String, Any> {
        return hashMapOf(
            "token" to getToken(),
            "accid" to getAccid(),
            "_timestamp" to System.currentTimeMillis(),
            "device_id" to SmAntiFraud.getDeviceId()
        )
    }

    fun getSignParams(params: HashMap<String, Any> = hashMapOf()): HashMap<String, Any> {
        if (params["_signature"] != null)
            params.remove("_signature")
        params.putAll(getBaseParams())
        var sign = "ppsns${AppUtils.getAppVersionName()}dcyfyf"
        var params1 = params
        for (param in params.toSortedMap()) {
            sign = sign.plus("${param.key}=${param.value}&")
        }
        params1["_signature"] = MD5.getStringMD5(sign.substring(0, sign.length - 1))
        return params1
    }


    fun getSignParams1(params: HashMap<String, Any?> = hashMapOf()): HashMap<String, Any?> {
        if (params["_signature"] != null)
            params.remove("_signature")
        params.putAll(getBaseParams())
        var sign = "ppsns${AppUtils.getAppVersionName()}dcyfyf"
        var params1 = params
        for (param in params.toSortedMap()) {
            sign = sign.plus("${param.key}=${param.value}&")
        }
        params1["_signature"] = MD5.getStringMD5(sign.substring(0, sign.length - 1))
        return params1
    }


    fun startToPersonalInfoActivity(context: Context, nothing: LoginInfo?, data: LoginBean?) {
        if (LoginActivity.weakrefrece != null && LoginActivity.weakrefrece!!.get() != null) {
            (LoginActivity.weakrefrece!!.get() as LoginActivity).finish()
        }

        touristMode = false//登录成功清空游客模式

        SPUtils.getInstance(Constants.SPNAME).put("imToken", nothing?.token)
        SPUtils.getInstance(Constants.SPNAME).put("imAccid", nothing?.account)

        SPUtils.getInstance(Constants.SPNAME).put("qntoken", data?.qntk)
        SPUtils.getInstance(Constants.SPNAME).put("token", data?.token)
        SPUtils.getInstance(Constants.SPNAME).put("accid", data?.accid)
        if (!(data?.userinfo?.avatar.isNullOrEmpty() || (data?.userinfo?.avatar ?: "").contains(
                Constants.DEFAULT_AVATAR
            ))
        )
            SPUtils.getInstance(Constants.SPNAME).put("avatar", data?.userinfo?.avatar)
        DemoCache.setAccount(nothing?.account)
        //初始化消息提醒配置
        initNotificationConfig()

//    var supplement: Int = 0,//补充资料 1 前置 2后置 3 关闭
//    var threshold: Boolean = false,//门槛开关 开启true 关闭false
//    var tourists: Boolean = false//	游客模式 开启true 关闭false

        //昵称 生日 性别 头像 个签
        if (data?.userinfo == null
            || data.userinfo.nickname.isNullOrEmpty()
            || data.userinfo.birth == 0
            || data.userinfo.gender == 0
            || data.userinfo.avatar.isNullOrEmpty()
            || data.userinfo.avatar!!.contains(Constants.DEFAULT_AVATAR)
        ) {
            context.startActivity<RegisterInfoActivity>()
            return
        } else if (data?.extra_data?.living_btn == true) {
            context.startActivity<IDVerifyActivity>(
                "type" to IDVerifyActivity.TYPE_LIVE_CAPTURE, "morematchbean" to MoreMatchBean(
                    data.extra_data?.city_name ?: "",
                    data.extra_data?.gender_str ?: "",
                    data?.extra_data?.people_amount ?: 0
                )
            )
        } else if (registerFileBean?.supplement == 1) { //补充资料前移未走过
            data.userinfo.gender?.let { SPUtils.getInstance(Constants.SPNAME).put("gender", it) }
            if (data.extra_data?.want_steps != true) {//没有走完补充资料，就先走补充资料
                context.startActivity<GetMoreMatchActivity>(
                    "moreMatch" to MoreMatchBean(
                        data.extra_data?.city_name ?: "",
                        data.extra_data?.gender_str ?: "",
                        data?.extra_data?.people_amount ?: 0
                    )
                )
            } else if (registerFileBean?.threshold == true && !data.extra_data?.isvip && getGender() == 1) {//走完补充资料,判断是否开启门槛，开启门槛并且不是会员就支付门槛
                OpenVipDialog(
                    context,
                    MoreMatchBean(
                        data?.extra_data?.city_name ?: "",
                        data?.extra_data?.gender_str ?: "",
                        data?.extra_data?.people_amount ?: 0
                    ),
                    OpenVipDialog.FROM_REGISTER_OPEN_VIP
                ).show()
            } else {
                //跳到主页
                //保存个人信息
                SPUtils.getInstance(Constants.SPNAME)
                    .put("people_amount", data?.extra_data?.people_amount ?: 0)
                saveUserInfo(data)
//            AppManager.instance.finishAllActivity()
                context.startActivity<MainActivity>()

            }
            return
        } else if (registerFileBean?.supplement == 2 && data.extra_data?.want_steps != true) {//补充资料后移并且没有走过
            data.userinfo.gender?.let { SPUtils.getInstance(Constants.SPNAME).put("gender", it) }
            if (data.extra_data?.isvip != true) {//没有支付过门槛就跳门槛支付
                OpenVipDialog(
                    context,
                    MoreMatchBean(
                        data?.extra_data?.city_name ?: "",
                        data?.extra_data?.gender_str ?: "",
                        data?.extra_data?.people_amount ?: 0
                    ),
                    OpenVipDialog.FROM_REGISTER_OPEN_VIP
                ).show()
            } else {//支付过门槛就跳更多关系
                context.startActivity<GetRelationshipActivity>(
                    "moreMatch" to MoreMatchBean(
                        data.extra_data?.city_name ?: "",
                        data.extra_data?.gender_str ?: "",
                        data?.extra_data?.people_amount ?: 0
                    )
                )
            }
        } else if (registerFileBean?.supplement == 3 && registerFileBean?.threshold == true && data.extra_data?.isvip != true) { //补充资料不开启
            data.userinfo.gender?.let { SPUtils.getInstance(Constants.SPNAME).put("gender", it) }
            OpenVipDialog(
                context,
                MoreMatchBean(
                    data?.extra_data?.city_name ?: "",
                    data?.extra_data?.gender_str ?: "",
                    data?.extra_data?.people_amount ?: 0
                ),
                OpenVipDialog.FROM_REGISTER_OPEN_VIP
            ).show()
        } else {
            //跳到主页
            //保存个人信息
            SPUtils.getInstance(Constants.SPNAME)
                .put("people_amount", data?.extra_data?.people_amount ?: 0)
            saveUserInfo(data)
            context.startActivity<MainActivity>()
        }
    }

    fun startToFlow(context: Context, moreMatchBean: MoreMatchBean?) {
        if (registerFileBean?.supplement == 1) { //补充资料前移未走过
            context.startActivity<GetMoreMatchActivity>("moreMatch" to moreMatchBean)
            return
        } else if (registerFileBean?.supplement == 2) {//补充资料后移并且没有走过
            if (moreMatchBean?.isvip != true) {//没有支付过门槛就跳门槛支付
                OpenVipDialog(
                    context,
                    moreMatchBean,
                    OpenVipDialog.FROM_REGISTER_OPEN_VIP
                ).show()
            } else {//支付过门槛就跳更多关系
                context.startActivity<GetRelationshipActivity>(
                    "moreMatch" to MoreMatchBean(
                        moreMatchBean?.city_name ?: "",
                        moreMatchBean?.gender_str ?: "",
                        moreMatchBean?.people_amount ?: 0
                    )
                )
            }
        } else if (registerFileBean?.supplement == 3 && registerFileBean?.threshold == true && moreMatchBean?.isvip != true) { //补充资料不开启
            OpenVipDialog(
                context,
                moreMatchBean,
                OpenVipDialog.FROM_REGISTER_OPEN_VIP
            ).show()

        } else {
            //跳到主页
            //保存个人信息
            SPUtils.getInstance(Constants.SPNAME).put("nickname", moreMatchBean?.nickname)
            SPUtils.getInstance(Constants.SPNAME).put("avatar", moreMatchBean?.avatar)
            SPUtils.getInstance(Constants.SPNAME).put("birth", moreMatchBean?.birth ?: 0)
            SPUtils.getInstance(Constants.SPNAME).put("gender", moreMatchBean?.gender ?: 0)
            SPUtils.getInstance(Constants.SPNAME)
                .put("people_amount", moreMatchBean?.people_amount ?: 0)
            context.startActivity<MainActivity>()
        }
    }

    private fun initNotificationConfig() {
        // 初始化消息提醒
        NIMClient.toggleNotification(UserPreferences.getNotificationToggle())
        // 加载状态栏配置
        var statusBarNotificationConfig = UserPreferences.getStatusConfig()
        if (statusBarNotificationConfig == null) {
            statusBarNotificationConfig = DemoCache.getNotificationConfig()
            UserPreferences.setStatusConfig(statusBarNotificationConfig)
        }
        //更新配置
        NIMClient.updateStatusBarNotificationConfig(statusBarNotificationConfig)
    }


}