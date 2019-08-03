package com.example.demoapplication.utils

import android.app.Activity
import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.model.LoginBean
import com.example.demoapplication.ui.activity.LoginActivity
import com.kotlin.base.common.AppManager
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
        activity.startActivity<LoginActivity>()
        AppManager.instance.finishActivity(activity)
        SPUtils.getInstance(Constants.SPNAME).remove("token", true)
        SPUtils.getInstance(Constants.SPNAME).remove("accid", true)
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
            data.userinfo.isvip?.let { SPUtils.getInstance(Constants.SPNAME).put("isvip", it) }
            data.userinfo.isverify?.let { SPUtils.getInstance(Constants.SPNAME).put("verify", it) }
        }
    }

    /**
     * 登录成功保存用户信息
     */
    fun isUserInfoMade(): Boolean {
        return !(SPUtils.getInstance(Constants.SPNAME).getString("nickname").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getString("avatar").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getInt("gender") == 0 ||
                SPUtils.getInstance(Constants.SPNAME).getString("birth").isNullOrEmpty())
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

    /**
     * 判断用户是否是vip
     */
    fun isUserVip(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getInt("isvip", 0) == 1
    }

    /**
     * 判断用户是否是认证
     */
    fun isUserVerify(): Boolean {
        return SPUtils.getInstance(Constants.SPNAME).getInt("verify", 0) == 1
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
        parmas["gender"] = sp.getInt("gender", 3)

        return parmas
    }


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
}