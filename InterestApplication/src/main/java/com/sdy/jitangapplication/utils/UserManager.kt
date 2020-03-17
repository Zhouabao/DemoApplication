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
import com.sdy.jitangapplication.model.ApproveBean
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.MediaParamBean
import com.sdy.jitangapplication.model.TagBean
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.nim.sp.UserPreferences
import com.sdy.jitangapplication.ui.activity.*
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
    var approveBean: ApproveBean? = null
    //认证和替换的参数
    var motion = -1//1，强制替换 2，引导替换 3，引导添加相册 其他不管
    var slide_times = 0
    var perfect_times = 0 //滑动x次数跳【完善相册】
    var replace_times = 0 //滑动x次数跳【替换头像】


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


    fun saveAccountDanger(danger: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("accountDanger", danger)
    }

    fun getAccountDanger(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("accountDanger", false)
    }


    fun saveAccountDangerAvatorNotPass(danger: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("AccountDangerAvatorNotPass", danger)
    }

    fun getAccountDangerAvatorNotPass(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("AccountDangerAvatorNotPass", false)
    }

    /**
     * 保存当前需要弹去完善标签弹窗的次数
     */
    fun saveCompleteLabelCount(count: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("completeLabelCount", count)
    }

    fun getCompleteLabelCount(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("completeLabelCount", -1)
    }

    /**
     * 保存滑动次数
     */
    fun saveSlideCount(count: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("SlideCount", count)
    }

    fun getSlideCount(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("SlideCount", -1)
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


    fun isShowCompleteLabelDialog(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("IsShowCompleteLabelDialog", false)
    }


    /**
     * 保存当前调研弹窗的版本号
     */
    fun saveCurrentSurveyVersion() {
        SPUtils.getInstance(Constants.SPNAME).put("currentVersion", AppUtils.getAppVersionName())
    }

    fun getCurrentSurveyVersion(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("currentVersion")
    }

    /**
     * 保存滑动多少次弹调研弹窗
     */
    fun saveShowSurveyCount(showcard_cnt: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("showcard_cnt", showcard_cnt)
    }

    fun getShowSurveyCount(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("showcard_cnt", 0)
    }


    /**
     * 保存匹配页面用户滑动的次数
     */
    fun saveSlideSurveyCount(slideCount: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("SlideSurveyCount", slideCount)
    }

    fun getSlideSurveyCount(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("SlideSurveyCount", 0)
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

    //是否需要强制替换头像  1头像不通过强制替换   2真人头像不通过 强制替换
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



    //是否提示过引导替换相册
    fun saveAlertChangeAlbum(isNeedChangeAvator: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("AlertChangeAlbum", isNeedChangeAvator)
    }

    fun getAlertChangeAlbum(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("AlertChangeAlbum", false)
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
     * 清除认证数据
     */
    fun cleanVerifyData() {
        motion = -1
        replace_times = 0
        perfect_times = 0
        slide_times = 0
//        SPUtils.getInstance(Constants.SPNAME).remove("ChangeAvator")
//        SPUtils.getInstance(Constants.SPNAME).remove("isNeedChangeAvator")
//        SPUtils.getInstance(Constants.SPNAME).remove("isForceChangeAvator")
    }


    /**
     * 跳至登录界面
     */
    fun startToLogin(activity: Activity) {
        AppManager.instance.finishAllActivity()
        activity.startActivity<LoginActivity>()
        clearLoginData()
    }

    /**
     * 登录成功保存用户信息
     */
    fun saveUserInfo(data: LoginBean) {
        if (data.userinfo != null) {
            SPUtils.getInstance(Constants.SPNAME).put("nickname", data.userinfo.nickname)
            SPUtils.getInstance(Constants.SPNAME).put("avatar", data.userinfo.avatar)
            data.userinfo.gender?.let { SPUtils.getInstance(Constants.SPNAME).put("gender", it) }
            SPUtils.getInstance(Constants.SPNAME).put("birth", data.userinfo.birth)
            if (data.userinfo.isvip != -1) {
                saveUserVip(data.userinfo.isvip)
            }

            if (data.userinfo.isfaced != -1)
                saveUserVerify(data.userinfo.isfaced)
            SPUtils.getInstance(Constants.SPNAME).put("userIntroduce", data.extra_data?.aboutme ?: "")
            if (!data.extra_data?.mytaglist.isNullOrEmpty()) {
                saveLabels(data.extra_data?.mytaglist ?: mutableListOf())
            }
        }
    }

    fun saveLabels(data: MutableList<TagBean>) {
        val savaLabels = mutableSetOf<String>()
        for (label in data) {
            savaLabels.add(SharedPreferenceUtil.Object2String(label))
        }
        SPUtils.getInstance(Constants.SPNAME).put("myLabels", savaLabels)
    }

    /**
     * 登录成功保存用户信息
     */
    fun isUserInfoMade(): Boolean {
        return !(SPUtils.getInstance(Constants.SPNAME).getString("nickname").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getString("avatar").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getString("avatar").contains(Constants.DEFAULT_AVATAR) ||
                SPUtils.getInstance(Constants.SPNAME).getInt("gender") == 0 ||
                SPUtils.getInstance(Constants.SPNAME).getInt("birth", 0) == 0 ||
                SPUtils.getInstance(Constants.SPNAME).getString("userIntroduce").isNullOrEmpty() ||
                getSpLabels().isNullOrEmpty()
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
        return SPUtils.getInstance(Constants.SPNAME).getInt("gender",0)
    }

    fun getUserIntroduce(): String {
        return SPUtils.getInstance(Constants.SPNAME).getString("userIntroduce")
    }


    /**
     * 判断用户是否是vip
     */
    fun isUserVip(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getInt("isvip", 0) == 1
    }

    fun saveUserVip(vip: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("isvip", vip)
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
     * //是否同城筛选 1否 2是 local_only
     * //选择了同城 传递城市id city_code
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
        if (sp.getInt("local_only", -1) != -1) {
            parmas["local_only"] = sp.getInt("local_only", -1)
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
        parmas["city_code"] = getCityCode()

        return parmas
    }

    /**
     * 获取本地存放的兴趣
     */
    fun getSpLabels(): MutableList<TagBean> {
        val tempLabels = mutableListOf<TagBean>()
        if (SPUtils.getInstance(Constants.SPNAME).getStringSet("myLabels").isNotEmpty()) {
            (SPUtils.getInstance(Constants.SPNAME).getStringSet("myLabels")).forEach {
                tempLabels.add(SharedPreferenceUtil.String2Object(it) as TagBean)
            }
        }
        tempLabels.sortWith(Comparator { p0, p1 -> p0.id.compareTo(p1.id) })
        return tempLabels
    }

    fun getGlobalLabelSquareId(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelSquareId", 0)
    }

    fun saveGlobalLabelSquareId(id: Int) {
        return SPUtils.getInstance(Constants.SPNAME).put("globalLabelSquareId", id)
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
        SPUtils.getInstance(Constants.SPNAME).remove("isvip")
        SPUtils.getInstance(Constants.SPNAME).remove("verify")
        SPUtils.getInstance(Constants.SPNAME).remove("checkedLabels")
        SPUtils.getInstance(Constants.SPNAME).remove("userIntroduce")
        SPUtils.getInstance(Constants.SPNAME).remove("globalLabelId")
        SPUtils.getInstance(Constants.SPNAME).remove("globalLabelSquareId")
        SPUtils.getInstance(Constants.SPNAME).remove("countdowntime")
        SPUtils.getInstance(Constants.SPNAME).remove("lightingCount")
        SPUtils.getInstance(Constants.SPNAME).remove("leftSlideCount")
        SPUtils.getInstance(Constants.SPNAME).remove("highlight_times")
        SPUtils.getInstance(Constants.SPNAME).remove("slideCount")
        SPUtils.getInstance(Constants.SPNAME).remove("hiCount")
        SPUtils.getInstance(Constants.SPNAME).remove("likeCount")
        SPUtils.getInstance(Constants.SPNAME).remove("likeUnreadCount")
        SPUtils.getInstance(Constants.SPNAME).remove("squareCount")
        SPUtils.getInstance(Constants.SPNAME).remove("msgCount")
        SPUtils.getInstance(Constants.SPNAME).remove("myLabels")
        SPUtils.getInstance(Constants.SPNAME).remove("maxInterestLabelCount")


        //位置信息
//        SPUtils.getInstance(Constants.SPNAME).remove("latitude")
//        SPUtils.getInstance(Constants.SPNAME).remove("longtitude")
//        SPUtils.getInstance(Constants.SPNAME).remove("province")
//        SPUtils.getInstance(Constants.SPNAME).remove("city")
//        SPUtils.getInstance(Constants.SPNAME).remove("district")
//        SPUtils.getInstance(Constants.SPNAME).remove("citycode")

        //筛选信息
        SPUtils.getInstance(Constants.SPNAME).remove("filter_gender")
        SPUtils.getInstance(Constants.SPNAME).remove("filter_square_gender")
        SPUtils.getInstance(Constants.SPNAME).remove("limit_age_high")
        SPUtils.getInstance(Constants.SPNAME).remove("limit_age_low")
        SPUtils.getInstance(Constants.SPNAME).remove("local_only")
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
        SPUtils.getInstance(Constants.SPNAME).remove("AlertProtocol")
        SPUtils.getInstance(Constants.SPNAME).remove("notice")
        cleanVerifyData()
        SPUtils.getInstance(Constants.SPNAME).remove("ChangeAvator")
        SPUtils.getInstance(Constants.SPNAME).remove("ChangeAvatorType")
        SPUtils.getInstance(Constants.SPNAME).remove("AlertChangeAlbum")
        SPUtils.getInstance(Constants.SPNAME).remove("isNeedChangeAvator")
        SPUtils.getInstance(Constants.SPNAME).remove("isForceChangeAvator")

        /**
         * 弹窗缓存信息清空
         */
        SPUtils.getInstance(Constants.SPNAME).remove("isShowHarassment")
        SPUtils.getInstance(Constants.SPNAME).remove("currentVersion")
        SPUtils.getInstance(Constants.SPNAME).remove("showcard_cnt")
        SPUtils.getInstance(Constants.SPNAME).remove("SlideSurveyCount")
        SPUtils.getInstance(Constants.SPNAME).remove("completeLabelCount")
        SPUtils.getInstance(Constants.SPNAME).remove("maxMyLabelCount")
        SPUtils.getInstance(Constants.SPNAME).remove("SlideCount")

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
     * 保存提示剩余滑动次数
     */
    fun saveHighlightCount(highlight_times: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("highlight_times", highlight_times)
    }

    /**
     * 获取提示剩余滑动次数
     */
    fun getHighlightCount(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("highlight_times", 0)
    }


    /**
     * 保存剩余滑动次数
     */
    fun saveLeftSlideCount(slideTimes: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("leftSlideCount", slideTimes)
    }

    /**
     * 获取剩余滑动次数
     */
    fun getLeftSlideCount(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("leftSlideCount", 0)
    }


    /**
     * 保存剩余招呼次数
     */
    fun saveLightingCount(count: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("lightingCount", count)
    }

    /**
     * 获取剩余招呼次数
     */
    fun getLightingCount(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("lightingCount", 0)
    }

    /**
     * 是否展示首页的引导使用
     */
    fun isShowGuideIndex(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuide", false)
    }

    fun saveShowGuideIndex(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuide", isShow)
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
     * 是否展示引导招呼
     */
    fun isShowGuideGreet(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getBoolean("isShowGuideGreet", false)
    }

    fun saveShowGuideGreet(isShow: Boolean) {
        SPUtils.getInstance(Constants.SPNAME).put("isShowGuideGreet", isShow)
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
        SPUtils.getInstance(Constants.SPNAME).put("imToken", nothing?.token)
        SPUtils.getInstance(Constants.SPNAME).put("imAccid", nothing?.account)

        SPUtils.getInstance(Constants.SPNAME).put("qntoken", data?.qntk)
        SPUtils.getInstance(Constants.SPNAME).put("token", data?.token)
        SPUtils.getInstance(Constants.SPNAME).put("accid", data?.accid)
        DemoCache.setAccount(nothing?.account)
        //初始化消息提醒配置
        initNotificationConfig()

        //昵称 生日 性别 头像
        if (data?.userinfo == null || data.userinfo.nickname.isNullOrEmpty()) {
            context.startActivity<UserNickNameActivity>()
            return
        } else if (data.userinfo.birth == 0) {//生日没填写
            context.startActivity<UserBirthActivity>()
            return
        } else if (data.userinfo.gender == 0) {//性别没选择
            context.startActivity<UserGenderActivity>()
            return
        } else if (data.userinfo.avatar.isNullOrEmpty() || data.userinfo.avatar!!.contains(Constants.DEFAULT_AVATAR)) {//头像未选择
            context.startActivity<UserAvatorActivity>()
            return
        } else if (data.extra_data?.aboutme.isNullOrEmpty() || data.extra_data?.aboutme?.trim().isNullOrEmpty()) {//个人介绍未填写
            context.startActivity<UserIntroduceActivity>("from" to UserIntroduceActivity.REGISTER)
            return
        } else if (data.extra_data?.mytaglist.isNullOrEmpty()) {//兴趣没有选择
            context.startActivity<AddLabelActivity>("from" to AddLabelActivity.FROM_REGISTER)
            return
        } else {//跳到主页
            saveUserInfo(data)
            AppManager.instance.finishAllActivity()
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