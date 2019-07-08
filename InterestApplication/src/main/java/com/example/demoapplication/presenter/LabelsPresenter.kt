package com.example.demoapplication.presenter

import com.example.demoapplication.api.Api
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.presenter.view.LabelsView
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

class LabelsPresenter : BasePresenter<LabelsView>() {

    /**
     * 获取标签
     */
    fun getLabels(params: HashMap<String, String>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getTagLists(params)
            .excute(object : BaseSubscriber<BaseResp<MutableList<LabelBean>>>(mView) {
                override fun onNext(t: BaseResp<MutableList<LabelBean>>) {
                    super.onNext(t)
                    t.data[0].checked = true
                    mView.onGetLabelsResult(t.data)
                }
            })


//        mView.onGetLabelsResult(
//            mutableListOf(
//                Label("精选", checked = true, parId = 0),
//                Label("PlayStation", checked = false, parId = 1),
//                Label("游戏", checked = false, parId = 2)
////                Label("主机"),
////                Label("PlayStation"),
////                Label("独立游戏"),
////                Label("XBOX"),
////                Label("精选"),
////                Label("PlayStation"),
////                Label("游戏"),
////                Label("主机"),
////                Label("PlayStation"),
////                Label("独立游戏"),
////                Label("XBOX"), Label("精选"),
////                Label("PlayStation"),
////                Label("游戏"),
////                Label("主机"),
////                Label("PlayStation"),
////                Label("独立游戏"),
////                Label("XBOX")
//
//            )
//        )
    }


}