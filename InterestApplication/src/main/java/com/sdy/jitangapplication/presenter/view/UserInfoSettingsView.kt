package com.sdy.jitangapplication.presenter.view

import com.sdy.jitangapplication.model.UserInfoSettingBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/8/110:05
 *    desc   :
 *    version: 1.0
 */
interface UserInfoSettingsView : BaseView {

    fun onPersonalInfoResult(data: UserInfoSettingBean?)


    /**
     * type  1 个人信息 2 头像
     */
    fun onSavePersonalResult(result: Boolean, type: Int)

    fun uploadImgResult(b: Boolean, key: String)
}