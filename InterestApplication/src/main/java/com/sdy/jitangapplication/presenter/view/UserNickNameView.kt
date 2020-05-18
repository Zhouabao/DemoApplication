package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.MyPhotoBean

interface UserNickNameView : BaseView {

    //上传用户信息结果
    fun onUploadUserInfoResult(
        uploadResult: Boolean,
        msg: String?,
        moreMatchBean: MoreMatchBean? = null
    )

    fun uploadImgResult(ok: Boolean, key: String)

    fun onAddPhotoWallResult(data: MyPhotoBean)

    fun onRegisterAddPhoto(data: MoreMatchBean?)


}