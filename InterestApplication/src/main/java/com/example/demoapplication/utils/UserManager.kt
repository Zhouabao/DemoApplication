package com.example.demoapplication.utils

import android.app.Activity
import com.blankj.utilcode.util.SPUtils
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.model.LoginBean
import com.example.demoapplication.ui.activity.LoginActivity
import com.kotlin.base.common.AppManager
import org.jetbrains.anko.startActivity

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
                            title = label?.title ?: "",
                            id = label?.id ?: -1
                        )
                    )
                )
        }
        SPUtils.getInstance(Constants.SPNAME).put("checkedLabels", savaLabels)
        if (data.userinfo != null) {
            SPUtils.getInstance(Constants.SPNAME).put("nickname", data.userinfo!!.nickname!!)
            SPUtils.getInstance(Constants.SPNAME).put("avatar", data.userinfo!!.avatar!!)
            SPUtils.getInstance(Constants.SPNAME).put("gender", data.userinfo!!.gender!!)
            SPUtils.getInstance(Constants.SPNAME).put("birth", data.userinfo!!.birth!!)
        }
    }

    /**
     * 登录成功保存用户信息
     */
    fun isUserInfoMade(): Boolean {
        return !(SPUtils.getInstance(Constants.SPNAME).getString("nickname").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getString("avatar").isNullOrEmpty() ||
                SPUtils.getInstance(Constants.SPNAME).getInt("gender") == 0 ||
                SPUtils.getInstance(Constants.SPNAME).getInt("birth") == 0)
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

     fun getSpLabels(): MutableList<LabelBean> {
        val tempLabels = mutableListOf<LabelBean>()
        if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels").isNotEmpty()) {
            (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels")).forEach {
                tempLabels.add(SharedPreferenceUtil.String2Object(it) as LabelBean)
            }
        }
        return tempLabels
    }
}