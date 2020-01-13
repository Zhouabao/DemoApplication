package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.GetNewMsgEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.model.NewLikeMeBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.presenter.view.LikeMeReceivedView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import org.greenrobot.eventbus.EventBus

class LikeMeReceivedPresenter : BasePresenter<LikeMeReceivedView>() {

    /**
     * 获取打招呼列表
     * Relationship/likeListsV2
     */
    fun likeListsV2(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .likeListsV2(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<NewLikeMeBean?>>(mView) {
                override fun onNext(t: BaseResp<NewLikeMeBean?>) {
                    when {
                        t.code == 200 -> mView.onGreatListResult(t)
                        else -> mView.onError(t.msg)
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
     * 不喜欢 左滑
     */
    fun bindMemberHandle(target_accid: String) {
        if (!checkNetWork()) {
            return
        }
        val params = hashMapOf<String, Any>()
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .bindMemberHandle(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {

                    if (t.code == 200) {
                        mView.onGetDislikeResult(true)
                    } else {
                        mView.onGetDislikeResult(false)
                    }

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetDislikeResult(false)
                }
            })
    }


    /**
     * 喜欢 右滑
     * type:1 dianji  2 youhua
     */

    fun addLike(target_accid: String, tag_id: Int, type: Int) {
        if (!checkNetWork()) {
            return
        }
        val params = hashMapOf<String, Any>()
        params["target_accid"] = target_accid
        params["tag_id"] = tag_id
        RetrofitFactory.instance.create(Api::class.java)
            .addLike(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(mView) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    when {
                        t.code == 200 -> mView.onLikeOrGreetStateResult(t, type)
                        else -> {
                            mView.onLikeOrGreetStateResult(t, type)
                            CommonFunction.toast(t.msg)
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError(context.getString(R.string.service_error))
                }
            })
    }



    /**
     * 标记喜欢我的为已读
     */
    fun markLikeRead() {
        RetrofitFactory.instance.create(Api::class.java)
            .markLikeRead(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        EventBus.getDefault().post(GetNewMsgEvent())
                        EventBus.getDefault().post(UpdateHiEvent())
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