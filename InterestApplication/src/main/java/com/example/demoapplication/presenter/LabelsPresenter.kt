package com.example.demoapplication.presenter

import com.example.demoapplication.model.Label
import com.example.demoapplication.presenter.view.LabelsView
import com.kotlin.base.presenter.BasePresenter

class LabelsPresenter : BasePresenter<LabelsView>() {

    fun getLabels(level: Int) {
        mView.onGetLabelsResult(
            mutableListOf(
                Label("精选", checked = true, parId = 0),
                Label("PlayStation", checked = false, parId = 1),
                Label("游戏", checked = false, parId = 2)
//                Label("主机"),
//                Label("PlayStation"),
//                Label("独立游戏"),
//                Label("XBOX"),
//                Label("精选"),
//                Label("PlayStation"),
//                Label("游戏"),
//                Label("主机"),
//                Label("PlayStation"),
//                Label("独立游戏"),
//                Label("XBOX"), Label("精选"),
//                Label("PlayStation"),
//                Label("游戏"),
//                Label("主机"),
//                Label("PlayStation"),
//                Label("独立游戏"),
//                Label("XBOX")

            )
        )
    }


    fun getSubLabels(parId: Int, subId: Int, subSubId: Int) {
        mView.onGetSubLabelsResult(
            mutableListOf(
                Label("精选$parId", checked = false, parId = parId, subId = 1,subSubId = 1)
//                Label("精选$parId", checked = false, parId = parId, subId = 2,subSubId = 2)
//                Label("精选$parId"),
//                Label("精选$parId"),
//                Label("精选$parId"),
//                Label("精选$parId"),
//                Label("精选$parId")
            )
            , parId
        )
    }


}