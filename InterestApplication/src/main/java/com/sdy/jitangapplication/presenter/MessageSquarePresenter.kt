package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.event.GetNewMsgEvent
import com.sdy.jitangapplication.model.SquareMsgBean
import com.sdy.jitangapplication.presenter.view.MessageSquareView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
class MessageSquarePresenter : BasePresenter<MessageSquareView>() {
    /**
     * 获取广场消息列表
     */
    fun squareLists(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .squareLists(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<SquareMsgBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<SquareMsgBean>?>) {
                    if (t.code == 200)
                        mView.onSquareListsResult(t.data ?: mutableListOf())
                    else
                        mView.onError("")
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
     * 标记广场消息已读
     */
    fun markSquareRead(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .markSquareRead(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    //  type=1  点赞的  type=2 评论的
                    if (t.code == 200) {
                        EventBus.getDefault().post(GetNewMsgEvent())
                    }
                }

                override fun onError(e: Throwable?) {

                }
            })
    }

    /**
     * 删除广场消息
     */
    fun delSquareMsg(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .delSquareMsg(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200)
                        mView.onDelSquareMsgResult(true)
                    else
                        mView.onDelSquareMsgResult(false)
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onDelSquareMsgResult(false)
                }
            })
    }


}