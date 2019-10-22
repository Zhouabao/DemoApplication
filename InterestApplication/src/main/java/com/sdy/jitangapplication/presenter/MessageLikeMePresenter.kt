package com.sdy.jitangapplication.presenter

import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.NewMsgEvent
import com.sdy.jitangapplication.model.LikeMeListBean
import com.sdy.jitangapplication.presenter.view.MessageLikeMeView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
class MessageLikeMePresenter : BasePresenter<MessageLikeMeView>() {

    fun likeLists(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .likeLists(params)
            .excute(object : BaseSubscriber<BaseResp<LikeMeListBean?>>(mView) {
                override fun onNext(t: BaseResp<LikeMeListBean?>) {
                    if (t.code == 200) {
                        SPUtils.getInstance(Constants.SPNAME).put("isvip", t.data?.isvip ?: 0)
                        mView.onLikeListsResult(t.data?.free_show ?: false, t.data?.list ?: mutableListOf())
                    } else if (t.code == 403) {
                        TickDialog(context).show()
                    } else {
                        mView.onError("")
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError("")
                }
            })
    }


    /**
     * 标记喜欢我的为已读
     */
    fun markLikeRead(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .markLikeRead(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        UserManager.saveLikeCount(0)
                        EventBus.getDefault().post(NewMsgEvent())
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }
}