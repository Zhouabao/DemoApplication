package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MoreMatchBean

interface RegisterGenderView : BaseView {

    //上传用户信息结果
    fun onUploadUserInfoResult(
        uploadResult: Boolean,
        moreMatchBean: MoreMatchBean? = null
    )
}