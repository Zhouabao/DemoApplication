package com.example.demoapplication.presenter

import com.example.demoapplication.presenter.view.SetInfoView
import com.kotlin.base.presenter.BasePresenter

class SetInfoPresenter : BasePresenter<SetInfoView>() {

    fun checkNickName(nickName: String) {
        mView.onCheckNickNameResult(!nickName.contains("约"))
    }

}