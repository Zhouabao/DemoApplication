package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.LikeMeBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.chat.MatchSucceedActivity
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_like_me.view.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/8/516:02
 *    desc   : 喜欢我的
 *    version: 1.0
 */
class LikeMeAdapter : BaseQuickAdapter<LikeMeBean, BaseViewHolder>(R.layout.item_like_me) {
    override fun convert(holder: BaseViewHolder, item: LikeMeBean) {
        holder.addOnClickListener(R.id.likeMeCount)
        val itemView = holder.itemView
        itemView.likeMeDate.text = item.date ?: ""
        itemView.likeMeCount.text = "${item.count} 人对你感兴趣"
        itemView.likeMeNew.isVisible = item.hasread ?: false

        itemView.likeOneDayRv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        val adapter = LikeMeOneDayAdapter()
        itemView.likeOneDayRv.adapter = adapter
        adapter.setNewData(item.list ?: mutableListOf())
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.likeMeType -> {
                    if (adapter.data[position].isfriend == 1) {
                        ChatActivity.start(mContext, adapter.data[position].accid ?: "")
                    } else {
                        RetrofitFactory.instance.create(Api::class.java)
                            .addLike(
                                hashMapOf(
                                    "accid" to UserManager.getAccid(),
                                    "token" to UserManager.getToken(),
                                    "target_accid" to (adapter.data[position].accid ?: "")
//                                    , "tag_id" to (adapter.data[position].tag_title ?: "")
                                )
                            )
                            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(null) {
                                override fun onNext(t: BaseResp<StatusBean?>) {
                                    if (t.code == 200) {
                                        if (t.data != null) {
                                            if (t.data!!.status == 2) {//匹配成功
                                                adapter.data[position].isfriend = 1
                                                adapter.notifyItemChanged(position)
                                                val chatHiAttachment = ChatHiAttachment(
                                                    UserManager.getGlobalLabelName(),
                                                    ChatHiAttachment.CHATHI_MATCH
                                                )
                                                val message = MessageBuilder.createCustomMessage(
                                                    adapter.data[position].accid,
                                                    SessionTypeEnum.P2P,
                                                    "",
                                                    chatHiAttachment,
                                                    CustomMessageConfig()
                                                )

                                                NIMClient.getService(MsgService::class.java).sendMessage(message, false)
                                                    .setCallback(object :
                                                        RequestCallback<Void?> {
                                                        override fun onSuccess(param: Void?) {
                                                            (mContext as Activity).startActivity<MatchSucceedActivity>(
                                                                "avator" to adapter.data[position].avatar,
                                                                "nickname" to adapter.data[position].nickname,
                                                                "accid" to adapter.data[position].accid
                                                            )
                                                        }

                                                        override fun onFailed(code: Int) {
                                                        }

                                                        override fun onException(exception: Throwable) {
                                                        }
                                                    })
                                            }
                                        }
                                    } else {
                                        ToastUtils.showShort(t.msg)
                                    }
                                }

                                override fun onError(e: Throwable?) {
                                    if (e is BaseException) {
                                        TickDialog(mContext).show()
                                    } else
                                        ToastUtils.showShort(CommonFunction.getErrorMsg(mContext))
                                }
                            })
                    }
                }
            }
        }

        adapter.setOnItemClickListener { _, view, position ->
            if (UserManager.isUserVip())
                MatchDetailActivity.start(mContext, adapter.data[position].accid ?: "")
        }
    }

}