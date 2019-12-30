package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.MessageListBean1
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.presenter.view.MessageListView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import com.umeng.commonsdk.stateless.UMSLEnvelopeBuild.mContext

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
class MessageListPresenter : BasePresenter<MessageListView>() {

    /**
     * 获取消息中心的内容
     */
    fun messageCensus(param: HashMap<String, String>) {
        val params = UserManager.getBaseParams()
        params.putAll(param)
        RetrofitFactory.instance.create(Api::class.java)
            .messageCensus(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MessageListBean1?>>(mView) {
                override fun onNext(t: BaseResp<MessageListBean1?>) {
                    if (t.code == 200) {
                        mView.onMessageCensusResult(t.data)
                    } else {
                        mView.onMessageCensusResult(null)
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
     * 获取云信最近联系人
     */
    fun getRecentContacts() {
        NIMClient.getService(MsgService::class.java)
            .queryRecentContacts()
            .setCallback(object : RequestCallbackWrapper<MutableList<RecentContact>>() {
                override fun onResult(code: Int, result: MutableList<RecentContact>?, exception: Throwable?) {
                    if (code != ResponseCode.RES_SUCCESS.toInt() || result == null) {
                        return
                    }
                    mView.onGetRecentContactResults(result)
                }

            })
    }

    fun likeUser(accid: String, position: Int) {

        val params = hashMapOf<String, Any>()
        params["target_accid"] = accid
        params["tag_id"] = UserManager.getGlobalLabelId()
        RetrofitFactory.instance.create(Api::class.java)
            .addLike(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(null) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    mView.onLikeUserResult(t,position)
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else
                        CommonFunction.toast(CommonFunction.getErrorMsg(mContext))
                }
            })

    }
}