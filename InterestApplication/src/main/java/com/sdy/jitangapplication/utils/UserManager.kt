package com.sdy.jitangapplication.utils

import android.content.Context
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.common.AppManager
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.LoginInfo
import com.qiniu.android.storage.UpCancellationSignal
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.MediaParamBean
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.RegisterFileBean
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.nim.sp.UserPreferences
import com.sdy.jitangapplication.nim.uikit.common.util.string.MD5
import com.sdy.jitangapplication.ui.activity.*
import com.sdy.jitangapplication.ui.dialog.OpenVipActivity
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

    //记录当前设备是否为海外
    var overseas = false

    var registerFileBean: RegisterFileBean? = null
    var my_mv_url: Boolean = false //是否上传过视频介绍
    var showIndexRecommend: Boolean = false
    var touristMode: Boolean = false//是否是处于游客模式
    var showCandyMessage: Boolean = true//聊天是否显示糖果
    var showCandyTime: Long = 0L//聊天显示糖果的时间

    var goldOnlyDating: Boolean = false

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
    fun startToLogin(activity: Context, fromNimNotification: Boolean = false) {
        touristMode = false
        clearLoginData()
        NIMClient.getService(AuthService::class.java).logout()
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
            data.userinfo.gender?.let { saveGender(it) }
            SPUtils.getInstance(Constants.SPNAME).put("birth", data.userinfo.birth)

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
                SPUtils.getInstance(Constants.SPNAME).getString("avatar")
                    .contains(Constants.DEFAULT_AVATAR) ||
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
        province: String? = null,
        city: String? = null,
        district: String? = null,
        code: String? = null
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
    fun getCountryCode(): String {
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

    fun saveGender(gender: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("gender", gender)
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
     * 用户是否是门槛会员
     */
    fun isUserFoot(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("userFoot", false)
    }

    fun saveUserFoot(userFoot: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("userFoot", userFoot)
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


        if (sp.getInt("online_type", -1) != -1) {
            parmas["online_type"] = sp.getInt("online_type", -1)
        }
        if (sp.getString("roaming_city").isNotEmpty()) {
            parmas["roaming_city"] = sp.getString("roaming_city")
            parmas["is_roaming"] = 2  //1不是漫游, 2 是漫游
        }
//        if (sp.getInt("filter_gender", -1) != -1) {
//            parmas["gender"] = sp.getInt("filter_gender", -1)
//        }

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
        SPUtils.getInstance(Constants.SPNAME).remove("userFoot")
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
        SPUtils.getInstance(Constants.SPNAME).remove("city_code")
        SPUtils.getInstance(Constants.SPNAME).remove("audit_only")
        SPUtils.getInstance(Constants.SPNAME).remove("online_type")
        SPUtils.getInstance(Constants.SPNAME).remove("roaming_city")

        //敏感词
        SPUtils.getInstance(Constants.SPNAME).remove("sensitive")
        //草稿箱清除
        SPUtils.getInstance(Constants.SPNAME).remove("draft")

        clearPublishParams()
        /**
         * 认证相关缓存清空
         */
        SPUtils.getInstance(Constants.SPNAME).remove("isShowSettingNew")
        SPUtils.getInstance(Constants.SPNAME).remove("isShowGuideWechat")
//        SPUtils.getInstance(Constants.SPNAME).remove("AlertProtocol")
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


        /**
         * 甜心圈
         */
        SPUtils.getInstance(Constants.SPNAME).remove("styleList")


        EventBus.getDefault().removeAllStickyEvents()//移除全部
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
     * 是否展示认证提醒
     */
    fun isShowGuideWechat(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuideWechat", false)
    }

    fun saveShowGuideWechat(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuideWechat", isShow)
    }


    /**
     * 是否是列表形式展示
     */
    fun isStyleList(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("styleList", getGender() != 1)
    }


    fun saveStyleList(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("styleList", isShow)
    }


    /**
     * 是否引导过列表切换
     */
    fun saveGuideBrowseStyleCl(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("guideBrowseStyleCl", isShow)
    }

    fun isGuideBrowseStyleCl(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("guideBrowseStyleCl", false)
    }


    /**
     * 是否展示认证提醒
     */
    fun isShowSweetHeartNew(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("sweetHeartNew", false)
    }

    fun saveShowSweetHeartNew(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("sweetHeartNew", isShow)
    }


    fun isShowSettingNew(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowSettingNew", false)
    }

    fun saveShowSettingNew(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowSettingNew", isShow)
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
     * 是否进行过认证头像
     */
    fun isHasFaceUrl(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("hasFaceUrl", false)
    }

    fun saveHasFaceUrl(has_face_url: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("hasFaceUrl", has_face_url)
    }


    /**
     * 保存google购买的token
     */
    fun savePurchaseToken(purchaseToken: String) {
        SPUtils.getInstance(Constants.SPNAME).put("purchaseToken", purchaseToken)
    }


    /**
     * 保存google购买的token
     */
    fun getPurchaseToken(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("purchaseToken")
    }


    fun getLocationParams(): HashMap<String, Any> {
        return hashMapOf(
            "lng" to getlongtitude().toFloat(),
            "lat" to getlatitude().toFloat(),
            "city_name" to getCity(),
            "province_name" to getProvince()
        )
    }


    fun getBaseParams(): HashMap<String, Any> {
        return hashMapOf(
            "token" to getToken(),
            "accid" to getAccid(),
            "_timestamp" to System.currentTimeMillis(),
            "device_id" to DeviceUtils.getUniqueDeviceId()
        )
    }

    fun getSignParams(params: HashMap<String, Any> = hashMapOf()): HashMap<String, Any> {
        if (params["_signature"] != null)
            params.remove("_signature")
        params.putAll(getBaseParams())
        params.putAll(getLocationParams())
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
        params.putAll(getLocationParams())
        var sign = "ppsns${AppUtils.getAppVersionName()}dcyfyf"
        var params1 = params
        for (param in params.toSortedMap()) {
            sign = sign.plus("${param.key}=${param.value}&")
        }
        params1["_signature"] = MD5.getStringMD5(sign.substring(0, sign.length - 1))
        return params1
    }


    fun startToPersonalInfoActivity(context: Context, nothing: LoginInfo?, data: LoginBean?) {
        touristMode = false//登录成功清空游客模式
        registerFileBean = RegisterFileBean(
            data?.extra_data?.people_amount ?: 0,
            data?.extra_data?.supplement ?: 0,
            data?.extra_data?.threshold ?: false,
            data?.extra_data?.living_btn ?: false,
            data?.extra_data?.tourists ?: false
        )
        saveUserFoot(data?.extra_data?.isvip ?: false)

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
        //昵称 生日 性别 头像
        if (data?.userinfo == null || data.userinfo.gender == 0) {
            context.startActivity<RigisterGenderActivity>()
            return
        } else if (data.userinfo.gender == 1) {//男生流程：性别、更多配对、更多关系、充值VIP、填写个人信息、落地
            data.userinfo.gender.let { saveGender(it) }
            if (data.extra_data?.want_steps != true) { //更多配对、更多关系、
                context.startActivity<GetMoreMatchActivity>(
                    "morematchbean" to MoreMatchBean(
                        data.extra_data?.city_name ?: "",
                        data.extra_data?.gender_str ?: "",
                        data.extra_data?.people_amount ?: 0
                    )
                )
            } else if (registerFileBean?.threshold == true && !data.extra_data.isvip && !data.extra_data.my_experience_card_state) {//门槛付费
                // want_steps填写了，并且门槛开启，用户不是会员或者用户的体验卡状态为false
                OpenVipActivity.start(
                    context, MoreMatchBean(
                        data.extra_data.city_name,
                        data.extra_data.gender_str,
                        data.extra_data.people_amount,
                        share_btn = data.extra_data.share_btn
                    ),
                    OpenVipActivity.FROM_REGISTER_OPEN_VIP
                )
            } else if (data.userinfo.nickname.isNullOrEmpty()
                || data.userinfo.birth == 0
                || data.userinfo.avatar.isNullOrEmpty()
                || data.userinfo.avatar.contains(Constants.DEFAULT_AVATAR)
            ) { //填写个人信息
                context.startActivity<RegisterInfoActivity>()
            } else {//完成注册
                //跳到主页
                //保存个人信息
                saveUserInfo(data)
                context.startActivity<MainActivity>()
            }
        } else if (data.userinfo.gender == 2) {//女生流程：性别、填写个人信息、活体、更多配对、更多关系、充值VIP、落地
            data.userinfo.gender.let { saveGender(it) }
            if (data.userinfo.nickname.isNullOrEmpty()
                || data.userinfo.birth == 0
                || data.userinfo.avatar.isNullOrEmpty()
                || data.userinfo.avatar.contains(Constants.DEFAULT_AVATAR)
            ) {
                context.startActivity<RegisterInfoActivity>()
            } else if (data?.extra_data?.want_steps != true) {
                context.startActivity<GetMoreMatchActivity>(
                    "morematchbean" to MoreMatchBean(
                        data.extra_data?.city_name ?: "",
                        data.extra_data?.gender_str ?: "",
                        data.extra_data?.people_amount ?: 0,
                        data?.userinfo?.avatar, living_btn = data.extra_data?.living_btn ?: true
                    )
                )
            } else if (data.extra_data.living_btn) {
                context.startActivity<IDVerifyActivity>(
                    "type" to IDVerifyActivity.TYPE_LIVE_CAPTURE,
                    "morematchbean" to MoreMatchBean(
                        data.extra_data.city_name,
                        data.extra_data.gender_str,
                        data.extra_data.people_amount
                    )
                )
            } else {
                //跳到主页
                saveUserInfo(data)
                context.startActivity<MainActivity>()
            }
        }
    }

    fun startToFlow(context: Context, moreMatchBean: MoreMatchBean?) {
        SPUtils.getInstance(Constants.SPNAME).put("avatar", moreMatchBean?.avatar)
        if (getGender() == 1) {
            //跳到主页
            SPUtils.getInstance(Constants.SPNAME).put("nickname", moreMatchBean?.nickname)
            SPUtils.getInstance(Constants.SPNAME).put("birth", moreMatchBean?.birth ?: 0)
            SPUtils.getInstance(Constants.SPNAME)
                .put("people_amount", moreMatchBean?.people_amount ?: 0)
            context.startActivity<MainActivity>()
        } else {
            context.startActivity<GetMoreMatchActivity>("morematchbean" to moreMatchBean)
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