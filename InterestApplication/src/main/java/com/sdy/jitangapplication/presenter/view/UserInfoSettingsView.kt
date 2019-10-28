package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MyPhotoBean
import com.sdy.jitangapplication.model.UserInfoSettingBean

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
    fun onSavePersonalResult(result: Boolean, type: Int, from: Int = 0)

    /**
     * 单张上传照片结果
     */
    fun onAddPhotoWallResult(replaceAvator: Boolean, result: MyPhotoBean)

    fun uploadImgResult(b: Boolean, key: String, replaceAvator: Boolean = false)
}