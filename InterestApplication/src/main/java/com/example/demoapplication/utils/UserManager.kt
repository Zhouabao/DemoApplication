package com.example.demoapplication.utils

import android.app.Activity
import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.model.LoginBean
import com.example.demoapplication.nim.DemoCache
import com.example.demoapplication.ui.activity.LoginActivity
import com.kotlin.base.common.AppManager
import com.netease.nimlib.sdk.auth.LoginInfo
import org.jetbrains.anko.startActivity
import java.util.*

/**
 *    author : ZFM
 *    date   : 2019/7/1115:23
 *    desc   :
 *    version: 1.0
 */
object UserManager {

    /**
     * 跳至登录界面
     */
    fun startToLogin(activity: Activity) {
        AppManager.instance.finishAllActivity()
        activity.startActivity<LoginActivity>()
        SPUtils.getInstance(Constants.SPNAME).remove("token", true)
        SPUtils.getInstance(Constants.SPNAME).remove("accid", true)
        clearLoginData()
    }

    /**
     * 登录成功保存用户信息
     */
    fun saveUserInfo(data: LoginBean) {
        val savaLabels = mutableSetOf<String>()
        for (label in data!!.taglist ?: mutableListOf()) {
            if (label != null)
                savaLabels.add(
                    SharedPreferenceUtil.Object2String(
                        LabelBean(
                            title = label.title ?: "",
                            id = label.id ?: -1
                        )
                    )
                )
        }
        SPUtils.getInstance(Constants.SPNAME).put("checkedLabels", savaLabels)
        if (data.userinfo != null) {
            SPUtils.getInstance(Constants.SPNAME).put("nickname", data.userinfo.nickname)
            SPUtils.getInstance(Constants.SPNAME).put("avatar", data.userinfo.avatar)
            data.userinfo.gender?.let { SPUtils.getInstance(Constants.SPNAME).put("gender", it) }
            SPUtils.getInstance(Constants.SPNAME).put("birth", data.userinfo.birth)
            data.userinfo.isvip?.let { saveUserVip(it) }
            data.userinfo.isfaced?.let { saveUserVerify(it) }
        }
    }

    /**
     * 登录成功保存用户信息
     */
    public fun isUserInfoMade(): Boolean {
        return !(SPUtils.getInstance(Constants.SPNAME).getString("nickname").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getString("avatar").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getInt("gender") == 0 ||
                SPUtils.getInstance(Constants.SPNAME).getString("birth").isNullOrEmpty())
    }


    /**
     * 保存位置信息
     */
    public fun saveLocation(
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
     * //标签id
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
        parmas["limit_age_low"] = sp.getInt("limit_age_low", 18)
        parmas["limit_age_high"] = sp.getInt("limit_age_high", 30)
        parmas["local_only"] = sp.getInt("local_only", 1)
        parmas["city_code"] = sp.getInt("city_code", 0)
        parmas["audit_only"] = sp.getInt("audit_only", 1)
        parmas["gender"] = sp.getInt("filter_gender", 3)

        return parmas
    }

    /**
     * 获取本地存放的标签
     */
    fun getSpLabels(): MutableList<LabelBean> {
        val tempLabels = mutableListOf<LabelBean>()
        if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels").isNotEmpty()) {
            (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels")).forEach {
                tempLabels.add(SharedPreferenceUtil.String2Object(it) as LabelBean)
            }
        }
        tempLabels.sortWith(Comparator { p0, p1 -> p0.id.compareTo(p1.id) })
        return tempLabels
    }

    fun getGlobalLabelId(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId", 0)
    }

    fun getGlobalLabelName(): String {
        val labels = getSpLabels()
        val id = getGlobalLabelId()
        for (label in labels) {
            if (label.id == id) {
                return label.title
            }
        }
        return ""
    }


    // 如果已经存在IM用户登录信息，返回LoginInfo，否则返回null即可
    public fun loginInfo(): LoginInfo? {
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
        SPUtils.getInstance(Constants.SPNAME).remove("globalLabelId")
        SPUtils.getInstance(Constants.SPNAME).remove("countdowntime")
        SPUtils.getInstance(Constants.SPNAME).remove("lightingCount")

        //位置信息
        SPUtils.getInstance(Constants.SPNAME).remove("latitude")
        SPUtils.getInstance(Constants.SPNAME).remove("longtitude")
        SPUtils.getInstance(Constants.SPNAME).remove("province")
        SPUtils.getInstance(Constants.SPNAME).remove("city")
        SPUtils.getInstance(Constants.SPNAME).remove("district")
        SPUtils.getInstance(Constants.SPNAME).remove("citycode")

        //筛选信息
        SPUtils.getInstance(Constants.SPNAME).remove("filter_gender")
        SPUtils.getInstance(Constants.SPNAME).remove("limit_age_high")
        SPUtils.getInstance(Constants.SPNAME).remove("limit_age_low")
        SPUtils.getInstance(Constants.SPNAME).remove("local_only")
        SPUtils.getInstance(Constants.SPNAME).remove("city_code")
        SPUtils.getInstance(Constants.SPNAME).remove("audit_only")

        //敏感词
        SPUtils.getInstance(Constants.SPNAME).remove("sensitive")
        //草稿箱清除
        SPUtils.getInstance(Constants.SPNAME).remove("draft")
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
     * 保存补充招呼次数的时间
     */
    fun saveCountDownTime(time: Int) {
        SPUtils.getInstance(Constants.SPNAME).put("countdowntime", time)
    }

    /**
     * 获取补充招呼次数的时间
     */
    fun getCountDownTimet(): Int {
        return SPUtils.getInstance(Constants.SPNAME).getInt("countdowntime", 0)
    }

}